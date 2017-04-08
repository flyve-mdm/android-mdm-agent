/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android.
 *
 * Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.service;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.teclib.api.FlyveLog;
import com.teclib.api.GPSTracker;
import com.teclib.database.SharedPreferenceAction;
import com.teclib.database.SharedPreferenceConnectivity;
import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.database.SharedPreferencePolicies;
import com.teclib.database.SharedPreferenceSettings;
import com.teclib.flyvemdm.MainApplication;

import org.fusioninventory.InventoryTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MQTTService extends Service implements MqttCallback {
    public static final String TAG = "MqttService"; // Debug TAG

    //TODO: add support for Mqtt SSL Port Setting in preferences. MQTT_PORT_SSL constant should be removed

    private static final boolean MQTT_CLEAN_SESSION = true; // Start a clean session?
    private static final String MQTT_URL_FORMAT_SSL = "ssl://%s:%d"; // URL Format
    private static final String MQTT_URL_FORMAT_TCP = "tcp://%s:%d"; // URL Format normally don't change

    public static final String ACTION_SEND = TAG + ".SEND"; // Action to send keepalive
    public static final String ACTION_INVENTORY = TAG + ".INVENTORY"; // Action to send inventory
    public static final String ACTION_START = TAG + ".START"; // Action to start
    public static final String ACTION_STOP = TAG + ".STOP"; // Action to stop
    private static final String ACTION_RECONNECT = TAG + ".RECONNECT"; // Action to reconnect
    public static final String ACTION_GPS = TAG + ".GPS"; // Action to reconnect


    // Note: There is a 23 character limit you will get
    // An NPE if you go over that limit
    private boolean mStarted = false; // Is the Client started?
    private boolean isStartedThread = false;
    private String mDeviceId;          // Device ID, Secure.ANDROID_ID
    private MemoryPersistence mMemStore;        // On Fail reverts to MemoryStore
    private MqttConnectOptions mOpts;            // Connection Options

    private MqttAndroidClient mClient;                    // Mqtt Client
    private PendingIntent alarmIntent;
    private AlarmManager mAlarmManager;            // Alarm manager to perform repeating tasks
    private ConnectivityManager mConnectivityManager; // To check for connectivity changes
    Handler mHandler;
    private SharedPreferenceMQTT sharedPreferenceMQTT;
    private String password;

    private static boolean hasWifi = false;
    private static boolean hasMmobile = false;
    boolean isRunning = true;
    private File certFile;

    public static final String MQTT_MSG_RECEIVED_INTENT = "com.dalelane.mqtt.MSGRECVD";
    public static final String MQTT_MSG_RECEIVED_MSG = "com.dalelane.mqtt.MSGRECVD_MSGBODY";

    public static final String MQTT_STATUS_INTENT = "com.dalelane.mqtt.STATUS";
    public static final String MQTT_STOP_INTENT = "com.teclib.service.STOP";

    MQTTBroadcastReceiver mqttBroadcastReceiver;


    class MQTTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo infos[] = mConnectivityManager.getAllNetworkInfo();

            for (int i = 0; i < infos.length; i++) {
                if ("MOBILE".equalsIgnoreCase(infos[i].getTypeName())) {
                    if ((infos[i].isConnected() != hasMmobile)) {
                        hasMmobile = infos[i].isConnected();
                    }
                    FlyveLog.d(infos[i].getTypeName() + " is " + infos[i].isConnected());
                } else if ("WIFI".equalsIgnoreCase(infos[i].getTypeName())) {
                    if ((infos[i].isConnected() != hasWifi)) {
                        hasWifi = infos[i].isConnected();
                    }
                    FlyveLog.d(infos[i].getTypeName() + " is " + infos[i].isConnected());
                }
            }

        }
    }

    /**
     * Initalizes the DeviceId and most instance variables
     * Including the Connection Handler, Datastore, Alarm Manager
     * and ConnectivityManager.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentf = new IntentFilter();
        intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mqttBroadcastReceiver = new MQTTBroadcastReceiver();
        registerReceiver(mqttBroadcastReceiver, intentf);

        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        Intent intentNotificationService = new Intent(MQTTService.this, MQTTNotificationService.class);
        MQTTService.this.startService(intentNotificationService);

        // Do not set keep alive interval on mOpts we keep track of it with alarm's
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mHandler = new Handler();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        android.os.Debug.waitForDebugger();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(mqttBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * Service onStartCommand
     * Handles the action passed via the Intent
     *
     * @return START_REDELIVER_INTENT
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if (intent != null && intent.getAction() != null) {
            action = intent.getAction();
        } else {
            action = null;
        }
        FlyveLog.v("onStartCommand: Received action of " + action);

        if (action == null) {
            FlyveLog.i("onStartCommand: Starting service with no action\n Probably from a crash");
        } else {
            if (action.equals(ACTION_START)) {
                if ((mClient == null || !mClient.isConnected())) {
                    FlyveLog.d("MQTT START");
                    connect();
                }
            }
            if (action.equals(ACTION_RECONNECT)) {
                FlyveLog.d("onStartCommand: Received ACTION_RECONNECT");
                connect();
            }
            if (action.equals(ACTION_GPS)) {
                FlyveLog.d("onStartCommand: Received ACTION_GPS");
                try {
                    sendGPS();
                } catch (JSONException e) {
                    FlyveLog.e("sendGPS", e);
                }
            }
            if (action.equals(ACTION_SEND)) {
                FlyveLog.d("onStartCommand: Received ACTION_SEND");
                publishMessage(intent.getStringExtra("topic"), intent.getStringExtra("message"));
            }
            if (action.equals(ACTION_INVENTORY)) {
                FlyveLog.d("onStartCommand: Received ACTION_INVENTORY");
                try {
                    sendInventory();
                } catch (MqttConnectivityException e) {
                    FlyveLog.e("sendInventory MQTT Connectivity", e);
                } catch (MqttException e) {
                    FlyveLog.e("sendInventory MQTT", e);
                }
            }
        }

        return START_STICKY;
    }


    private synchronized void subscribe() {

        List subList = new ArrayList();
        Set<String> fleetTopics = sharedPreferenceMQTT.getTopics(getBaseContext());
        String setToList[] = fleetTopics.toArray(new String[fleetTopics.size()]);
        // Subscribe serials topics

        String serialTopic = sharedPreferenceMQTT.getSerialTopic(getBaseContext())[0];
        String all = serialTopic + "/#";

        subList.add(serialTopic);
        subList.add(all);

        for (int i = 0; i < setToList.length; i++) {
            subList.add(setToList[i]);
            subList.add(setToList[i] + "/#");
        }


        String sub[] = (String[]) subList.toArray(new String[subList.size()]);
        for (String subItem : sub)
            FlyveLog.i(subItem);

        int Qos[] = new int[sub.length];
        for (int k = 0; k < Qos.length; k++) {
            Qos[k] = 0;
        }

        try {
            IMqttToken subToken = mClient.subscribe(sub, Qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    FlyveLog.i("Subscribe Success");
                    mClient.setCallback(MQTTService.this);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    FlyveLog.e(exception.getMessage());
                }
            });
        } catch (MqttException e) {
            FlyveLog.e("MQTT Token", e);
        }

    }

    private void unsubscribe() {
        List subList = new ArrayList();
        Set<String> fleetTopics = sharedPreferenceMQTT.getTopics(getBaseContext());
        String setToList[] = fleetTopics.toArray(new String[fleetTopics.size()]);

        String serialTopic = sharedPreferenceMQTT.getSerialTopic(getBaseContext())[0];
        String all = serialTopic + "/#";

        subList.add(serialTopic);
        subList.add(all);

        for (int i = 0; i < setToList.length; i++) {
            subList.add(setToList[i]);
            subList.add(setToList[i] + "/#");
        }


        String sub[] = (String[]) subList.toArray(new String[subList.size()]);
        for (String sItem : sub)
            FlyveLog.i(sItem);
        try {
            mClient.unsubscribe(sub);

        } catch (MqttException e) {
            FlyveLog.e("MQTT unsubscribe", e);
        }

        sharedPreferenceMQTT.removeTopics(getBaseContext());

        return;
    }


    /**
     * Connects to the broker with the appropriate datastore
     */
    private synchronized void connect() {

        //  IMqttAsyncClient client = new MqttWebSocketAsyncClient(
        //        uriString, clientId, new MemoryPersistence());


        mDeviceId = MqttClient.generateClientId();
        mMemStore = new MemoryPersistence();

        FlyveLog.d("connect client id = " + mDeviceId);

        sharedPreferenceMQTT = new SharedPreferenceMQTT();
        password = sharedPreferenceMQTT.getPassword(getBaseContext());
        String strMqttPort = sharedPreferenceMQTT.getPort(getBaseContext());
        String mqttHost = sharedPreferenceMQTT.getServer(getBaseContext());
        boolean isTls = "1".equals(sharedPreferenceMQTT.getTLS(getBaseContext()));
        int mqttPort = 1883;
        try {
            mqttPort = Integer.parseInt(strMqttPort);
        } catch (Exception ex) {
            FlyveLog.e("Invalid mqtt port value '" + mqttPort + "', defaulted to 1883", ex);
        }

        FlyveLog.d("defined mqtt setting: port " + mqttPort + " address: " + mqttHost);
        String url = String.format(isTls ? MQTT_URL_FORMAT_SSL : MQTT_URL_FORMAT_TCP,
                mqttHost, mqttPort);
        FlyveLog.i("will connect to mqtt url " + url + " (tlsEnabled: " + isTls + ")");
        mClient = new MqttAndroidClient(this.getApplicationContext(), url, mDeviceId, mMemStore);
        mOpts = new MqttConnectOptions();
        mOpts.setPassword(password.toCharArray());
        mOpts.setUserName(Build.SERIAL);
        mOpts.setCleanSession(MQTT_CLEAN_SESSION);
        // disable timeout
        mOpts.setConnectionTimeout(0);
        if (isTls) {
            try {

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caCertFile = getAssets().open("flyve.org.ca.crt");
                Certificate ca = cf.generateCertificate(caCertFile);
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

                mOpts.setSocketFactory(sslContext.getSocketFactory());
                FlyveLog.i("ssl socket factory created from flyve ca");
            } catch (Exception ex) {
                FlyveLog.e("error while building ssl mqtt cnx", ex);
            }
        }

        FlyveLog.i("Connecting with URL: " + url);

        try {
            IMqttToken token = mClient.connect(mOpts);

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    FlyveLog.i("onSuccess");
                    subscribe();
                    mStarted = true; // Service is now connected
                    isRunning = false;
                    isStartedThread = false;
                    FlyveLog.i("Successfully connected and subscribed starting keep alives");
                    broadcastServiceStatus(true);
                    if (!sharedPreferenceMQTT.getIsInventory(getBaseContext())) {
                        try {
                            generateInventory();
                            sharedPreferenceMQTT.saveIsInventory(getBaseContext(), true);
                        } catch (MqttConnectivityException e) {
                            FlyveLog.e("generateInventory MQTT Connectivity", e);
                        } catch (MqttException e) {
                            FlyveLog.e("generateInventory MQTT", e);
                        }
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    FlyveLog.i("onFailure");
                    FlyveLog.e(exception.getMessage() + asyncActionToken.getException());
                    mClient = null;
                    mStarted = false;
                    broadcastServiceStatus(false);
                    if (!isStartedThread)
                        ReconnectThread();
                }
            });
        } catch (MqttException e) {
            switch (e.getReasonCode()) {
                case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                case MqttException.REASON_CODE_CONNECTION_LOST:
                case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                    FlyveLog.e(e.getMessage());
                    break;
                case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                    Intent i = new Intent("RAISEALLARM");
                    i.putExtra("ALLARM", e);
                    break;
                default:
                    FlyveLog.e(e.getMessage());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void broadcastReceivedMessage(String message) {
        // pass a message received from the MQTT server on to the Activity UI
        //   (for times when it is running / active) so that it can be displayed
        //   in the app GUI
        FlyveLog.d("broadcastReceivedMessage");
        Intent broadcastIntent2 = new Intent();
        broadcastIntent2.setAction(MQTT_MSG_RECEIVED_INTENT);
        broadcastIntent2.putExtra(MQTT_MSG_RECEIVED_MSG, message);
        sendBroadcast(broadcastIntent2);
    }

    private void broadcastServiceStatus(boolean statusDescription) {
        FlyveLog.d("broadcastServiceStatus");
        sharedPreferenceMQTT.saveStatus(getBaseContext(), statusDescription);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MQTT_STATUS_INTENT);
        sendBroadcast(broadcastIntent);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Connectivity Lost from broker
     */
    @Override
    public void connectionLost(Throwable arg0) {
        mClient = null;
        mStarted = false;
        isRunning = true;
        FlyveLog.i("connectionLost: ", arg0);

        if (isOnline()) {
            connect();
        } else {
            broadcastServiceStatus(false);
            ReconnectThread();
        }

    }

    private void ReconnectThread() {
        FlyveLog.i("ReconnectThread: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isRunning) {
                    isStartedThread = true;
                    try {
                        Thread.sleep(30000);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                // Write your code here to update the UI.
                                isConnected();
                            }
                        });
                    } catch (Exception e) {
                        FlyveLog.e("Reconnect Thread", e);
                    }
                }
            }
        }).start();
    }

    private void isConnected() {
        if (mStarted && mClient != null && !mClient.isConnected()) {
            FlyveLog.i("Mismatch between what we think is connected and what is connected");
        }

        if (mStarted == false) {
            FlyveLog.i("isConnected: non connect");
            connect();
        }
    }

    private synchronized void sendKeepAlive() throws MqttConnectivityException, MqttException {

        MqttMessage message = new MqttMessage("!".getBytes());
        message.setQos(0);
        mClient.publish(sharedPreferenceMQTT.getSerialTopic(getBaseContext())[0] + "/Status/Ping", message);
        return;
    }

    private synchronized void generateInventory() throws MqttConnectivityException, MqttException {
        InventoryTask inventory = new InventoryTask(getBaseContext());
        inventory.execute();
        return;
    }

    private synchronized void sendInventory() throws MqttConnectivityException, MqttException {
        if (mClient != null && sharedPreferenceMQTT != null) {
            MqttMessage message = new MqttMessage(ReadInventory().getBytes());
            message.setQos(0);
            mClient.publish(sharedPreferenceMQTT.getSerialTopic(getBaseContext())[0] + "/Status/Inventory", message);
        }
    }

    private synchronized void unEnrolment() throws MqttConnectivityException, MqttException {
        SharedPreferenceAction sharedPreferenceAction = new SharedPreferenceAction();
        SharedPreferenceConnectivity sharedPreferenceConnectivity = new SharedPreferenceConnectivity();
        SharedPreferencePolicies sharedPreferencePolicies = new SharedPreferencePolicies();
        SharedPreferenceSettings sharedPreferenceSettings = new SharedPreferenceSettings();

        //send message to the server
        MqttMessage message = new MqttMessage("{\"unenroll\": \"unenrolled\"}".getBytes());
        message.setQos(0);

        mClient.publish(sharedPreferenceMQTT.getSerialTopic(getBaseContext())[0] + "/Status/Unenroll", message);
        isStartedThread = true;
        unsubscribe();

        // delete all MQTT data
        sharedPreferenceMQTT.clearSharedPreference(getBaseContext());
        // need to clear all data
        sharedPreferenceAction.clearSharedPreference(getBaseContext());
        sharedPreferenceConnectivity.clearSharedPreference(getBaseContext());
        sharedPreferencePolicies.clearSharedPreference(getBaseContext());
        sharedPreferenceSettings.clearSharedPreference(getBaseContext());

        FlyveLog.d("After clear sharedPreference");

        // stop MQTT notification
        Intent broadcastNotificationIntent = new Intent();
        broadcastNotificationIntent.setAction(MQTT_STOP_INTENT);
        getBaseContext().sendBroadcast(broadcastNotificationIntent);

        FlyveLog.d("STOP MQTT broadcast");

        //stop MQTT activity
        Intent broadcastActivityIntent = new Intent();
        broadcastActivityIntent.setAction("MQTTNotifierActivity");
        getBaseContext().sendBroadcast(broadcastActivityIntent);

        MainApplication.getInstance().clearApplicationData();

        FlyveLog.d("After clear application data");

        //kill mqtt service
        stopForeground(true);

        MainApplication.getInstance().killApp();
        return;
    }


    public String ReadInventory() {
        BufferedReader reader = null;
        File tempFile;
        FileReader tempFileReader = null;
        StringBuilder text = new StringBuilder();
        try {
            tempFile = new File(getBaseContext().getFilesDir(), "/android_inventory.xml");
            tempFileReader = new FileReader(tempFile);
            reader = new BufferedReader(tempFileReader);
            String line;

            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (FileNotFoundException e) {
            FlyveLog.e("file not found exception", e);
        } catch (IOException e) {
            FlyveLog.e("io error", e);
        } finally {
            try {
                reader.close();
                tempFileReader.close();
            } catch (IOException e) {
                FlyveLog.e("io error", e);
            } catch (NullPointerException e){
                FlyveLog.e("Error on close", e);
            }
        }
        return text.toString();
    }

    public void publishMessage(String topic, String message) {
        MqttMessage mqttmessage = new MqttMessage(message.getBytes());
        FlyveLog.i("publishMessage: " + message);
        try {
            mClient.publish(topic, mqttmessage);
        } catch (MqttException e) {
            FlyveLog.e("mqtt exception on message publish", e);
        }

    }

    public void sendGPSDesactivated() throws JSONException {
        String gpsDesac;
        JSONObject jsonNoGPS = new JSONObject();

        jsonNoGPS.put("gps", "off");

        gpsDesac = jsonNoGPS.toString();

        MqttMessage messageGps = new MqttMessage(gpsDesac.getBytes());
        messageGps.setQos(0);

        try {
            mClient.publish(sharedPreferenceMQTT.getSerialTopic(getBaseContext())[0] + "/Status/Geolocation", messageGps);
            FlyveLog.d("Message published" + gpsDesac);
        } catch (Exception e) {
            FlyveLog.e("mqtt exception on gps message publish", e);
        }
    }


    public void sendGPS() throws JSONException {
        String gpsLoc;
        double test = 0.0;
        GPSTracker mGPS = new GPSTracker(this);
        mGPS.getLocation();

        FlyveLog.i("sendGPS: " + "Lat = " + mGPS.getLatitude() + "Lon = " + mGPS.getLongitude());
        JSONObject jsonGPS = new JSONObject();

        if(Double.compare(test, mGPS.getLatitude())==0){
            jsonGPS.put("latitude", "na");
            jsonGPS.put("longitude", "na");
        }
        else{
            jsonGPS.put("latitude", mGPS.getLatitude());
            jsonGPS.put("longitude", mGPS.getLongitude());
        }

        jsonGPS.put("datetime", GetUnixTime());

        gpsLoc = jsonGPS.toString();

        MqttMessage messageGps = new MqttMessage(gpsLoc.getBytes());
        messageGps.setQos(0);

        try {
            mClient.publish(sharedPreferenceMQTT.getSerialTopic(getBaseContext())[0] + "/Status/Geolocation", messageGps);
            FlyveLog.d("Message published" + gpsLoc);
        } catch (Exception ex) {
            FlyveLog.e("mqtt exception on gps message publish", ex);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        FlyveLog.i("  Topic:\t" + topic +
                "  Message:\t" + new String(message.getPayload()) +
                "  QoS:\t" + message.getQos());

        String messageBody;
        messageBody = new String(message.getPayload());

        try {
            JSONObject jsonObj = new JSONObject(messageBody);

            // KeepAlive
            if (jsonObj.has("query")) {
                if ("Inventory".equals(jsonObj.getString("query"))) {
                    generateInventory();
                    return;
                }
                if ("Ping".equals(jsonObj.getString("query"))) {
                    sendKeepAlive();
                    return;
                }
                if ("Geolocate".equals(jsonObj.getString("query"))) {
                    GPSTracker mGPS = new GPSTracker(this);
                    if (mGPS.isCanGetLocation()) {
                        sendGPS();
                        return;
                    } else {
                        Intent intent = new Intent(getBaseContext(), NotificationGPSActivation.class);
                        getBaseContext().startService(intent);
                        sendGPSDesactivated();
                        return;
                    }
                }
            }

            if (jsonObj.has("unenroll")) {
                if ("now".equals(jsonObj.getString("unenroll"))) {
                    unEnrolment();
                    return;
                }
            }

            if (jsonObj.has("subscribe")) {
                String NewTopics;
                JSONArray array = jsonObj.getJSONArray("subscribe");
                Set<String> topicSet = sharedPreferenceMQTT.getTopics(getBaseContext());
                String[] topicsTestTab = topicSet.toArray(new String[topicSet.size()]);

                if (topicsTestTab.length != 0) {
                    for (int j = 0; j < array.length(); j++) {
                        NewTopics = array.getJSONObject(j).getString("topic");
                        if (NewTopics.equals(topicsTestTab[j])) {
                            return;
                        }
                    }
                }

                unsubscribe();
                for (int i = 0; i < array.length(); i++) {
                    NewTopics = array.getJSONObject(i).getString("topic");
                    FlyveLog.i("NewTopics save = " + NewTopics);
                    sharedPreferenceMQTT.saveTopics(getBaseContext(), NewTopics);
                }
                subscribe();
            } else {
                broadcastReceivedMessage(messageBody);
            }

        } catch (JSONException e) {
            FlyveLog.e("exception on message receive", e);
        }
    }

    public int GetUnixTime() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        int utc = (int) (now / 1000);
        return (utc);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            FlyveLog.i("deliveryComplete: " + token.getMessage().toString());
        } catch (MqttException e) {
            FlyveLog.e("exception on deliveryComplete", e);
        }
    }
    /**
     * MqttConnectivityException Exception class
     */
    private class MqttConnectivityException extends Exception {
        private static final long serialVersionUID = -7385866796799469420L;
    }
}