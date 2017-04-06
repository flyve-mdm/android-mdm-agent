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
 * @author    Thierry Bugier Pineau
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.mqtt;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.flyvemdm.MainApplication;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class MQTTAction {

    protected JSONArray mTaskFeedback;

    protected MQTTAction() {
        mTaskFeedback = new JSONArray();
    }

    protected void addTaskToFeedback(JSONObject policy, String status) {
        if (policy.has("taskId")) {
            JSONObject task = new JSONObject();
            try {
                task.put("taskId", policy.getInt("taskId"));
                task.put("status", status);
            } catch (JSONException e) {
                FlyveLog.e(e.getMessage());
            }
            mTaskFeedback.put(task);
        }
    }

    protected void sendTaskFeedback() {
        if (mTaskFeedback.length() < 1) {
            return;
        }

        JSONObject feedbackMessage = new JSONObject();
        try {
            feedbackMessage.put("updateStatus", mTaskFeedback);
        } catch (JSONException e) {
            FlyveLog.e(e.getMessage());
        }
        MainApplication application = MainApplication.getInstance();
        MqttAndroidClient client = application.getMQTTService().getClient();
        if (client != null) {
            String serialTopic = new SharedPreferenceMQTT().getSerialTopic(application.getBaseContext())[0];
            MqttMessage message = new MqttMessage(feedbackMessage.toString().getBytes());
            message.setQos(0);
            try {
                client.publish(serialTopic + "/Status/Task", message);
            } catch (MqttException e) {
                FlyveLog.e(e.getMessage());
            }
        }
    }
}
