/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.core.mqtt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.data.database.TopicsData;
import org.flyve.mdm.agent.data.localstorage.AppData;
import org.flyve.mdm.agent.policies.PoliciesAsyncTask;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.ui.MainActivity;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MqttModel implements mqtt.Model {

    private static final String QUERY = "query";

    private mqtt.Presenter presenter;
    private MqttAndroidClient client;
    private Boolean connected = false;
    private String url;

    private Timer reconnectionTimer;
    private int reconnectionCounter = 0;
    private int reconnectionPeriod = 1000;
    private int reconnectionDelay = 5; //delay in milliseconds before task is to be executed.
    private long timeLastReconnection = 0;
    private Boolean executeConnection = true;
    private int tryEverySeconds = 30;

    private MqttController policiesController = null;

    public MqttModel(mqtt.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public MqttAndroidClient getMqttClient() {
        return client;
    }

    @Override
    public Boolean isConnected() {
        return connected;
    }

    @Override
    public void connect(final Context context, final MqttCallback callback) {
        // if the device is connected exit
        if(getMqttClient()!=null && getMqttClient().isConnected()) {
            setStatus(context, callback, true);
            return;
        }

        MqttData cache = new MqttData(context);

        final String mBroker = cache.getBroker();
        final String mPort = cache.getPort();
        final String mUser = cache.getMqttUser();
        final String mPassword = cache.getMqttPasswd();
        final String mTopic = cache.getTopic();
        final String mTLS = cache.getTls();

        final StringBuilder connectionInformation = new StringBuilder();
        connectionInformation.append("\n\nBroker: ").append(mBroker).append("\n");
        connectionInformation.append("Port: ").append(mPort).append("\n");
        connectionInformation.append("User: ").append(mUser).append("\n");
        connectionInformation.append("Topic: ").append(mTopic).append("\n");
        connectionInformation.append("TLS: ").append(mTLS).append("\n");

        Log.d("MQTT", connectionInformation.toString());
        Helpers.storeLog("MQTT", "Connection Information", connectionInformation.toString());

        String protocol = "tcp";
        // TLS is active change protocol
        if(mTLS.equals("1")) {
            protocol = "ssl";
        }

        String clientId;
        MqttConnectOptions options;

        if(client==null) {
            try {
                clientId = MqttClient.generateClientId();
                client = new MqttAndroidClient(context, protocol + "://" + mBroker + ":" + mPort, clientId);
            } catch (ExceptionInInitializerError ex) {
                showDetailError(context, CommonErrorType.MQTT_IN_INITIALIZER_ERROR, ex.getMessage());
                reconnect(context, callback);
                return;
            }

            client.setCallback(callback);
        }

        try {
            options = new MqttConnectOptions();
            options.setPassword(mPassword.toCharArray());
            options.setUserName(mUser);
            options.setCleanSession(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setConnectionTimeout(MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT);
            options.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
            options.setAutomaticReconnect(true);

            // Create a testament to send when MQTT connection is down
            String will = "{ \"online\": false }";
            options.setWill(mTopic + "/Status/Online", will.getBytes(), 0, true);

            // If TLS is active needs ssl connection option
            if (mTLS.equals("1")) {

                try{
                    //load ssl certificate from broker and put it in file
                    //need to be done in anothger
                    new MqttDownloadSSL().execute(mBroker, Integer.valueOf(mPort), context);

                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                    KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    caKeyStore.load(null, null);

                    CertificateFactory certificationFactory = CertificateFactory.getInstance("X.509");

                    //load certificate file previously loaded from broker
                    FileInputStream inputStream;
                    inputStream = context.openFileInput("broker_cert");
                    X509Certificate ca = (X509Certificate) certificationFactory.generateCertificate(inputStream);
                    String alias = ca.getSubjectX500Principal().getName();
                    // Set proper alias name
                    caKeyStore.setCertificateEntry(alias, ca);
                    trustManagerFactory.init(caKeyStore);

                    FlyveLog.v("Certificate Owner: %s", ca.getSubjectDN().toString());
                    FlyveLog.v("Certificate Issuer: %s", ca.getIssuerDN().toString());
                    FlyveLog.v("Certificate Serial Number: %s", ca.getSerialNumber().toString());
                    FlyveLog.v("Certificate Algorithm: %s", ca.getSigAlgName());
                    FlyveLog.v("Certificate Version: %s", ca.getVersion());
                    FlyveLog.v("Certificate OID: %s", ca.getSigAlgOID());
                    Enumeration<String> aliasesCA = caKeyStore.aliases();
                    for (; aliasesCA.hasMoreElements(); ) {
                        String o = aliasesCA.nextElement();
                        FlyveLog.v("Alias: %s isKeyEntry:%s isCertificateEntry:%s", o, caKeyStore.isKeyEntry(o), caKeyStore.isCertificateEntry(o));
                    }

                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
                    keyManagerFactory.init(null,null);

                    // SSL
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
                    options.setSocketFactory(sslContext.getSocketFactory());
                }catch (Exception ex) {
                    //restart connection
                    setStatus(context, callback, false);
                    showDetailError(context, CommonErrorType.MQTT_CONNECTION, ex.getMessage());
                }

            }
        } catch (Exception ex) {
            showDetailError(context, CommonErrorType.MQTT_OPTIONS, ex.getMessage());
            return;
        }

        // set all the topics on database to unconnected
        new TopicsData(context).clearTopics();

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Helpers.storeLog("MQTT", "Connection Success", "");

                    // Everything ready waiting for message
                    policiesController = new MqttController(context, client);

                    // We are connected
                    setStatus(context, callback, true);

                    // set the reconnection counter to 0
                    reconnectionCounter = 0;

                    // main topic
                    String topic = mTopic + "/#";
                    policiesController.subscribe(topic);

                    // subscribe to manifest
                    policiesController.subscribe("FlyvemdmManifest/Status/Version");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                    setStatus(context, callback, false);
                    String messageError;
                    if(ex.getCause() != null) {
                        messageError = ex.getCause().toString();
                    } else {
                        messageError = ex.getMessage();
                    }
                    showDetailError(context, CommonErrorType.MQTT_ACTION_CALLBACK, messageError);
                }
            });
        }
        catch (Exception ex) {
            setStatus(context, callback, false);
            showDetailError(context, CommonErrorType.MQTT_CONNECTION, ex.getMessage());
        }
    }

    private void reconnect(final Context context, final MqttCallback callback) {
        if(reconnectionTimer==null) {
            reconnectionTimer = new Timer();
        }

        reconnectionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                // Check the reconnection every 30 seconds
                if(timeLastReconnection>0) {
                    long currentDate = new Date().getTime();
                    long diff = (currentDate - timeLastReconnection);
                    long seconds = diff / 1000 % 60;
                    if(seconds>=tryEverySeconds) {
                        timeLastReconnection = currentDate;
                        executeConnection = true;
                    } else {
                        executeConnection = false;
                        return;
                    }
                } else {
                    timeLastReconnection = new Date().getTime();
                    executeConnection = true;
                }

                if(!isConnected()) {
                    if(executeConnection) {
                        reconnectionCounter++;
                        String message = "Reconnecting " + reconnectionCounter + " times";
                        Helpers.storeLog("MQTT", "Reconnection", message);
                        if(new AppData(context).getEnableNotificationConnection()) {
                            Helpers.sendToNotificationBar(context, 101, context.getString(R.string.app_name), message, false, MainActivity.class, "service_disconnect");
                        }

                        FlyveLog.d(message);
                        connect(context, callback);
                    }
                } else {
                    FlyveLog.d("Reconnection finish");
                    Helpers.storeLog("MQTT", "Reconnection Success", "");
                    reconnectionCounter = 0;
                    reconnectionTimer.cancel();
                    reconnectionTimer = null;
                }
            }
        }, reconnectionDelay, reconnectionPeriod);
    }

    public void messageArrived(Context context, String topic, MqttMessage message) {
        int priority = topic.contains("fleet") ? 0 : 1;

        String messageBody = new String(message.getPayload());
        MqttController mqttController = new MqttController(context, getMqttClient());

        if(topic.isEmpty()) {
            // exit if the topic if empty
            return;
        }

        // Delete policy information
        if(messageBody.contains("default")) {
            try {
                String taskId = new JSONObject(messageBody).getString("taskId");
                new PoliciesData(context).removeValue(taskId);
                FlyveLog.i("Deleting policy " + message + " - " + topic);
            } catch (Exception ex) {
                FlyveLog.e("fcm", "error deleting policy " + message + " - " + topic, ex.getMessage());
            }
            return;
        }

        //Command/Policies
        new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.POLICIES, topic, messageBody, this.client);

        // Command/Ping
        if(topic.toLowerCase().contains("ping")) {
            new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.PING, topic,messageBody, this.client);
        }

        // Command/Geolocate
        if(topic.toLowerCase().contains("geolocate")) {
            new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.GEOLOCATE, topic,messageBody, this.client);
        }

        // Command/Inventory
        if(topic.toLowerCase().contains("inventory")) {
            new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.INVENTORY, topic,messageBody, this.client);
        }

        // Command/Wipe
        if(topic.toLowerCase().contains("wipe")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.WIPE, topic,messageBody);
        }

        // Command/Unenroll
        if(topic.toLowerCase().contains("unenroll")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.UNENROLL, topic,messageBody);
        }

        // Command/Lock
        if(topic.toLowerCase().contains("lock")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.LOCK, topic,messageBody);
        }

        // Command/Subscribe
        if(topic.toLowerCase().contains("subscribe")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has("subscribe")) {
                    JSONArray jsonTopics = jsonObj.getJSONArray("subscribe");
                    for(int i=0; i<jsonTopics.length();i++) {
                        JSONObject jsonTopic = jsonTopics.getJSONObject(i);

                        String channel = jsonTopic.getString("topic")+"/#";
                        if(channel == null || channel.contains("null")) {
                            mqttController.unsubscribe();
                        }else{
                            mqttController.subscribe(channel);
                        }
                    }
                }
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_SUBSCRIBE, ex.getMessage());
            }
        }

    }

    @Override
    public void showDetailError(Context context, int type, String message) {
        FlyveLog.e(this.getClass().getName() + ", showDetailError", context.getResources().getString(R.string.error_message_with_number, String.valueOf(type), message));
    }

    @Override
    public void onDestroy(Context context) {
        Helpers.deleteMQTTCache(context);
        try {
            context.startService(new Intent(context, MQTTService.class));
        } catch (Exception ex) {
            showDetailError(context, CommonErrorType.MQTT_DESTROY_START_SERVICE, ex.getMessage());
        }
    }

    @Override
    public void deliveryComplete(Context context, IMqttDeliveryToken token) {
        try {
            FlyveLog.d("deliveryComplete Token: " + token.isComplete() + " : " + token.getMessage().toString());
            Helpers.storeLog(context.getString(R.string.mqtt_delivery), context.getString(R.string.response_id), String.valueOf(token.getMessageId()));
        } catch (Exception ex) {
            showDetailError(context, CommonErrorType.MQTT_DELIVERY_COMPLETE, ex.getMessage());
        }
    }

    @Override
    public void connectionLost(Context context, MqttCallback callback, String message) {
        showDetailError(context, CommonErrorType.MQTT_CONNECTION_LOST, "Method: connectionLost " + message);
        setStatus(context, callback, false);
    }

    private void setStatus(Context context, MqttCallback callback, Boolean isConnected){
        //send broadcast
        this.connected = isConnected;

        // reconnect
        if(!isConnected) {
            reconnect(context, callback);
        } else {
            // send via http the status connected
            PoliciesAsyncTask.sendStatusbyHttp(context, true);
        }

        AppData cache = new AppData(context);
        cache.setOnlineStatus(isConnected);

        Helpers.sendBroadcast(isConnected, Helpers.BROADCAST_STATUS, context);
    }


}
