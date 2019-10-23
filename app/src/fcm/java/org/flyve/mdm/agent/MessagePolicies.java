package org.flyve.mdm.agent;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.messaging.FirebaseMessaging;

import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.ApplicationData;
import org.flyve.mdm.agent.data.database.FileData;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.data.database.entity.Application;
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
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.ui.PoliciesAsyncTask;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.Inventory;
import org.flyve.policies.manager.AndroidPolicies;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @author    rafaelhernandez
 * @date      13/12/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

public class MessagePolicies {

    public static final int PING = 1;
    public static final int GEOLOCATE = 2;
    public static final int INVENTORY = 3;
    public static final int POLICIES = 4;

    public MessagePolicies() {

    }

    public void messageArrived(final Context context, String topic, String message) {

        // Delete policy information
        if(message.contains("default")) {
            try {
                String taskId = new JSONObject(message).getString("taskId");
                new PoliciesData(context).removeValue(taskId);
                FlyveLog.i("Deleting policy " + message + " - " + topic);
            } catch (Exception ex) {
                FlyveLog.e("fcm", "error deleting policy " + message + " - " + topic, ex.getMessage());
            }
            return;
        }

        //Command/Policies
        new PoliciesAsyncTask().execute(context,POLICIES, topic, message);


        // Command/Ping
        if(topic.toLowerCase().contains("ping")) {
            new PoliciesAsyncTask().execute(context,PING, topic,message);
        }

        // Command/Geolocate
        if(topic.toLowerCase().contains("geolocate")) {
            new PoliciesAsyncTask().execute(context,GEOLOCATE, topic,message);
        }

        // Command/Inventory
        if(topic.toLowerCase().contains("inventory")) {
            new PoliciesAsyncTask().execute(context,INVENTORY, topic,message);
        }

        // Command/Wipe
        if(topic.toLowerCase().contains("wipe")) {
            if(MDMAgent.isSecureVersion()) {
                MessagePolicies.sendStatusbyHttp(context, false);
                new AndroidPolicies(context, FlyveAdminReceiver.class).wipe();
            }
        }

        // Command/Unenroll
        if(topic.toLowerCase().contains("unenroll")) {
            MessagePolicies.sendStatusbyHttp(context, false);

            // Remove all the information
            new ApplicationData(context).deleteAll();
            new FileData(context).deleteAll();
            new MqttData(context).deleteAll();
            new PoliciesData(context).deleteAll();
        }

        if(topic.toLowerCase().contains("subscribe")) {
            try {
                JSONObject jsonMessage = new JSONObject(message);
                JSONArray jsonSubscribe = jsonMessage.getJSONArray("subscribe");
                String newTopic = jsonSubscribe.getJSONObject(0).getString("topic");

                if(!newTopic.contains("null")) {
                    FirebaseMessaging.getInstance().subscribeToTopic(newTopic);
                }
            } catch (Exception ex) {
                Helpers.storeLog("fcm", "subscribe fail", message);
            }
        }

    }

    public static void pluginHttpResponse(final Context context, final String url, final String data) {
        Helpers.storeLog("fcm", "http response payload", data);

        EnrollmentHelper enrollmentHelper = new EnrollmentHelper(context);
        enrollmentHelper.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                ConnectionHTTP.sendHttpResponse(context, url, data, sessionToken, new ConnectionHTTP.DataCallback() {
                    @Override
                    public void callback(String data) {
                        Helpers.storeLog("fcm", "http response from url", data);
                    }
                });
            }

            @Override
            public void onError(int type, String error) {
                Helpers.storeLog("fcm", "active session fail", data);
            }
        });

    }

    public static void sendTaskStatusbyHttp(final Context context,final String status, final String taskId ){
        EnrollmentHelper enrollmentHelper = new EnrollmentHelper(context);
        enrollmentHelper.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                Helpers.storeLog("fcm", "http response session token", sessionToken);
                String payload = "";
                try {
                    JSONObject jsonPayload = new JSONObject();
                    jsonPayload.put("status", status);

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("input", jsonPayload);

                    payload = jsonInput.toString();
                } catch (Exception ex) {
                    Helpers.storeLog("fcm", "Error sending status http", ex.getMessage());
                }

                ConnectionHTTP.sendHttpResponsePolicies(context, taskId, payload, sessionToken, new ConnectionHTTP.DataCallback() {
                    @Override
                    public void callback(String data) {
                        Helpers.storeLog("fcm", "http response from policy", data);
                    }
                });
            }

            @Override
            public void onError(int type, String error) {
                Helpers.storeLog("fcm", "problem with session token", error);
            }
        });
    }

    public static void sendStatusbyHttp(Context context, boolean status) {
        try {
            JSONObject jsonPayload = new JSONObject();

            jsonPayload.put("is_online", status);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("input", jsonPayload);

            String payload = jsonInput.toString();
            Routes routes = new Routes(context);
            MqttData cache = new MqttData(context);
            String url = routes.pluginFlyvemdmAgent(cache.getAgentId());
            pluginHttpResponse(context, url, payload);
        } catch (Exception ex) {
            Helpers.storeLog("fcm", "Error sending status http", ex.getMessage());
        }
    }


    public static void callPolicy(Context context, Class<? extends BasePolicies> classPolicy, String policyName, int policyPriority, String topic, String messageBody) {

        if(topic.toLowerCase().contains(policyName.toLowerCase())) {
            
            FlyveLog.d("Call policies "+messageBody);
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
                    policies.setParameters(topic, taskId, messageBody);
                    policies.setValue(value);
                    policies.setPriority(policyPriority);
                    policies.execute();
                }
            } catch (Exception ex) {
                FlyveLog.e("MessagePolicies " + ", callPolicy",ex.getMessage());
            }
        }
    }

    private void showDetailError(Context context, int ErrorType, String policy){
        FlyveLog.d(ErrorType + policy);
    }

}
