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

package com.teclib.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.flyvemdm.inventory.InventoryTask;
import com.teclib.data.DataStorage;
import com.teclib.flyvemdm.BuildConfig;
import com.teclib.utils.FlyveLog;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;

/**
 * This is the service that get and send message from MQTT
 */

public class MQTTService extends IntentService implements MqttCallback {

    private static final String TAG = "MQTT";
    private MqttAndroidClient client;
    private DataStorage cache;
    private String mTopic = "";

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

//      String mBroker = "mqdev.flyve.org";
        String mBroker = cache.getBroker();
        String mPort = "8883"; //cache.getPort();
        String mUser = cache.getMqttuser();
        String mPassword = cache.getMqttpasswd();

        mTopic = cache.getTopic();

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
                    suscribe();
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
     * When a message from server arrive
     * @param topic String topic where the message from
     * @param message MqttMessage message content
     * @throws Exception error
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "Topic " + topic);
        Log.d(TAG, "Message " + message.getPayload());

        String messageBody;
        messageBody = new String(message.getPayload());

        try {
            JSONObject jsonObj = new JSONObject(messageBody);

            if (jsonObj.has("query")) {
                // PING request
                if ("Ping".equals(jsonObj.getString("query"))) {

                    Intent in = new Intent();
                    in.putExtra("message", "PING!");
                    in.setAction("flyve.mqtt.msg");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

                    sendKeepAlive();
                }
                // INVENTORY Request
                if("Inventory".equals(jsonObj.getString("query"))) {
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
            }

            if(jsonObj.has("wipe")) {
                if("NOW".equals(jsonObj.getString("wipe"))) {
                    FlyveLog.v("Wipe in progress");

                    //send broadcast
                    broadcastReceivedMessage("Wipe in progress");
                }
            }

            if (jsonObj.has("unenroll")) {
                FlyveLog.v("Unenroll in progress");
                broadcastReceivedMessage("Unenroll in progress");
            }

        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
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
     * If delivery of the message was complete
     * @param token get message token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        FlyveLog.d( "deliveryComplete: " + token.toString());
    }

    /**
     * Suscribe to the topic
     */
    private void suscribe() {
        String topic = mTopic + "/#";
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    FlyveLog.d("suscribed");

                    broadcastReceivedMessage("suscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    FlyveLog.e("ERROR: " + exception.getMessage());

                    broadcastReceivedMessage("ERROR: " + exception.getMessage());
                }
            });
        } catch (MqttException ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Send PING to the MQTT server
     */
    private void sendKeepAlive() {
        String topic = mTopic + "/Status/Ping";
        String payload = "!";
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
     * Send INVENTORY to the MQTT server
     */
    private void sendInventory(String payload) {
        String topic = mTopic + "/Status/Inventory";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            client.publish(topic, message);

            // send broadcast
            broadcastReceivedMessage("Inventory send!");
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // send broadcast
            broadcastReceivedMessage("Error Inventory: " + ex.getCause().toString());
        }
    }

    /**
     * Send the Status version of the agent
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
     */
    private void sendOnlineStatus(Boolean status) {
        String topic = mTopic + "/Status/Online";
        String payload = "{\"online\": \"" + Boolean.toString( status ) + "\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

}