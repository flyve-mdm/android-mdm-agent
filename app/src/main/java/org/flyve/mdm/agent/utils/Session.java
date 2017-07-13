package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import org.flyve.mdm.agent.data.DataStorage;
import org.json.JSONObject;

import java.util.HashMap;

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
 * @date      12/7/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class Session {

    private static Handler uiHandler;

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    private static void runOnUI(Runnable runnable) {
        uiHandler.post(runnable);
    }

    private Context context;
    private DataStorage cache;
    private Routes routes;

    public Session(Context context) {
        this.context = context;
        cache = new DataStorage(context);
        routes = new Routes(context);
    }

    /**
     * STEP 1 get session token
     */
    public void getActiveSessionToken(final sessionCallback callback) {

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    // STEP 1 get session token
                    final String data = ConnectionHTTP.getSyncWebData(routes.initSession(cache.getUserToken()), "GET", null);
                    if(data.contains("Exception")) {
                        FlyveLog.e(data);
                        Session.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(data);
                            }
                        });
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

                    final String dataFullSession = ConnectionHTTP.getSyncWebData(routes.getFullSession(), "GET", header);
                    if(dataFullSession.contains("Exception")) {
                        FlyveLog.e(dataFullSession);
                        Session.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(dataFullSession);
                            }
                        });
                    }

                    JSONObject jsonFullSession = new JSONObject(dataFullSession);
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

                    final String dataActiveProfile = ConnectionHTTP.getSyncWebData(routes.changeActiveProfile(cache.getProfileId()), "GET", header);
                    if(dataActiveProfile.contains("Exception")) {
                        FlyveLog.e(dataActiveProfile);
                        Session.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(dataActiveProfile);
                            }
                        });
                    } else {
                        Session.runOnUI(new Runnable() {
                            public void run() {
                                callback.onSuccess(cache.getSessionToken());
                            }
                        });
                    }
                } catch (final Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    Session.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(ex.getMessage());
                        }
                    });
                }
            }
        });
        t.start();
    }

    public interface sessionCallback {
        void onSuccess(String data);
        void onError(String error);
    }

}
