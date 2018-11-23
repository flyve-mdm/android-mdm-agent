package org.flyve.mdm.agent.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
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
import org.flyve.mdm.agent.utils.AppThreadManager;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

public class PushPoliciesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_policies);

        String policy = getIntent().getStringExtra("policy");
        String topic = getIntent().getStringExtra("topic");
        String taskId = getIntent().getStringExtra("taskId");

        messageArrived(PushPoliciesActivity.this, topic, policy);

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PushPoliciesActivity.this.finish();
            }
        });
    }

    private void showDetailError(Context context, int ErrorType, String policy){
        FlyveLog.d(ErrorType + policy);
    }

    private void messageArrived(Context context, String topic, String policy) {
        int priority = 1;

        // Policy/passwordEnabled
        callPolicy(context, PasswordEnablePolicy.class, PasswordEnablePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordQuality
        callPolicy(context, PasswordQualityPolicy.class, PasswordQualityPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordMinLength
        callPolicy(context, PasswordMinLengthPolicy.class, PasswordMinLengthPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordMinLowerCase
        callPolicy(context, PasswordMinLowerCasePolicy.class, PasswordMinLowerCasePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordMinUpperCase
        callPolicy(context, PasswordMinUpperCasePolicy.class, PasswordMinUpperCasePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordMinNonLetter
        callPolicy(context, PasswordMinNonLetterPolicy.class, PasswordMinNonLetterPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordMinLetters
        callPolicy(context, PasswordMinLetterPolicy.class, PasswordMinLetterPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordMinNumeric
        callPolicy(context, PasswordMinNumericPolicy.class, PasswordMinNumericPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/passwordMinSymbols
        callPolicy(context, PasswordMinSymbolsPolicy.class, PasswordMinSymbolsPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/MaximumFailedPasswordsForWipe
        callPolicy(context, MaximumFailedPasswordForWipePolicy.class, MaximumFailedPasswordForWipePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/MaximumTimeToLock
        callPolicy(context, MaximumTimeToLockPolicy.class, MaximumTimeToLockPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/storageEncryption
        callPolicy(context, StorageEncryptionPolicy.class, StorageEncryptionPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableCamera
        callPolicy(context, CameraPolicy.class, CameraPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableBluetooth
        callPolicy(context, BluetoothPolicy.class, BluetoothPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableHostpotTethering
        callPolicy(context, HostpotTetheringPolicy.class, HostpotTetheringPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableRoaming
        callPolicy(context, RoamingPolicy.class, RoamingPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableWifi
        callPolicy(context, WifiPolicy.class, WifiPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableSpeakerphone
        callPolicy(context, SpeakerphonePolicy.class, SpeakerphonePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableUsbOnTheGo
        callPolicy(context, SMSPolicy.class, SMSPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableCreateVpnProfiles
        callPolicy(context, VPNPolicy.class, VPNPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStreamMusic
        callPolicy(context, StreamMusicPolicy.class, StreamMusicPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStreamRing
        callPolicy(context, StreamRingPolicy.class, StreamRingPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStreamAlarm
        callPolicy(context, StreamAlarmPolicy.class, StreamAlarmPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStreamNotification
        callPolicy(context, StreamNotificationPolicy.class, StreamNotificationPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStreamAccessibility
        callPolicy(context, StreamAccessibilityPolicy.class, StreamAccessibilityPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStreamVoiceCall
        callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStreamDTMF
        callPolicy(context, StreamVoiceCallPolicy.class, StreamVoiceCallPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableScreenCapture
        //  ROOT REQUIRED
        callPolicy(context, ScreenCapturePolicy.class, ScreenCapturePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableAirplaneMode
        //  ROOT REQUIRED
        callPolicy(context, AirplaneModePolicy.class, AirplaneModePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableGPS
        //  ROOT REQUIRED
        callPolicy(context, GPSPolicy.class, GPSPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableMobileLine
        // ROOT
        callPolicy(context, MobileLinePolicy.class, MobileLinePolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableNfc
        // ROOT
        callPolicy(context, NFCPolicy.class, NFCPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableStatusBar
        // ROOT
        callPolicy(context, StatusBarPolicy.class, StatusBarPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableUsbMtp
        // ROOT
        callPolicy(context, UsbMtpPolicy.class, UsbMtpPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableUsbPtp
        // ROOT
        callPolicy(context, UsbPtpPolicy.class, UsbPtpPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/disableUsbAdb
        // ROOT
        callPolicy(context, UsbAdbPolicy.class, UsbAdbPolicy.POLICY_NAME, priority, topic, policy);

        // Policy/deployApp
        String DEPLOY_APP = "deployApp";
        if(topic.toLowerCase().contains(DEPLOY_APP.toLowerCase())) {
            //MDMAgent.setMqttClient(getMqttClient());
            AppThreadManager manager = MDMAgent.getAppThreadManager();
            try {

                JSONObject jsonObj = new JSONObject(policy);

                if(jsonObj.has(DEPLOY_APP)) {
                    manager.add(context, jsonObj);
                }
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_DEPLOYAPP, ex.getMessage());
                manager.finishProcess(context);
            }
        }

        // Policy/deployApp
        String REMOVE_APP = "removeApp";
        if(topic.toLowerCase().contains(REMOVE_APP.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(policy);

                if(jsonObj.has(REMOVE_APP)) {
                    String removeApp = jsonObj.getString(REMOVE_APP);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    //mqttPoliciesController.removePackage(taskId, removeApp);
                }
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_REMOVEAPP, ex.getMessage());
            }
        }

        // Policy/deployFile
        String DEPLOY_FILE = "deployFile";
        if(topic.toLowerCase().contains(DEPLOY_FILE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(policy);

                if(jsonObj.has(DEPLOY_FILE)) {
                    String deployFile = jsonObj.getString(DEPLOY_FILE);
                    String id = jsonObj.getString("id");
                    String versionCode = jsonObj.getString("version");
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    //mqttPoliciesController.downloadFile(deployFile, id, versionCode, taskId);
                }
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_DEPLOYFILE, ex.getMessage());
            }
        }

        // Policy/deployFile
        String REMOVE_FILE = "removeFile";
        if(topic.toLowerCase().contains(REMOVE_FILE.toLowerCase())) {
            try {
                JSONObject jsonObj = new JSONObject(policy);

                if(jsonObj.has(REMOVE_FILE)) {
                    String removeFile = jsonObj.getString(REMOVE_FILE);
                    String taskId = jsonObj.getString("taskId");

                    // execute the policy
                    //mqttPoliciesController.removeFile(taskId, removeFile);
                }
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_REMOVEFILE, ex.getMessage());
            }
        }


    }

    private void callPolicy(Context context, Class<? extends BasePolicies> classPolicy, String policyName, int policyPriority, String topic, String messageBody) {
        if(topic.toLowerCase().contains(policyName.toLowerCase())) {

            BasePolicies policies;

            try {
                policies = classPolicy.getDeclaredConstructor(Context.class).newInstance(context);
            } catch (Exception ex) {
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
                    policies.setMqttEnable(false);
                    policies.setValue(value);
                    policies.setPriority(policyPriority);
                    policies.execute();
                }
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", callPolicy",ex.getMessage());
            }
        }
    }

}
