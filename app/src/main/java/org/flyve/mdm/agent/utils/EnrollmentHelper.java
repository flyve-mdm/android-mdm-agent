package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.AndroidCryptoProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static org.flyve.mdm.agent.utils.ConnectionHTTP.getSyncWebData;

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
 * @date      12/7/17
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class EnrollmentHelper {

    private static Handler uiHandler;
    
    private static final String SESSION_TOKEN = "Session-Token";
    private static final String ACCEPT = "Accept";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CHARSET = "charset=UTF-8";
    private static final String USER_AGENT = "User-Agent";
    private static final String FLYVE_MDM = "Flyve MDM";
    private static final String REFERER = "Referer";

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    private static void runOnUI(Runnable runnable) {
        uiHandler.post(runnable);
    }

    private Context context;
    private DataStorage cache;
    private Routes routes;

    /**
     * This constructor loads the context of the current class
     * @param context of the class
     */
    public EnrollmentHelper(Context context) {
        this.context = context;
        cache = new DataStorage(context);
        routes = new Routes(context);
    }

    /**
     * Manage the errors 
     * @param error
     * @return string the message of the error
     */
    private String manageError(String error) {
        String errorMessage = "";

        if(error.contains("EXCEPTION_HTTP") || error.contains("ERROR")) {
            FlyveLog.e(error);

            errorMessage = context.getResources().getString(R.string.ERROR_INTERNAL);

            if(error.contains("EXCEPTION_HTTP")) {
                return errorMessage;
            }

            // Manage error from backend
            // Example: ["ERROR_SESSION_TOKEN_MISSING","parameter session_token is missing or empty; view documentation in your browser at https://dev.flyve.org/glpi/apirest.php/#ERROR_SESSION_TOKEN_MISSING"]
            try {
                JSONArray jError = new JSONArray(error);
                errorMessage = jError.getString(1);

                String[] errorArray = errorMessage.split(";");
                if(errorArray.length > 0) {
                    errorMessage = errorArray[0];
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }

            return errorMessage;
        }

        return errorMessage;
    }

    /**
     * Get session token
     */
    public void getActiveSessionToken(final EnrollCallBack callback) {

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                try {
                    // STEP 1 get session token
                    final String data = getSyncWebData(routes.initSession(cache.getUserToken()), "GET", null);

                    final String errorMessage = manageError(data);
                    if(!errorMessage.equals("")) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(errorMessage);
                            }
                        });
                        return;
                    }

                    JSONObject jsonSession = new JSONObject(data);
                    cache.setSessionToken(jsonSession.getString("session_token"));

                    // STEP 2 get full session information
                    HashMap<String, String> header = new HashMap();
                    header.put(SESSION_TOKEN,cache.getSessionToken());
                    header.put(ACCEPT,APPLICATION_JSON);
                    header.put(CONTENT_TYPE,APPLICATION_JSON + ";" + CHARSET);
                    header.put(USER_AGENT,FLYVE_MDM);
                    header.put(REFERER,routes.getFullSession());

                    final String dataFullSession = getSyncWebData(routes.getFullSession(), "GET", header);
                    final String errorMessageFullSession = manageError(dataFullSession);
                    if(!errorMessageFullSession.equals("")) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(errorMessageFullSession);
                            }
                        });
                        return;
                    }

                    JSONObject jsonFullSession = new JSONObject(dataFullSession);
                    jsonSession = jsonFullSession.getJSONObject("session");
                    String profileId = jsonSession.getString("plugin_flyvemdm_guest_profiles_id");

                    cache.setProfileId( profileId );

                    // STEP 3 Activated the profile
                    final String dataActiveProfile = getSyncWebData(routes.changeActiveProfile(cache.getProfileId()), "POST", header);
                    final String errorActiveProfile = manageError(dataActiveProfile);
                    if(!errorActiveProfile.equals("")) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(errorActiveProfile);
                            }
                        });
                    } else {
                        // Success
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onSuccess(cache.getSessionToken());
                            }
                        });
                    }
                } catch (final Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(context.getResources().getString(R.string.ERROR_INTERNAL));
                        }
                    });
                }
            }
        });
        t.start();
    }

    /**
     * Get the information to enroll
     * @param payload the information of the phone
     * @param callback
     */
    public void enrollment(final JSONObject payload, final EnrollCallBack callback) {
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                try {
                    HashMap<String, String> header = new HashMap();
                    header.put(SESSION_TOKEN,cache.getSessionToken());

                    header.put(ACCEPT,APPLICATION_JSON);
                    header.put(CONTENT_TYPE,APPLICATION_JSON + ";" + CHARSET);

                    JSONObject input = new JSONObject();
                    input.put("input", payload);

                    String data = getSyncWebData(routes.pluginFlyvemdmAgent(), input, header);
                    if(data.contains("ERROR")){
                        JSONArray jsonArr = new JSONArray(data);
                        final String msgError = jsonArr.get(1).toString();
                        FlyveLog.e(msgError);

                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(msgError);
                            }
                        });
                    } else {
                        JSONObject jsonAgent = new JSONObject(data);
                        cache.setAgentId(jsonAgent.getString("id"));

                        header = new HashMap();
                        header.put(SESSION_TOKEN,cache.getSessionToken());
                        header.put(ACCEPT,APPLICATION_JSON);
                        header.put(CONTENT_TYPE,APPLICATION_JSON + ";" + CHARSET);
                        header.put(USER_AGENT,FLYVE_MDM);
                        header.put(REFERER,routes.pluginFlyvemdmAgent());

                        String dataAgent = ConnectionHTTP.getSyncWebData(routes.pluginFlyvemdmAgent(cache.getAgentId()), "GET", header);

                        JSONObject jsonObject = new JSONObject(dataAgent);

                        String mbroker = jsonObject.getString("broker");
                        String mport = jsonObject.getString("port");
                        String mssl = jsonObject.getString("tls");
                        String mtopic = jsonObject.getString("topic");
                        String mpassword = jsonObject.getString("mqttpasswd");
                        String mcert = jsonObject.getString("certificate");
                        String mNameEmail = jsonObject.getString("name");
                        int mComputersId = jsonObject.getInt("computers_id");
                        int mId = jsonObject.getInt("id");
                        FlyveLog.d("Id: " + mId);
                        int mEntitiesId = jsonObject.getInt("entities_id");
                        int mFleetId = jsonObject.getInt("plugin_flyvemdm_fleets_id");

                        // Agent information
                        cache.setBroker( mbroker );
                        cache.setPort( mport );
                        cache.setTls( mssl );
                        cache.setTopic( mtopic );
                        cache.setMqttuser( Helpers.getDeviceSerial() );
                        cache.setMqttpasswd( mpassword );
                        cache.setCertificate( mcert );
                        cache.setName( mNameEmail );
                        cache.setComputersId( String.valueOf(mComputersId) );
                        cache.setEntitiesId( String.valueOf(mEntitiesId) );
                        cache.setPluginFlyvemdmFleetsId( String.valueOf(mFleetId) );

                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onSuccess("success");
                            }
                        });
                    }

                } catch (Exception ex) {
                    final String error  = ex.getMessage();
                    FlyveLog.e(error);
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(context.getResources().getString(R.string.ERROR_INTERNAL));
                        }
                    });
                }
            }
        });
        t.start();
    }

    /**
     * Create X509 certificate
     */
    public void createX509cert(final EnrollCallBack callback) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    AndroidCryptoProvider createCertificate = new AndroidCryptoProvider(context);
                    createCertificate.generateRequest(new AndroidCryptoProvider.GenerateCallback() {
                        @Override
                        public void onGenerate(final boolean work) {
                            EnrollmentHelper.runOnUI(new Runnable() {
                                public void run() {
                                    if(work) {
                                        callback.onSuccess("true");
                                    }
                                }
                            });
                        }
                    });
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError("false");
                        }
                    });
                }
            }
        }).start();
    }

    public interface EnrollCallBack {
        void onSuccess(String data);
        void onError(String error);
    }

}
