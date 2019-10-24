package org.flyve.mdm.agent;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessaging;

import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.ApplicationData;
import org.flyve.mdm.agent.data.database.FileData;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.policies.PoliciesAsyncTask;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
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

    public MessagePolicies() {

    }

    public void messageArrived(final Context context, String topic, String message) {

        // Delete policy information if message contains default
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
        new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.POLICIES, topic, message);

        // Command/Ping
        if(topic.toLowerCase().contains("ping")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.PING, topic,message);
        }

        // Command/Geolocate
        if(topic.toLowerCase().contains("geolocate")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.GEOLOCATE, topic,message);
        }

        // Command/Inventory
        if(topic.toLowerCase().contains("inventory")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.INVENTORY, topic,message);
        }

        // Command/Wipe
        if(topic.toLowerCase().contains("wipe")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.WIPE, topic,message);
        }

        // Command/Lock
        if(topic.toLowerCase().contains("lock")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.LOCK, topic,message);
        }

        // Command/Unenroll
        if(topic.toLowerCase().contains("unenroll")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.UNENROLL, topic,message);
        }
    }

}
