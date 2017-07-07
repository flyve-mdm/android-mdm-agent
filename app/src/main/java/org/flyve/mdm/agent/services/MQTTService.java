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

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;


/**
 * This is the service that get and send message from MQTT
 */

public class MQTTService extends IntentService implements MqttCallback {

    private static final String TAG = "MQTT";
    private MqttAndroidClient client;
    private DataStorage cache;
    private String mTopic = "";
    private ArrayList<String> arrTopics = new ArrayList<String>();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MQTTService(String name) {
        super(name);
    }

    /**
     * The IntentService start point
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("START", "SERVICE MQTT");
        connect();
    }

    /**
     * Constructor
     */
    public MQTTService() {
        super("MQTTService");
    }

    /**
     * This function connect the agent with MQTT server
     */
    public void connect() {
        cache = new DataStorage(this.getApplicationContext());

        String mBroker = cache.getBroker();
        String mPort = "8883"; //cache.getPort();
        String mUser = cache.getMqttuser();
        String mPassword = cache.getMqttpasswd();

        mTopic = cache.getTopic();

        broadcastReceivedLog("MQTT Broker:" + mBroker);
        broadcastReceivedLog("MQTT Port:" + mPort);
        broadcastReceivedLog("MQTT User:" + mUser);
        broadcastReceivedLog("MQTT Password:" + mPassword);
        broadcastReceivedLog("MQTT Topic:" + mTopic);

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

                Log.d("Flyve", "ssl socket factory created from flyve ca");
            } catch (Exception ex) {
                Log.e("Flyve","error while building ssl mqtt cnx", ex);
            }


            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    // Everything ready waiting for message
                    FlyveLog.d("Success we are online!");
                    broadcastServiceStatus(true);

