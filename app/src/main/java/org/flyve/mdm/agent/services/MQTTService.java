/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * this file is part of flyve-mdm-android-agent
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
 * @date      02/06/2017
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.flyvemdm.inventory.InventoryTask;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.FlyveDeviceAdminUtils;
import org.flyve.mdm.agent.utils.AppInfo;
import org.flyve.mdm.agent.utils.EnrollmentHelper;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FilesHelper;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;


/**
 * This is the service that get and send message from MQTT
 */

public class MQTTService extends Service implements MqttCallback {

    private static final String TAG = "MQTT";
    private MqttAndroidClient client;
    private DataStorage cache;
    private String mTopic = "";
    private ArrayList<String> arrTopics = new ArrayList<String>();
    private Boolean connected = false;
    private Timer timer;

    public MQTTService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        FlyveLog.i("START", "SERVICE MQTT");
        connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().startService(new Intent(getApplicationContext(), MQTTService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This function connect the agent with MQTT server
     */
    public void connect() {
        cache = new DataStorage(this.getApplicationContext());

        String mBroker = cache.getBroker();
        String mPort = cache.getPort();
        String mUser = cache.getMqttuser();
        String mPassword = cache.getMqttpasswd();

        if(mPassword==null) {
            FlyveLog.d("Flyve", "Password can't be null");
            return;
        }

        mTopic = cache.getTopic();

        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Broker", mBroker));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Port", mPort));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "User", mUser));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Topic", mTopic));

        String clientId = MqttClient.generateClientId();
            client = new MqttAndroidClient(this.getApplicationContext(), "ssl://" + mBroker + ":" + mPort,
                clientId);

        client.setCallback( this );
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setPassword(mPassword.toCharArray());
            options.setUserName(mUser);
            options.setCleanSession(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setConnectionTimeout(0);

            try {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);

                options.setSocketFactory(sslContext.getSocketFactory());

                FlyveLog.d("Flyve", "ssl socket factory created from flyve ca");
            } catch (Exception ex) {
                FlyveLog.e("Flyve","error while building ssl mqtt cnx", ex);
            }


            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    // Everything ready waiting for message
                    FlyveLog.d("Success we are online!");
                    broadcastServiceStatus(true);

                    // clear topic channel on array
                    arrTopics.clear();

                    // principal channel
                    String channel = mTopic + "/#";
                    FlyveLog.d("MQTT Channel: " + channel);
                    suscribe(channel);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    FlyveLog.e("onFailure:" + ex.getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on connect - client.connect", ex.getMessage()));
                    broadcastServiceStatus(false);
                }
            });
        }
        catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on connect", ex.getMessage()));
        }
    }

    /**
     * If connection fail trigger this function
     * @param cause Throwable error
     */
    @Override
    public void connectionLost(Throwable cause) {
        // send to backend that agent lost connection
        sendOnlineStatus(false);
        broadcastServiceStatus(false);
        broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error", cause.getMessage()));
        FlyveLog.d(TAG, "Connection fail " + cause.getMessage());

        reconnect();
    }

    private void reconnect() {
        timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            public void run() {
                if(!connected) {
                    connect();
                    FlyveLog.d("try to reconnect");
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Reconnect", "Try to reconnect"));
                } else {
                    FlyveLog.d("Timer cancel");
                    timer.cancel();
                    timer = null;
                }
            }
        };
        timer.schedule(timerTask, 0, 6000); // retry every 600000 10 minutes
    }

    /**
     * If delivery of the message was complete
     * @param token get message token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        FlyveLog.d( "deliveryComplete: " + token.toString());
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Response id", String.valueOf(token.getMessageId())));
    }

    /**
     * When a message from server arrive
     * @param topic String topic where the message from
     * @param message MqttMessage message content
     * @throws Exception error
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        FlyveLog.i("Topic " + topic);
        FlyveLog.i("Message " + new String(message.getPayload()));

        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Topic", topic));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Message", new String(message.getPayload())));

        String messageBody = new String(message.getPayload());

        try {
            JSONObject jsonObj = new JSONObject(messageBody);

            if (jsonObj.has("query")) {
                // PING request
                if ("Ping".equalsIgnoreCase(jsonObj.getString("query"))) {
                    sendKeepAlive();
                    return;
                }
                // Inventory Request
                if("Inventory".equalsIgnoreCase(jsonObj.getString("query"))) {
                    createInventory();
                    return;
                }

                // Geolocation request
                if("Geolocate".equalsIgnoreCase(jsonObj.getString("query"))) {
                    sendGPS();
                    return;
                }
            }

            // Wipe Request
            if(jsonObj.has("wipe")) {
                if("NOW".equalsIgnoreCase(jsonObj.getString("wipe"))) {
                    wipe();
                    return;
                }
            }

            // Unenroll Request
            if (jsonObj.has("unenroll")) {
                unenroll();
                return;
            }

            // Subscribe a new channel in MQTT
            if(jsonObj.has("subscribe")) {
                JSONArray jsonTopics = jsonObj.getJSONArray("subscribe");
                for(int i=0; i<jsonTopics.length();i++) {
                    JSONObject jsonTopic = jsonTopics.getJSONObject(0);

                    // Add new channel
                    suscribe(jsonTopic.getString("topic")+"/#");
                }
                return;
            }

            // Lock
            if(jsonObj.has("lock")) {
                lockDevice(jsonObj);
                return;
            }

            // FLEET Camera
            if(jsonObj.has("camera")) {
                disableCamera(jsonObj);
                return;
            }

            // FLEET connectivity
            if(jsonObj.has("connectivity")) {
                disableConnectivity(jsonObj);
                return;
            }

            // FLEET encryption
            if(jsonObj.has("encryption")) {
                storageEncryption(jsonObj);
                return;
            }

            // FLEET policies
            if(jsonObj.has("policies")) {
                policiesDevice(jsonObj);
                return;
            }

            // Files
            if(jsonObj.has("file")) {
                filesOnDevices(jsonObj);
                return;
            }

            // Applications
            if(jsonObj.has("application")) {
                applicationOnDevices(jsonObj);
                return;
            }


        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on messageArrived", ex.getMessage()));
        }
    }

    /**
     * Send broadcast for received messages from MQTT
     * @param message String to send
     */
    public void broadcastReceivedMessage(String message) {
        //send broadcast
        Intent in = new Intent();
        in.setAction("flyve.mqtt.msg");
        in.putExtra("message", message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
    }

    /**
     * Send broadcast for log messages from MQTT
     * @param message String to send
     */
    public void broadcastReceivedLog(String message) {
        FlyveLog.i(message);
        //send broadcast
        Intent in = new Intent();
        in.setAction("flyve.mqtt.log");
        in.putExtra("message", message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
    }

    /**
     * Send broadcast for status of the service
     * @param status boolean status
     */
    private void broadcastServiceStatus(boolean status) {
        this.connected = status;

        //send broadcast
        Intent in = new Intent();
        in.setAction("flyve.mqtt.status");
        in.putExtra("message", Boolean.toString( status ) );
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
    }

    /**
     * Prevent duplicated topic and create String array with all the topic available
     * @param channel String new channel to add
     * @return String array
     */
    private String[] addTopic(String channel) {
        for(int i=0; i<arrTopics.size();i++) {
            if(channel.equalsIgnoreCase(arrTopics.get(i))) {
                return null;
            }
        }
        arrTopics.add(channel);
        return arrTopics.toArray(new String[arrTopics.size()]);
    }

    /**
     * Subscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":"/2/fleet/22"}]}
     */
    private void suscribe(String channel) {
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
                    broadcastReceivedLog(Helpers.broadCastMessage("TOPIC", "Subscribed", "Success"));
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    FlyveLog.e("ERROR: " + exception.getCause().getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on suscribe", exception.getMessage()));
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
    private void createInventory() {
        InventoryTask inventoryTask = new InventoryTask(getApplicationContext(), "FlyveMDM-Agent_v1.0");
        inventoryTask.getXML(new InventoryTask.OnTaskCompleted() {
            @Override
            public void onTaskSuccess(String data) {
                FlyveLog.xml(data);

                // send inventory to MQTT
                sendInventory(data);
            }

            @Override
            public void onTaskError(Throwable error) {
                FlyveLog.e(error.getCause().toString());
                //send broadcast
                broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on createInventory", error.getMessage()));
            }
        });
    }

    /**
     * Lock Device
     * Example { "lock": [ { "locknow" : "true|false"} ] }
     */
    private void lockDevice(JSONObject json) {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.getApplicationContext());

            JSONObject jsonLock = json.getJSONArray("lock").getJSONObject(0);
            boolean lock = jsonLock.getBoolean("locknow");
            if(lock) {
                mdm.lockDevice();
                broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Lock", "Device Lock"));
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
    private void disableCamera(JSONObject json) {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.getApplicationContext());

            JSONArray jsonCameras = json.getJSONArray("camera");
            for(int i=0; i<= jsonCameras.length(); i++) {
                JSONObject jsonCamera = jsonCameras.getJSONObject(0);
                boolean disable = jsonCamera.getBoolean("disableCamera");
                mdm.disableCamera(disable);
                broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Camera", "Disable " + disable));
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
    private void disableConnectivity(JSONObject json) {

        try {
            JSONArray jsonConnectivities = json.getJSONArray("connectivity");
            for (int i = 0; i <= jsonConnectivities.length(); i++) {
                JSONObject jsonConnectivity = jsonConnectivities.getJSONObject(0);

                if (jsonConnectivity.has("disableWifi")) {
                    boolean disable = jsonConnectivity.getBoolean("disableWifi");
                    cache.setConnectivityWifiDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Wifi", "Disable " + disable));
                }

                if (jsonConnectivity.has("disableBluetooth")) {
                    boolean disable = jsonConnectivity.getBoolean("disableBluetooth");
                    cache.setConnectivityBluetoothDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Bluetooth", "Disable " + disable));
                }

                if (jsonConnectivity.has("disableGPS")) {
                    boolean disable = jsonConnectivity.getBoolean("disableGPS");
                    cache.setConnectivityGPSDisable(disable);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "GPS", "Disable " + disable));
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
    private void applicationOnDevices(final JSONObject json) {

            EnrollmentHelper sToken = new EnrollmentHelper(getApplicationContext());
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

    private void appWork(JSONArray checkInstall, String sessionToken) throws Exception {
        AppInfo appInfo = new AppInfo(getApplicationContext());
        FilesHelper filesHelper = new FilesHelper(getApplicationContext());

        for(int i=0; i<checkInstall.length(); i++) {

            if(checkInstall.getJSONObject(i).has("removeApp")){
                FlyveLog.d("uninstall apps");

                JSONObject jsonApp = checkInstall.getJSONObject(i);
                if(appInfo.isInstall(jsonApp.getString("removeApp"))) {
                    FilesHelper.removeApk(getApplicationContext(), jsonApp.getString("removeApp"));
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Remove app", "Package: " + jsonApp.getString("removeApp")));
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
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Download app", "Package: " + packageNamelist));
                }
            }
        }
    }

    /**
     * Files
     * {"file":[{"deployFile":"%SDCARD%/","id":"2","version":"1"}]}
     */
    private void filesOnDevices(final JSONObject json) {

            EnrollmentHelper sToken = new EnrollmentHelper(getApplicationContext());
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

    private void filesWork(JSONArray jsonFiles, String sessionToken) throws Exception {

        FilesHelper filesHelper = new FilesHelper(getApplicationContext());

        for(int i=0; i<=jsonFiles.length();i++) {
            JSONObject jsonFile = jsonFiles.getJSONObject(i);

            if(jsonFile.has("removeFile")){
                filesHelper.removeFile(jsonFile.getString("removeFile"));
                broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Remove file", jsonFile.getString("removeFile")));
            }

            if(jsonFile.has("deployFile")) {
                String fileId = jsonFile.getString("id");
                String filePath = jsonFile.getString("deployFile");

                if (filesHelper.downloadFile(filePath, fileId, sessionToken)) {
                    FlyveLog.v("File was stored on: " + filePath);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Download file", filePath));
                }
            }
        }
    }

    /**
     * FLEET encryption
     * Example {"encryption":[{"storageEncryption":"false"}]}
     */
    private void storageEncryption(JSONObject json) {
        try {
            JSONObject jsonEncryption = json.getJSONArray("encryption").getJSONObject(0);
            boolean enable = jsonEncryption.getBoolean("storageEncryption");
            if(jsonEncryption.has("storageEncryption")) {
                FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.getApplicationContext());
                mdm.storageEncryptionDevice(enable);
                broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Encryption", "Encryption: " + enable));
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
    private void policiesDevice(JSONObject json) {

        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.getApplicationContext());

            JSONArray jsonPolicies = json.getJSONArray("policies");

            for (int i = 0; i <= jsonPolicies.length(); i++) {
                JSONObject jsonPolicie = jsonPolicies.getJSONObject(0);

                if (jsonPolicie.has("passwordMinLength")) {
                    int length = jsonPolicie.getInt("passwordMinLength");
                    mdm.setPasswordLength(length);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinLength", String.valueOf(length)));
                }

                if (jsonPolicie.has("passwordQuality")) {
                    String quality = jsonPolicie.getString("passwordQuality");
                    mdm.setPasswordQuality(quality);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinLength", quality));
                }

                if (jsonPolicie.has("passwordEnabled")) {
                    // Nothing
                }

                if (jsonPolicie.has("passwordMinLetters")) {
                    int min = jsonPolicie.getInt("passwordMinLetters");
                    mdm.setPasswordMinumimLetters(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinLetters", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinLowerCase")) {
                    int min = jsonPolicie.getInt("passwordMinLowerCase");
                    mdm.setPasswordMinimumLowerCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinLowerCase", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinNonLetter")) {
                    int min = jsonPolicie.getInt("passwordMinNonLetter");
                    mdm.setPasswordMinimumNonLetter(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinNonLetter", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinNumeric")) {
                    int min = jsonPolicie.getInt("passwordMinNumeric");
                    mdm.setPasswordMinimumNumeric(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinNumeric", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinSymbols")) {
                    int min = jsonPolicie.getInt("passwordMinSymbols");
                    mdm.setPasswordMinimumSymbols(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinSymbols", String.valueOf(min)));
                }

                if (jsonPolicie.has("passwordMinUpperCase")) {
                    int min = jsonPolicie.getInt("passwordMinUpperCase");
                    mdm.setPasswordMinimumUpperCase(min);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "passwordMinUpperCase", String.valueOf(min)));
                }

                if (jsonPolicie.has("MaximumFailedPasswordsForWipe")) {
                    int max = jsonPolicie.getInt("MaximumFailedPasswordsForWipe");
                    mdm.setMaximumFailedPasswordsForWipe(max);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "MaximumFailedPasswordsForWipe", String.valueOf(max)));
                }

                if (jsonPolicie.has("MaximumTimeToLock")) {
                    int time = jsonPolicie.getInt("MaximumTimeToLock");
                    mdm.setMaximumTimeToLock(time);
                    broadcastReceivedLog(Helpers.broadCastMessage("MDM", "MaximumFailedPasswordsForWipe", String.valueOf(time)));
                }

            }// end for
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on policiesDevice", ex.getMessage()));
        }
    }

    /**
     * Unenroll the device
     */
    private boolean unenroll() {
        // Send message with unenroll
        String topic = mTopic + "/Status/Unenroll";
        String payload = "{\"unenroll\": \"unenrolled\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Unenroll", "Unenroll success"));

            // clear cache
            cache.clearSettings();

            // send message
            broadcastReceivedMessage(Helpers.broadCastMessage("action", "open", "splash"));

            // show offline
            broadcastServiceStatus(false);

            return true;
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on unenroll", ex.getMessage()));
            return false;
        }
    }

    /**
     * Erase all device data include SDCard
     */
    private void wipe() {
        try {
            FlyveDeviceAdminUtils mdm = new FlyveDeviceAdminUtils(this.getApplicationContext());
            mdm.wipe();
            broadcastReceivedLog(Helpers.broadCastMessage("MDM", "Wipe", "Wipe success"));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on wipe", ex.getMessage()));
        }
    }

    /**
     * Send PING to the MQTT server
     * payload: !
     */
    private void sendKeepAlive() {
        String topic = mTopic + "/Status/Ping";
        String payload = "!";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "PING", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendKeepAlive", ex.getMessage()));
        }
    }

    /**
     * Send INVENTORY to the MQTT server
     * payload: XML FusionInventory
     */
    private void sendInventory(String payload) {
        String topic = mTopic + "/Status/Inventory";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);

            // send broadcast
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Send Inventory", "ID: " + token.getMessageId()));
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
    private void sendStatusVersion() {
        String topic = mTopic + "/FlyvemdmManifest/Status/Version";
        String payload = "{\"version\":\"" + BuildConfig.VERSION_NAME + "\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Send Status Version", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendStatusVersion", ex.getMessage()));
        }
    }

    /**
     * Send the Status version of the agent
     * payload: {"online": "true"}
     */
    private void sendOnlineStatus(Boolean status) {
        String topic = mTopic + "/Status/Online";
        String payload = "{\"online\": \"" + Boolean.toString( status ) + "\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Send Online Status", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on sendStatusVersion", ex.getMessage()));
        }
    }

    /**
     * Send the GPS information to MQTT
     * payload: {"latitude":"10.2485486","longitude":"-67.5904498","datetime":1499364642}
     */
    public void sendGPS() {
        boolean isNetworkEnabled = FastLocationProvider.requestSingleUpdate(getApplicationContext(), new FastLocationProvider.LocationCallback() {
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
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Send Geolocation", "ID: " + token.getMessageId()));
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

}