package org.flyve.mdm.agent.utils;

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
 * @date      27/7/17
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.location.Location;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.FlyveDeviceAdminUtils;
import org.flyve.mdm.agent.services.LockScreenService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MQTTHelper {

    private static final String ERROR = "ERROR";
    private static final String MQTT_SEND = "MQTT Send";
    private static final String REMOVE_APP = "removeApp";
    private static final String REMOVE_FILE = "removeFile";
    private static final String MIN_LENGTH = "passwordMinLength";
    private static final String MIN_LETTERS = "passwordMinLetters";
    private static final String MIN_LOWERCASE = "passwordMinLowerCase";
    private static final String MIN_NON_LETTER = "passwordMinNonLetter";
    private static final String MIN_NUMERIC = "passwordMinNumeric";
    private static final String MIN_SYMBOLS = "passwordMinSymbols";
    private static final String MIN_UPPERCASE = "passwordMinUpperCase";
    private static final String MAXIMUM_FAILED_FOR_WIPE = "MaximumFailedPasswordsForWipe";
    private static final String UTF_8 = "UTF-8";
    
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
                return new String[0];
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
        if(topics==null || topics.length == 0) {
            FlyveLog.e("NULL TOPIC");
            return;
        }

        int[] qos = new int[arrTopics.size()];

        try {
            for (int k = 0; k < qos.length; k++) {
                qos[k] = 0;
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }

        String str = Arrays.toString(topics);
        FlyveLog.i("Topics: " + str + " Qos: " + qos.length);

        try {
            IMqttToken subToken = client.subscribe(topics, qos);
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
                    String errorMessage = " unknown";
                    if(exception != null) {
                        errorMessage = exception.getMessage();
                    }
                    FlyveLog.e("ERROR on subscribe: " + errorMessage);
                    broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on subscribe", errorMessage));
                }
            });
        } catch (Exception ex) {
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

                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Inventory", "Inventory Send"));
            }

            @Override
            public void onTaskError(Throwable error) {
                FlyveLog.e(error.getMessage());
                //send broadcast
                broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on createInventory", error.getMessage()));
            }
        });
    }

    /**
     * Lock Device
     * Example {"lock":"now|unlock"}
     */
    public void lockDevice(ContextWrapper context, JSONObject json) {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);

            String lock = json.getString("lock");
            if(lock.equalsIgnoreCase("now")) {
                // Start lock screen service
                context.getBaseContext().startService(new Intent(context, LockScreenService.class));

                mdm.lockDevice();
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Lock", "Device Lock"));
            } else {
                Helpers.sendBroadcast("unlock", "org.flyve.mdm.agent.unlock", context);
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on lockDevice", ex.getMessage()));
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
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Camera", "Camera is disable: " + disable));
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on disableCamera", ex.getMessage()));
            FlyveLog.e(ex.getCause().getMessage());
        }
    }

    public void disableUI(JSONObject json) {
        try {
            JSONArray jsonConnectivities = json.getJSONArray("connectivity");
            for (int i = 0; i <= jsonConnectivities.length(); i++) {
                JSONObject jsonConnectivity = jsonConnectivities.getJSONObject(i);

                if (jsonConnectivity.has("disableScreenCapture")) {
                    boolean disable = jsonConnectivity.getBoolean("disableScreenCapture");
                    new FlyveDeviceAdminUtils(context).disableCaptureScreen(disable);
                }

                if (jsonConnectivity.has("disableStatusBar")) {
                    boolean disable = jsonConnectivity.getBoolean("disableStatusBar");
                    new FlyveDeviceAdminUtils(context).disableCaptureScreen(disable);
                }

            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
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
            for (int i = 0; i < jsonConnectivities.length(); i++) {
                JSONObject jsonConnectivity = jsonConnectivities.getJSONObject(i);

                if (jsonConnectivity.has("disableWifi")) {
                    boolean disable = jsonConnectivity.getBoolean("disableWifi");
                    cache.setConnectivityWifiDisable(disable);
                    ConnectivityHelper.disableWifi(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Wifi", "Wifi is disable: " + disable));
                }

                if (jsonConnectivity.has("disableBluetooth")) {
                    boolean disable = jsonConnectivity.getBoolean("disableBluetooth");
                    cache.setConnectivityBluetoothDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Bluetooth", "Bluetooth is disable: " + disable));
                }

                if (jsonConnectivity.has("disableGPS")) {
                    boolean disable = jsonConnectivity.getBoolean("disableGPS");
                    cache.setConnectivityGPSDisable(disable);
                    ConnectivityHelper.disableGps(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "GPS", "GPS is disable: " + disable));
                }

                if (jsonConnectivity.has("disableRoaming")) {
                    boolean disable = jsonConnectivity.getBoolean("disableRoaming");
                    cache.setConnectivityRoamingDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "ROAMING", "ROAMING is disable: " + disable));
                }

                if (jsonConnectivity.has("disableAirplaneMode")) {
                    boolean disable = jsonConnectivity.getBoolean("disableAirplaneMode");
                    cache.setConnectivityAirplaneModeDisable(disable);
                    ConnectivityHelper.disableAirplaneMode(cache.getConnectivityAirplaneModeDisable());
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "AIRPLANEMODE", "AIRPLANEMODE is disable: " + disable));
                }

                if (jsonConnectivity.has("disableMobileLine")) {
                    boolean disable = jsonConnectivity.getBoolean("disableMobileLine");
                    cache.setConnectivityMobileLineDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "MOBILE_LINE", "MOBILE_LINE is disable: " + disable));
                }

                if (jsonConnectivity.has("disableNFC")) {
                    boolean disable = jsonConnectivity.getBoolean("disableNFC");
                    cache.setConnectivityNFCDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "NFC", "NFC is disable: " + disable));
                }

                if (jsonConnectivity.has("disableHostpotTethering")) {
                    boolean disable = jsonConnectivity.getBoolean("disableHostpotTethering");
                    cache.setConnectivityHostpotTetheringDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "HostpotTethering", "HostpotTethering is disable: " + disable));
                }

                if (jsonConnectivity.has("disableSmsMms")) {
                    boolean disable = jsonConnectivity.getBoolean("disableSmsMms");
                    cache.setConnectivitySmsMmsDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "SmsMms", "SmsMms is disable: " + disable));
                }

                if (jsonConnectivity.has("disableUsbFileTransferProtocols")) {
                    boolean disable = jsonConnectivity.getBoolean("disableUsbFileTransferProtocols");
                    cache.setConnectivityUsbFileTransferProtocolsDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "UsbFileTransferProtocols", "UsbFileTransferProtocols is disable: " + disable));
                }

                if (jsonConnectivity.has("disableStatusBar")) {
                    boolean disable = jsonConnectivity.getBoolean("disableStatusBar");
                    new FlyveDeviceAdminUtils(context).disableStatusBar(disable);
                }

                if (jsonConnectivity.has("disableSreenCapture")) {
                    boolean disable = jsonConnectivity.getBoolean("disableSreenCapture");
                    new FlyveDeviceAdminUtils(context).disableCaptureScreen(disable);
                }

                if (jsonConnectivity.has("resetPassword")) {
                    String newpassword = jsonConnectivity.getString("resetPassword");
                    new FlyveDeviceAdminUtils(context).resetPassword(newpassword);
                }

            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on disableConnectivity", ex.getMessage()));
        }
    }

    /**
     * Application
     * {"application":[{"deployApp":"org.flyve.inventory.agent","id":"1","versionCode":"1"}]}
     */
    public void applicationOnDevices(final JSONObject json) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String data) {
                try {
                    JSONArray appsInstall = json.getJSONArray("application");
                    appWork(appsInstall, data);
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on getActiveSessionToken", ex.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                FlyveLog.e(error);
                broadcastReceivedLog(Helpers.broadCastMessage(ERROR, ERROR, error));
                broadcastReceivedLog("Application fail: " + error);
            }
        });


    }

    /**
     * Check if the App is to be installed or uninstalled
     * @param appsInstall if the object has remove or deploy app
     * @param sessionToken the session token
     */
    public void appWork(JSONArray appsInstall, String sessionToken) throws Exception {
        AppInfo appInfo = new AppInfo(this.context);

        for(int i=0; i<appsInstall.length(); i++) {

            FilesHelper filesHelper = new FilesHelper(this.context);

            if(appsInstall.getJSONObject(i).has(REMOVE_APP)){
                FlyveLog.d("uninstall apps");

                JSONObject jsonApp = appsInstall.getJSONObject(i);
                if(appInfo.isInstall(jsonApp.getString(REMOVE_APP))) {
                    FilesHelper.removeApk(this.context, jsonApp.getString(REMOVE_APP));
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Remove app", "Package: " + jsonApp.getString(REMOVE_APP)));
                }
            }

            if(appsInstall.getJSONObject(i).has("deployApp")){
                FlyveLog.d("install apps");

                JSONObject jsonApp = appsInstall.getJSONObject(i);

                String idlist;
                String packageNamelist;
                String versionCode;

                idlist = jsonApp.getString("id");
                FlyveLog.d("Id: " + idlist);

                packageNamelist = jsonApp.getString("deployApp");
                versionCode = jsonApp.getString("versionCode");

                if(!appInfo.isInstall(packageNamelist,versionCode)){
                    filesHelper.execute("app",packageNamelist, idlist, sessionToken);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Download app", "Package: " + packageNamelist));
                }
            }
        }
    }

    /**
     * Files
     * {"file":[{"deployFile":"%SDCARD%/","id":"1","version":"1","taskId":"1"}]}
     */
    public void filesOnDevices(final JSONObject json) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String data) {
                try {
                    JSONArray jsonFiles = json.getJSONArray("file");
                    filesWork(jsonFiles, data);
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on applicationOnDevices", ex.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                FlyveLog.e(error);
                broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on applicationOnDevices", error));
            }
        });
    }

    /**
     * Check if the file is to be removed or downloaded
     * @param jsonFiles if the object has remove or deploy file
     * @param sessionToken the session token
     */
    public void filesWork(JSONArray jsonFiles, String sessionToken) throws Exception {

        for(int i=0; i<=jsonFiles.length();i++) {
            FilesHelper filesHelper = new FilesHelper(this.context);

            JSONObject jsonFile = jsonFiles.getJSONObject(i);

            if(jsonFile.has(REMOVE_FILE)){
                filesHelper.removeFile(jsonFile.getString(REMOVE_FILE));
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Remove file", jsonFile.getString(REMOVE_FILE)));
            }

            if(jsonFile.has("deployFile")) {
                String fileId = jsonFile.getString("id");
                String filePath = jsonFile.getString("deployFile");

                if("true".equals(filesHelper.execute("file", filePath, fileId, sessionToken))) {
                    FlyveLog.v("File was stored on: " + filePath);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Download file", filePath));
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
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Encryption", "Encryption: " + enable));
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on storageEncryption", ex.getMessage()));
        }
    }

    /**
     * FLEET policies
     * Example {"policies":[{MIN_LENGTH:"6"},
     * {"passwordQuality":"PASSWORD_QUALITY_UNSPECIFIED"},
     * {"passwordEnabled":"PASSWORD_PIN"},
     * {MIN_LETTERS:"0"},
     * {MIN_LOWERCASE:"0"},
     * {MIN_NON_LETTER:"0"},
     * {MIN_NUMERIC:"0"},
     * {MIN_SYMBOLS:"0"},
     * {MIN_UPPERCASE:"0"},
     * {MAXIMUM_FAILED_FOR_WIPE:"0"},
     * {"MaximumTimeToLock":"60000"}]}
     */
    public void policiesDevice(JSONObject json) {

        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);

            JSONArray jsonPolicies = json.getJSONArray("policies");

            for (int i = 0; i <= jsonPolicies.length(); i++) {
                JSONObject jsonPolicie = jsonPolicies.getJSONObject(0);

                if (jsonPolicie.has(MIN_LENGTH)) {
                    int length = jsonPolicie.getInt(MIN_LENGTH);
                    mdm.setPasswordLength(length);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LENGTH, String.valueOf(length)));
                }

                if (jsonPolicie.has("passwordQuality")) {
                    String quality = jsonPolicie.getString("passwordQuality");
                    mdm.setPasswordQuality(quality);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LENGTH, quality));
                }

                if (jsonPolicie.has("passwordEnabled")) {
                    // Nothing
                }

                if (jsonPolicie.has(MIN_LETTERS)) {
                    int min = jsonPolicie.getInt(MIN_LETTERS);
                    mdm.setPasswordMinumimLetters(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LETTERS, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_LOWERCASE)) {
                    int min = jsonPolicie.getInt(MIN_LOWERCASE);
                    mdm.setPasswordMinimumLowerCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LOWERCASE, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_NON_LETTER)) {
                    int min = jsonPolicie.getInt(MIN_NON_LETTER);
                    mdm.setPasswordMinimumNonLetter(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_NON_LETTER, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_NUMERIC)) {
                    int min = jsonPolicie.getInt(MIN_NUMERIC);
                    mdm.setPasswordMinimumNumeric(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_NUMERIC, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_SYMBOLS)) {
                    int min = jsonPolicie.getInt(MIN_SYMBOLS);
                    mdm.setPasswordMinimumSymbols(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_SYMBOLS, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_UPPERCASE)) {
                    int min = jsonPolicie.getInt(MIN_UPPERCASE);
                    mdm.setPasswordMinimumUpperCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_UPPERCASE, String.valueOf(min)));
                }

                if (jsonPolicie.has(MAXIMUM_FAILED_FOR_WIPE)) {
                    int max = jsonPolicie.getInt(MAXIMUM_FAILED_FOR_WIPE);
                    mdm.setMaximumFailedPasswordsForWipe(max);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MAXIMUM_FAILED_FOR_WIPE, String.valueOf(max)));
                }

                if (jsonPolicie.has("MaximumTimeToLock")) {
                    int time = jsonPolicie.getInt("MaximumTimeToLock");
                    mdm.setMaximumTimeToLock(time);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MAXIMUM_FAILED_FOR_WIPE, String.valueOf(time)));
                }

            }// end for
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on policiesDevice", ex.getMessage()));
        }
    }

    /**
     * Erase all device data include SDCard
     */
    public void wipe() {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.context);
            mdm.wipe();
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Wipe", "Wipe success"));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on wipe", ex.getMessage()));
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
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Unenroll", "Unenroll success"));

            // clear cache
            cache.clearSettings();

            // send message
            Helpers.sendBroadcast(Helpers.broadCastMessage("action", "open", "splash"), Helpers.BROADCAST_MSG, this.context);

            // show offline
            Helpers.sendBroadcast(false, Helpers.BROADCAST_STATUS, this.context);

            return true;
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on unenroll", ex.getMessage()));
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
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "PING", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendKeepAlive", ex.getMessage()));
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
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);

            // send broadcast
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Inventory", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // send broadcast
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendKeepAlive", ex.getMessage()));
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
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Status Version", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendStatusVersion", ex.getMessage()));
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
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Online Status", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendStatusVersion", ex.getMessage()));
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
                    jsonGPS.put("datetime", Helpers.getUnixTime());
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    // send broadcast
                    broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error GPS get location", ex.getMessage()));
                    return;
                }

                String topic = mTopic + "/Status/Geolocation";
                String payload = jsonGPS.toString();
                byte[] encodedPayload;
                try {
                    encodedPayload = payload.getBytes(UTF_8);
                    MqttMessage message = new MqttMessage(encodedPayload);
                    IMqttDeliveryToken token = client.publish(topic, message);

                    // send broadcast
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Geolocation", "ID: " + token.getMessageId()));
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());

                    // send broadcast
                    broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendGPS", ex.getMessage()));
                }
            }
        });

        // is network fail
        if(!isNetworkEnabled) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error GPS", "Network fail"));
        }
    }

    /**
     * Broadcast the received log
     * @param message
     */
    private void broadcastReceivedLog(String message){
        // write log file
        FlyveLog.f(message, FlyveLog.FILE_NAME_LOG);
    }

}
