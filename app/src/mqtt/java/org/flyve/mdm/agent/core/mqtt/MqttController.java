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
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.TopicsData;
import org.flyve.mdm.agent.data.database.entity.Topics;
import org.flyve.mdm.agent.data.database.setup.AppDataBase;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.utils.FlyveLog;
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
     * Unsubscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":null}]}
     */
    public void unsubscribe() {
        final AppDataBase dataBase = AppDataBase.getAppDatabase(context);
        List<Topics> topics = dataBase.TopicsDao().getFleets();
        if(!topics.isEmpty()) {

            for (int i = 0; i < topics.size(); i++) {
                final Topics topicToUnsubscribe = topics.get(i);
                try {
                    IMqttToken subToken = client.unsubscribe(topicToUnsubscribe.topic);
                    subToken.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            FlyveLog.d("Unsubscribe from fleet "+topicToUnsubscribe.topic);
                            dataBase.TopicsDao().delete(topicToUnsubscribe);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken,
                                              Throwable exception) {
                            // The unsubscription could not be performed, maybe the user was not
                            // authorized to subscribe on the specified topic e.g. using wildcards
                            String errorMessage = " unknown";
                            if(exception != null) {
                                errorMessage = exception.getMessage();
                                Log.d("Unsubscribe","Error", exception);
                            }
                            FlyveLog.e(this.getClass().getName() + ", unsubscribe", "ERROR on unsubscribe: " + errorMessage);

                            broadcastReceivedLog(ERROR, "Error on unsubscribe", errorMessage);
                        }
                    });


                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", unsubscribe", ex.getMessage());
                }
            }



        }


    }

    /**
     * Subscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":"/2/fleet/22"}]}
     */
    public void subscribe(final String channel) {
        if(channel == null || channel.contains("null")) {
            //case of unsubscribe
            AppDataBase dataBase = AppDataBase.getAppDatabase(context);
            dataBase.TopicsDao().deleteFleets();
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
                        FlyveLog.d("Subscribe from fleet "+topic);
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
     * Broadcast the received log
     * @param message
     */
    private void broadcastReceivedLog(String type, String title, String message){
        // write log file
        FlyveLog.f(type, title, message);
    }

}
