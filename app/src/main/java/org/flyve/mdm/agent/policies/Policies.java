package org.flyve.mdm.agent.policies;

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
 * @author    rafael hernandez
 * @date      15/5/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.data.PoliciesDataNew;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public abstract class Policies {

    private static final String ERROR = "ERROR";
    private static final String MQTT_SEND = "MQTT Send";

    private boolean enableLog;
    protected Context context;
    protected String policyName;
    protected PoliciesDataNew data;
    private MqttAndroidClient mqttClient;
    protected Object policyValue;
    protected int policyPriority;
    private boolean mqttEnable;

    public Policies(Context context, String name) {
        this.context = context;
        this.policyName = name;
        this.data = new PoliciesDataNew(context);
        org.flyve.mdm.agent.room.entity.Policies policies = data.getValue(this.policyName);
        this.policyValue = policies.value;
        this.policyPriority = policies.priority;
    }

    public void setMqttEnable(boolean enable) {
        this.mqttEnable = enable;
    }

    public void setPolicyName(String name) {
        this.policyName = name;
        storage();
    }

    public void setValue(String value) {
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

    public void storage()  {
        if(!policyName.isEmpty()) {
            data.setValue(this.policyName, String.valueOf(this.policyValue), this.policyPriority);
        }
    }

    public void mqttSendTaskStatus(String mqttTopic, String taskId, String status) {
        String topic = mqttTopic + "/Status/Task/" + taskId;
        byte[] encodedPayload;
        try {
            String payload = "{ \"status\": \"" + status + "\" }";

            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = this.mqttClient.publish(topic, message);

            // send broadcast
            Log(Helpers.broadCastMessage(MQTT_SEND, "Send Inventory", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // send broadcast
            Log(Helpers.broadCastMessage(ERROR, "Error on sendKeepAlive", ex.getMessage()));
        }
    }

    public void mqttResponse(MqttAndroidClient client, String mqttTopic, String taskId, String status) {
        if(mqttEnable) {
            this.setMQTTclient(client);
            this.mqttSendTaskStatus(mqttTopic, taskId, status);
        }
    }

    public void setMQTTclient(MqttAndroidClient client) {
        this.mqttClient = client;
    }

    protected void Log(String message){
        // write log file
        if(enableLog) {
            FlyveLog.f(message, FlyveLog.FILE_NAME_LOG);
        }
    }

    private void validate() throws PoliciesException {
        if(this.policyName.isEmpty()){
            throw new PoliciesException("provide a name for the policy");
        }

        if(this.policyValue.toString().isEmpty()){
            throw new PoliciesException("provide a value for the policy");
        }
    }

    public void execute(PolicyCallback policyCallback) throws PoliciesException {
        validate();
        Log("Start the policy: " + this.policyName + " value: " + this.policyValue + " priority: " + this.policyPriority);
        process(policyCallback);
    }

    protected abstract void process(PolicyCallback policyCallback);

    public interface PolicyCallback {
        void onSuccess();
        void onFail(String error);
    }
}
