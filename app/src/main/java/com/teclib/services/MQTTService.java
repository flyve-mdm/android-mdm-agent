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
import com.teclib.data.DataStorage;
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
                    Log.d(TAG, "onSuccess");
                    suscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                    Intent in = new Intent();
                    in.putExtra("message", exception.getMessage());
                    in.setAction("flyve.mqtt.msg");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
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

            // KeepAlive or PING
            if (jsonObj.has("query")) {
                if ("Ping".equals(jsonObj.getString("query"))) {

                    Intent in = new Intent();
                    in.putExtra("message", "PING!");
                    in.setAction("flyve.mqtt.msg");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

                    sendKeepAlive();
                }
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * If delivery of the message was complete
     * @param token get message token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete ");
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
                    Log.d(TAG, "suscribed");

                    Intent in = new Intent();
                    in.putExtra("message", "suscribed");
                    in.setAction("flyve.mqtt.msg");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Log.d(TAG, "ERROR: " + exception.getMessage());

                    Intent in = new Intent();
                    in.putExtra("message", "ERROR: " + exception.getMessage());
                    in.setAction("flyve.mqtt.msg");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

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
            Log.d(TAG, "payload sended");
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

}