                    // principal channel
                    suscribe(mTopic + "/#");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    FlyveLog.e("onFailure:" + ex.getCause().toString());
                    broadcastReceivedMessage(ex.getCause().toString());
                    broadcastServiceStatus(false);
                }
            });
        }
        catch (MqttException ex) {
            FlyveLog.e(ex.getMessage());
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
        Log.d(TAG, "Connection fail " + cause.getMessage());
    }

    /**
     * If delivery of the message was complete
     * @param token get message token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        FlyveLog.d( "deliveryComplete: " + token.toString());
        broadcastReceivedLog("Get response from MQTT:" + token.getMessageId());
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

        broadcastReceivedLog("GET TOPIC: " + topic + " - Message: " + new String(message.getPayload()) );

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
                disableConnetivity(jsonObj);
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

        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedMessage("Error: " + ex.getCause().toString());
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
                    FlyveLog.d("suscribed");
                    broadcastReceivedLog("suscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    FlyveLog.e("ERROR: " + exception.getCause().getMessage());
                    broadcastReceivedLog("ERROR: " + exception.getMessage());
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
        InventoryTask inventoryTask = new InventoryTask(getApplicationContext(), "agent_v1");
        inventoryTask.getXML(new InventoryTask.OnTaskCompleted() {
            @Override
            public void onTaskSuccess(String data) {
                FlyveLog.xml(data);

                // send inventory MQTT
                sendInventory(data);
            }

            @Override
            public void onTaskError(Throwable error) {
                FlyveLog.e(error.getCause().toString());

                //send broadcast
                broadcastReceivedMessage("Inventory Error: " + error.getCause().toString());
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
                broadcastReceivedLog("Device lock");
            }
        } catch (Exception ex) {
            broadcastReceivedLog("ERROR: disable camera" + ex.getCause().getMessage());
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
                broadcastReceivedLog("Disabled Camera: " + disable);
             }
        } catch (Exception ex) {
            broadcastReceivedLog("ERROR: disable camera" + ex.getCause().getMessage());
            FlyveLog.e(ex.getCause().getMessage());
        }
    }

    /**
     * FLEET connectivity
     * Example {"connectivity":[{"disableWifi":"false"},{"disableBluetooth":"false"},{"disableGPS":"false"}]}
     * The stored policies on cache this was used on MQTTConnectivityReceiver
     */
    private void disableConnetivity(JSONObject json) {

        try {
            JSONArray jsonConnectivities = json.getJSONArray("connectivity");
            for (int i = 0; i <= jsonConnectivities.length(); i++) {
                JSONObject jsonConnectivity = jsonConnectivities.getJSONObject(0);

                if (jsonConnectivity.has("disableWifi")) {
                    boolean disable = jsonConnectivity.getBoolean("disableWifi");
                    cache.setConnectivityWifiDisable(disable);
                    broadcastReceivedLog("disableWifi: " + disable);
                }

                if (jsonConnectivity.has("disableBluetooth")) {
                    boolean disable = jsonConnectivity.getBoolean("disableBluetooth");
                    cache.setConnectivityBluetoothDisable(disable);
                    broadcastReceivedLog("disableBluetooth: " + disable);
                }

                if (jsonConnectivity.has("disableGPS")) {
                    boolean disable = jsonConnectivity.getBoolean("disableGPS");
                    cache.setConnectivityGPSDisable(disable);
                    broadcastReceivedLog("disableGPS: " + disable);
                }
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getCause().getMessage());
            broadcastReceivedLog("Disable Connetivity fail: " + ex.getMessage());
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

                broadcastReceivedLog(" Begin storage encryption ");
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog("Storage encryption fail: " + ex.getMessage());
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
                }

                if (jsonPolicie.has("passwordQuality")) {
                    String quality = jsonPolicie.getString("passwordMinLength");
                    mdm.setPasswordQuality(quality);
                }

                if (jsonPolicie.has("passwordEnabled")) {
                    // Nothing
                }

                if (jsonPolicie.has("passwordMinLetters")) {
                    int min = jsonPolicie.getInt("passwordMinLetters");
                    mdm.setPasswordMinumimLetters(min);
                }

                if (jsonPolicie.has("passwordMinLowerCase")) {
                    int min = jsonPolicie.getInt("passwordMinLowerCase");
                    mdm.setPasswordMinimumLowerCase(min);
                }

                if (jsonPolicie.has("passwordMinNonLetter")) {
                    int min = jsonPolicie.getInt("passwordMinNonLetter");
                    mdm.setPasswordMinimumNonLetter(min);
                }

                if (jsonPolicie.has("passwordMinNumeric")) {
                    int min = jsonPolicie.getInt("passwordMinNumeric");
                    mdm.setPasswordMinimumNumeric(min);
                }

                if (jsonPolicie.has("passwordMinSymbols")) {
                    int min = jsonPolicie.getInt("passwordMinSymbols");
                    mdm.setPasswordMinimumSymbols(min);
                }

                if (jsonPolicie.has("passwordMinUpperCase")) {
                    int min = jsonPolicie.getInt("passwordMinUpperCase");
                    mdm.setPasswordMinimumUpperCase(min);
                }

                if (jsonPolicie.has("MaximumFailedPasswordsForWipe")) {
                    int max = jsonPolicie.getInt("MaximumFailedPasswordsForWipe");
                    mdm.setMaximumFailedPasswordsForWipe(max);
                }

                if (jsonPolicie.has("MaximumTimeToLock")) {
                    int time = jsonPolicie.getInt("MaximumTimeToLock");
                    mdm.setMaximumTimeToLock(time);
                }

            }// end for
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog("Storage encryption fail: " + ex.getMessage());
        }
    }

    /**
     * Unenroll the device
     */
    private boolean unenroll() {
        // clear settings
        DataStorage cache = new DataStorage(getApplicationContext());
        cache.clearSettings();

        // Send message with unenroll
        String topic = mTopic + "/Status/Unenroll";
        String payload = "{\"unenroll\": \"unenrolled\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            broadcastReceivedMessage("Unenroll");

            return true;
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedMessage("Unenroll Error: " + ex.getCause().toString());

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
        } catch (Exception e) {
            FlyveLog.e(e.getMessage());
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
            broadcastReceivedLog("Send to MQTT " + topic + "(ID:"+ token.getMessageId() +")" + " :" + message);
            broadcastReceivedMessage("PING!");
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            broadcastReceivedMessage("PING Error: " + ex.getCause().toString());
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
            broadcastReceivedLog("Send to MQTT " + topic + "(ID:"+ token.getMessageId() +")" + " :" + message);
            broadcastReceivedMessage("Inventory send!");
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // send broadcast
            broadcastReceivedMessage("Error Inventory: " + ex.getCause().toString());
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
            client.publish(topic, message);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
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

            broadcastReceivedLog("Send to MQTT " + topic + "(ID:"+ token.getMessageId() +")" + " :" + message);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
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
                    broadcastReceivedMessage("Geolocation error:" + ex.getCause().toString());
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
                    broadcastReceivedLog("Send to MQTT " + topic + "(ID:"+ token.getMessageId() +")" + " :" + message);
                    broadcastReceivedMessage("Geolocation send!");
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());

                    // send broadcast
                    broadcastReceivedMessage("Geolocation error:" + ex.getCause().toString());
                }
            }
        });

        // is network fail
        if(!isNetworkEnabled) {
            broadcastReceivedMessage("Geolocation error: network fail");
        }
    }

}