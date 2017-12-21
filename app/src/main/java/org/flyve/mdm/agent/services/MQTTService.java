/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
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
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.MQTTHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;

/**
 * This is the service get and send message from MQTT
 */
public class MQTTService extends Service implements MqttCallback {

    public static final String ACTION_START = "org.flyve.mdm.agent.ACTION_START";
    public static final String ACTION_INVENTORY = "org.flyve.mdm.agent.ACTION_INVENTORY";

    private static final String MQTT_LOGIN = "MQTT Login";
    private static final String ERROR = "ERROR";
    private static final String QUERY = "query";
    private static final String TAG = "MQTT - %s";

    private MqttAndroidClient client;
    private Boolean connected = false;
    private MQTTHelper mqttHelper;

    public static Intent start(Context context) {
        MQTTService mMQTTService = new MQTTService();
        Intent mServiceIntent = new Intent(context.getApplicationContext(), mMQTTService.getClass());

        // Start the service
        context.startService(mServiceIntent);

        return mServiceIntent;
    }

    /**
     * Constructor
     */
    public MQTTService() {
        FlyveLog.d("MQTT Service Constructor");
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling the method startService(Intent)
     * https://developer.android.com/reference/android/app/Service.html#START_STICKY Documentation of the Constant
     *
     * @param intent supplied to start the service
     * @param flags the additional data about this start request
     * @param startId a unique integer representing this specific request to start
     * @return constant START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = "";
        if (intent != null && intent.getAction() != null) {
            action = intent.getAction();
        }

        FlyveLog.i(TAG, "Start MQTT Service: with parameter: " + action);

        if(!connected) {
            connect();
        }

        if(action.equalsIgnoreCase(ACTION_INVENTORY) && connected) {
            mqttHelper.createInventory();
        }

        return START_STICKY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed
     * It calls the method from the parent
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Helpers.deleteMQTTCache(getApplicationContext());
        getApplicationContext().startService(new Intent(getApplicationContext(), MQTTService.class));
    }

