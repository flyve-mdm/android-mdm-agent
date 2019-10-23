/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.core.enrollment;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.security.AndroidCryptoProvider;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static org.flyve.mdm.agent.utils.ConnectionHTTP.getSyncWebData;

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
    private String sessionToken = "";
    private String data = "";

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    private static void runOnUI(Runnable runnable) {
        uiHandler.post(runnable);
    }

    private Context context;
    private MqttData cache;
    private Routes routes;

    /**
     * This constructor loads the context of the current class
     * @param context of the class
     */
    public EnrollmentHelper(Context context) {
        this.context = context;
        cache = new MqttData(context);
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
            FlyveLog.e(this.getClass().getName() + ", manageError", error);

            errorMessage = error;

            if(error.contains("EXCEPTION_HTTP")) {
                return errorMessage;
            }

            // Manage error from backend
            // Example: ["ERROR_SESSION_TOKEN_MISSING","parameter session_token is missing or empty; View documentation in your browser at https://dev.flyve.org/glpi/apirest.php/#ERROR_SESSION_TOKEN_MISSING"]
            try {
                JSONArray jError = new JSONArray(error);
                errorMessage = jError.getString(1);

                String[] errorArray = errorMessage.split(";");
                if(errorArray.length > 0) {
                    errorMessage = errorArray[0];
                }
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", manageError", ex.getMessage());
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
                JSONObject jsonSession;
                HashMap<String, String> header = new HashMap();
                header.put("user_token", cache.getApiToken());

                try {
                    // STEP 1 get session token
                    data = getSyncWebData(routes.initSession(cache.getApiToken()), "GET", header);

                    final String errorMessage = manageError(data);
                    if(!errorMessage.equals("")) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(CommonErrorType.ENROLLMENT_HELPER_INITSESSION, errorMessage);
                            }
                        });
                        return;
                    }

                    jsonSession = new JSONObject(data);
                    sessionToken = jsonSession.getString("session_token");
                    cache.setSessionToken(sessionToken);

                } catch (final Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", getActiveSessionToken", ex.getMessage());
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(CommonErrorType.ENROLLMENT_HELPER_INITSESSION, context.getString(R.string.wrong_json_format, data, ex.getMessage()));
                        }
                    });
                }

                // Success
                EnrollmentHelper.runOnUI(new Runnable() {
                    public void run() {
                        callback.onSuccess(sessionToken);
                    }
                });
            }
        });
        t.start();
    }

    public void getActiveSessionTokenEnrollment(final EnrollCallBack callback) {

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                String profileId = "";
                JSONObject jsonSession;
                HashMap<String, String> header = new HashMap();
                header.put("user_token", cache.getUserToken());

                try {
                    // STEP 1 get session token
                    data = getSyncWebData(routes.initSession(cache.getUserToken()), "GET", header);

                    final String errorMessage = manageError(data);
                    if(!errorMessage.equals("")) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(CommonErrorType.ENROLLMENT_HELPER_INITSESSION, errorMessage);
                            }
                        });
                        return;
                    }

                    jsonSession = new JSONObject(data);
                    sessionToken = jsonSession.getString("session_token");
                    cache.setSessionToken(sessionToken);

                } catch (final Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", getActiveSessionToken", ex.getMessage());
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(CommonErrorType.ENROLLMENT_HELPER_INITSESSION, context.getString(R.string.wrong_json_format, data, ex.getMessage()));
                        }
                    });
                }

                try {
                    // STEP 2 get full session information
                    header.put(SESSION_TOKEN, sessionToken);
                    header.put(ACCEPT,APPLICATION_JSON);
                    header.put(CONTENT_TYPE,APPLICATION_JSON + ";" + CHARSET);
                    header.put(USER_AGENT,FLYVE_MDM);
                    header.put(REFERER,routes.getFullSession());

                    data = getSyncWebData(routes.getFullSession(), "GET", header);
                    final String errorMessageFullSession = manageError(data);
                    if(!errorMessageFullSession.equals("")) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(CommonErrorType.ENROLLMENT_HELPER_FULLSESSION, errorMessageFullSession);
                            }
                        });
                        return;
                    }

                    JSONObject jsonFullSession = new JSONObject(data);
                    jsonSession = jsonFullSession.getJSONObject("session");
                    profileId = jsonSession.getString("plugin_flyvemdm_guest_profiles_id");

                    cache.setProfileId(profileId);

                } catch (final Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", getActiveSessionToken", ex.getMessage());
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(CommonErrorType.ENROLLMENT_HELPER_FULLSESSION, context.getString(R.string.wrong_json_format, data, ex.getMessage()));
                        }
                    });
                }

                try {
                    // STEP 3 Activated the profile
                    data = getSyncWebData(routes.changeActiveProfile(profileId), "POST", header);
                    final String errorActiveProfile = manageError(data);
                    if(!errorActiveProfile.equals("")) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(CommonErrorType.ENROLLMENT_HELPER_CHANGEACTIVEPROFILE, errorActiveProfile);
                            }
                        });
                    } else {
                        // Success
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onSuccess(sessionToken);
                            }
                        });
                    }
                } catch (final Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", getActiveSessionToken", ex.getMessage());
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(CommonErrorType.ENROLLMENT_HELPER_CHANGEACTIVEPROFILE, context.getString(R.string.wrong_json_format, data, ex.getMessage()));
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
                HashMap<String, String> header = new HashMap();
                header.put(SESSION_TOKEN, cache.getSessionToken());

                header.put(ACCEPT,APPLICATION_JSON);
                header.put(CONTENT_TYPE,APPLICATION_JSON + ";" + CHARSET);

                JSONObject input;
                try {
                    input = new JSONObject();
                    input.put("input", payload);
                    FlyveLog.d("Flyve MDM Payload: " + input);
                } catch (final Exception ex) {
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(CommonErrorType.ENROLLMENT_HELPER_INPUT_PAYLOAD, ex.getMessage());
                        }
                    });
                    return;
                }

                // Sending the payload to the backend
                String data = getSyncWebData(routes.pluginFlyvemdmAgent(), input, header);
                FlyveLog.d("Payload return: " + data);

                if(data.contains("ERROR")){
                    final String msgError = manageError(data);
                    FlyveLog.e(this.getClass().getName() + ", enrollment", msgError + " - Device serial: " + Helpers.getDeviceSerial());

                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(CommonErrorType.ENROLLMENT_HELPER_REQUEST_PAYLOAD, msgError);
                        }
                    });
                } else {
                    String agentId;
                    try {
                        JSONObject jsonAgent = new JSONObject(data);
                        agentId = jsonAgent.getString("id");
                    } catch (final Exception ex) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(CommonErrorType.ENROLLMENT_HELPER_AGENT_ID, ex.getMessage());
                            }
                        });
                        return;
                    }

                    header = new HashMap();
                    header.put(SESSION_TOKEN, cache.getSessionToken());
                    header.put(ACCEPT,APPLICATION_JSON);
                    header.put(CONTENT_TYPE,APPLICATION_JSON + ";" + CHARSET);
                    header.put(USER_AGENT,FLYVE_MDM);
                    header.put(REFERER,routes.pluginFlyvemdmAgent());

                    String dataAgent = ConnectionHTTP.getSyncWebData(routes.pluginFlyvemdmAgent(agentId), "GET", header);

                    try {
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
                        String mApiToken = jsonObject.getString("api_token");

                        cache.setAgentId(agentId);
                        cache.setBroker(mbroker);
                        cache.setPort(mport);
                        cache.setTls(mssl);
                        cache.setTopic(mtopic);
                        cache.setMqttUser(Helpers.getDeviceSerial());
                        cache.setMqttPasswd(mpassword);
                        cache.setCertificate(mcert);
                        cache.setName(mNameEmail);
                        cache.setComputersId(String.valueOf(mComputersId));
                        cache.setEntitiesId(String.valueOf(mEntitiesId));
                        cache.setPluginFlyvemdmFleetsId(String.valueOf(mFleetId));
                        cache.setApiToken(mApiToken);

                    } catch (final Exception ex) {
                        EnrollmentHelper.runOnUI(new Runnable() {
                            public void run() {
                                callback.onError(CommonErrorType.ENROLLMENT_HELPER_DATA_AGENT, ex.getMessage());
                            }
                        });
                        return;
                    }

                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onSuccess("success");
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
                    FlyveLog.e(this.getClass().getName() + ", createX509cert", ex.getMessage());
                    EnrollmentHelper.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(CommonErrorType.ENROLLMENT_HELPER_X509CERTIFICATION, "false");
                        }
                    });
                }
            }
        }).start();
    }

    public interface EnrollCallBack {
        void onSuccess(String data);
        void onError(int type, String error);
    }
}
