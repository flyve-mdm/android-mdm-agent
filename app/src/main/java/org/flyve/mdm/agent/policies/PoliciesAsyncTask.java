package org.flyve.mdm.agent.policies;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.ApplicationData;
import org.flyve.mdm.agent.data.database.FileData;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.ui.LockActivity;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.Inventory;
import org.flyve.policies.manager.AndroidPolicies;
import org.json.JSONObject;

public class PoliciesAsyncTask extends AsyncTask<Object, Integer, Boolean> {

    public static final int PING = 1;
    public static final int GEOLOCATE = 2;
    public static final int INVENTORY = 3;
    public static final int POLICIES = 4;
    public static final int UNENROLL = 5;
    public static final int WIPE = 6;
    public static final int LOCK = 7;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int priority = 1;

    protected Boolean doInBackground(Object... object) {

        final Context context = (Context) object[0];
        final Integer action = (Integer) object[1];
        final String topic = (String) object[2];
        final String message = (String) object[3];

        mHandler.post(new Runnable() {
            public void run() {

                switch (action)
                {

                    case LOCK :
                        {
                            if(!MDMAgent.isSecureVersion()) {
                                try {
                                    JSONObject jsonObj = new JSONObject(message);
                                    if (jsonObj.has("lock")) {
                                        String lock = jsonObj.getString("lock");
                                        AndroidPolicies androidPolicies = new AndroidPolicies(context, FlyveAdminReceiver.class);

                                        if(lock.equalsIgnoreCase("now")) {
                                            //lock screen
                                            androidPolicies.lockScreen(LockActivity.class,context);
                                            //lock device
                                            androidPolicies.lockDevice();
                                        } else {

                                            //unlock screen
                                            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                                                    && Settings.canDrawOverlays(context)){
                                                try{
                                                    MDMAgent mainActivity = ((MDMAgent)
                                                            context);
                                                    mainActivity.getLockActivity().unlockScreen();
                                                }catch (Exception e){

                                                }

                                            }
                                            //unlock device
                                            androidPolicies.unlockDevice();

                                            Helpers.sendBroadcast("unlock", "org.flyvemdm.finishlock", context);
                                        }
                                    }
                                } catch (Exception ex) {
                                    FlyveLog.e(this.getClass().getName() + ", LOCK ", ex.getMessage());
                                }
                            }
                        }
                    case PING :
                        {
                            String data = "{\"input\":{\"_pong\":\"!\"}}";
                            Routes routes = new Routes(context);
                            MqttData cache = new MqttData(context);
                            String url = routes.pluginFlyvemdmAgent(cache.getAgentId());
                            pluginHttpResponse(context, url, data);
                        }
                        break;

                    case WIPE :
                    {
                        if(!MDMAgent.isSecureVersion()) {
                            try {
                                JSONObject jsonObj = new JSONObject(message);
                                if(jsonObj.has("wipe") && "NOW".equalsIgnoreCase(jsonObj.getString("wipe")) ) {
                                    sendStatusbyHttp(context, false);
                                    new AndroidPolicies(context, FlyveAdminReceiver.class).wipe();
                                }
                            } catch (Exception ex) {
                                FlyveLog.e(this.getClass().getName() + ", WIPE ",ex.getMessage());
                            }
                        }
                    }
                    break;
                    case UNENROLL :
                    {
                        //set offline
                        sendStatusbyHttp(context, false);

                        // Remove all the information
                        new ApplicationData(context).deleteAll();
                        new FileData(context).deleteAll();
                        new MqttData(context).deleteAll();
                        new PoliciesData(context).deleteAll();

                    }
                    break;

                    case GEOLOCATE:
                        {
                            FastLocationProvider fastLocationProvider = new FastLocationProvider();
                            Routes routes = new Routes(context);
                            final String url = routes.pluginFlyvemdmGeolocation();

                            boolean isAvailable = fastLocationProvider.getLocation(context, new FastLocationProvider.LocationResult() {
                                @Override
                                public void gotLocation(Location location) {
                                    if(location == null) {
                                        FlyveLog.e(this.getClass().getName() + ", sendGPS", "without location yet...");
                                        //{"input":{"_agents_id":":id","_datetime":":string","_gps":"off"}}

                                        try {
                                            JSONObject jsonPayload = new JSONObject();

                                            jsonPayload.put("_datetime", Helpers.getUnixTime(context));
                                            jsonPayload.put("_agents_id", new MqttData(context).getAgentId());
                                            jsonPayload.put("computers_id", new MqttData(context).getComputersId());
                                            jsonPayload.put("_gps", "off");

                                            JSONObject jsonInput = new JSONObject();
                                            jsonInput.put("input", jsonPayload);

                                            String payload = jsonInput.toString();
                                            pluginHttpResponse(context, url, payload);
                                        } catch (Exception ex) {
                                            Helpers.storeLog("fcm", "Error on GPS location", ex.getMessage());
                                        }

                                    } else {

                                        try {
                                            String latitude = String.valueOf(location.getLatitude());
                                            String longitude = String.valueOf(location.getLongitude());

                                            //"{"input":{"_agents_id":":id","_datetime":":string","latitude":":float","longitude":":float"}}"
                                            JSONObject jsonGPS = new JSONObject();

                                            jsonGPS.put("latitude", latitude);
                                            jsonGPS.put("longitude", longitude);
                                            jsonGPS.put("_datetime", Helpers.getUnixTime(context));
                                            jsonGPS.put("_agents_id", new MqttData(context).getAgentId());
                                            jsonGPS.put("computers_id", new MqttData(context).getComputersId());

                                            JSONObject jsonInput = new JSONObject();
                                            jsonInput.put("input", jsonGPS);

                                            String payload = jsonInput.toString();
                                            pluginHttpResponse(context, url, payload);

                                        } catch (Exception ex) {
                                            FlyveLog.e(this.getClass().getName() + ", sendGPS", ex.getMessage());
                                            Helpers.storeLog("fcm", "Error on GPS location", ex.getMessage());
                                        }
                                    }
                                }
                            });

                            if(!isAvailable) {
                                try {
                                    JSONObject jsonPayload = new JSONObject();

                                    jsonPayload.put("_datetime", Helpers.getUnixTime(context));
                                    jsonPayload.put("_agents_id", new MqttData(context).getAgentId());
                                    jsonPayload.put("_gps", "off");
                                    jsonPayload.put("computers_id", new MqttData(context).getComputersId());
                                    
                                    JSONObject jsonInput = new JSONObject();
                                    jsonInput.put("input", jsonPayload);

                                    String payload = jsonInput.toString();
                                    pluginHttpResponse(context, url, payload);
                                } catch (Exception ex) {
                                    Helpers.storeLog("fcm", "Error on GPS location", ex.getMessage());
                                }
                            }
                        }
                        break;
                    case INVENTORY :
                    {
                        Inventory inventory = new Inventory();
                        inventory.getXMLInventory(context, new InventoryTask.OnTaskCompleted() {
                            @Override
                            public void onTaskSuccess(String s) {
                                Routes routes = new Routes(context);
                                MqttData cache = new MqttData(context);
                                String url = routes.pluginFlyvemdmAgent(cache.getAgentId());

                                try {
                                    JSONObject jsonPayload = new JSONObject();

                                    jsonPayload.put("_inventory", Helpers.base64encode(s));

                                    JSONObject jsonInput = new JSONObject();
                                    jsonInput.put("input", jsonPayload);

                                    String payload = jsonInput.toString();
                                    pluginHttpResponse(context, url, payload);
                                    Helpers.storeLog("fcm", "Inventory", "Inventory Send");
                                } catch (Exception ex) {
                                    Helpers.storeLog("fcm", "Error on json createInventory", ex.getMessage());
                                }
                            }

                            @Override
                            public void onTaskError(Throwable throwable) {
                                Helpers.storeLog("fcm", "Error on createInventory", throwable.getMessage());
                            }
                        });
                    }
                    case POLICIES:
                    {
                        // Policy/passwordEnabled
                        callPolicy(context, PasswordEnablePolicy.class, PasswordEnablePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordQuality
                        callPolicy(context, PasswordQualityPolicy.class, PasswordQualityPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinLength
                        callPolicy(context, PasswordMinLengthPolicy.class, PasswordMinLengthPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinLowerCase
                        callPolicy(context, PasswordMinLowerCasePolicy.class, PasswordMinLowerCasePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinUpperCase
                        callPolicy(context, PasswordMinUpperCasePolicy.class, PasswordMinUpperCasePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinNonLetter
                        callPolicy(context, PasswordMinNonLetterPolicy.class, PasswordMinNonLetterPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinLetters
                        callPolicy(context, PasswordMinLetterPolicy.class, PasswordMinLetterPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinNumeric
                        callPolicy(context, PasswordMinNumericPolicy.class, PasswordMinNumericPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinSymbols
                        callPolicy(context, PasswordMinSymbolsPolicy.class, PasswordMinSymbolsPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/MaximumFailedPasswordsForWipe
                        callPolicy(context, MaximumFailedPasswordForWipePolicy.class, MaximumFailedPasswordForWipePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/MaximumTimeToLock
                        callPolicy(context, MaximumTimeToLockPolicy.class, MaximumTimeToLockPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/storageEncryption
                        callPolicy(context, StorageEncryptionPolicy.class, StorageEncryptionPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableCamera
                        callPolicy(context, CameraPolicy.class, CameraPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableBluetooth
                        callPolicy(context, BluetoothPolicy.class, BluetoothPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableHostpotTethering
                        callPolicy(context, HostpotTetheringPolicy.class, HostpotTetheringPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableRoaming
                        callPolicy(context, RoamingPolicy.class, RoamingPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableWifi
                        callPolicy(context, WifiPolicy.class, WifiPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableSpeakerphone
                        callPolicy(context, SpeakerphonePolicy.class, SpeakerphonePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableCreateVpnProfiles
                        callPolicy(context, VPNPolicy.class, VPNPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamMusic
                        callPolicy(context, StreamMusicPolicy.class, StreamMusicPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamRing
                        callPolicy(context, StreamRingPolicy.class, StreamRingPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamAlarm
                        callPolicy(context, StreamAlarmPolicy.class, StreamAlarmPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamNotification
                        callPolicy(context, StreamNotificationPolicy.class, StreamNotificationPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamAccessibility
                        callPolicy(context, StreamAccessibilityPolicy.class, StreamAccessibilityPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamVoiceCall
                        callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamDTMF
                        callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableScreenCapture
                        //  ROOT REQUIRED
                        callPolicy(context, ScreenCapturePolicy.class, ScreenCapturePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableAirplaneMode
                        //  ROOT REQUIRED
                        callPolicy(context, AirplaneModePolicy.class, AirplaneModePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableGPS
                        //  ROOT REQUIRED
                        callPolicy(context, GPSPolicy.class, GPSPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableMobileLine
                        // ROOT
                        callPolicy(context, MobileLinePolicy.class, MobileLinePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableNfc
                        // ROOT
                        callPolicy(context, NFCPolicy.class, NFCPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStatusBar
                        // ROOT
                        callPolicy(context, StatusBarPolicy.class, StatusBarPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableUsbMtp
                        // ROOT
                        callPolicy(context, UsbMtpPolicy.class, UsbMtpPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableUsbPtp
                        // ROOT
                        callPolicy(context, UsbPtpPolicy.class, UsbPtpPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableUsbAdb
                        // ROOT
                        callPolicy(context, UsbAdbPolicy.class, UsbAdbPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/deployApp
                        // ROOT
                        callPolicy(context, DeployAppPolicy.class, DeployAppPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/removeApp
                        // ROOT
                        callPolicy(context, RemoveAppPolicy.class, RemoveAppPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/deployFile
                        // ROOT
                        callPolicy(context, DeployFilePolicy.class, DeployFilePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/removeFile
                        // ROOT
                        callPolicy(context, RemoveFilePolicy.class, RemoveFilePolicy.POLICY_NAME, priority, topic, message);
                    }
                    break;
                    default:
                }
            }
        });

        return true;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Long result) {
    }

    public static void sendStatusbyHttp(Context context, boolean status) {
        try {
            JSONObject jsonPayload = new JSONObject();

            jsonPayload.put("is_online", status);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("input", jsonPayload);

            String payload = jsonInput.toString();
            Routes routes = new Routes(context);
            MqttData cache = new MqttData(context);
            String url = routes.pluginFlyvemdmAgent(cache.getAgentId());
            pluginHttpResponse(context, url, payload);
        } catch (Exception ex) {
            Helpers.storeLog("fcm", "Error sending status http", ex.getMessage());
        }
    }

    public static void pluginHttpResponse(final Context context, final String url, final String data) {
        Helpers.storeLog("fcm", "http response payload", data);

        EnrollmentHelper enrollmentHelper = new EnrollmentHelper(context);
        enrollmentHelper.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                ConnectionHTTP.sendHttpResponse(context, url, data, sessionToken, new ConnectionHTTP.DataCallback() {
                    @Override
                    public void callback(String data) {
                        Helpers.storeLog("fcm", "http response from url", data);
                    }
                });
            }

            @Override
            public void onError(int type, String error) {
                Helpers.storeLog("fcm", "active session fail", data);
            }
        });

    }

    public static void callPolicy(Context context, Class<? extends BasePolicies> classPolicy, String policyName, int policyPriority, String topic, String messageBody) {

        if(topic.toLowerCase().contains(policyName.toLowerCase())) {

            FlyveLog.d("Call policies "+messageBody);
            BasePolicies policies;

            try {
                policies = classPolicy.getDeclaredConstructor(Context.class).newInstance(context);
            } catch (Exception ex) {
                return;
            }

            if(messageBody.isEmpty()) {
                policies.remove();
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(policyName)) {
                    Object value = jsonObj.get(policyName);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policies.setParameters(topic, taskId, messageBody);
                    policies.setValue(value);
                    policies.setPriority(policyPriority);
                    policies.execute();
                }
            } catch (Exception ex) {
                FlyveLog.e("PoliciesAsyncTask", ", Unenroll ",ex.getMessage());

            }
        }
    }
}
