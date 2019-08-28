package org.flyve.mdm.agent.ui;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.MessagePolicies;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.policies.AirplaneModePolicy;
import org.flyve.mdm.agent.policies.BluetoothPolicy;
import org.flyve.mdm.agent.policies.CameraPolicy;
import org.flyve.mdm.agent.policies.GPSPolicy;
import org.flyve.mdm.agent.policies.HostpotTetheringPolicy;
import org.flyve.mdm.agent.policies.MaximumFailedPasswordForWipePolicy;
import org.flyve.mdm.agent.policies.MaximumTimeToLockPolicy;
import org.flyve.mdm.agent.policies.MobileLinePolicy;
import org.flyve.mdm.agent.policies.NFCPolicy;
import org.flyve.mdm.agent.policies.PasswordEnablePolicy;
import org.flyve.mdm.agent.policies.PasswordMinLengthPolicy;
import org.flyve.mdm.agent.policies.PasswordMinLetterPolicy;
import org.flyve.mdm.agent.policies.PasswordMinLowerCasePolicy;
import org.flyve.mdm.agent.policies.PasswordMinNonLetterPolicy;
import org.flyve.mdm.agent.policies.PasswordMinNumericPolicy;
import org.flyve.mdm.agent.policies.PasswordMinSymbolsPolicy;
import org.flyve.mdm.agent.policies.PasswordMinUpperCasePolicy;
import org.flyve.mdm.agent.policies.PasswordQualityPolicy;
import org.flyve.mdm.agent.policies.RoamingPolicy;
import org.flyve.mdm.agent.policies.SMSPolicy;
import org.flyve.mdm.agent.policies.ScreenCapturePolicy;
import org.flyve.mdm.agent.policies.SpeakerphonePolicy;
import org.flyve.mdm.agent.policies.StatusBarPolicy;
import org.flyve.mdm.agent.policies.StorageEncryptionPolicy;
import org.flyve.mdm.agent.policies.StreamAccessibilityPolicy;
import org.flyve.mdm.agent.policies.StreamAlarmPolicy;
import org.flyve.mdm.agent.policies.StreamMusicPolicy;
import org.flyve.mdm.agent.policies.StreamNotificationPolicy;
import org.flyve.mdm.agent.policies.StreamRingPolicy;
import org.flyve.mdm.agent.policies.StreamVoiceCallPolicy;
import org.flyve.mdm.agent.policies.UsbAdbPolicy;
import org.flyve.mdm.agent.policies.UsbMtpPolicy;
import org.flyve.mdm.agent.policies.UsbPtpPolicy;
import org.flyve.mdm.agent.policies.VPNPolicy;
import org.flyve.mdm.agent.policies.WifiPolicy;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.Inventory;
import org.json.JSONObject;

public class PoliciesAsyncTask extends AsyncTask<Object, Integer, Boolean> {

    private Handler mHandler = new Handler(Looper.getMainLooper());
    int priority = 1;

