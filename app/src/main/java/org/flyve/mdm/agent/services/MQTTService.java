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
import android.os.Binder;
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
import org.flyve.mdm.agent.data.AppData;
import org.flyve.mdm.agent.data.MqttData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

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
    private static final String DEFAULT_TASK_ESTATUS = "Received";

    private Timer _timer;
    private MqttAndroidClient client;
    private Boolean connected = false;
    private PoliciesController policiesController;
    IBinder mBinder = new LocalBinder();

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

    public class LocalBinder extends Binder {
        public MQTTService getServerInstance() {
            return MQTTService.this;
        }
    }

    public void sendInventory() {
        if(connected) {
            policiesController.createInventory();
        } else {
            FlyveLog.i("Cannot sent the inventory the device is offline");
        }
    }

    /**
     * Return the communication channel to the service
     * @param intent that was used to bind to this service
     * @return IBinder null if clients cannot bind to the service
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
     * This function connect the agent with MQTT server
     */
    public void connect() {
        Context mContext = this.getApplicationContext();

        MqttData cache = new MqttData(mContext);

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

                    policiesController = new PoliciesController(getApplicationContext(), client);

                    // send status online true to MQTT
                    policiesController.sendOnlineStatus(true);

                    // main channel
                    String channel = mTopic + "/#";
                    FlyveLog.d(TAG, "MQTT Channel: " + channel);
                    policiesController.subscribe("#");

                    // subscribe to manifest
                    policiesController.subscribe("/FlyvemdmManifest/Status/Version");

                    // send inventory on connect
                    policiesController.createInventory();
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

    public void reconnect() {
        _timer = new Timer();
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!MQTTService.this.connected) {
                    FlyveLog.d("Reconnecting...");
                    connect();
                } else {
                    FlyveLog.d("Reconnection finish");
                    _timer.cancel();

                }
            }
        }, 1000, 30000);
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

        if(topic.isEmpty()) {
            // exit if the topic if empty
            return;
        }

        if(messageBody.isEmpty()) {
            // exit if the message if empty
            return;
        }

        // Command/Ping
        if(topic.toLowerCase().contains("ping")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);
                if (jsonObj.has(QUERY)
                        && "Ping".equalsIgnoreCase(jsonObj.getString(QUERY))) {
                    policiesController.sendKeepAlive();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Command/Geolocate
        if(topic.toLowerCase().contains("geolocate")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);
                if (jsonObj.has(QUERY)
                        && "Geolocate".equalsIgnoreCase(jsonObj.getString(QUERY))) {
                    policiesController.sendGPS();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Command/Inventory
        if(topic.toLowerCase().contains("inventory")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);
                if (jsonObj.has(QUERY)
                        && "Inventory".equalsIgnoreCase(jsonObj.getString(QUERY))) {
                    policiesController.createInventory();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Command/Lock
        if(topic.toLowerCase().contains("lock")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if (jsonObj.has("lock")) {
                    String lock = jsonObj.getString("lock");
                    policiesController.lockDevice(lock.equalsIgnoreCase("now"));
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Command/Wipe
        if(topic.toLowerCase().contains("wipe")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has("wipe") && "NOW".equalsIgnoreCase(jsonObj.getString("wipe")) ) {
                    policiesController.wipe();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Command/Unenroll
        if(topic.toLowerCase().contains("unenroll")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has("unenroll") && "NOW".equalsIgnoreCase(jsonObj.getString("unenroll")) ) {
                    FlyveLog.d("unroll");
                    policiesController.unenroll();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
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
                        FlyveLog.d(channel);

                        // Add new channel
                        policiesController.subscribe(channel);
                    }
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordEnabled
        String PASSWORD_ENABLE = "passwordEnabled";
        if(topic.toLowerCase().contains(PASSWORD_ENABLE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_ENABLE)) {
                    policiesController.passwordEnabled();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordEnabled
        String PASSWORD_QUALITY = "passwordQuality";
        if(topic.toLowerCase().contains(PASSWORD_QUALITY.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_QUALITY)) {
                    String quality = jsonObj.getString(PASSWORD_QUALITY);
                    policiesController.passwordQuality(quality);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordMinLength
        String PASSWORD_MIN_LENGTH = "passwordMinLength";
        if(topic.toLowerCase().contains(PASSWORD_MIN_LENGTH.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_MIN_LENGTH)) {
                    int length = jsonObj.getInt(PASSWORD_MIN_LENGTH);
                    policiesController.passwordMinLength(length);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordMinLowerCase
        String PASSWORD_MIN_LOWERCASE = "passwordMinLowerCase";
        if(topic.toLowerCase().contains(PASSWORD_MIN_LOWERCASE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_MIN_LOWERCASE)) {
                    int minimum = jsonObj.getInt(PASSWORD_MIN_LOWERCASE);
                    policiesController.passwordMinLowerCase(minimum);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordMinUpperCase
        String PASSWORD_MIN_UPPERCASE = "passwordMinUpperCase";
        if(topic.toLowerCase().contains(PASSWORD_MIN_UPPERCASE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_MIN_UPPERCASE)) {
                    int minimum = jsonObj.getInt(PASSWORD_MIN_UPPERCASE);
                    policiesController.passwordMinUpperCase(minimum);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordMinNonLetter
        String PASSWORD_MIN_NON_LETTER = "passwordMinNonLetter";
        if(topic.toLowerCase().contains(PASSWORD_MIN_NON_LETTER.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_MIN_NON_LETTER)) {
                    int minimum = jsonObj.getInt(PASSWORD_MIN_NON_LETTER);
                    policiesController.passwordMinNonLetter(minimum);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordMinLetters
        String PASSWORD_MIN_LETTERS = "passwordMinLetters";
        if(topic.toLowerCase().contains(PASSWORD_MIN_LETTERS.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_MIN_LETTERS)) {
                    int minimum = jsonObj.getInt(PASSWORD_MIN_LETTERS);
                    policiesController.passwordMinLetter(minimum);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordMinNumeric
        String PASSWORD_MIN_NUMERIC = "passwordMinNumeric";
        if(topic.toLowerCase().contains(PASSWORD_MIN_NUMERIC.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_MIN_NUMERIC)) {
                    int minimum = jsonObj.getInt(PASSWORD_MIN_NUMERIC);
                    policiesController.passwordMinNumeric(minimum);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordMinSymbols
        String PASSWORD_MIN_SYMBOLS = "passwordMinSymbols";
        if(topic.toLowerCase().contains(PASSWORD_MIN_SYMBOLS.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(PASSWORD_MIN_SYMBOLS)) {
                    int minimum = jsonObj.getInt(PASSWORD_MIN_SYMBOLS);
                    policiesController.passwordMinSymbols(minimum);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/MaximumFailedPasswordsForWipe
        String MAXIMUM_FAILED_PASSWORDS_FOR_WIPE = "maximumFailedPasswordsForWipe";
        if(topic.toLowerCase().contains(MAXIMUM_FAILED_PASSWORDS_FOR_WIPE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(MAXIMUM_FAILED_PASSWORDS_FOR_WIPE)) {
                    int max = jsonObj.getInt(MAXIMUM_FAILED_PASSWORDS_FOR_WIPE);
                    policiesController.maximumFailedPasswordsForWipe(max);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/MaximumTimeToLock
        String MAXIMUM_TIME_TO_LOCK = "maximumTimeToLock";
        if(topic.toLowerCase().contains(MAXIMUM_TIME_TO_LOCK.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(MAXIMUM_TIME_TO_LOCK)) {
                    int max = jsonObj.getInt(MAXIMUM_TIME_TO_LOCK);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.maximumTimeToLock(max);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/storageEncryption
        String STORAGE_ENCRYPTION = "storageEncryption";
        if(topic.toLowerCase().contains(STORAGE_ENCRYPTION.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);
                if(jsonObj.has(STORAGE_ENCRYPTION)) {
                    boolean enable = jsonObj.getBoolean(STORAGE_ENCRYPTION);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.storageEncryption(enable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableCamera
        String DISABLE_CAMERA = "disableCamera";
        if(topic.toLowerCase().contains(DISABLE_CAMERA.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_CAMERA)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_CAMERA);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableCamera(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableBluetooth
        String DISABLE_BLUETOOTH = "disableBluetooth";
        if(topic.toLowerCase().contains(DISABLE_BLUETOOTH.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_BLUETOOTH)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_BLUETOOTH);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableBluetooth(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/deployApp
        String DEPLOY_APP = "deployApp";
        if(topic.toLowerCase().contains(DEPLOY_APP.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DEPLOY_APP)) {
                    String deployApp = jsonObj.getString(DEPLOY_APP);
                    String id = jsonObj.getString("id");
                    String versionCode = jsonObj.getString("versionCode");
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.installPackage(deployApp, id, versionCode, taskId);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/deployApp
        String REMOVE_APP = "removeApp";
        if(topic.toLowerCase().contains(REMOVE_APP.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(REMOVE_APP)) {
                    String removeApp = jsonObj.getString(REMOVE_APP);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.removePackage(removeApp);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/deployFile
        String DEPLOY_FILE = "deployFile";
        if(topic.toLowerCase().contains(DEPLOY_FILE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DEPLOY_FILE)) {
                    String deployFile = jsonObj.getString(DEPLOY_FILE);
                    String id = jsonObj.getString("id");
                    String versionCode = jsonObj.getString("version");
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.downloadFile(deployFile, id, versionCode, taskId);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/deployFile
        String REMOVE_FILE = "removeFile";
        if(topic.toLowerCase().contains(REMOVE_FILE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(REMOVE_FILE)) {
                    String removeFile = jsonObj.getString(REMOVE_FILE);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.removeFile(removeFile);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableScreenCapture
        //  ROOT REQUIRED
        String DISABLE_SCREEN_CAPTURE = "disableScreenCapture";
        if(topic.toLowerCase().contains(DISABLE_SCREEN_CAPTURE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_SCREEN_CAPTURE)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_SCREEN_CAPTURE);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableScreenCapture(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableAirplaneMode
        //  ROOT REQUIRED
        String DISABLE_AIRPLANE_MODE = "disableAirplaneMode";
        if(topic.toLowerCase().contains(DISABLE_AIRPLANE_MODE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_AIRPLANE_MODE)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_AIRPLANE_MODE);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableAirplaneMode(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableGPS
        //  ROOT REQUIRED
        String DISABLE_GPS = "disableGPS";
        if(topic.toLowerCase().contains(DISABLE_GPS.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_GPS)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_GPS);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableGPS(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableHostpotTethering
        String DISABLE_HOSTPOT_TETHERING = "disableHostpotTethering";
        if(topic.toLowerCase().contains(DISABLE_HOSTPOT_TETHERING.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_HOSTPOT_TETHERING)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_HOSTPOT_TETHERING);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableHostpotTethering(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableRoaming
        String DISABLE_ROAMING = "disableRoaming";
        if(topic.toLowerCase().contains(DISABLE_ROAMING.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_ROAMING)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_ROAMING);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableRoaming(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableWifi
        String DISABLE_WIFI = "disableWifi";
        if(topic.toLowerCase().contains(DISABLE_WIFI.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_WIFI)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_WIFI);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableWifi(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/useTLS
        String USE_TLS = "useTLS";
        if(topic.toLowerCase().contains(USE_TLS.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(USE_TLS)) {
                    Boolean enable = jsonObj.getBoolean(USE_TLS);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.useTLS(enable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableMobileLine
        // ROOT
        String DISABLE_MOBILE_LINE = "disableMobileLine";
        if(topic.toLowerCase().contains(DISABLE_MOBILE_LINE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_MOBILE_LINE)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_MOBILE_LINE);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableMobileLine(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableNfc
        // ROOT
        String DISABLE_NFC = "disableNfc";
        if(topic.toLowerCase().contains(DISABLE_NFC.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_NFC)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_NFC);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableNFC(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableStatusBar
        // ROOT
        String DISABLE_STATUS_BAR = "disableStatusBar";
        if(topic.toLowerCase().contains(DISABLE_STATUS_BAR.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STATUS_BAR)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STATUS_BAR);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableStatusBar(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/resetPassword
        // ROOT
        String RESET_PASSWORD = "resetPassword";
        if(topic.toLowerCase().contains(RESET_PASSWORD.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(RESET_PASSWORD)) {
                    Boolean disable = jsonObj.getBoolean(RESET_PASSWORD);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    //policiesController.resetPassword(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableUsbMtp
        // ROOT
        String DISABLE_USB_MTP = "disableUsbMtp";
        if(topic.toLowerCase().contains(DISABLE_USB_MTP.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_USB_MTP)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_USB_MTP);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableMTPUsbFileTransferProtocols(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableUsbPtp
        // ROOT
        String DISABLE_USB_PTP = "disableUsbPtp";
        if(topic.toLowerCase().contains(DISABLE_USB_PTP.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_USB_PTP)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_USB_PTP);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disablePTPUsbFileTransferProtocols(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableUsbAdb
        // ROOT
        String DISABLE_USB_ADB = "disableUsbAdb";
        if(topic.toLowerCase().contains(DISABLE_USB_ADB.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_USB_ADB)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_USB_ADB);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableADBUsbFileTransferProtocols(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableSpeakerphone
        String DISABLE_SPEAKER_PHONE = "disableSpeakerphone";
        if(topic.toLowerCase().contains(DISABLE_SPEAKER_PHONE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_SPEAKER_PHONE)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_SPEAKER_PHONE);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableSpeakerphone(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableUsbOnTheGo
        String DISABLE_SMSMMS = "disableSmsMms";
        if(topic.toLowerCase().contains(DISABLE_SMSMMS.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_SMSMMS)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_SMSMMS);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableSmsMms(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableCreateVpnProfiles
        String DISABLE_CREATE_VPN_PROFILES = "disableCreateVpnProfiles";
        if(topic.toLowerCase().contains(DISABLE_CREATE_VPN_PROFILES.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_CREATE_VPN_PROFILES)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_CREATE_VPN_PROFILES);
                    String taskId = jsonObj.getString("taskId");

                    // return the status of the task
                    policiesController.sendTaskStatus(taskId, DEFAULT_TASK_ESTATUS);

                    // execute the policy
                    policiesController.disableCreateVpnProfiles(disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
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

        // reconnect
        if(!status) {
            reconnect();
        }

        AppData cache = new AppData(this.getApplicationContext());
        cache.setOnlineStatus(status);

        Helpers.sendBroadcast(status, Helpers.BROADCAST_STATUS, getApplicationContext());
    }
}