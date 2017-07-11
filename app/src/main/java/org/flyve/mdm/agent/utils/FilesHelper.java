package org.flyve.mdm.agent.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StrictMode;

import org.flyve.mdm.agent.data.DataStorage;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
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
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FilesHelper {

    private Context context;
    private DataStorage cache;
    private Routes routes;

    public FilesHelper(Context context) {
        this.context = context;
        cache = new DataStorage(context);
        routes = new Routes(context);
    }

    private static String getDataDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/apk/");
        return System.getenv("EXTERNAL_STORAGE") + "/apk/";
    }

    private static String getSDcardDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE"));
        return System.getenv("EXTERNAL_STORAGE");
    }

    private static String getUpkDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/.fdroid/");
        return System.getenv("EXTERNAL_STORAGE") + "/.fdroid/";
    }

    private static String getFileDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/file/");
        return System.getenv("EXTERNAL_STORAGE") + "/file/";
    }

    private static String getPicturesDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DCIM);
        return System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DCIM;
    }

    private static String getDocumentsDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DOWNLOADS);
        return System.getenv("EXTERNAL_STORAGE") + "/" + Environment.DIRECTORY_DOWNLOADS;
    }

    private static String getMusicsDir() throws Exception {
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


    /**
     * Download and save file from Id to path
     * @param path String path to save the file on device
     * @param id String Id from
     */
    public Boolean downloadFile(String path, String id, String sessionToken) {

        //prevent CPU from going off if the user presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        String filePath;
        try {
            filePath = convertPath(path);
        } catch (Exception ex) {
            filePath = "";
            FlyveLog.e(ex.getMessage());
        }

        return download(id, filePath, sessionToken);
    }

    /**
     * STEP 1 get session token
     */
    public String getActiveSessionToken() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // STEP 1 get session token
            String data = ConnectionHTTP.getSyncWebData(routes.initSession(cache.getUserToken()), "GET", null);
            if(data.contains("Exception")) {
                FlyveLog.e(data);
                return "";
            }

            JSONObject jsonSession = new JSONObject(data);
            cache.setSessionToken(jsonSession.getString("session_token"));

            // STEP 2 get full session information
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());
            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            data = ConnectionHTTP.getSyncWebData(routes.getFullSession(), "GET", header);
            if(data.contains("Exception")) {
                FlyveLog.e(data);
                return "";
            }

            JSONObject jsonFullSession = new JSONObject(data);
            jsonSession = jsonFullSession.getJSONObject("session");
            JSONObject jsonActiveProfile = jsonSession.getJSONObject("glpiactiveprofile");
            String profileId = jsonActiveProfile.getString("id");
            cache.setProfileId( profileId );

            // STEP 3 Activated the profile
            header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());
            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            data = ConnectionHTTP.getSyncWebData(routes.changeActiveProfile(cache.getProfileId()), "GET", header);
            if(data.contains("Exception")) {
                FlyveLog.e(data);
                return "";
            } else {
                return cache.getSessionToken();
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            return "";
        }
    }

    /**
     * Download file from url to start need a fresh sessionToken
     * @param fileId String file Id
     * @param sessionToken String fresh sessionToken
     * @return Boolean state of download
     */
    private Boolean download(String fileId, final String path, String sessionToken) {

        final String url = routes.PluginFlyvemdmFile(fileId, sessionToken);
        String data = ConnectionHTTP.getSyncWebData(url, "GET",null);

        if(!data.contains("Exception")) {
            try {
                JSONObject jsonObjDownload = new JSONObject(data);
                if (jsonObjDownload.has("name")) {

                    String fileName = jsonObjDownload.getString("name");
                    String filePath = path + fileName;

                    Boolean isSave = ConnectionHTTP.getSyncFile(url, filePath);
                    if(isSave) {
                        FlyveLog.d("Download ready");
                        return true;
                    } else {
                        FlyveLog.e("Download fail: " + data);
                        return false;
                    }
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
                return false;
            }
        } // endif Exception

        FlyveLog.e(data);
        return false;
    }

    public int removeApp(String mPackage){
        Uri packageUri = Uri.parse("package:"+mPackage);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(uninstallIntent);
        } catch (ActivityNotFoundException e) {
            FlyveLog.e(e.getMessage());
            return 0;
        }
        return 1;
    }

    public void installApk(String file) {
        FlyveLog.d(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(Uri.parse("file://" + file), "application/vnd.android.package-archive");
        intent.putExtra("isFromMDM", true);
        intent.putExtra("UPKFilePath", file);
        intent.putExtra("repoaddress", "");
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.putExtra("token_id", 1);
        context.startActivity(intent);
    }
}