    protected Boolean doInBackground(Object... object) {

        final Context context = (Context) object[0];
        final Integer action = (Integer) object[1];
        final String topic = (String) object[2];
        final String message = (String) object[3];

        mHandler.post(new Runnable() {
            public void run() {

                switch (action)
                {
                    case MessagePolicies.PING :
                        {
                            String data = "{\"input\":{\"_pong\":\"!\"}}";
                            Routes routes = new Routes(context);
                            MqttData cache = new MqttData(context);
                            String url = routes.pluginFlyvemdmAgent(cache.getAgentId());
                            MessagePolicies.pluginHttpResponse(context, url, data);
                        }
                        break;

                    case MessagePolicies.GEOLOCATE:
                        {
                            FastLocationProvider fastLocationProvider = new FastLocationProvider();
                            Routes routes = new Routes(context);
                            final String url = routes.pluginFlyvemdmGeolocation();

                            Boolean isAvailable = fastLocationProvider.getLocation(context, new FastLocationProvider.LocationResult() {
                                @Override
                                public void gotLocation(Location location) {
                                    if(location == null) {
                                        FlyveLog.e(this.getClass().getName() + ", sendGPS", "without location yet...");
                                        //{"input":{"_agents_id":":id","_datetime":":string","_gps":"off"}}

                                        try {
                                            JSONObject jsonPayload = new JSONObject();

                                            jsonPayload.put("_datetime", Helpers.getUnixTime());
                                            jsonPayload.put("_agents_id", new MqttData(context).getAgentId());
                                            jsonPayload.put("computers_id", new MqttData(context).getComputersId());
                                            jsonPayload.put("_gps", "off");

                                            JSONObject jsonInput = new JSONObject();
                                            jsonInput.put("input", jsonPayload);

                                            String payload = jsonInput.toString();

                                            MessagePolicies.pluginHttpResponse(context, url, payload);
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
                                            jsonGPS.put("_datetime", Helpers.getUnixTime());
                                            jsonGPS.put("_agents_id", new MqttData(context).getAgentId());
                                            jsonGPS.put("computers_id", new MqttData(context).getComputersId());

                                            JSONObject jsonInput = new JSONObject();
                                            jsonInput.put("input", jsonGPS);

                                            String payload = jsonInput.toString();
                                            MessagePolicies.pluginHttpResponse(context, url, payload);

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

                                    jsonPayload.put("_datetime", Helpers.getUnixTime());
                                    jsonPayload.put("_agents_id", new MqttData(context).getAgentId());
                                    jsonPayload.put("_gps", "off");
                                    jsonPayload.put("computers_id", new MqttData(context).getComputersId());
                                    
                                    JSONObject jsonInput = new JSONObject();
                                    jsonInput.put("input", jsonPayload);

                                    String payload = jsonInput.toString();
                                    MessagePolicies.pluginHttpResponse(context, url, payload);
                                } catch (Exception ex) {
                                    Helpers.storeLog("fcm", "Error on GPS location", ex.getMessage());
                                }
                            }
                        }
                        break;
                    case MessagePolicies.INVENTORY :
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
                                    MessagePolicies.pluginHttpResponse(context, url, payload);
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
                    case MessagePolicies.POLICIES:
                    {
                        // Policy/passwordEnabled
                        MessagePolicies.callPolicy(context, PasswordEnablePolicy.class, PasswordEnablePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordQuality
                        MessagePolicies.callPolicy(context, PasswordQualityPolicy.class, PasswordQualityPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinLength
                        MessagePolicies.callPolicy(context, PasswordMinLengthPolicy.class, PasswordMinLengthPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinLowerCase
                        MessagePolicies.callPolicy(context, PasswordMinLowerCasePolicy.class, PasswordMinLowerCasePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinUpperCase
                        MessagePolicies.callPolicy(context, PasswordMinUpperCasePolicy.class, PasswordMinUpperCasePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinNonLetter
                        MessagePolicies.callPolicy(context, PasswordMinNonLetterPolicy.class, PasswordMinNonLetterPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinLetters
                        MessagePolicies.callPolicy(context, PasswordMinLetterPolicy.class, PasswordMinLetterPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinNumeric
                        MessagePolicies.callPolicy(context, PasswordMinNumericPolicy.class, PasswordMinNumericPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/passwordMinSymbols
                        MessagePolicies.callPolicy(context, PasswordMinSymbolsPolicy.class, PasswordMinSymbolsPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/MaximumFailedPasswordsForWipe
                        MessagePolicies.callPolicy(context, MaximumFailedPasswordForWipePolicy.class, MaximumFailedPasswordForWipePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/MaximumTimeToLock
                        MessagePolicies.callPolicy(context, MaximumTimeToLockPolicy.class, MaximumTimeToLockPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/storageEncryption
                        MessagePolicies.callPolicy(context, StorageEncryptionPolicy.class, StorageEncryptionPolicy.POLICY_NAME, priority, topic, message);

      
                        // Policy/disableCamera
                        MessagePolicies.callPolicy(context, CameraPolicy.class, CameraPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableBluetooth
                        MessagePolicies.callPolicy(context, BluetoothPolicy.class, BluetoothPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableHostpotTethering
                        MessagePolicies.callPolicy(context, HostpotTetheringPolicy.class, HostpotTetheringPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableRoaming
                        MessagePolicies.callPolicy(context, RoamingPolicy.class, RoamingPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableWifi
                        MessagePolicies.callPolicy(context, WifiPolicy.class, WifiPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableSpeakerphone
                        MessagePolicies.callPolicy(context, SpeakerphonePolicy.class, SpeakerphonePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableUsbOnTheGo
                        MessagePolicies.callPolicy(context, SMSPolicy.class, SMSPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableCreateVpnProfiles
                        MessagePolicies.callPolicy(context, VPNPolicy.class, VPNPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamMusic
                        MessagePolicies.callPolicy(context, StreamMusicPolicy.class, StreamMusicPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamRing
                        MessagePolicies.callPolicy(context, StreamRingPolicy.class, StreamRingPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamAlarm
                        MessagePolicies.callPolicy(context, StreamAlarmPolicy.class, StreamAlarmPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamNotification
                        MessagePolicies.callPolicy(context, StreamNotificationPolicy.class, StreamNotificationPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamAccessibility
                        MessagePolicies.callPolicy(context, StreamAccessibilityPolicy.class, StreamAccessibilityPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamVoiceCall
                        MessagePolicies.callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStreamDTMF
                        MessagePolicies.callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableScreenCapture
                        //  ROOT REQUIRED
                        MessagePolicies.callPolicy(context, ScreenCapturePolicy.class, ScreenCapturePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableAirplaneMode
                        //  ROOT REQUIRED
                        MessagePolicies.callPolicy(context, AirplaneModePolicy.class, AirplaneModePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableGPS
                        //  ROOT REQUIRED
                        MessagePolicies.callPolicy(context, GPSPolicy.class, GPSPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableMobileLine
                        // ROOT
                        MessagePolicies.callPolicy(context, MobileLinePolicy.class, MobileLinePolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableNfc
                        // ROOT
                        MessagePolicies.callPolicy(context, NFCPolicy.class, NFCPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableStatusBar
                        // ROOT
                        MessagePolicies.callPolicy(context, StatusBarPolicy.class, StatusBarPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableUsbMtp
                        // ROOT
                        MessagePolicies.callPolicy(context, UsbMtpPolicy.class, UsbMtpPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableUsbPtp
                        // ROOT
                        MessagePolicies.callPolicy(context, UsbPtpPolicy.class, UsbPtpPolicy.POLICY_NAME, priority, topic, message);

                        // Policy/disableUsbAdb
                        // ROOT
                        MessagePolicies.callPolicy(context, UsbAdbPolicy.class, UsbAdbPolicy.POLICY_NAME, priority, topic, message);
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

}
