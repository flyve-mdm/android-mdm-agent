package org.flyve.mdm.agent.services;

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

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.MqttData;
import org.flyve.mdm.agent.data.PoliciesData;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.AppInfo;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
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
    private PoliciesData cache;
    private MqttData mqttCache;
    private String mTopic;

    public MQTTHelper(Context context, MqttAndroidClient client) {
        this.client = client;
        this.context = context;
        cache = new PoliciesData(context);
        mqttCache = new MqttData(context);
        mTopic = mqttCache.getTopic();
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
            mqttCache.setManifestVersion(version);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Subscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":"/2/fleet/22"}]}
     */
    public void subscribe(final String channel) {
        String[] topics = addTopic(channel);

        // if topic null
        if(topics==null || topics.length == 0) {
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
     * MDM
     * Example  "MDM": [ { "useTLS": "true|false", "taskId": "25" }]
     */
    public void mdm(Context context, JSONObject json) {
        try {

            MqttData cache = new MqttData(context);

            JSONArray jsonMDM = json.getJSONArray("MDM");
            for(int i=0; i < jsonMDM.length(); i++) {
                JSONObject j = jsonMDM.getJSONObject(i);

                if(j.has("useTLS")) {
                    String useTLS = j.getString("useTLS");

                    if ("true".equals(useTLS) && !cache.getTls().equals("1")) {
                        cache.setTls("1");

                        // stop service
                        ((Service) context).stopSelf();

                        // restart MQTT connection with this new parameters
                        MQTTService.start(MDMAgent.getInstance());
                    } else if("false".equals(useTLS) && !cache.getTls().equals("0")) {
                        cache.setTls("0");

                        // stop service
                        ((Service) context).stopSelf();

                        // restart MQTT connection with this new parameters
                        MQTTService.start(MDMAgent.getInstance());
                    }
                }
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Lock Device
     * Example {"lock":"now|unlock"}
     */
    public void lockDevice(ContextWrapper context, JSONObject json) {
        try {
            PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);

            String lock = json.getString("lock");
            if(lock.equalsIgnoreCase("now")) {
                mdm.lockScreen();
                mdm.lockDevice();
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Lock", "Device Lock"));
            } else if (lock.equalsIgnoreCase("unlock")) {
                Helpers.sendBroadcast("unlock", "org.flyvemdm.finishlock", context);
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
            PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);

            JSONArray jsonCameras = json.getJSONArray("disableCamera");
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
                    new PoliciesDeviceManager(context).disableCaptureScreen(disable);
                }

                if (jsonConnectivity.has("disableStatusBar")) {
                    boolean disable = jsonConnectivity.getBoolean("disableStatusBar");
                    new PoliciesDeviceManager(context).disableCaptureScreen(disable);
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
                    PoliciesConnectivity.disableWifi(disable);
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
                    PoliciesConnectivity.disableGps(disable);
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
                    PoliciesConnectivity.disableAirplaneMode(cache.getConnectivityAirplaneModeDisable());
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "AIRPLANEMODE", "AIRPLANEMODE is disable: " + disable));
                }

                if (jsonConnectivity.has("disableMobileLine")) {
                    boolean disable = jsonConnectivity.getBoolean("disableMobileLine");
                    cache.setConnectivityMobileLineDisable(disable);
                    PoliciesConnectivity.disableMobileLine(disable);
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
                    new PoliciesDeviceManager(context).disableStatusBar(disable);
                }

                if (jsonConnectivity.has("disableSreenCapture")) {
                    boolean disable = jsonConnectivity.getBoolean("disableSreenCapture");
                    new PoliciesDeviceManager(context).disableCaptureScreen(disable);
                }

                if (jsonConnectivity.has("resetPassword")) {
                    String newpassword = jsonConnectivity.getString("resetPassword");
                    new PoliciesDeviceManager(context).resetPassword(newpassword);
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
                    managePackage(appsInstall, data);
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
    public void managePackage(JSONArray appsInstall, String sessionToken) throws Exception {
        AppInfo appInfo = new AppInfo(this.context);

        for(int i=0; i<appsInstall.length(); i++) {

            PoliciesFiles policiesFiles = new PoliciesFiles(this.context);

            if(appsInstall.getJSONObject(i).has(REMOVE_APP)){
                FlyveLog.d("uninstall apps");

                JSONObject jsonApp = appsInstall.getJSONObject(i);
                if(appInfo.isInstall(jsonApp.getString(REMOVE_APP))) {
                    PoliciesFiles.removeApk(this.context, jsonApp.getString(REMOVE_APP));
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Remove app", "Package: " + jsonApp.getString(REMOVE_APP)));
                }
            }

            if(appsInstall.getJSONObject(i).has("deployApp")){
                JSONObject jsonApp = appsInstall.getJSONObject(i);

                String idList = jsonApp.getString("id");
                String packageNameList = jsonApp.getString("deployApp");
                String versionCode = jsonApp.getString("versionCode");

                FlyveLog.d("installing app id: " + idList + " packageNamelist: " + packageNameList + " versionCode: " + versionCode);

                if(!appInfo.isInstall(packageNameList,versionCode)){
                    policiesFiles.execute("package",packageNameList, idList, sessionToken);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Download app", "Package: " + packageNameList));
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
                    manageFiles(jsonFiles, data);
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
    public void manageFiles(JSONArray jsonFiles, String sessionToken) throws Exception {

        for(int i=0; i<=jsonFiles.length();i++) {
            PoliciesFiles policiesFiles = new PoliciesFiles(this.context);

            JSONObject jsonFile = jsonFiles.getJSONObject(i);

            if(jsonFile.has(REMOVE_FILE)){
                policiesFiles.removeFile(jsonFile.getString(REMOVE_FILE));
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Remove file", jsonFile.getString(REMOVE_FILE)));
            }

            if(jsonFile.has("deployFile")) {
                String fileId = jsonFile.getString("id");
                String filePath = jsonFile.getString("deployFile");

                if("true".equals(policiesFiles.execute("file", filePath, fileId, sessionToken))) {
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
                PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
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
     * Example {
     * "policies": [
     * { "passwordEnabled": "true|false", "taskId": "2"},
     * { "passwordQuality" : "PASSWORD_QUALITY_NUMERIC|PASSWORD_QUALITY_ALPHABETIC|PASSWORD_QUALITY_ALPHANUMERIC|PASSWORD_QUALITY_COMPLEX|PASSWORD_QUALITY_SOMETHING|PASSWORD_QUALITY_UNSPECIFIED", "taskId": "3"},
     * { "passwordMinLetters" : "0|1|2|..", "taskId": "4"},
     * { "passwordMinLowerCase" : "0|1|2|..", "taskId": "5"},
     * { "passwordMinUpperCase" : "0|1|2|..", "taskId": "6"},
     * { "passwordMinNonLetter" : "0|1|2|..", "taskId": "7"},
     * { "passwordMinNumeric" : "0|1|2|..", "taskId": "7"},
     * { "passwordMinLength" : "0|1|2|..", "taskId": "8"},
     * { "MaximumFailedPasswordsForWipe" : "0|1|2|..", "taskId": "9"},
     * { "MaximumTimeToLock" : "time in MS", "taskId": "10"},
     * { "passwordMinSymbols" : "0|1|2|..", "taskId": "11"}
     * ]
     * }
     */
    public void policiesDevice(JSONObject json) {

        try {
            PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);

            JSONArray jsonPolicies = json.getJSONArray("policies");

            for (int i = 0; i <= jsonPolicies.length(); i++) {
                JSONObject jsonPolicie = jsonPolicies.getJSONObject(0);

                if (jsonPolicie.has("passwordQuality")) {
                    String quality = jsonPolicie.getString("passwordQuality");
                    mdm.setPasswordQuality(quality);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LENGTH, quality));
                }

                if (jsonPolicie.has("passwordEnabled")) {
                    mdm.enablePassword();
                }

                if (jsonPolicie.has(MIN_LENGTH)) {
                    int length = 0;
                    try {
                        length = jsonPolicie.getInt(MIN_LENGTH);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }
                    mdm.setPasswordLength(length);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LENGTH, String.valueOf(length)));
                }

                if (jsonPolicie.has(MIN_LETTERS)) {
                    int min = 0;
                    try {
                        min = jsonPolicie.getInt(MIN_LETTERS);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }
                    mdm.setPasswordMinimumLetters(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LETTERS, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_NON_LETTER)) {
                    int min = 0;
                    try {
                        min = jsonPolicie.getInt(MIN_NON_LETTER);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }

                    mdm.setPasswordMinimumNonLetter(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_NON_LETTER, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_NUMERIC)) {
                    int min = 0;
                    try {
                        min = jsonPolicie.getInt(MIN_NUMERIC);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }

                    mdm.setPasswordMinimumNumeric(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_NUMERIC, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_SYMBOLS)) {
                    int min = 0;
                    try {
                        min = jsonPolicie.getInt(MIN_SYMBOLS);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }

                    mdm.setPasswordMinimumSymbols(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_SYMBOLS, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_LOWERCASE)) {
                    int min = 0;
                    try {
                        min = jsonPolicie.getInt(MIN_LOWERCASE);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }
                    mdm.setPasswordMinimumLowerCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_LOWERCASE, String.valueOf(min)));
                }

                if (jsonPolicie.has(MIN_UPPERCASE)) {
                    int min = 0;
                    try {
                        min = jsonPolicie.getInt(MIN_UPPERCASE);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }

                    mdm.setPasswordMinimumUpperCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MIN_UPPERCASE, String.valueOf(min)));
                }

                if (jsonPolicie.has(MAXIMUM_FAILED_FOR_WIPE)) {
                    int max = 0;
                    try {
                        max = jsonPolicie.getInt(MAXIMUM_FAILED_FOR_WIPE);
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }

                    mdm.setMaximumFailedPasswordsForWipe(max);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, MAXIMUM_FAILED_FOR_WIPE, String.valueOf(max)));
                }

                if (jsonPolicie.has("MaximumTimeToLock")) {
                    int time = 0;
                    try {
                        time = jsonPolicie.getInt("MaximumTimeToLock");
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }

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
            PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
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

        new FastLocationProvider().getLocation(context, new FastLocationProvider.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if(location == null) {
                    FlyveLog.d("without location yet...");
                } else {
                    FlyveLog.d("lat: " + location.getLatitude() + " lon: " + location.getLongitude());

                    try {
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());

                        JSONObject jsonGPS = new JSONObject();

                        jsonGPS.put("latitude", latitude);
                        jsonGPS.put("longitude", longitude);
                        jsonGPS.put("datetime", Helpers.getUnixTime());

                        String topic = mTopic + "/Status/Geolocation";
                        String payload = jsonGPS.toString();
                        byte[] encodedPayload;

                        encodedPayload = payload.getBytes(UTF_8);
                        MqttMessage message = new MqttMessage(encodedPayload);
                        IMqttDeliveryToken token = client.publish(topic, message);

                        // send broadcast
                        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Geolocation", "ID: " + token.getMessageId()));
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                        broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on GPS location", ex.getMessage()));
                    }
                }
            }
        });
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
