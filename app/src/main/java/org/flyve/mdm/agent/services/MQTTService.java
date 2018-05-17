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

package org.flyve.mdm.agent.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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
import org.flyve.mdm.agent.policies.AirplaneModePolicy;
import org.flyve.mdm.agent.policies.BluetoothPolicy;
import org.flyve.mdm.agent.policies.CameraPolicy;
import org.flyve.mdm.agent.policies.GPSPolicy;
import org.flyve.mdm.agent.policies.HostpotTetheringPolicy;
import org.flyve.mdm.agent.policies.MaximumFailedPasswordForWipePolicy;
import org.flyve.mdm.agent.policies.MaximumTimeToLockPolicy;
import org.flyve.mdm.agent.policies.MobileLinePolicy;
import org.flyve.mdm.agent.policies.NFCPolicy;
import org.flyve.mdm.agent.policies.PasswordEnablePolicy;
import org.flyve.mdm.agent.policies.PasswordMinLengthPolicy;
import org.flyve.mdm.agent.policies.PasswordMinLetterPolicy;
import org.flyve.mdm.agent.policies.PasswordMinLowerCasePolicy;
import org.flyve.mdm.agent.policies.PasswordMinNonLetterPolicy;
import org.flyve.mdm.agent.policies.PasswordMinNumericPolicy;
import org.flyve.mdm.agent.policies.PasswordMinSymbolsPolicy;
import org.flyve.mdm.agent.policies.PasswordMinUpperCasePolicy;
import org.flyve.mdm.agent.policies.PasswordQualityPolicy;
import org.flyve.mdm.agent.policies.RoamingPolicy;
import org.flyve.mdm.agent.policies.SMSPolicy;
import org.flyve.mdm.agent.policies.ScreenCapturePolicy;
import org.flyve.mdm.agent.policies.SpeakerphonePolicy;
import org.flyve.mdm.agent.policies.StatusBarPolicy;
import org.flyve.mdm.agent.policies.StorageEncryptionPolicy;
import org.flyve.mdm.agent.policies.StreamMusicPolicy;
import org.flyve.mdm.agent.policies.UsbAdbPolicy;
import org.flyve.mdm.agent.policies.UsbMtpPolicy;
import org.flyve.mdm.agent.policies.UsbPtpPolicy;
import org.flyve.mdm.agent.policies.VPNPolicy;
import org.flyve.mdm.agent.policies.WifiPolicy;
import org.flyve.mdm.agent.ui.MainActivity;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
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

    private int reconnectionCounter = 0;
    private int reconnectionPeriod = 1000;
    private int reconnectionDelay = 5; //delay in milliseconds before task is to be executed.
    private long timeLastReconnection = 0;
    private Timer reconnectionTimer;
    private Boolean executeConnection = true;
    private int tryEverySeconds = 30;

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

        Context mContext = this.getApplicationContext();
        MqttData cache = new MqttData(mContext);

        String mBroker = cache.getBroker();
        String mPort = cache.getPort();
        String mUser = cache.getMqttUser();

        if(mBroker.equals("") || mPort.equals("") || mUser.equals("")) {
            connected = false;
        }

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
        final String mUser = cache.getMqttUser();
        final String mPassword = cache.getMqttPasswd();
        final String mTopic = cache.getTopic();
        final String mTLS = cache.getTls();

        final StringBuilder connectionInformation = new StringBuilder();
        connectionInformation.append("\n\nBroker: " + mBroker + "\n");
        connectionInformation.append("Port: " + mPort + "\n");
        connectionInformation.append("User: " + mUser + "\n");
        connectionInformation.append("Topic: " + mTopic + "\n");
        connectionInformation.append("TLS: " + mTLS + "\n");

        if(mBroker.equals("") || mPort.equals("") || mUser.equals("")) {
            String brokerValue = "Empty";
            String portValue = "Empty";
            String userValue = "Empty";

            if(!mBroker.equals("")) {
                brokerValue = mBroker;
            }

            if(!mPort.equals("")) {
                portValue = mBroker;
            }

            if(!mUser.equals("")) {
                userValue = mBroker;
            }

            // Helpers.openErrorActivity(mContext, "Some important variable can't be null\n\n - Port: " + portValue + "\n - Broker: " + brokerValue + "\n - User: " + userValue);
            FlyveLog.d(TAG, "Some important variable can't be null - Port: " + portValue + " - Broker: " + brokerValue + " - User: " + userValue);
            return;
        }

        storeLog(Helpers.broadCastMessage(MQTT_LOGIN, "Connection variables", connectionInformation.toString()));

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

                    // main topic
                    String topic = mTopic + "/#";
                    FlyveLog.d(TAG, "MQTT topic: " + topic);
                    policiesController.subscribe(topic);

                    // subscribe to manifest
                    policiesController.subscribe("/FlyvemdmManifest/Status/Version");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                    // Something went wrong e.g. connection timeout or firewall problems

                    String errorMessage;
                    if(ex.getMessage().equalsIgnoreCase("MqttException")) {
                        errorMessage = ex.toString();
                    } else {
                        errorMessage = ex.getMessage();
                    }

                    FlyveLog.e(TAG, "Error on client.connect: " + errorMessage);

                    storeLog(Helpers.broadCastMessage(ERROR, "Error on client.connect", errorMessage + connectionInformation));
                    broadcastMessage(Helpers.broadCastMessage(ERROR, "Error on client.connect", errorMessage + connectionInformation));
                    broadcastServiceStatus(false);
                }
            });
        }
        catch (MqttException ex) {
            FlyveLog.e(TAG, ex.getMessage());
            broadcastServiceStatus(false);
            broadcastMessage(Helpers.broadCastMessage(ERROR, "MQTT Exception: " + String.valueOf(ex.getReasonCode()), ex.getMessage() + connectionInformation));
            storeLog(Helpers.broadCastMessage(ERROR, "MQTT Exception", ex.getMessage() + connectionInformation));
        } catch (Exception ex) {
            FlyveLog.e(TAG, ex.getMessage());
            broadcastServiceStatus(false);
            broadcastMessage(Helpers.broadCastMessage(ERROR, "General Exception", mContext.getResources().getString(R.string.MQTT_ERROR_CONNECTION) + connectionInformation));
            storeLog(Helpers.broadCastMessage(ERROR, "General Exception", ex.getMessage() + connectionInformation));
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
        storeLog(Helpers.broadCastMessage(ERROR, "MQTT Connection lost", cause.getMessage()));
        FlyveLog.d(TAG, "Connection fail " + cause.getMessage());
    }

    public void reconnect() {
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
                    }

                } else {
                    timeLastReconnection = new Date().getTime();
                    executeConnection = true;
                }

                if(!MQTTService.this.connected) {
                    if(executeConnection) {
                        reconnectionCounter++;

                        if((reconnectionCounter % 10)==0) {
                            tryEverySeconds *= 2;
                        }

                        String message = "Reconnecting " + reconnectionCounter + " times";

                        if(new AppData(getApplicationContext()).getEnableNotificationConnection()) {
                            Helpers.sendToNotificationBar(getApplicationContext(), 101, "MDM Agent", message, false, MainActivity.class, "service_disconnect");
                        }

                        FlyveLog.d(message);

                        connect();
                    }
                } else {
                    FlyveLog.d("Reconnection finish");
                    reconnectionTimer.cancel();
                    reconnectionTimer = null;
                }
            }
        }, reconnectionDelay, reconnectionPeriod);
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

        int priority = topic.contains("fleet") ? 0 : 1;

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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordEnablePolicy passwordEnablePolicy = new PasswordEnablePolicy(getApplicationContext());
                    passwordEnablePolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordEnablePolicy.setValue(true);
                    passwordEnablePolicy.setPriority(priority);
                    passwordEnablePolicy.execute();

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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordQualityPolicy passwordQualityPolicy = new PasswordQualityPolicy(getApplicationContext());
                    passwordQualityPolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordQualityPolicy.setValue(quality);
                    passwordQualityPolicy.setPriority(priority);
                    passwordQualityPolicy.execute();

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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordMinLengthPolicy passwordMinLengthPolicy = new PasswordMinLengthPolicy(getApplicationContext());
                    passwordMinLengthPolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordMinLengthPolicy.setValue(length);
                    passwordMinLengthPolicy.setPriority(priority);
                    passwordMinLengthPolicy.execute();
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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordMinLowerCasePolicy passwordMinLowerCasePolicy = new PasswordMinLowerCasePolicy(getApplicationContext());
                    passwordMinLowerCasePolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordMinLowerCasePolicy.setValue(minimum);
                    passwordMinLowerCasePolicy.setPriority(priority);
                    passwordMinLowerCasePolicy.execute();
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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordMinUpperCasePolicy passwordMinUpperCasePolicy = new PasswordMinUpperCasePolicy(getApplicationContext());
                    passwordMinUpperCasePolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordMinUpperCasePolicy.setValue(minimum);
                    passwordMinUpperCasePolicy.setPriority(priority);
                    passwordMinUpperCasePolicy.execute();
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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordMinNonLetterPolicy passwordMinNonLetterPolicy = new PasswordMinNonLetterPolicy(getApplicationContext());
                    passwordMinNonLetterPolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordMinNonLetterPolicy.setValue(minimum);
                    passwordMinNonLetterPolicy.setPriority(priority);
                    passwordMinNonLetterPolicy.execute();

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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordMinLetterPolicy passwordMinLetterPolicy = new PasswordMinLetterPolicy(getApplicationContext());
                    passwordMinLetterPolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordMinLetterPolicy.setValue(minimum);
                    passwordMinLetterPolicy.setPriority(priority);
                    passwordMinLetterPolicy.execute();

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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordMinNumericPolicy passwordMinNumericPolicy = new PasswordMinNumericPolicy(getApplicationContext());
                    passwordMinNumericPolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordMinNumericPolicy.setValue(minimum);
                    passwordMinNumericPolicy.setPriority(priority);
                    passwordMinNumericPolicy.execute();

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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    PasswordMinSymbolsPolicy passwordMinSymbolsPolicy = new PasswordMinSymbolsPolicy(getApplicationContext());
                    passwordMinSymbolsPolicy.setMQTTparameters(this.client, topic, taskId);
                    passwordMinSymbolsPolicy.setValue(minimum);
                    passwordMinSymbolsPolicy.setPriority(priority);
                    passwordMinSymbolsPolicy.execute();

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
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    MaximumFailedPasswordForWipePolicy maximumFailedPasswordForWipePolicy = new MaximumFailedPasswordForWipePolicy(getApplicationContext());
                    maximumFailedPasswordForWipePolicy.setMQTTparameters(this.client, topic, taskId);
                    maximumFailedPasswordForWipePolicy.setValue(max);
                    maximumFailedPasswordForWipePolicy.setPriority(priority);
                    maximumFailedPasswordForWipePolicy.execute();

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

                    // execute the policy
                    MaximumTimeToLockPolicy maximumTimeToLockPolicy = new MaximumTimeToLockPolicy(getApplicationContext());
                    maximumTimeToLockPolicy.setMQTTparameters(this.client, topic, taskId);
                    maximumTimeToLockPolicy.setValue(max);
                    maximumTimeToLockPolicy.setPriority(priority);
                    maximumTimeToLockPolicy.execute();

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

                    // execute the policy
                    StorageEncryptionPolicy storageEncryptionPolicy = new StorageEncryptionPolicy(getApplicationContext());
                    storageEncryptionPolicy.setMQTTparameters(this.client, topic, taskId);
                    storageEncryptionPolicy.setValue(enable);
                    storageEncryptionPolicy.setPriority(priority);
                    storageEncryptionPolicy.execute();
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

                    // execute the policy
                    CameraPolicy cameraPolicy = new CameraPolicy(getApplicationContext());
                    cameraPolicy.setMQTTparameters(this.client, topic, taskId);
                    cameraPolicy.setValue(disable);
                    cameraPolicy.setPriority(priority);
                    cameraPolicy.execute();
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

                    // execute the policy
                    BluetoothPolicy bluetoothPolicy = new BluetoothPolicy(getApplicationContext());
                    bluetoothPolicy.setMQTTparameters(this.client, topic, taskId);
                    bluetoothPolicy.setValue(disable);
                    bluetoothPolicy.setPriority(priority);
                    bluetoothPolicy.execute();
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

                    // execute the policy
                    policiesController.removePackage(taskId, removeApp);
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

                    // execute the policy
                    policiesController.removeFile(taskId, removeFile);
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

                    // execute the policy
                    ScreenCapturePolicy screenCapturePolicy = new ScreenCapturePolicy(getApplicationContext());
                    screenCapturePolicy.setMQTTparameters(this.client, topic, taskId);
                    screenCapturePolicy.setValue(disable);
                    screenCapturePolicy.setPriority(priority);
                    screenCapturePolicy.execute();
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

                    // execute the policy
                    AirplaneModePolicy airplaneModePolicy = new AirplaneModePolicy(getApplicationContext());
                    airplaneModePolicy.setMQTTparameters(this.client, topic, taskId);
                    airplaneModePolicy.setValue(disable);
                    airplaneModePolicy.setPriority(priority);
                    airplaneModePolicy.execute();
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

                    // execute the policy
                    GPSPolicy gpsPolicy = new GPSPolicy(getApplicationContext());
                    gpsPolicy.setMQTTparameters(this.client, topic, taskId);
                    gpsPolicy.setValue(disable);
                    gpsPolicy.setPriority(priority);
                    gpsPolicy.execute();
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

                    // execute the policy
                    HostpotTetheringPolicy hostpotTetheringPolicy = new HostpotTetheringPolicy(getApplicationContext());
                    hostpotTetheringPolicy.setMQTTparameters(this.client, topic, taskId);
                    hostpotTetheringPolicy.setValue(disable);
                    hostpotTetheringPolicy.setPriority(priority);
                    hostpotTetheringPolicy.execute();

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

                    // execute the policy
                    RoamingPolicy roamingPolicy = new RoamingPolicy(getApplicationContext());
                    roamingPolicy.setMQTTparameters(this.client, topic, taskId);
                    roamingPolicy.setValue(disable);
                    roamingPolicy.setPriority(priority);
                    roamingPolicy.execute();
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

                    // execute the policy
                    WifiPolicy wifiPolicy = new WifiPolicy(getApplicationContext());
                    wifiPolicy.setMQTTparameters(this.client, topic, taskId);
                    wifiPolicy.setValue(disable);
                    wifiPolicy.setPriority(priority);
                    wifiPolicy.execute();
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

                    // execute the policy
                    policiesController.useTLS(taskId, enable);
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

                    // execute the policy
                    MobileLinePolicy mobileLinePolicy = new MobileLinePolicy(getApplicationContext());
                    mobileLinePolicy.setMQTTparameters(this.client, topic, taskId);
                    mobileLinePolicy.setValue(disable);
                    mobileLinePolicy.setPriority(priority);
                    mobileLinePolicy.execute();
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

                    // execute the policy
                    NFCPolicy nfcPolicy = new NFCPolicy(getApplicationContext());
                    nfcPolicy.setMQTTparameters(this.client, topic, taskId);
                    nfcPolicy.setValue(disable);
                    nfcPolicy.setPriority(priority);
                    nfcPolicy.execute();
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

                    // execute the policy
                    StatusBarPolicy statusBarPolicy = new StatusBarPolicy(getApplicationContext());
                    statusBarPolicy.setMQTTparameters(this.client, topic, taskId);
                    statusBarPolicy.setValue(disable);
                    statusBarPolicy.setPriority(priority);
                    statusBarPolicy.execute();
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

                    // execute the policy
                    //policiesController.resetPassword(taskId, disable);
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

                    // execute the policy
                    UsbMtpPolicy usbMtpPolicy = new UsbMtpPolicy(getApplicationContext());
                    usbMtpPolicy.setMQTTparameters(this.client, topic, taskId);
                    usbMtpPolicy.setValue(disable);
                    usbMtpPolicy.setPriority(priority);
                    usbMtpPolicy.execute();
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

                    // execute the policy
                    UsbPtpPolicy usbPtpPolicy = new UsbPtpPolicy(getApplicationContext());
                    usbPtpPolicy.setMQTTparameters(this.client, topic, taskId);
                    usbPtpPolicy.setValue(disable);
                    usbPtpPolicy.setPriority(priority);
                    usbPtpPolicy.execute();
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

                    // execute the policy
                    UsbAdbPolicy usbAdbPolicy = new UsbAdbPolicy(getApplicationContext());
                    usbAdbPolicy.setMQTTparameters(this.client, topic, taskId);
                    usbAdbPolicy.setValue(disable);
                    usbAdbPolicy.setPriority(priority);
                    usbAdbPolicy.execute();
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

                    // execute the policy
                    SpeakerphonePolicy speakerphonePolicy = new SpeakerphonePolicy(getApplicationContext());
                    speakerphonePolicy.setMQTTparameters(this.client, topic, taskId);
                    speakerphonePolicy.setValue(disable);
                    speakerphonePolicy.setPriority(priority);
                    speakerphonePolicy.execute();
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

                    // execute the policy
                    SMSPolicy smsPolicy = new SMSPolicy(getApplicationContext());
                    smsPolicy.setMQTTparameters(this.client, topic, taskId);
                    smsPolicy.setValue(disable);
                    smsPolicy.setPriority(priority);
                    smsPolicy.execute();

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

                    // execute the policy
                    VPNPolicy vpnPolicy = new VPNPolicy(getApplicationContext());
                    vpnPolicy.setMQTTparameters(this.client, topic, taskId);
                    vpnPolicy.setValue(disable);
                    vpnPolicy.setPriority(priority);
                    vpnPolicy.execute();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/disableAllSounds
        String DISABLE_ALL_SOUNDS = "disableAllSounds";
        if(topic.toLowerCase().contains(DISABLE_ALL_SOUNDS.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_ALL_SOUNDS)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_ALL_SOUNDS);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.disableAllSounds(taskId, disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        String DISABLE_STREAM_MUSIC = "disableStreamMusic";
        if(topic.toLowerCase().contains(DISABLE_STREAM_MUSIC.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STREAM_MUSIC)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STREAM_MUSIC);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    StreamMusicPolicy streamMusicPolicy = new StreamMusicPolicy(getApplicationContext());
                    streamMusicPolicy.setMQTTparameters(this.client, topic, taskId);
                    streamMusicPolicy.setValue(disable);
                    streamMusicPolicy.setPriority(priority);
                    streamMusicPolicy.execute();

                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        String DISABLE_STREAM_RING = "disableStreamRing";
        if(topic.toLowerCase().contains(DISABLE_STREAM_RING.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STREAM_RING)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STREAM_RING);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.disableSounds(AudioManager.STREAM_RING, taskId, disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        String DISABLE_STREAM_ALARM = "disableStreamAlarm";
        if(topic.toLowerCase().contains(DISABLE_STREAM_ALARM.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STREAM_ALARM)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STREAM_ALARM);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.disableSounds(AudioManager.STREAM_RING, taskId, disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        String DISABLE_STREAM_NOTIFICATION = "disableStreamNotification";
        if(topic.toLowerCase().contains(DISABLE_STREAM_NOTIFICATION.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STREAM_NOTIFICATION)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STREAM_NOTIFICATION);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.disableSounds(AudioManager.STREAM_NOTIFICATION, taskId, disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        String DISABLE_STREAM_ACCESSIBILITY = "disableStreamAccessibility";
        if(topic.toLowerCase().contains(DISABLE_STREAM_ACCESSIBILITY.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STREAM_ACCESSIBILITY)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STREAM_ACCESSIBILITY);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.disableSounds(AudioManager.STREAM_ACCESSIBILITY, taskId, disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        String DISABLE_STREAM_VOICECALL = "disableStreamVoiceCall";
        if(topic.toLowerCase().contains(DISABLE_STREAM_VOICECALL.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STREAM_VOICECALL)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STREAM_VOICECALL);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.disableSounds(AudioManager.STREAM_VOICE_CALL, taskId, disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        String DISABLE_STREAM_DTMF = "disableStreamDTMF";
        if(topic.toLowerCase().contains(DISABLE_STREAM_DTMF.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DISABLE_STREAM_DTMF)) {
                    Boolean disable = jsonObj.getBoolean(DISABLE_STREAM_DTMF);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.disableSounds(AudioManager.STREAM_DTMF, taskId, disable);
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