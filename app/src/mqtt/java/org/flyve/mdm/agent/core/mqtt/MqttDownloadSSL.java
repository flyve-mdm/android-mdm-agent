package org.flyve.mdm.agent.core.mqtt;

import android.content.Context;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Base64;

import org.flyve.mdm.agent.utils.FlyveLog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class MqttDownloadSSL  extends AsyncTask<Object, Integer, Boolean> {


    protected Boolean doInBackground(Object... object) {

        String mBroker = (String) object[0];
        Integer mPort = (Integer) object[1];
        Context ctx = (Context) object[2];

        FlyveLog.v("Trying to load certificate from : %s",mBroker+":"+mPort);
        String cert = "";

        // create custom trust manager to ignore trust paths
        TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };


        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { trm }, null);
            SSLSocketFactory factory =sc.getSocketFactory();
            SSLSocket socket =(SSLSocket)factory.createSocket(mBroker, mPort);
            socket.startHandshake();
            SSLSession session = socket.getSession();

            //get all certificates
            java.security.cert.Certificate[] certs = session.getPeerCertificates();
            for (int i = 0; i < certs.length; i++) {
                cert =  cert+"-----BEGIN CERTIFICATE-----\n"+ Base64.encodeToString(certs[i].getEncoded(), Base64.DEFAULT)+"-----END CERTIFICATE-----\n\n";
            }

            FlyveLog.v("Find certificates : %s",cert);

        } catch (NetworkOnMainThreadException ex){
            FlyveLog.e(this.getClass().getName() + ", NetworkOnMainThreadException", ex.toString());
        } catch (Exception ex){
            FlyveLog.e(this.getClass().getName() + ", Exception", ex.toString());
        }

        //try to save certificates into file
        try {
            String filename = "broker_cert";
            FileOutputStream outputStream;
            outputStream = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(cert.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException ex) {
            FlyveLog.e(this.getClass().getName() + ", FileNotFoundException", ex.toString());
        } catch (IOException ex){
            FlyveLog.e(this.getClass().getName() + ", IOException", ex.toString());
        }
        return true;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Long result) {
    }

}
