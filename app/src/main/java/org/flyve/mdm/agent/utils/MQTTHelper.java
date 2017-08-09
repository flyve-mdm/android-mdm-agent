package org.flyve.mdm.agent.utils;

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
 * @date      27/7/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;
import android.location.Location;

import com.flyvemdm.inventory.InventoryTask;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.FlyveDeviceAdminUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MQTTHelper {

    private ArrayList<String> arrTopics;
    private MqttAndroidClient client;
    private Context context;
    private DataStorage cache;
    private String mTopic;

    public MQTTHelper(Context context, MqttAndroidClient client) {
        this.client = client;
        this.context = context;
        cache = new DataStorage(context);
        mTopic = cache.getTopic();
        arrTopics = new ArrayList<>();
    }

    /**
     * Prevent duplicated topic and create String array with all the topic available
     * @param channel String new channel to add
     * @return String array
     */
    public String[] addTopic(String channel) {
        for(int i=0; i<arrTopics.size();i++) {
            if(channel.equalsIgnoreCase(arrTopics.get(i))) {
                return null;
            }
        }
        arrTopics.add(channel);
        return arrTopics.toArray(new String[arrTopics.size()]);
    }

    /**
     * Add Manifest version of backend to local storage
     * @param json JSONObject with this format {"version":"2.0.0-dev"}
     */
    public void addManifest(JSONObject json) {
        try {
            String version = json.getString("version");
            cache.setManifestVersion(version);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Subscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":"/2/fleet/22"}]}
     */
    public void suscribe(final String channel) {
        String[] topics = addTopic(channel);

        // if topic null
        if(topics==null) {
            FlyveLog.e("NULL TOPIC");
            return;
        }

        int Qos[] = new int[arrTopics.size()];
        for (int k = 0; k < Qos.length; k++) {
            Qos[k] = 0;
        }

        try {
            IMqttToken subToken = client.subscribe(topics, Qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    FlyveLog.d("Subscribed");
                    broadcastReceivedLog(Helpers.broadCastMessage("TOPIC", "Subscribed", channel));
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    FlyveLog.e("ERROR: " + exception.getCause().getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on subscribe", exception.getMessage()));
                }
            });
        } catch (MqttException ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Create Device Inventory
     * Example {"query": "inventory"}
     */
    public void createInventory() {
        InventoryTask inventoryTask = new InventoryTask(this.context, "FlyveMDM-Agent_v1.0");
        inventoryTask.getXML(new InventoryTask.OnTaskCompleted() {
            @Override
            public void onTaskSuccess(String data) {
                FlyveLog.xml(data);

                // send inventory to MQTT
                sendInventory(data);

                broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Inventory", "Inventory Send"));
            }

            @Override
            public void onTaskError(Throwable error) {
                FlyveLog.e(error.getMessage());
                //send broadcast
                broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on createInventory", error.getMessage()));
            }
        });
    }

    /**
     * Lock Device
     * Example { "lock": [ { "locknow" : "true|false"} ] }
     */
    public void lockDevice(JSONObject json) {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);

            JSONObject jsonLock = json.getJSONArray("lock").getJSONObject(0);
            boolean lock = jsonLock.getBoolean("locknow");
            if(lock) {
                mdm.lockDevice();
                broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Lock", "Device Lock"));
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on lockDevice", ex.getMessage()));
            FlyveLog.e(ex.getCause().getMessage());
        }
    }

    /**
     * FLEET Camera
     * Example {"camera":[{"disableCamera":"true"}]}
     */
    public void disableCamera(JSONObject json) {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);

            JSONArray jsonCameras = json.getJSONArray("camera");
            for(int i=0; i<= jsonCameras.length(); i++) {
                JSONObject jsonCamera = jsonCameras.getJSONObject(0);
                boolean disable = jsonCamera.getBoolean("disableCamera");
                mdm.disableCamera(disable);
                broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Camera", "Disable " + disable));
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on disableCamera", ex.getMessage()));
            FlyveLog.e(ex.getCause().getMessage());
        }
    }

    /**
     * FLEET connectivity
     * Example {"connectivity":[{"disableWifi":"false"},{"disableBluetooth":"false"},{"disableGPS":"false"}]}
     * The stored policies on cache this was used on MQTTConnectivityReceiver
     */
    public void disableConnectivity(JSONObject json) {

        try {
            JSONArray jsonConnectivities = json.getJSONArray("connectivity");
            for (int i = 0; i <= jsonConnectivities.length(); i++) {
                JSONObject jsonConnectivity = jsonConnectivities.getJSONObject(0);

                if (jsonConnectivity.has("disableWifi")) {
                    boolean disable = jsonConnectivity.getBoolean("disableWifi");
                    cache.setConnectivityWifiDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Wifi", "Disable " + disable));
                }

                if (jsonConnectivity.has("disableBluetooth")) {
                    boolean disable = jsonConnectivity.getBoolean("disableBluetooth");
                    cache.setConnectivityBluetoothDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Bluetooth", "Disable " + disable));
                }

                if (jsonConnectivity.has("disableGPS")) {
                    boolean disable = jsonConnectivity.getBoolean("disableGPS");
                    cache.setConnectivityGPSDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "GPS", "Disable " + disable));
                }
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on disableConnectivity", ex.getMessage()));
        }
    }

    /**
     * Application
     * {"application":[{"deployApp":"org.flyve.inventory.agent","id":"1","versionCode":"1"}]}
     */
    public void applicationOnDevices(final JSONObject json) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.enrollCallBack() {
            @Override
            public void onSuccess(String data) {
                try {
                    JSONArray checkInstall = json.getJSONArray("application");
                    appWork(checkInstall, data);
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on applicationOnDevices - getActiveSessionToken", ex.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                FlyveLog.e(error);
                broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on applicationOnDevices", error));
                broadcastReceivedLog("Application fail: " + error);
            }
        });


    }

    public void appWork(JSONArray checkInstall, String sessionToken) throws Exception {
        AppInfo appInfo = new AppInfo(this.context);
        FilesHelper filesHelper = new FilesHelper(this.context);

        for(int i=0; i<checkInstall.length(); i++) {

            if(checkInstall.getJSONObject(i).has("removeApp")){
                FlyveLog.d("uninstall apps");

                JSONObject jsonApp = checkInstall.getJSONObject(i);
                if(appInfo.isInstall(jsonApp.getString("removeApp"))) {
                    FilesHelper.removeApk(this.context, jsonApp.getString("removeApp"));
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Remove app", "Package: " + jsonApp.getString("removeApp")));
                }
            }

            if(checkInstall.getJSONObject(i).has("deployApp")){
                FlyveLog.d("install apps");

                JSONObject jsonApp = checkInstall.getJSONObject(i);

                String idlist;
                String packageNamelist;
                String versionCode;

                idlist = jsonApp.getString("id");
                packageNamelist = jsonApp.getString("deployApp");
                versionCode = jsonApp.getString("versionCode");

                if(!appInfo.isInstall(packageNamelist,versionCode)){
                    filesHelper.downloadApk(packageNamelist, versionCode, sessionToken);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Download app", "Package: " + packageNamelist));
                }
            }
        }
    }

    /**
     * Files
     * {"file":[{"deployFile":"%SDCARD%/","id":"2","version":"1"}]}
     */
    public void filesOnDevices(final JSONObject json) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.enrollCallBack() {
            @Override
            public void onSuccess(String data) {
                try {
                    JSONArray jsonFiles = json.getJSONArray("file");
                    filesWork(jsonFiles, data);
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on applicationOnDevices", ex.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                FlyveLog.e(error);
                broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on applicationOnDevices", error));
            }
        });

    }

    public void filesWork(JSONArray jsonFiles, String sessionToken) throws Exception {

        FilesHelper filesHelper = new FilesHelper(this.context);

        for(int i=0; i<=jsonFiles.length();i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);

            if(jsonFile.has("removeFile")){
                filesHelper.removeFile(jsonFile.getString("removeFile"));
                broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Remove file", jsonFile.getString("removeFile")));
            }

            if(jsonFile.has("deployFile")) {
                String fileId = jsonFile.getString("id");
                String filePath = jsonFile.getString("deployFile");

                if (filesHelper.downloadFile(filePath, fileId, sessionToken)) {
                    FlyveLog.v("File was stored on: " + filePath);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Download file", filePath));
                }
            }
        }
    }

    /**
     * FLEET encryption
     * Example {"encryption":[{"storageEncryption":"false"}]}
     */
    public void storageEncryption(JSONObject json) {
        try {
            JSONObject jsonEncryption = json.getJSONArray("encryption").getJSONObject(0);
            boolean enable = jsonEncryption.getBoolean("storageEncryption");
            if(jsonEncryption.has("storageEncryption")) {
                FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);
                mdm.storageEncryptionDevice(enable);
                broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Encryption", "Encryption: " + enable));
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("Error", "Error on storageEncryption", ex.getMessage()));
        }
    }

    /**
     * FLEET policies
     * Example {"policies":[{"passwordMinLength":"6"},
     * {"passwordQuality":"PASSWORD_QUALITY_UNSPECIFIED"},
     * {"passwordEnabled":"PASSWORD_PIN"},
     * {"passwordMinLetters":"0"},
     * {"passwordMinLowerCase":"0"},
     * {"passwordMinNonLetter":"0"},
     * {"passwordMinNumeric":"0"},
     * {"passwordMinSymbols":"0"},
     * {"passwordMinUpperCase":"0"},
     * {"MaximumFailedPasswordsForWipe":"0"},
     * {"MaximumTimeToLock":"60000"}]}
     */
    public void policiesDevice(JSONObject json) {

        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);

            JSONArray jsonPolicies = json.getJSONArray("policies");

            for (int i = 0; i <= jsonPolicies.length(); i++) {
                JSONObject jsonPolicie = jsonPolicies.getJSONObject(0);

                if (jsonPolicie.has("passwordMinLength")) {
                    int length = jsonPolicie.getInt("passwordMinLength");
                    mdm.setPasswordLength(length);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinLength", String.valueOf(length)));
                }

                if (jsonPolicie.has("passwordQuality")) {
                    String quality = jsonPolicie.getString("passwordQuality");
                    mdm.setPasswordQuality(quality);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinLength", quality));
                }

                if (jsonPolicie.has("passwordEnabled")) {
                    // Nothing
                }

                if (jsonPolicie.has("passwordMinLetters")) {
                    int min = jsonPolicie.getInt("passwordMinLetters");
                    mdm.setPasswordMinumimLetters(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinLetters", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinLowerCase")) {
                    int min = jsonPolicie.getInt("passwordMinLowerCase");
                    mdm.setPasswordMinimumLowerCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinLowerCase", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinNonLetter")) {
                    int min = jsonPolicie.getInt("passwordMinNonLetter");
                    mdm.setPasswordMinimumNonLetter(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinNonLetter", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinNumeric")) {
                    int min = jsonPolicie.getInt("passwordMinNumeric");
                    mdm.setPasswordMinimumNumeric(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinNumeric", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinSymbols")) {
                    int min = jsonPolicie.getInt("passwordMinSymbols");
                    mdm.setPasswordMinimumSymbols(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinSymbols", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinUpperCase")) {
                    int min = jsonPolicie.getInt("passwordMinUpperCase");
                    mdm.setPasswordMinimumUpperCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "passwordMinUpperCase", String.valueOf(min)));
                }

                if (jsonPolicie.has("MaximumFailedPasswordsForWipe")) {
                    int max = jsonPolicie.getInt("MaximumFailedPasswordsForWipe");
                    mdm.setMaximumFailedPasswordsForWipe(max);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "MaximumFailedPasswordsForWipe", String.valueOf(max)));
                }

                if (jsonPolicie.has("MaximumTimeToLock")) {
                    int time = jsonPolicie.getInt("MaximumTimeToLock");
                    mdm.setMaximumTimeToLock(time);
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "MaximumFailedPasswordsForWipe", String.valueOf(time)));
                }

            }// end for
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on policiesDevice", ex.getMessage()));
        }
    }

    /**
     * Erase all device data include SDCard
     */
    public void wipe() {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);
            mdm.wipe();
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Wipe", "Wipe success"));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on wipe", ex.getMessage()));
        }
    }

    /**
     * Unenroll the device
     */
    public boolean unenroll() {
        // Send message with unenroll
        String topic = mTopic + "/Status/Unenroll";
        String payload = "{\"unenroll\": \"unenrolled\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Unenroll", "Unenroll success"));

            // clear cache
            cache.clearSettings();

            // send message
            Helpers.sendBroadcast(Helpers.broadCastMessage("action", "open", "splash"), Helpers.BROADCAST_MSG, this.context);

            // show offline
            Helpers.sendBroadcast(false, Helpers.BROADCAST_STATUS, this.context);

            return true;
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on unenroll", ex.getMessage()));
            return false;
        }
    }

    /**
     * Send PING to the MQTT server
     * payload: !
     */
    public void sendKeepAlive() {
        String topic = mTopic + "/Status/Ping";
        String payload = "!";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "PING", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendKeepAlive", ex.getMessage()));
        }
    }

    /**
     * Send INVENTORY to the MQTT server
     * payload: XML FusionInventory
     */
    public void sendInventory(String payload) {
        String topic = mTopic + "/Status/Inventory";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);

            // send broadcast
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Send Inventory", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // send broadcast
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendKeepAlive", ex.getMessage()));
        }
    }

    /**
     * Send the Status version of the agent
     * payload: {"version": "0.99.0"}
     */
    public void sendStatusVersion() {
        String topic = mTopic + "/FlyvemdmManifest/Status/Version";
        String payload = "{\"version\":\"" + BuildConfig.VERSION_NAME + "\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Send Status Version", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendStatusVersion", ex.getMessage()));
        }
    }

    /**
     * Send the Status version of the agent
     * payload: {"online": "true"}
     */
    public void sendOnlineStatus(Boolean status) {
        String topic = mTopic + "/Status/Online";
        String payload = "{\"online\": \"" + Boolean.toString( status ) + "\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Send Online Status", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendStatusVersion", ex.getMessage()));
        }
    }

    /**
     * Send the GPS information to MQTT
     * payload: {"latitude":"10.2485486","longitude":"-67.5904498","datetime":1499364642}
     */
    public void sendGPS() {
        boolean isNetworkEnabled = FastLocationProvider.requestSingleUpdate(this.context, new FastLocationProvider.LocationCallback() {
            @Override
            public void onNewLocationAvailable(Location location) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                FlyveLog.i("sendGPS: " + "Lat = " + latitude + "Lon = " + longitude);

                JSONObject jsonGPS = new JSONObject();

                try {
                    jsonGPS.put("latitude", latitude);
                    jsonGPS.put("longitude", longitude);
                    jsonGPS.put("datetime", Helpers.GetUnixTime());
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    // send broadcast
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendGPS", ex.getMessage()));
                    return;
                }

                String topic = mTopic + "/Status/Geolocation";
                String payload = jsonGPS.toString();
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    IMqttDeliveryToken token = client.publish(topic, message);

                    // send broadcast
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT Send", "Send Geolocation", "ID: " + token.getMessageId()));
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());

                    // send broadcast
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendGPS", ex.getMessage()));
                }
            }
        });

        // is network fail
        if(!isNetworkEnabled) {
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendGPS", "Network fail"));
        }
    }

    private void broadcastReceivedLog(String message){
        // write log file
        FlyveLog.f(message, FlyveLog.FILE_NAME_LOG);
    }

}
