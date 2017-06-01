/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android.
 *
 * Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.security;

import android.content.Context;
import android.util.Base64;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import javax.security.auth.x500.X500Principal;

public class AndroidCryptoProvider {

    private final File csrFile;
    private final File certFile;
    private final File keyFile;

    private X509Certificate cert;
    private PKCS10CertificationRequest csr;
    private RSAPrivateKey key;
    private byte[] pemCsrBytes;
    private static final Object globalCryptoLock = new Object();
    private Context context;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public AndroidCryptoProvider(Context c) {
        context = c;

        String dataPath = c.getFilesDir().getAbsolutePath();

        csrFile = new File(dataPath + File.separator + "client.csr");
        keyFile = new File(dataPath + File.separator + "client.key");
        certFile = new File(dataPath + File.separator + "client.crt");
    }

    private byte[] loadFileToBytes(File f) {
        if (!f.exists()) {
            return null;
        }
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(f);
            byte[] fileData = new byte[(int) f.length()];
            if (fin.read(fileData) != f.length()) {
                // Failed to read
                fileData = null;
            }
            return fileData;
        } catch (IOException e) {
            //FlyveLog.e("loadFileToBytes IOException",e);
            return null;
        } finally {
            try {
                fin.close();
            } catch (IOException e){
                //FlyveLog.e("close FileInputStream, IO exception", e);
            } catch (NullPointerException e){
                //FlyveLog.e("close FileInputStream, NullP exception", e);
            }
        }
    }

    public String getlCsr() {
        byte[] csrBytes = loadFileToBytes(csrFile);
        String test = new String(csrBytes);
        // If either file was missing, we definitely can't succeed
        if (csrBytes == null) {
            //FlyveLog.i("loadCsr: Missing csr need to generate a new one");
            //return false;
        }
        return test;
    }

    public boolean loadCsr() {
        byte[] csrBytes = loadFileToBytes(csrFile);
        String test = new String(csrBytes);
        // If either file was missing, we definitely can't succeed
        if (csrBytes == null) {
            //FlyveLog.i("loadCsr: Missing csr need to generate a new one");
            return false;
        }
        //FlyveLog.d("loadCsr: " + test);
        return true;
    }


    public boolean generateRequest() {
        byte[] snBytes = new byte[8];
        new SecureRandom().nextBytes(snBytes);

        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(4096);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e1) {
            //FlyveLog.wtf("generateRequest",e1);
            // Should never happen
            return false;
        } catch (NoSuchProviderException e) {
            // Should never happen
            //FlyveLog.wtf("generateRequest",e);
            return false;
        }

        X500Principal subjectName = new X500Principal("CN=mydevice.stork-mdm.com");

        ContentSigner signGen = null;
        try {
            signGen = new JcaContentSignerBuilder("SHA1withRSA").build(keyPair.getPrivate());
        } catch (OperatorCreationException e) {
            //FlyveLog.e("generateRequest",e);
        }
        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subjectName, keyPair.getPublic());
        csr = builder.build(signGen);

        try {
            key = (RSAPrivateKey) keyPair.getPrivate();
        } catch (Exception e) {
            // Nothing should go wrong here
            ///FlyveLog.wtf("generateRequest",e);
            return false;
        }

        //FlyveLog.d("generateCertKeyPair: Generated a new key pair");
        // Save the resulting pair
        saveCsrKey();

        return true;
    }

    private void saveCsrKey() {
        FileOutputStream csrOut = null;
        FileOutputStream keyOut = null;
        try {
            csrOut = new FileOutputStream(csrFile);
            keyOut = new FileOutputStream(keyFile);

            String type = "CERTIFICATE REQUEST";
            byte[] encoding = csr.getEncoded();

            // Write the certificate in OpenSSL PEM format (important for the server)
            StringWriter strWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(strWriter);
            pemWriter.writeObject(csr);
            pemWriter.close();

            // Line endings MUST be UNIX for the PC to accept the cert properly
            OutputStreamWriter csrWriter = new OutputStreamWriter(csrOut);
            String pemStr = strWriter.getBuffer().toString();
            for (int i = 0; i < pemStr.length(); i++) {
                char c = pemStr.charAt(i);
                if (c != '\r')
                    csrWriter.append(c);
            }
            csrWriter.close();

            // Write the private out in PKCS8 format
            keyOut.write(key.getEncoded());


        } catch (IOException e) {
            //FlyveLog.e("saveCsrKey",e);
            // This isn't good because it means we'll have
            // to re-pair next time
            e.printStackTrace();
        } finally {
            try {
                csrOut.close();
                keyOut.close();
            } catch (IOException e){
                //FlyveLog.e("saveCsrKey, IOException", e);
            } catch (NullPointerException e){
                //FlyveLog.e("saveCsrKey, NullP Exception", e);
            }
        }
    }

    public void saveCertKey(String certString) {
        FileOutputStream certOut = null;
        try {
            certOut = new FileOutputStream(certFile);

            byte[] certBytes = certString.getBytes();

            CertificateFactory certFactory = null;
            try {
                certFactory = CertificateFactory.getInstance("X.509", "BC");
                cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
            } catch (CertificateException e) {
                //FlyveLog.e("saveCertKey",e);
            } catch (NoSuchProviderException e) {
                //FlyveLog.e("saveCertKey",e);
            }

            // Write the certificate in OpenSSL PEM format (important for the server)
            StringWriter strWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(strWriter);
            pemWriter.writeObject(cert);
            pemWriter.close();

            // Line endings MUST be UNIX for the PC to accept the cert properly
            OutputStreamWriter certWriter = new OutputStreamWriter(certOut);
            String pemStr = strWriter.getBuffer().toString();
            for (int i = 0; i < pemStr.length(); i++) {
                char c = pemStr.charAt(i);
                if (c != '\r')
                    certWriter.append(c);
            }
            certWriter.close();

        } catch (IOException e) {
            // This isn't good because it means we'll have
            // to re-pair next time
            //FlyveLog.e("saveCertKey",e);
        } finally {
            try {
                certOut.close();
            } catch (IOException e){
                //FlyveLog.e("saveCertKey, IOException", e);
            } catch (NullPointerException e){
                //FlyveLog.e("saveCertKey, NullP exception", e);
            }
        }
    }

    public X509Certificate getClientCertificate() {
        // Use a lock here to ensure only one guy will be generating or loading
        // the certificate and key at a time
        synchronized (globalCryptoLock) {
            // Return a loaded cert if we have one
            if (cert != null) {
                return cert;
            }

            // No loaded cert yet, let's see if we have one on disk
            if (loadCsr()) {
                // Got one
                return cert;
            }

            // Try to generate a new key pair
            if (!generateRequest()) {
                // Failed
                return null;
            }

            // Load the generated pair
            loadCsr();
            return cert;
        }
    }

    public RSAPrivateKey getClientPrivateKey() {
        // Use a lock here to ensure only one guy will be generating or loading
        // the certificate and key at a time
        synchronized (globalCryptoLock) {
            // Return a loaded key if we have one
            if (key != null) {
                return key;
            }

            // No loaded key yet, let's see if we have one on disk
            if (loadCsr()) {
                // Got one
                return key;
            }

            // Try to generate a new key pair
            if (!generateRequest()) {
                // Failed
                return null;
            }

            // Load the generated pair
            loadCsr();
            return key;
        }
    }

    public byte[] getPemEncodedClientCertificate() {
        synchronized (globalCryptoLock) {
            // Call our helper function to do the cert loading/generation for us
            getClientCertificate();

            // Return a cached value if we have it
            return pemCsrBytes;
        }
    }


    public String encodeBase64String(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }
}

