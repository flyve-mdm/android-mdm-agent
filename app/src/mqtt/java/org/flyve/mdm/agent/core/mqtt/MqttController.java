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
import android.util.Log;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.data.database.ApplicationData;
import org.flyve.mdm.agent.data.database.FileData;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.data.database.TopicsData;
import org.flyve.mdm.agent.data.database.entity.Topics;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.ui.LockActivity;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.policies.manager.AndroidPolicies;

import java.util.ArrayList;
import java.util.List;

public class MqttController {

    private static final String ERROR = "ERROR";
    private static final String MQTT_SEND = "MQTT Send";
    private static final String UTF_8 = "UTF-8";

    private ArrayList<String> arrTopics;
    private MqttAndroidClient client;
    private Context context;
    private MqttData mqttData;
    private String mTopic;
    private AndroidPolicies androidPolicies;

    private String url;


    public MqttController(Context context, MqttAndroidClient client) {
        this.client = client;
        this.context = context;
        mqttData = new MqttData(context);
        androidPolicies = new AndroidPolicies(context, FlyveAdminReceiver.class);
        mTopic = mqttData.getTopic();

        arrTopics = new ArrayList<>();

        Routes routes = new Routes(context);
        MqttData cache = new MqttData(context);
        url = routes.pluginFlyvemdmAgent(cache.getAgentId());
    }


    /**
     * Subscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":"/2/fleet/22"}]}
     */
    public void subscribe(final String channel) {
        if(channel == null || channel.contains("null")) {
            return;
        }

        final List<Topics> topics = new TopicsData(context).setValue(channel, 0);

        // if topic null
        if(topics==null || topics.isEmpty()) {
            return;
        }

        //use List instead array because we need to keep only topic with status 0
        List<String> lstTopics = new ArrayList<>();
        List<Integer> lstQos = new ArrayList<>();

        for(int i=0; i< topics.size(); i++) {
            // if not subscribed
            if(topics.get(i).status != 1) {
                lstTopics.add(topics.get(i).topic);
                lstQos.add(topics.get(i).qos);
            }
        }

        try {


            String[] arrayTopics;
            arrayTopics = lstTopics.toArray(new String[lstTopics.size()]);

            //transform list of Integer to array of int 'primitive'
            int size = lstQos.size();
            int[] arrayQos = new int[size];
            Integer[] temp = lstQos.toArray(new Integer[size]);
            for (int n = 0; n < size; ++n) {
                arrayQos[n] = temp[n];
            }

            IMqttToken subToken = client.subscribe(arrayTopics, arrayQos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    for(String topic : asyncActionToken.getTopics()) {
                        new TopicsData(context).setStatusTopic(topic, 1);
                        broadcastReceivedLog(" -> " + topic, "Subscribed", String.valueOf(asyncActionToken.getTopics().length));
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    String errorMessage = " unknown";
                    if(exception != null) {
                        errorMessage = exception.getMessage();
                        Log.d("Subscribe","Error", exception);
                    }
                    FlyveLog.e(this.getClass().getName() + ", subscribe", "ERROR on subscribe: " + errorMessage);

                    broadcastReceivedLog(ERROR, "Error on subscribe", errorMessage);
                }
            });
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", subscribe", ex.getMessage());
        }
    }


    /**
     * Lock Device
     * Example {"lock":"now|unlock"}
     */
    public void lockDevice(Boolean lock) {

        if(MDMAgent.isSecureVersion()) {
            return;
        }

        try {


            if(lock) {
                androidPolicies.lockScreen(LockActivity.class);
                androidPolicies.lockDevice();
                broadcastReceivedLog(MQTT_SEND, "Lock", "Device Lock");
            } else {
                Helpers.sendBroadcast("unlock", "org.flyvemdm.finishlock", context);
            }
        } catch (Exception ex) {
            broadcastReceivedLog(ERROR, "Error on lockDevice", ex.getMessage());
            FlyveLog.e(this.getClass().getName() + ", lockDevice", ex.getCause().getMessage());
        }
    }

    /**
     * Erase all device data include SDCard
     */
    public void wipe() {
        if(MDMAgent.isSecureVersion()) {
            return;
        }

        try {
            androidPolicies.wipe();
            broadcastReceivedLog(MQTT_SEND, "Wipe", "Wipe success");
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", wipe", ex.getMessage());
            broadcastReceivedLog(ERROR, "Error on wipe", ex.getMessage());
        }
    }

    /**
     * Unenroll the device
     */
    public boolean unenroll() {
        if(MDMAgent.isSecureVersion()) {
            return false;
        }

        // Send message with unenroll
        String topic = mTopic + "/Status/Unenroll";
        String payload = "{\"unenroll\": \"unenrolled\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            broadcastReceivedLog(MQTT_SEND, "Unenroll", "Unenroll success");

            // clear cache
            mqttData.deleteAll();

            // Remove all the information
            new ApplicationData(context).deleteAll();
            new FileData(context).deleteAll();
            new MqttData(context).deleteAll();
            new PoliciesData(context).deleteAll();

            // send message
            Helpers.sendBroadcast(Helpers.broadCastMessage("action", "open", "splash"), Helpers.BROADCAST_MSG, this.context);

            // show offline
            Helpers.sendBroadcast(false, Helpers.BROADCAST_STATUS, this.context);

            return true;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", unenroll", ex.getMessage());
            broadcastReceivedLog(ERROR, "Error on unenroll", ex.getMessage());
            return false;
        }
    }

    /**
     * Send the Status version of the agent
     * payload: {"online": true}
     */
    public void sendOnlineStatus(Boolean status) {
        String topic = mTopic + "/Status/Online";
        String payload = "{\"online\": " + Boolean.toString( status ) + "}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(MQTT_SEND, "Send Online Status", "ID: " + token.getMessageId());
        } catch (Exception ex) {
            broadcastReceivedLog(ERROR, "Error on sendStatusVersion", ex.getMessage());
        }
    }


    /**
     * Broadcast the received log
     * @param message
     */
    private void broadcastReceivedLog(String type, String title, String message){
        // write log file
        FlyveLog.f(type, title, message);
    }

}
