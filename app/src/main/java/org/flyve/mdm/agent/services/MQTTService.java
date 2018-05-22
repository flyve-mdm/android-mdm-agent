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
import org.flyve.mdm.agent.policies.BasePolicies;
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
import org.flyve.mdm.agent.policies.StreamAccessibilityPolicy;
import org.flyve.mdm.agent.policies.StreamAlarmPolicy;
import org.flyve.mdm.agent.policies.StreamDTMFPolicy;
import org.flyve.mdm.agent.policies.StreamMusicPolicy;
import org.flyve.mdm.agent.policies.StreamNotificationPolicy;
import org.flyve.mdm.agent.policies.StreamRingPolicy;
import org.flyve.mdm.agent.policies.StreamVoiceCallPolicy;
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
                    FlyveLog.i(TAG, "Success we are online!");
                    broadcastServiceStatus(true);

                    policiesController = new PoliciesController(getApplicationContext(), client);

                    // send status online true to MQTT
                    policiesController.sendOnlineStatus(true);

                    // main topic
                    String topic = mTopic + "/#";
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
        try {
            FlyveLog.d(TAG, "deliveryComplete: \nis Complete: " + token.isComplete() + "\nMessage: " + token.getMessage().toString());
            storeLog(Helpers.broadCastMessage("MQTT Delivery", "Response id", String.valueOf(token.getMessageId())));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * When a message from server arrive
     * @param topic String topic where the message from
     * @param message MqttMessage message content
     * @throws Exception error
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        FlyveLog.d(TAG, "Topic: " + topic + "\n\n- Message: " + new String(message.getPayload()));

        int priority = topic.contains("fleet") ? 0 : 1;

        String messageBody = new String(message.getPayload());

        storeLog(Helpers.broadCastMessage("MQTT Message", "Body", messageBody));

        if(topic.isEmpty()) {
            // exit if the topic if empty
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

        // Policy/useTLS
        String USE_TLS = "useTLS";
        if(topic.toLowerCase().contains(USE_TLS.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if (jsonObj.has(USE_TLS)) {
                    Boolean enable = jsonObj.getBoolean(USE_TLS);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policiesController.useTLS(taskId, enable);
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }

        // Policy/passwordEnabled
        String PASSWORD_ENABLE = "passwordEnabled";
        callPolicy(PasswordEnablePolicy.class, PASSWORD_ENABLE, priority, topic, messageBody);

        // Policy/passwordQuality
        String PASSWORD_QUALITY = "passwordQuality";
        callPolicy(PasswordQualityPolicy.class, PASSWORD_QUALITY, priority, topic, messageBody);

        // Policy/passwordMinLength
        String PASSWORD_MIN_LENGTH = "passwordMinLength";
        callPolicy(PasswordMinLengthPolicy.class, PASSWORD_MIN_LENGTH, priority, topic, messageBody);

        // Policy/passwordMinLowerCase
        String PASSWORD_MIN_LOWERCASE = "passwordMinLowerCase";
        callPolicy(PasswordMinLowerCasePolicy.class, PASSWORD_MIN_LOWERCASE, priority, topic, messageBody);

        // Policy/passwordMinUpperCase
        String PASSWORD_MIN_UPPERCASE = "passwordMinUpperCase";
        callPolicy(PasswordMinUpperCasePolicy.class, PASSWORD_MIN_UPPERCASE, priority, topic, messageBody);

        // Policy/passwordMinNonLetter
        String PASSWORD_MIN_NON_LETTER = "passwordMinNonLetter";
        callPolicy(PasswordMinNonLetterPolicy.class, PASSWORD_MIN_NON_LETTER, priority, topic, messageBody);

        // Policy/passwordMinLetters
        String PASSWORD_MIN_LETTERS = "passwordMinLetters";
        callPolicy(PasswordMinLetterPolicy.class, PASSWORD_MIN_LETTERS, priority, topic, messageBody);

        // Policy/passwordMinNumeric
        String PASSWORD_MIN_NUMERIC = "passwordMinNumeric";
        callPolicy(PasswordMinNumericPolicy.class, PASSWORD_MIN_NUMERIC, priority, topic, messageBody);

        // Policy/passwordMinSymbols
        String PASSWORD_MIN_SYMBOLS = "passwordMinSymbols";
        callPolicy(PasswordMinSymbolsPolicy.class, PASSWORD_MIN_SYMBOLS, priority, topic, messageBody);

        // Policy/MaximumFailedPasswordsForWipe
        String MAXIMUM_FAILED_PASSWORDS_FOR_WIPE = "maximumFailedPasswordsForWipe";
        callPolicy(MaximumFailedPasswordForWipePolicy.class, MAXIMUM_FAILED_PASSWORDS_FOR_WIPE, priority, topic, messageBody);

        // Policy/MaximumTimeToLock
        String MAXIMUM_TIME_TO_LOCK = "maximumTimeToLock";
        callPolicy(MaximumTimeToLockPolicy.class, MAXIMUM_TIME_TO_LOCK, priority, topic, messageBody);

        // Policy/storageEncryption
        String STORAGE_ENCRYPTION = "storageEncryption";
        callPolicy(StorageEncryptionPolicy.class, STORAGE_ENCRYPTION, priority, topic, messageBody);

        // Policy/disableCamera
        String DISABLE_CAMERA = "disableCamera";
        callPolicy(CameraPolicy.class, DISABLE_CAMERA, priority, topic, messageBody);

        // Policy/disableBluetooth
        String DISABLE_BLUETOOTH = "disableBluetooth";
        callPolicy(BluetoothPolicy.class, DISABLE_BLUETOOTH, priority, topic, messageBody);

        // Policy/disableHostpotTethering
        String DISABLE_HOSTPOT_TETHERING = "disableHostpotTethering";
        callPolicy(HostpotTetheringPolicy.class, DISABLE_HOSTPOT_TETHERING, priority, topic, messageBody);

        // Policy/disableRoaming
        String DISABLE_ROAMING = "disableRoaming";
        callPolicy(RoamingPolicy.class, DISABLE_ROAMING, priority, topic, messageBody);

        // Policy/disableWifi
        String DISABLE_WIFI = "disableWifi";
        callPolicy(WifiPolicy.class, DISABLE_WIFI, priority, topic, messageBody);

        // Policy/disableSpeakerphone
        String DISABLE_SPEAKER_PHONE = "disableSpeakerphone";
        callPolicy(SpeakerphonePolicy.class, DISABLE_SPEAKER_PHONE, priority, topic, messageBody);

        // Policy/disableUsbOnTheGo
        String DISABLE_SMSMMS = "disableSmsMms";
        callPolicy(SMSPolicy.class, DISABLE_SMSMMS, priority, topic, messageBody);

        // Policy/disableCreateVpnProfiles
        String DISABLE_CREATE_VPN_PROFILES = "disableCreateVpnProfiles";
        callPolicy(VPNPolicy.class, DISABLE_CREATE_VPN_PROFILES, priority, topic, messageBody);

        // Policy/disableStreamMusic
        String DISABLE_STREAM_MUSIC = "disableStreamMusic";
        callPolicy(StreamMusicPolicy.class, DISABLE_STREAM_MUSIC, priority, topic, messageBody);

        // Policy/disableStreamRing
        String DISABLE_STREAM_RING = "disableStreamRing";
        callPolicy(StreamRingPolicy.class, DISABLE_STREAM_RING, priority, topic, messageBody);

        // Policy/disableStreamAlarm
        String DISABLE_STREAM_ALARM = "disableStreamAlarm";
        callPolicy(StreamAlarmPolicy.class, DISABLE_STREAM_ALARM, priority, topic, messageBody);

        // Policy/disableStreamNotification
        String DISABLE_STREAM_NOTIFICATION = "disableStreamNotification";
        callPolicy(StreamNotificationPolicy.class, DISABLE_STREAM_NOTIFICATION, priority, topic, messageBody);

        // Policy/disableStreamAccessibility
        String DISABLE_STREAM_ACCESSIBILITY = "disableStreamAccessibility";
        callPolicy(StreamAccessibilityPolicy.class, DISABLE_STREAM_ACCESSIBILITY, priority, topic, messageBody);

        // Policy/disableStreamVoiceCall
        String DISABLE_STREAM_VOICECALL = "disableStreamVoiceCall";
        callPolicy(StreamVoiceCallPolicy.class, DISABLE_STREAM_VOICECALL, priority, topic, messageBody);

        // Policy/disableStreamDTMF
        String DISABLE_STREAM_DTMF = "disableStreamDTMF";
        callPolicy(StreamVoiceCallPolicy.class, DISABLE_STREAM_DTMF, priority, topic, messageBody);

        // Policy/disableScreenCapture
        //  ROOT REQUIRED
        String DISABLE_SCREEN_CAPTURE = "disableScreenCapture";
        callPolicy(ScreenCapturePolicy.class, DISABLE_SCREEN_CAPTURE, priority, topic, messageBody);

        // Policy/disableAirplaneMode
        //  ROOT REQUIRED
        String DISABLE_AIRPLANE_MODE = "disableAirplaneMode";
        callPolicy(AirplaneModePolicy.class, DISABLE_AIRPLANE_MODE, priority, topic, messageBody);

        // Policy/disableGPS
        //  ROOT REQUIRED
        String DISABLE_GPS = "disableGPS";
        callPolicy(GPSPolicy.class, DISABLE_GPS, priority, topic, messageBody);

        // Policy/disableMobileLine
        // ROOT
        String DISABLE_MOBILE_LINE = "disableMobileLine";
        callPolicy(MobileLinePolicy.class, DISABLE_MOBILE_LINE, priority, topic, messageBody);

        // Policy/disableNfc
        // ROOT
        String DISABLE_NFC = "disableNfc";
        callPolicy(NFCPolicy.class, DISABLE_NFC, priority, topic, messageBody);

        // Policy/disableStatusBar
        // ROOT
        String DISABLE_STATUS_BAR = "disableStatusBar";
        callPolicy(StatusBarPolicy.class, DISABLE_STATUS_BAR, priority, topic, messageBody);

        // Policy/disableUsbMtp
        // ROOT
        String DISABLE_USB_MTP = "disableUsbMtp";
        callPolicy(UsbMtpPolicy.class, DISABLE_USB_MTP, priority, topic, messageBody);

        // Policy/disableUsbPtp
        // ROOT
        String DISABLE_USB_PTP = "disableUsbPtp";
        callPolicy(UsbPtpPolicy.class, DISABLE_USB_PTP, priority, topic, messageBody);

        // Policy/disableUsbAdb
        // ROOT
        String DISABLE_USB_ADB = "disableUsbAdb";
        callPolicy(UsbAdbPolicy.class, DISABLE_USB_ADB, priority, topic, messageBody);

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
    }

    private void callPolicy(Class<? extends BasePolicies> classPolicy, String policyName, int policyPriority, String topic, String messageBody) {

        if(topic.toLowerCase().contains(policyName.toLowerCase())) {

            BasePolicies policies;

            try {
                policies = classPolicy.getDeclaredConstructor(Context.class).newInstance(getApplicationContext());
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
                return;
            }

            if(messageBody.isEmpty()) {
                policies.remove();
                return;
            }

            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(policyName)) {
                    Object value = jsonObj.get(policyName);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    policies.setMQTTparameters(this.client, topic, taskId);
                    policies.setValue(value);
                    policies.setPriority(policyPriority);
                    policies.execute();
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