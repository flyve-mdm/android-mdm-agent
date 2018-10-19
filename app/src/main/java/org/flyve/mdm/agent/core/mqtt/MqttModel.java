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
import org.flyve.mdm.agent.data.localstorage.AppData;
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
import org.flyve.mdm.agent.policies.StreamMusicPolicy;
import org.flyve.mdm.agent.policies.StreamNotificationPolicy;
import org.flyve.mdm.agent.policies.StreamRingPolicy;
import org.flyve.mdm.agent.policies.StreamVoiceCallPolicy;
import org.flyve.mdm.agent.policies.UsbAdbPolicy;
import org.flyve.mdm.agent.policies.UsbMtpPolicy;
import org.flyve.mdm.agent.policies.UsbPtpPolicy;
import org.flyve.mdm.agent.policies.VPNPolicy;
import org.flyve.mdm.agent.policies.WifiPolicy;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.services.PoliciesController;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.ui.MainActivity;
import org.flyve.mdm.agent.utils.AppThreadManager;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private Timer reconnectionTimer;
    private int reconnectionCounter = 0;
    private int reconnectionPeriod = 1000;
    private int reconnectionDelay = 5; //delay in milliseconds before task is to be executed.
    private long timeLastReconnection = 0;
    private Boolean executeConnection = true;
    private int tryEverySeconds = 30;
    ;

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
    public void sendInventory(Context context) {
        if(isConnected()) {
            new PoliciesController(context, getMqttClient()).createInventory();
        } else {
            showDetailError(context, CommonErrorType.MQTT_INVENTORY_FAIL, context.getString(R.string.inventory_cannot_send_offline));
        }
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
        connectionInformation.append("\n\nBroker: " + mBroker + "\n");
        connectionInformation.append("Port: " + mPort + "\n");
        connectionInformation.append("User: " + mUser + "\n");
        connectionInformation.append("Topic: " + mTopic + "\n");
        connectionInformation.append("TLS: " + mTLS + "\n");

        Log.d("MQTT", connectionInformation.toString());
        Helpers.storeLog(Helpers.broadCastMessage("MQTT", "Connection Information", connectionInformation.toString()));

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
            options.setConnectionTimeout(0);
            options.setAutomaticReconnect(true);

            // Create a testament to send when MQTT connection is down
            String will = "{ online: false }";
            options.setWill("/Status/Online", will.getBytes(), 0, false);

            // If TLS is active needs ssl connection option
            if (mTLS.equals("1")) {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                caKeyStore.load(null, null);

                CertificateFactory certificationFactory = CertificateFactory.getInstance("X.509");
                X509Certificate ca = (X509Certificate) certificationFactory.generateCertificate(context.getResources().openRawResource(R.raw.flyve_org));
                String alias = ca.getSubjectX500Principal().getName();

                // Set propper alias name
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
            }
        } catch (Exception ex) {
            showDetailError(context, CommonErrorType.MQTT_OPTIONS, ex.getMessage());
            return;
        }


        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    setStatus(context, callback, true);

                    // set the reconnection counter to 0
                    reconnectionCounter = 0;

                    // Everything ready waiting for message
                    PoliciesController policiesController = new PoliciesController(context, client);

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

                        if(new AppData(context).getEnableNotificationConnection()) {
                            Helpers.sendToNotificationBar(context, 101, context.getString(R.string.app_name), message, false, MainActivity.class, "service_disconnect");
                        }

                        FlyveLog.d(message);
                        connect(context, callback);
                    }
                } else {
                    FlyveLog.d("Reconnection finish");
                    reconnectionTimer.cancel();
                    reconnectionTimer = null;
                }
            }
        }, reconnectionDelay, reconnectionPeriod);
    }

    public void messageArrived(Context context, String topic, MqttMessage message) {
        FlyveLog.d("- Topic: " + topic + "\n\n- Message: " + new String(message.getPayload()));

        FlyveLog.d("- Topic: " + topic + "\n\n- Message: " + new String(message.getPayload()));
        int priority = topic.contains("fleet") ? 0 : 1;

        String messageBody = new String(message.getPayload());
        PoliciesController policiesController = new PoliciesController(context, getMqttClient());

        Helpers.storeLog(Helpers.broadCastMessage("MQTT Message", "Body", messageBody));

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
                showDetailError(context, CommonErrorType.MQTT_PING, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_GEOLOCATE, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_INVENTORY, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_LOCK, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_WIPE, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_UNENROLL, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_SUBSCRIBE, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_RESETPASSWORD, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_USETLS, ex.getMessage());
            }
        }

        // Policy/passwordEnabled
        callPolicy(context, PasswordEnablePolicy.class, PasswordEnablePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordQuality
        callPolicy(context, PasswordQualityPolicy.class, PasswordQualityPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordMinLength
        callPolicy(context, PasswordMinLengthPolicy.class, PasswordMinLengthPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordMinLowerCase
        callPolicy(context, PasswordMinLowerCasePolicy.class, PasswordMinLowerCasePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordMinUpperCase
        callPolicy(context, PasswordMinUpperCasePolicy.class, PasswordMinUpperCasePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordMinNonLetter
        callPolicy(context, PasswordMinNonLetterPolicy.class, PasswordMinNonLetterPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordMinLetters
        callPolicy(context, PasswordMinLetterPolicy.class, PasswordMinLetterPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordMinNumeric
        callPolicy(context, PasswordMinNumericPolicy.class, PasswordMinNumericPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/passwordMinSymbols
        callPolicy(context, PasswordMinSymbolsPolicy.class, PasswordMinSymbolsPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/MaximumFailedPasswordsForWipe
        callPolicy(context, MaximumFailedPasswordForWipePolicy.class, MaximumFailedPasswordForWipePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/MaximumTimeToLock
        callPolicy(context, MaximumTimeToLockPolicy.class, MaximumTimeToLockPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/storageEncryption
        callPolicy(context, StorageEncryptionPolicy.class, StorageEncryptionPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableCamera
        callPolicy(context, CameraPolicy.class, CameraPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableBluetooth
        callPolicy(context, BluetoothPolicy.class, BluetoothPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableHostpotTethering
        callPolicy(context, HostpotTetheringPolicy.class, HostpotTetheringPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableRoaming
        callPolicy(context, RoamingPolicy.class, RoamingPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableWifi
        callPolicy(context, WifiPolicy.class, WifiPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableSpeakerphone
        callPolicy(context, SpeakerphonePolicy.class, SpeakerphonePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableUsbOnTheGo
        callPolicy(context, SMSPolicy.class, SMSPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableCreateVpnProfiles
        callPolicy(context, VPNPolicy.class, VPNPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStreamMusic
        callPolicy(context, StreamMusicPolicy.class, StreamMusicPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStreamRing
        callPolicy(context, StreamRingPolicy.class, StreamRingPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStreamAlarm
        callPolicy(context, StreamAlarmPolicy.class, StreamAlarmPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStreamNotification
        callPolicy(context, StreamNotificationPolicy.class, StreamNotificationPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStreamAccessibility
        callPolicy(context, StreamAccessibilityPolicy.class, StreamAccessibilityPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStreamVoiceCall
        callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStreamDTMF
        callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableScreenCapture
        //  ROOT REQUIRED
        callPolicy(context, ScreenCapturePolicy.class, ScreenCapturePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableAirplaneMode
        //  ROOT REQUIRED
        callPolicy(context, AirplaneModePolicy.class, AirplaneModePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableGPS
        //  ROOT REQUIRED
        callPolicy(context, GPSPolicy.class, GPSPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableMobileLine
        // ROOT
        callPolicy(context, MobileLinePolicy.class, MobileLinePolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableNfc
        // ROOT
        callPolicy(context, NFCPolicy.class, NFCPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableStatusBar
        // ROOT
        callPolicy(context, StatusBarPolicy.class, StatusBarPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableUsbMtp
        // ROOT
        callPolicy(context, UsbMtpPolicy.class, UsbMtpPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableUsbPtp
        // ROOT
        callPolicy(context, UsbPtpPolicy.class, UsbPtpPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/disableUsbAdb
        // ROOT
        callPolicy(context, UsbAdbPolicy.class, UsbAdbPolicy.POLICY_NAME, priority, topic, messageBody);

        // Policy/deployApp
        String DEPLOY_APP = "deployApp";
        if(topic.toLowerCase().contains(DEPLOY_APP.toLowerCase())) {
            MDMAgent.setMqttClient(getMqttClient());
            AppThreadManager manager = MDMAgent.getAppThreadManager();
            try {

                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has(DEPLOY_APP)) {
                    manager.add(context, jsonObj);
                    String deployApp = jsonObj.getString(DEPLOY_APP);
                    String taskId = jsonObj.getString("taskId");
                    String id = jsonObj.getString("id");
                    String versionCode = jsonObj.getString("versionCode");

                    // execute the policy
                    policiesController.installPackage(deployApp, id, versionCode, taskId);
                }
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_DEPLOYAPP, ex.getMessage());
                manager.finishProcess(context);
            }
        }

        // Policy/removeApp
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
                showDetailError(context, CommonErrorType.MQTT_REMOVEAPP, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_DEPLOYFILE, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_REMOVEFILE, ex.getMessage());
            }
        }
    }

    @Override
    public void showDetailError(Context context, int type, String message) {
        FlyveLog.e(context.getResources().getString(R.string.error_message_with_number, String.valueOf(type), message));
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
            Helpers.storeLog(Helpers.broadCastMessage(context.getString(R.string.mqtt_delivery), context.getString(R.string.response_id), String.valueOf(token.getMessageId())));
        } catch (Exception ex) {
            showDetailError(context, CommonErrorType.MQTT_DELIVERY_COMPLETE, ex.getMessage());
        }
    }

    public void callPolicy(Context context, Class<? extends BasePolicies> classPolicy, String policyName, int policyPriority, String topic, String messageBody) {
        if(topic.toLowerCase().contains(policyName.toLowerCase())) {

            BasePolicies policies;

            try {
                policies = classPolicy.getDeclaredConstructor(Context.class).newInstance(context);
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_CALLPOLICY_NEWINSTANCE, ex.getMessage());
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
                showDetailError(context, CommonErrorType.MQTT_CALLPOLICY_JSON_PARSE, ex.getMessage());
            }
        }
    }

    @Override
    public void connectionLost(Context context, MqttCallback callback, String message) {
        showDetailError(context, CommonErrorType.MQTT_CONNECTION_LOST, message);
        setStatus(context, callback, false);
    }

    private void setStatus(Context context, MqttCallback callback, Boolean isConnected){
        //send broadcast
        this.connected = isConnected;

        // reconnect
        if(!isConnected) {
            reconnect(context, callback);
        }

        AppData cache = new AppData(context);
        cache.setOnlineStatus(isConnected);

        Helpers.sendBroadcast(isConnected, Helpers.BROADCAST_STATUS, context);
    }
}
