package org.flyve.mdm.agent.policies;

/*
 *   Copyright  2018 Teclib. All rights reserved.
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
 * @author    rafael hernandez
 * @date      15/5/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.data.database.entity.Policies;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONObject;

public abstract class BasePolicies {

    private static final String ERROR = "ERROR";
    private static final String MQTT_SEND = "MQTT Send";

    private static final String MQTT_FEEDBACK_PENDING = "pending";
    private static final String MQTT_FEEDBACK_RECEIVED = "received";
    private static final String MQTT_FEEDBACK_DONE = "done";
    private static final String MQTT_FEEDBACK_FAILED = "failed";
    private static final String MQTT_FEEDBACK_CANCELED = "canceled";
    private static final String MQTT_FEEDBACK_WAITING = "waiting";

    private boolean enableLog;
    protected Context context;
    protected String policyName;
    protected PoliciesData data;
    protected Object policyValue;
    protected int policyPriority;

    private MqttAndroidClient mqttClient;
    private boolean mqttEnable;
    private String mqttTopic;
    private String mqttTaskId;

    public BasePolicies(Context context, String name) {
        this.context = context;
        this.policyName = name;
        this.data = new PoliciesData(context);
        this.mqttEnable = true;

        Policies policies = data.getValue(this.policyName);
        if(policies!=null) {
            this.policyValue = policies.value;
            this.policyPriority = policies.priority;
        }
    }

    public void setMqttEnable(boolean enable) {
        this.mqttEnable = enable;
    }

    public void setValue(Object value) {
        this.policyValue = value;
        storage();
    }

    public void setPriority(int priority) {
        this.policyPriority = priority;
        storage();
    }

    public void setLog(Boolean enable) {
        this.enableLog = enable;
    }

    public void setMQTTparameters(MqttAndroidClient client, String topic, String taskId) {
        this.mqttClient = client;
        this.mqttTopic = topic;
        this.mqttTaskId = taskId;
    }

    private void storage()  {
        if(!policyName.isEmpty() && !this.policyValue.toString().isEmpty() && !this.mqttTaskId.isEmpty()) {
            data.setValue(this.policyName, this.mqttTaskId, String.valueOf(this.policyValue), this.policyPriority);
        }
    }

    public void remove() {
        if(!this.mqttTaskId.isEmpty()) {
            data.removeValue(this.mqttTaskId);
        }
    }

    private void sendTaskStatus(String topic, final String taskId, final String status) {
        EnrollmentHelper enrollmentHelper = new EnrollmentHelper(context);
        enrollmentHelper.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                Helpers.storeLog("mqtt", "http response session token", sessionToken);
                String payload = "";
                try {
                    JSONObject jsonPayload = new JSONObject();

                    jsonPayload.put("status", status);

                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("input", jsonPayload);

                    payload = jsonInput.toString();
                } catch (Exception ex) {
                    Helpers.storeLog("mqtt", "Error sending status http", ex.getMessage());
                }

                ConnectionHTTP.sendHttpResponsePolicies(context, taskId, payload, sessionToken, new ConnectionHTTP.DataCallback() {
                    @Override
                    public void callback(String data) {
                        Helpers.storeLog("mqtt", "http response from policy", data);
                    }
                });
            }

            @Override
            public void onError(int type, String error) {
                Helpers.storeLog("mqtt", "problem with session token", error);
            }
        });
    }

    private void mqttResponse(String status) {
        this.sendTaskStatus(this.mqttTopic, this.mqttTaskId, status);
    }

    protected void Log(String type, String title, String message){
        // write log file
        if(enableLog) {
            FlyveLog.f(type, title, message);
        }
    }

    private void validate() throws PoliciesException {
        if(this.policyName.isEmpty()){
            throw new PoliciesException("provide a name for the policy");
        }

        if(this.policyValue.toString().isEmpty()){
            throw new PoliciesException("provide a value for the policy");
        }

        if(mqttEnable) {
            if(this.mqttClient == null) {
                throw new PoliciesException("provide a MQTT client");
            }
            if(this.mqttTopic.isEmpty()){
                throw new PoliciesException("provide a MQTT topic");
            }
            if(this.mqttTaskId.isEmpty()) {
                throw new PoliciesException("provide a MQTT Task id");
            }
        }
    }

    public void execute() throws PoliciesException {
        validate();
        Log("Execute Policy", "Policy " + this.policyName, "Start the policy: " + this.policyName + "\nvalue: " + this.policyValue + "\npriority:" + this.policyPriority);
        boolean status = process();
        if(status) {
            policyDone();
        } else {
            policyFail();
        }
    }

    protected void policyDone() {
        mqttResponse(MQTT_FEEDBACK_DONE);
    }

    protected void policyFail() {
        Log("Policy ERROR", "Policy " + this.policyName,"Policy Fail: " + this.policyName + "\nvalue: " + this.policyValue + "\npriority: " + this.policyPriority);
        mqttResponse(MQTT_FEEDBACK_FAILED);
    }

    protected abstract boolean process();
}
