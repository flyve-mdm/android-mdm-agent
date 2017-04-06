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

package com.teclib.flyvemdm;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;

import com.teclib.api.FlyveLog;
import com.teclib.api.HttpRequest;
import com.teclib.database.SharedPreferenceAction;
import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.database.SharedPreferenceSettings;
import com.teclib.service.MQTTService;
import com.teclib.service.NotificationAdminRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DownloadTask extends AsyncTask<String, Integer, String> {

    private static final String SESSION_TOKEN = "session_token=";
    private static final String PLUGIN_FLYVEMDM_PACKAGE = "PluginFlyvemdmPackage/";
    private SharedPreferenceAction mSharedPreferenceAction;
    private SharedPreferenceMQTT sharedPreferenceMQTT;
    private SharedPreferenceSettings sharedPreferenceSettings;

    private Context mContext;

    private int fileType = 0;

    public static String mJsonDownload = null;
    private String PackageName;

    private String mSessionToken;
    private String mPluginId;

    private static String mDownloadFile;
    private static String mDestination;

    private AsyncTaskCallbackInterface mCallBack;

    public static String directory;


    public DownloadTask(Context context, AsyncTaskCallbackInterface callback) {
        mContext = context;
        mCallBack = callback;
        mSharedPreferenceAction = new SharedPreferenceAction();
        sharedPreferenceMQTT = new SharedPreferenceMQTT();
        sharedPreferenceSettings = new SharedPreferenceSettings();
        //mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static String getmJsonDownload() {
        return mJsonDownload;
    }

    public static String getDataDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/apk/");
        return System.getenv("EXTERNAL_STORAGE") + "/apk/";
    }

    public static String getSDcardDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE"));
        return System.getenv("EXTERNAL_STORAGE");
    }

    public static String getUpkDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/.fdroid/");
        return System.getenv("EXTERNAL_STORAGE") + "/.fdroid/";
    }

    public static String getFileDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/file/");
        return System.getenv("EXTERNAL_STORAGE") + "/file/";
    }

    public static String getPicturesDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DCIM);
        return System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DCIM;
    }

    public static String getDocumentsDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DOCUMENTS);
        return System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DOCUMENTS;
    }

    public static String getMusicsDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_MUSIC);
        return System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_MUSIC;
    }

    private String convertPath(String receivePath) throws Exception {

        String sreturn = receivePath;

        Pattern sdcard = Pattern.compile("%SDCARD%");
        Pattern document = Pattern.compile("%DOCUMENTS%");
        Pattern music = Pattern.compile("%MUSIC%");
        Pattern photo = Pattern.compile("%PHOTOS%");

        Matcher msdcard = sdcard.matcher(receivePath);
        Matcher mdocument = document.matcher(receivePath);
        Matcher mmusic = music.matcher(receivePath);
        Matcher mphoto = photo.matcher(receivePath);

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


    @SuppressWarnings("resource")
    @Override
    protected String doInBackground(String... sUrl) {
        String mFileId = sUrl[1];
        String mjsonGetDownload;
        String mServer = sharedPreferenceSettings.getApiServer(mContext);
        String mUserToken = sharedPreferenceSettings.getUserToken(mContext);


        if ("file".equals(sUrl[0])) {
            mDestination = sUrl[2];
        } else if (sUrl[0].equals("application")) {
            PackageName = sUrl[2];
        }
        FlyveLog.d("file id = " + mFileId);
        FlyveLog.d(sUrl[0]);
        FlyveLog.d(sUrl[1]);
        FlyveLog.d(sUrl[2]);

        //prevent CPU from going off if the user presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        // new way download API REST
        HttpRequest request = new HttpRequest(mContext);

        try {
            String jsonGet = request.GetRequest(false, mServer + "/" + "initSession?" + "user_token=" + mUserToken);
            FlyveLog.d(jsonGet);
            JSONObject jsonObj = new JSONObject(jsonGet);
            if (jsonObj.has("session_token")) {
                mSessionToken = jsonObj.getString("session_token");
            }
            String jsonFullSession = request.GetRequest(false, mServer + "/" + "getFullSession?" + SESSION_TOKEN + mSessionToken);
            String pattern = "\"plugin_flyvemdm_guest_profiles_id\":\\s*([0-9]+)";

            // Create a Pattern object
            Pattern search = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = search.matcher(jsonFullSession);
            if (m.find()) {
                mPluginId = m.group(1);
            } else {
                FlyveLog.wtf("Error plugin_flyvemdm_guest_profiles_id");
            }

            request.GetRequest(false, mServer + "/" + "changeActiveProfile?" + SESSION_TOKEN + mSessionToken + "&profiles_id=" + mPluginId);

            if ("file".equals(sUrl[0])) {
                mjsonGetDownload = request.GetRequest(false, mServer + "/" + "PluginFlyvemdmFile/" + mFileId + "?" + SESSION_TOKEN + mSessionToken);
            } else {
                mjsonGetDownload = request.GetRequest(false, mServer + "/" + PLUGIN_FLYVEMDM_PACKAGE + mFileId + "?" + SESSION_TOKEN + mSessionToken);
            }

            if ("file".equals(sUrl[0])) {
                JSONObject jsonObjDownload = new JSONObject(mjsonGetDownload);
                if (jsonObjDownload.has("name")) {
                    mJsonDownload = jsonObjDownload.getString("name");
                } else {
                    return "fail";
                }
                directory = convertPath(mDestination);

            } else {
                JSONObject jsonObjDownload = new JSONObject(mjsonGetDownload);
                if (jsonObjDownload.has("dl_filename")) {
                    mJsonDownload = jsonObjDownload.getString("dl_filename");
                } else {
                    return "fail";
                }

                String[] downloadType = mJsonDownload.split("\\.");
                if ("upk".equals(downloadType[downloadType.length - 1])) {
                    fileType = 2;
                    try {
                        directory = getUpkDir();
                    } catch (Exception e) {
                        FlyveLog.e(e.getMessage());
                    }
                }
                if ("apk".equals(downloadType[downloadType.length - 1])) {
                    fileType = 1;
                    try {
                        directory = getDataDir();
                    } catch (Exception e) {
                        FlyveLog.e(e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            FlyveLog.e(e.getMessage());
            return "fail";
        }
        File file = new File(directory + mJsonDownload);

        FlyveLog.d("Test file : " + directory + mJsonDownload);
        // test if file exist
        if (file.exists()) {
            FlyveLog.d("File exist");
            if (fileType == 1 || fileType == 2) {
                Intent intent = new Intent(mContext, NotificationAdminRequest.class);
                mContext.startService(intent);
            }
            return "fail";
        }

        try {
            new File(directory).mkdirs();
            try {

                if (fileType == 1) {
                    mDownloadFile = request.GetRequest(true, mServer + "/" + PLUGIN_FLYVEMDM_PACKAGE + mFileId + "?" + SESSION_TOKEN + mSessionToken, "Accept::application/octet-stream", "Content-Type::application/json");
                } else if (fileType == 2) {
                    mDownloadFile = request.GetRequest(true, mServer + "/" + PLUGIN_FLYVEMDM_PACKAGE + mFileId + "?" + SESSION_TOKEN + mSessionToken, "Accept::application/octet-stream", "Content-Type::application/json");
                } else if (fileType == 0) {
                    mDownloadFile = request.GetRequest(true, mServer + "/" + "PluginFlyvemdmFile/" + mFileId + "?" + SESSION_TOKEN + mSessionToken, "Accept::application/octet-stream", "Content-Type::application/json");
                }

            } catch (Exception e) {
                FlyveLog.e(e.getMessage());
                return e.toString();
            }
        } finally {
            wl.release(); // release the lock screen
        }
        if (fileType == 1) {
            String hash = null;
            try {
                hash = checksum(directory + "/" + mJsonDownload);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSharedPreferenceAction.saveApks(mContext, directory + mJsonDownload + ";;" + hash + ";;" + PackageName);
        } else if (fileType == 2) {
            mSharedPreferenceAction.saveUpks(mContext, directory + mJsonDownload);
            // TODO save hash and package name
        } else if (fileType == 0) {
            mSharedPreferenceAction.saveFiles(mContext, directory + mJsonDownload);
        }
        return null;
    }

    @Override // onPreExecute and onProgressUpdate run on ui thread so you can update ui from here
    protected void onPreExecute() {
        onProgressUpdate(0);
    }

    @Override
    protected void onPostExecute(String result) {

        if (result != null) {
            if (mCallBack != null) {
                mCallBack.onFailure(new Exception("failure"));
            }
        } else {
            FlyveLog.i("Download ok");

            if (fileType == 1) {
                if (mCallBack != null) {
                    mCallBack.onSuccess("downloaded");
                }

                fileType = 0;

            } else if (fileType == 2) {
                Intent myIntent = new Intent(mContext, AppManagementActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myIntent.setAction("android.intent.action.MAIN");
                mContext.startActivity(myIntent);
                //AppManagementActivity executeInstallUpk = new AppManagementActivity(mContext);
                //executeInstallUpk.executeInstallUpk();
                fileType = 0;
            }

        }
    }

    public String checksum(String file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        ;
        byte[] mdbytes = md.digest();


        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
        }

        FlyveLog.d("Hex format : " + hexString.toString());

        return hexString.toString();
    }

}