package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.os.Environment;
import android.os.PowerManager;

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
    private String fileId;

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

    public void downloadFile(String id) {

        //prevent CPU from going off if the user presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        fileId = id;

        initSession();
    }

    /**
     * STEP 1 get session token
     */
    private void initSession() {
        try {
            ConnectionHTTP.getWebData(
                    routes.initSession( cache.getUserToken() ),
                    "GET" ,
                    new ConnectionHTTP.DataCallback() {
                        @Override
                        public void callback(String data) {

                            try {
                                JSONObject jsonSession = new JSONObject(data);
                                cache.setSessionToken( jsonSession.getString("session_token") );
                                getFullSession();

                            } catch (Exception ex) {
                                FlyveLog.e( ex.getMessage() );
                            }
                        }
                    });
        }
        catch (Exception ex) {
            FlyveLog.e( ex.getMessage() );
        }
    }

    /**
     * STEP 2 get full session information
     */
    private void getFullSession() {
        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.getFullSession(), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                    try {
                        JSONObject jsonFullSession = new JSONObject(data);

                        JSONObject jsonSession = jsonFullSession.getJSONObject("session");

                        JSONObject jsonActiveProfile = jsonSession.getJSONObject("glpiactiveprofile");

                        String profileId = jsonActiveProfile.getString("id");
                        cache.setProfileId( profileId );

                        changeActiveProfile();

                    } catch (Exception ex) {
                        FlyveLog.e( ex.getMessage() );
                    }

                    changeActiveProfile();

                }
            });
        } catch (Exception ex) {
            FlyveLog.e( ex.getMessage() );
        }
    }

    /**
     * STEP 3 Activated the profile
     */
    private void changeActiveProfile() {

        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.changeActiveProfile(cache.getProfileId()), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {
                    download();
                }
            });

        } catch (Exception ex) {
            FlyveLog.e( ex.getMessage() );
        }
    }

    /**
     * STEP 4 Download file
     */
    private void download() {
        String url = routes.PluginFlyvemdmFile(fileId);
        ConnectionHTTP.getWebData(url, "GET", new ConnectionHTTP.DataCallback() {
            @Override
            public void callback(String data) {

                try {
                    JSONObject jsonObjDownload = new JSONObject(data);
                    if (jsonObjDownload.has("name")) {
                        String mJsonDownload = jsonObjDownload.getString("name");

                    }
                } catch (Exception ex) {
                    FlyveLog.e("Error downloading: " + ex.getMessage());
                }
            }
        });
    }
}
