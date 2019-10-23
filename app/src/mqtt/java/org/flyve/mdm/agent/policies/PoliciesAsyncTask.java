package org.flyve.mdm.agent.policies;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.core.mqtt.MqttModel;
import org.flyve.mdm.agent.data.database.MqttData;
//import org.flyve.mdm.agent.policies.DeployAppPolicy;
//import org.flyve.mdm.agent.policies.DeployFilePolicy;
//import org.flyve.mdm.agent.policies.RemoveAppPolicy;
//import org.flyve.mdm.agent.policies.RemoveFilePolicy;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.Inventory;
import org.json.JSONObject;

public class PoliciesAsyncTask extends AsyncTask<Object, Integer, Boolean> {

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int priority = 1;

    protected Boolean doInBackground(Object... object) {

        final Context context = (Context) object[0];
        final Integer action = (Integer) object[1];
        final String topic = (String) object[2];
        final String message = (String) object[3];
        final MqttAndroidClient client = (MqttAndroidClient) object[4];


        mHandler.post(new Runnable() {
            public void run() {

                switch (action)
                {
                    case MqttModel.PING :
                        {
                            String data = "{\"input\":{\"_pong\":\"!\"}}";
                            Routes routes = new Routes(context);
                            MqttData cache = new MqttData(context);
                            String url = routes.pluginFlyvemdmAgent(cache.getAgentId());
                            MqttModel.pluginHttpResponse(context, url, data);
                        }
                        break;

                    case MqttModel.GEOLOCATE:
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

                                            MqttModel.pluginHttpResponse(context, url, payload);
                                        } catch (Exception ex) {
                                            Helpers.storeLog("fcm", "Error on GPS location", ex.getMessage());
                                        }

                                    } else {
                                        FlyveLog.d("lat: " + location.getLatitude() + " lon: " + location.getLongitude());

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
                                            MqttModel.pluginHttpResponse(context, url, payload);

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
                                    MqttModel.pluginHttpResponse(context, url, payload);
                                } catch (Exception ex) {
                                    Helpers.storeLog("fcm", "Error on GPS location", ex.getMessage());
                                }
                            }
                        }
                        break;
                    case MqttModel.INVENTORY :
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
                                    MqttModel.pluginHttpResponse(context, url, payload);
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
                    case MqttModel.POLICIES:
                    {
                        // Policy/passwordEnabled
                        MqttModel.callPolicy(context, PasswordEnablePolicy.class, PasswordEnablePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordQuality
                        MqttModel.callPolicy(context, PasswordQualityPolicy.class, PasswordQualityPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordMinLength
                        MqttModel.callPolicy(context, PasswordMinLengthPolicy.class, PasswordMinLengthPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordMinLowerCase
                        MqttModel.callPolicy(context, PasswordMinLowerCasePolicy.class, PasswordMinLowerCasePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordMinUpperCase
                        MqttModel.callPolicy(context, PasswordMinUpperCasePolicy.class, PasswordMinUpperCasePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordMinNonLetter
                        MqttModel.callPolicy(context, PasswordMinNonLetterPolicy.class, PasswordMinNonLetterPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordMinLetters
                        MqttModel.callPolicy(context, PasswordMinLetterPolicy.class, PasswordMinLetterPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordMinNumeric
                        MqttModel.callPolicy(context, PasswordMinNumericPolicy.class, PasswordMinNumericPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/passwordMinSymbols
                        MqttModel.callPolicy(context, PasswordMinSymbolsPolicy.class, PasswordMinSymbolsPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/MaximumFailedPasswordsForWipe
                        MqttModel.callPolicy(context, MaximumFailedPasswordForWipePolicy.class, MaximumFailedPasswordForWipePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/MaximumTimeToLock
                        MqttModel.callPolicy(context, MaximumTimeToLockPolicy.class, MaximumTimeToLockPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/storageEncryption
                        MqttModel.callPolicy(context, StorageEncryptionPolicy.class, StorageEncryptionPolicy.POLICY_NAME, priority, topic, message, client);

      
                        // Policy/disableCamera
                        MqttModel.callPolicy(context, CameraPolicy.class, CameraPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableBluetooth
                        MqttModel.callPolicy(context, BluetoothPolicy.class, BluetoothPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableHostpotTethering
                        MqttModel.callPolicy(context, HostpotTetheringPolicy.class, HostpotTetheringPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableRoaming
                        MqttModel.callPolicy(context, RoamingPolicy.class, RoamingPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableWifi
                        MqttModel.callPolicy(context, WifiPolicy.class, WifiPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableSpeakerphone
                        MqttModel.callPolicy(context, SpeakerphonePolicy.class, SpeakerphonePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableUsbOnTheGo
                        MqttModel.callPolicy(context, SMSPolicy.class, SMSPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableCreateVpnProfiles
                        MqttModel.callPolicy(context, VPNPolicy.class, VPNPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStreamMusic
                        MqttModel.callPolicy(context, StreamMusicPolicy.class, StreamMusicPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStreamRing
                        MqttModel.callPolicy(context, StreamRingPolicy.class, StreamRingPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStreamAlarm
                        MqttModel.callPolicy(context, StreamAlarmPolicy.class, StreamAlarmPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStreamNotification
                        MqttModel.callPolicy(context, StreamNotificationPolicy.class, StreamNotificationPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStreamAccessibility
                        MqttModel.callPolicy(context, StreamAccessibilityPolicy.class, StreamAccessibilityPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStreamVoiceCall
                        MqttModel.callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStreamDTMF
                        MqttModel.callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableScreenCapture
                        //  ROOT REQUIRED
                        MqttModel.callPolicy(context, ScreenCapturePolicy.class, ScreenCapturePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableAirplaneMode
                        //  ROOT REQUIRED
                        MqttModel.callPolicy(context, AirplaneModePolicy.class, AirplaneModePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableGPS
                        //  ROOT REQUIRED
                        MqttModel.callPolicy(context, GPSPolicy.class, GPSPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableMobileLine
                        // ROOT
                        MqttModel.callPolicy(context, MobileLinePolicy.class, MobileLinePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableNfc
                        // ROOT
                        MqttModel.callPolicy(context, NFCPolicy.class, NFCPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableStatusBar
                        // ROOT
                        MqttModel.callPolicy(context, StatusBarPolicy.class, StatusBarPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableUsbMtp
                        // ROOT
                        MqttModel.callPolicy(context, UsbMtpPolicy.class, UsbMtpPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableUsbPtp
                        // ROOT
                        MqttModel.callPolicy(context, UsbPtpPolicy.class, UsbPtpPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/disableUsbAdb
                        // ROOT
                        MqttModel.callPolicy(context, UsbAdbPolicy.class, UsbAdbPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/deployApp
                        // ROOT
                        MqttModel.callPolicy(context, DeployAppPolicy.class, DeployAppPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/removeApp
                        // ROOT
                        MqttModel.callPolicy(context, RemoveAppPolicy.class, RemoveAppPolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/deployFile
                        // ROOT
                        MqttModel.callPolicy(context, DeployFilePolicy.class, DeployFilePolicy.POLICY_NAME, priority, topic, message, client);

                        // Policy/removeFile
                        // ROOT
                        MqttModel.callPolicy(context, RemoveFilePolicy.class, RemoveFilePolicy.POLICY_NAME, priority, topic, message, client);
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
}