    /**
     * Return the communication channel to the service
     * @param intent that was used to bind to this service
     * @return IBinder null if clients cannot bind to the service 
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This function connect the agent with MQTT server
     */
    public void connect() {
        Context mContext = this.getApplicationContext();

        DataStorage cache = new DataStorage(mContext);

        final String mBroker = cache.getBroker();
        final String mPort = cache.getPort();
        final String mUser = cache.getMqttuser();
        final String mPassword = cache.getMqttpasswd();
        final String mTopic = cache.getTopic();
        final String mTLS = cache.getTls();

        if(mPassword==null) {
            FlyveLog.d(TAG, "Password can't be null");
            return;
        }

        storeLog(Helpers.broadCastMessage(MQTT_LOGIN, "Broker", mBroker));
        storeLog(Helpers.broadCastMessage(MQTT_LOGIN, "Port", mPort));
        storeLog(Helpers.broadCastMessage(MQTT_LOGIN, "User", mUser));
        storeLog(Helpers.broadCastMessage(MQTT_LOGIN, "Topic", mTopic));

        FlyveLog.i("is TLS (0=false;1=true): %s", mTLS);

        String protocol = "tcp";
        // TLS is active change protocol
        if(mTLS.equals("1")) {
            protocol = "ssl";
        }

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(mContext, protocol + "://" + mBroker + ":" + mPort, clientId);

        client.setCallback( this );

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setPassword(mPassword.toCharArray());
            options.setUserName(mUser);
            options.setCleanSession(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setConnectionTimeout(0);
            options.setAutomaticReconnect(true);

            // Create a testament to send when MQTT connection is down
            String will = "{ online: false }";
            options.setWill("/Status/Online", will.getBytes(), 0, false);

            // If TLS is active needs ssl connection option
            if(mTLS.equals("1")) {
                // SSL
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);
                options.setSocketFactory(sslContext.getSocketFactory());
            }

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    // Everything ready waiting for message
                    FlyveLog.d(TAG, "Success we are online!");
                    broadcastServiceStatus(true);

                    mqttHelper = new MQTTHelper(getApplicationContext(), client);

                    // send status online true to MQTT
                    mqttHelper.sendOnlineStatus(true);

                    // main channel
                    String channel = mTopic + "/#";
                    FlyveLog.d(TAG, "MQTT Channel: " + channel);
                    mqttHelper.suscribe("#");

                    // subscribe to manifest
                    mqttHelper.suscribe("/FlyvemdmManifest/Status/Version");

                    // send inventory on connect
                    mqttHelper.createInventory();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                    // Something went wrong e.g. connection timeout or firewall problems

                    String errorMessage = "";
                    if(ex.getMessage().equalsIgnoreCase("MqttException")) {
                        errorMessage = ((MqttException)ex).toString();
                    } else {
                        errorMessage = ex.getMessage();
                    }

                    FlyveLog.e(TAG, "Connection fail: " + errorMessage);
                    String errorCode;

                    try {
                        errorCode = String.valueOf(((MqttException) ex).getReasonCode());
                    } catch (Exception exception) {
                        errorCode = "0";
                    }

                    storeLog(Helpers.broadCastMessage(ERROR, "Error on connect - client.connect", errorMessage));
                    broadcastMessage(Helpers.broadCastMessage(ERROR, errorCode, errorMessage));
                    broadcastServiceStatus(false);
                }
            });
        }
        catch (MqttException ex) {
            FlyveLog.e(TAG, ex.getMessage());
            broadcastMessage(Helpers.broadCastMessage(ERROR, String.valueOf(ex.getReasonCode()), ex.getMessage()));
            storeLog(Helpers.broadCastMessage(ERROR, "Error on connect", ex.getMessage()));
        } catch (Exception ex) {
            FlyveLog.e(TAG, ex.getMessage());
            broadcastMessage(Helpers.broadCastMessage(ERROR, "0", mContext.getResources().getString(R.string.MQTT_ERROR_CONNECTION)));
            storeLog(Helpers.broadCastMessage(ERROR, "Error on connect", ex.getMessage()));
        }
    }

    /**
     * If connection fail trigger this function
     * @param cause Throwable error
     */
    @Override
    public void connectionLost(Throwable cause) {
        // send to backend that agent lost connection
        broadcastServiceStatus(false);
        storeLog(Helpers.broadCastMessage(ERROR, ERROR, cause.getMessage()));
        FlyveLog.d(TAG, "Connection fail " + cause.getMessage());
    }

    /**
     * If delivery of the message was complete
     * @param token get message token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        FlyveLog.d(TAG, "deliveryComplete: " + token.toString());
        storeLog(Helpers.broadCastMessage("MQTT Delivery", "Response id", String.valueOf(token.getMessageId())));
    }

    /**
     * When a message from server arrive
     * @param topic String topic where the message from
     * @param message MqttMessage message content
     * @throws Exception error
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        FlyveLog.d(TAG, "Topic " + topic);
        FlyveLog.d(TAG, "Message " + new String(message.getPayload()));

        String messageBody = new String(message.getPayload());

        storeLog(Helpers.broadCastMessage("MQTT Message", "Body", messageBody));

        try {
            JSONObject jsonObj = new JSONObject(messageBody);

            if (jsonObj.has(QUERY)) {
                // PING request
                if ("Ping".equalsIgnoreCase(jsonObj.getString(QUERY))) {
                    mqttHelper.sendKeepAlive();
                    return;
                }
                // Inventory Request
                if("Inventory".equalsIgnoreCase(jsonObj.getString(QUERY))) {
                    mqttHelper.createInventory();
                    return;
                }

                // Geolocation request
                if("Geolocate".equalsIgnoreCase(jsonObj.getString(QUERY))) {
                    mqttHelper.sendGPS();
                    return;
                }
            }

            // Wipe Request
            if(jsonObj.has("wipe") && "NOW".equalsIgnoreCase(jsonObj.getString("wipe")) ) {
                mqttHelper.wipe();
                return;
            }

            // Version Manifest
            if(jsonObj.has("version")) {
                mqttHelper.addManifest(jsonObj);
            }

            // Unenroll Request
            if (jsonObj.has("unenroll")) {
                mqttHelper.unenroll();
                return;
            }

            // Unenroll Request
            if (jsonObj.has("unenroll")) {
                mqttHelper.unenroll();
                return;
            }

            // Subscribe a new channel in MQTT
            if(jsonObj.has("subscribe")) {
                JSONArray jsonTopics = jsonObj.getJSONArray("subscribe");
                for(int i=0; i<jsonTopics.length();i++) {
                    JSONObject jsonTopic = jsonTopics.getJSONObject(i);

                    // Add new channel
                    mqttHelper.suscribe(jsonTopic.getString("topic")+"/#");
                }
                return;
            }

            // MDM
            if(jsonObj.has("MDM")) {
                mqttHelper.mdm(MQTTService.this, jsonObj);
                return;
            }

            // Lock
            if(jsonObj.has("lock")) {
                mqttHelper.lockDevice(MQTTService.this, jsonObj);
                return;
            }

            // FLEET Camera
            if(jsonObj.has("camera")) {
                mqttHelper.disableCamera(jsonObj);
                return;
            }

            // FLEET connectivity
            if(jsonObj.has("connectivity")) {
                mqttHelper.disableConnectivity(jsonObj);
                return;
            }

            // UI
            if(jsonObj.has("ui")) {
                mqttHelper.disableUI(jsonObj);
                return;
            }

            // FLEET encryption
            if(jsonObj.has("encryption")) {
                mqttHelper.storageEncryption(jsonObj);
                return;
            }

            // FLEET policies
            if(jsonObj.has("policies")) {
                mqttHelper.policiesDevice(jsonObj);
                return;
            }

            // Files
            if(jsonObj.has("file")) {
                mqttHelper.filesOnDevices(jsonObj);
                return;
            }

            // Applications
            if(jsonObj.has("application")) {
                mqttHelper.applicationOnDevices(jsonObj);
                return;
            }
        } catch (Exception ex) {
            FlyveLog.e(TAG, ex.getMessage());
            storeLog(Helpers.broadCastMessage(ERROR, "Error on messageArrived", ex.getMessage()));
        }
    }

    /**
     * Send broadcast for log messages from MQTT
     * @param message String to send
     */
    public void broadcastMessage(String message) {
        //send broadcast
        Helpers.sendBroadcast(message, Helpers.BROADCAST_MSG, getApplicationContext());
    }

    /**
     * store log messages from MQTT
     * @param message String to send
     */
    public void storeLog(String message) {
        // write log file
        FlyveLog.f(message, FlyveLog.FILE_NAME_LOG);
    }

    /**
     * Send broadcast for status of the service
     * @param status boolean status
     */
    private void broadcastServiceStatus(boolean status) {
        //send broadcast
        this.connected = status;

        DataStorage cache = new DataStorage(this.getApplicationContext());
        cache.setOnlineStatus(status);

        Helpers.sendBroadcast(status, Helpers.BROADCAST_STATUS, getApplicationContext());
    }
}