package org.flyve.mdm.agent.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;

import org.flyve.mdm.agent.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      10/7/17
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FilesHelper {

    private static final String EXTERNAL_STORAGE = "EXTERNAL_STORAGE"; 
    private Context context;
    private Routes routes;
    private String sessionToken;

    /**
     * This constructor loads the context of the current class
     * @param context of the class
     */
    public FilesHelper(Context context) {
        this.context = context;
        routes = new Routes(context);
    }

    /**
     * Get the directory of the apk
     * @return string the apk directory
     */
    private static String getApkDir() {
        FlyveLog.d(System.getenv(EXTERNAL_STORAGE) + "/apk/");
        return System.getenv(EXTERNAL_STORAGE) + "/apk/";
    }

    /**
     * Get the directory of the Secure Digital card
     * @return string the SD card directory
     */
    private static String getSDcardDir() {
        FlyveLog.d(System.getenv(EXTERNAL_STORAGE));
        return System.getenv(EXTERNAL_STORAGE);
    }

    /**
     * Get the directory of the UPK
     * @return string the UPK directory
     */
    private static String getUpkDir() {
        FlyveLog.d(System.getenv(EXTERNAL_STORAGE) + "/.fdroid/");
        return System.getenv(EXTERNAL_STORAGE) + "/.fdroid/";
    }

    /**
     * Get the directory of the pictures
     * @return string the pictures directory
     */
    private static String getPicturesDir() {
        FlyveLog.d(System.getenv(EXTERNAL_STORAGE) + "/" + Environment.DIRECTORY_DCIM);
        return System.getenv(EXTERNAL_STORAGE) + "/" + Environment.DIRECTORY_DCIM;
    }

    /**
     * Get the directory of the documents
     * @return string the documents directory
     */
    private static String getDocumentsDir() {
        FlyveLog.d(System.getenv(EXTERNAL_STORAGE) + "/" + Environment.DIRECTORY_DOWNLOADS);
        return System.getenv(EXTERNAL_STORAGE) + "/" + Environment.DIRECTORY_DOWNLOADS;
    }

    /**
     * Get the directory of the music
     * @return string the music directory
     * @throws Exception
     */
    private static String getMusicsDir() {
        FlyveLog.d(System.getenv(EXTERNAL_STORAGE) + "/" + Environment.DIRECTORY_MUSIC);
        return System.getenv(EXTERNAL_STORAGE) + "/" + Environment.DIRECTORY_MUSIC;
    }

    /**
     * Convert the path according to the given arguments
     * @param receivePath
     * @return string the converted path
     */
    private String convertPath(String receivePath) {

        String sreturn = receivePath;

        Pattern sdcard = Pattern.compile("%SDCARD%");
        Pattern document = Pattern.compile("%DOCUMENTS%");
        Pattern music = Pattern.compile("%MUSIC%");
        Pattern photo = Pattern.compile("%PHOTOS%");

        Matcher msdcard = sdcard.matcher(receivePath);
        Matcher mdocument = document.matcher(receivePath);
        Matcher mmusic = music.matcher(receivePath);
        Matcher mphoto = photo.matcher(receivePath);

        //Find the sequence that matches the pattern
        if (msdcard.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%SDCARD%", getSDcardDir());
        }

        if (mdocument.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%DOCUMENTS%", getDocumentsDir());
        }

        if (mmusic.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%MUSIC%", getMusicsDir());
        }

        if (mphoto.find()) {
            sreturn = receivePath;
            sreturn = sreturn.replace("%PHOTOS%", getPicturesDir());
        }
        FlyveLog.d("convertPath return = " + sreturn);
        return sreturn;
    }

    /**
     * Download and save file from Id to path
     * @param path String path to save the file on device
     * @param id String Id from
     * @param sessionToken
     */
    public Boolean downloadFile(String path, String id, String sessionToken) {

        //prevent CPU from going off if the user presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        String filePath = "";
        try {
            filePath = convertPath(path);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }

        this.sessionToken = sessionToken;
        final String url = routes.pluginFlyvemdmFile(id, sessionToken);
        String completeFilePath = download(url, filePath);

        return(completeFilePath.equalsIgnoreCase(""));
    }

    /**
     * Download, save and install app
     * @param packageFile String package of the app
     * @param id String Id from
     * @param sessionToken
     */
    public Boolean downloadApk(String packageFile, String id, String sessionToken) {

        FlyveLog.d("packageFile: " + packageFile);

        //prevent CPU from going off if the user presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        String filePath = "";
        try {
            filePath = getApkDir();
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }

        this.sessionToken = sessionToken;
        final String url = routes.pluginFlyvemdmPackage(id, sessionToken);
        String completeFilePath = download(url, filePath);
        if(completeFilePath.equalsIgnoreCase("")) {
            return false;
        } else {
            String[] apk = completeFilePath.split("$$");
            for(int i=0; i<apk.length;i++) {
                if(!apk[i].equals("")) {
                    installApk(apk[i]);
                }
            }
            return true;
        }
    }

    /**
     * Download file from url to start need a fresh sessionToken
     * @param url String url to download the file
     * @param path String path to save
     * @return String complete path with name of the file
     */
    private String download(final String url, final String path) {

        String data = ConnectionHTTP.getSyncWebData(url, "GET",null);

        if(data.contains("ERROR")) {
            Helpers.sendToNotificationBar(context, context.getResources().getString(R.string.download_file_fail));
            FlyveLog.e(data);
        } else {
            try {
                JSONObject jsonObjDownload = new JSONObject(data);
                getFile(jsonObjDownload, path, url, data);
            } catch (Exception ex) {
                try {
                    JSONArray arr = new JSONArray(data);
                    StringBuffer str = new StringBuffer();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject jsonObjDownload = arr.getJSONObject(i);
                        str.append(getFile(jsonObjDownload, path, url, data) + "$$");
                    }
                    return str.toString();
                } catch (Exception ex1) {
                    FlyveLog.e(ex1.getMessage());
                    return "";
                }
            }
        } // endif Exception
        return "";
    }

    private String getFile(JSONObject jsonObjDownload, String path, String url, String data) {

        String fileName = "";

        try {
            // Both has name
            if (jsonObjDownload.has("name")) {
                fileName = jsonObjDownload.getString("name");
            }

            // is APK
            if (jsonObjDownload.has("dl_filename")) {
                fileName = jsonObjDownload.getString("dl_filename");
            }

            // validating if folder exists or create
            new File(path).mkdirs();

            // validating if file exists
            String filePath = path + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                FlyveLog.d("File exists");
                return "";
            }

            Boolean isSave = ConnectionHTTP.getSyncFile(url, filePath);
            if (isSave) {
                Helpers.sendToNotificationBar(context, context.getResources().getString(R.string.download_file_ready));
                FlyveLog.d("Download ready");
                return filePath;
            } else {
                Helpers.sendToNotificationBar(context, context.getResources().getString(R.string.download_file_fail));
                FlyveLog.e("Download fail: " + data);
                return "";
            }
        } catch(Exception ex) {
            FlyveLog.e(ex.getMessage());
            return "";
        }
    }

    /**
     * Remove the file according to the given path
     * @param filePath
     * @return boolean true if file deleted, false otherwise
     */
    public boolean removeFile(String filePath) {
        try {
            String realPath = convertPath(filePath);
            File file = new File(realPath);
            return file.delete();
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            return false;
        }
    }

    /**
     * Uninstall the Android Package
     * @param context
     * @param mPackage to uninstall
     * @return int if it succeed 1, otherwise 0
     */
    public static int removeApk(Context context, String mPackage){
        Uri packageUri = Uri.parse("package:"+mPackage);
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            FlyveLog.e(e.getMessage());
            return 0;
        }
        return 1;
    }

    /**
     * Install the Android Package
     * @param file to install
     */
    public void installApk(String file) {
        FlyveLog.d(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + file), "application/vnd.android.package-archive");
        intent.putExtra("isFromMDM", true);
        intent.putExtra("UPKFilePath", file);
        intent.putExtra("repoaddress", "");
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.putExtra("token_id", 1);
        context.startActivity(intent);
    }
}
