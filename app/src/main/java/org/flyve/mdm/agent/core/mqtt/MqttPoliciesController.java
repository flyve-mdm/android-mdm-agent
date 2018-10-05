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

import android.app.Service;
import android.content.Context;
import android.location.Location;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.ApplicationData;
import org.flyve.mdm.agent.data.database.FileData;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.ui.LockActivity;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.services.PoliciesFiles;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.Inventory;
import org.flyve.policies.manager.AndroidPolicies;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MqttPoliciesController {

    private static final String ERROR = "ERROR";
    private static final String MQTT_SEND = "MQTT Send";
    private static final String UTF_8 = "UTF-8";

    private static final String FEEDBACK_PENDING = "pending";
    private static final String FEEDBACK_RECEIVED = "received";
    private static final String FEEDBACK_DONE = "done";
    private static final String FEEDBACK_FAILED = "failed";
    private static final String FEEDBACK_CANCELED = "canceled";
    private static final String FEEDBACK_WAITING = "waiting";

    private ArrayList<String> arrTopics;
    private MqttAndroidClient client;
    private Context context;
    private MqttData mqttData;
    private String mTopic;
    private AndroidPolicies androidPolicies;

    public MqttPoliciesController(Context context, MqttAndroidClient client) {
        this.client = client;
        this.context = context;
        mqttData = new MqttData(context);
        androidPolicies = new AndroidPolicies(context, FlyveAdminReceiver.class);
        mTopic = mqttData.getTopic();

        arrTopics = new ArrayList<>();
    }

    /**
     * Prevent duplicated topic and create String array with all the topic available
     * @param channel String new channel to add
     * @return String array
     */
    public String[] addTopic(String channel) {
        for(int i=0; i<arrTopics.size();i++) {
            if(channel.equalsIgnoreCase(arrTopics.get(i))) {
                return new String[0];
            }
        }
        arrTopics.add(channel);
        return arrTopics.toArray(new String[arrTopics.size()]);
    }

    /**
     * Add Manifest version of backend to local storage
     * @param json JSONObject with this format {"version":"2.0.0-dev"}
     */
    public void addManifest(JSONObject json) {
        try {
            String version = json.getString("version");
            mqttData.setManifestVersion(version);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", addManifest", ex.getMessage());
        }
    }

    /**
     * Subscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":"/2/fleet/22"}]}
     */
    public void subscribe(final String channel) {
        final String[] topics = addTopic(channel);

        // if topic null
        if(topics==null || topics.length == 0) {
            return;
        }

        int[] qos = new int[arrTopics.size()];

        try {
            for (int k = 0; k < qos.length; k++) {
                qos[k] = 0;
            }
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", subscribe", ex.getMessage());
        }

        try {
            IMqttToken subToken = client.subscribe(topics, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    FlyveLog.i("Subscribed topics: " + Arrays.toString(topics));
                    broadcastReceivedLog("TOPIC", "Subscribed", channel);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    String errorMessage = " unknown";
                    if(exception != null) {
                        errorMessage = exception.getMessage();
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
     * Create Device Inventory
     * Example {"query": "inventory"}
     */
    public void createInventory() {
        Inventory inventory = new Inventory();
        inventory.getXMLInventory(context, new InventoryTask.OnTaskCompleted() {
            @Override
            public void onTaskSuccess(String s) {
                // send inventory to MQTT
                sendInventory(s);
                broadcastReceivedLog(MQTT_SEND, "Inventory", "Inventory Send");
            }

            @Override
            public void onTaskError(Throwable throwable) {
                FlyveLog.e(this.getClass().getName() + ", createInventory", throwable.getMessage());
                //send broadcast
                broadcastReceivedLog(ERROR, "Error on createInventory", throwable.getMessage());
            }
        });
    }

    /**
     * TLS
     * Example "useTLS": "true|false", "taskId": "25"
     */
    public void useTLS(String taskId, Boolean enable) {
        try {
            if(enable) {
                mqttData.setTls("1");

                // stop service
                ((Service) context).stopSelf();

                // restart MQTT connection with this new parameters
                MQTTService.start(MDMAgent.getInstance());

                // return the status of the task
                sendTaskStatus(taskId, FEEDBACK_DONE);

            } else {
                mqttData.setTls("0");

                // stop service
                ((Service) context).stopSelf();

                // restart MQTT connection with this new parameters
                MQTTService.start(MDMAgent.getInstance());

                // return the status of the task
                sendTaskStatus(taskId, FEEDBACK_DONE);
            }
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", useTLS", ex.getMessage());

            // return the status of the task
            sendTaskStatus(taskId, FEEDBACK_FAILED);
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

    public void resetPassword(String newPassword) {
        try {
            if(!newPassword.isEmpty()) {
                androidPolicies.resetPassword(newPassword);
                broadcastReceivedLog(MQTT_SEND, "Reset Password", "Reset Password : ****");
            } else {
                broadcastReceivedLog(ERROR, "Error on Reset Password", "the new password is empty");
            }
        } catch (Exception ex) {
            broadcastReceivedLog(ERROR, "Error on Reset Password", ex.getMessage());
        }
    }

    public void removePackage(String taskId, String packageName) {
        try {
            PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);
            policiesFiles.removeApk(packageName.trim());

            // return the status of the task
            sendTaskStatus(taskId, FEEDBACK_DONE);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", removePackage", ex.getMessage());

            // return the status of the task
            sendTaskStatus(taskId, FEEDBACK_FAILED);
        }
    }

    /**
     * Application
     */
    public void installPackage(final String deployApp, final String id, final String versionCode, final String taskId) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                try {
                    FlyveLog.d("Install package: " + deployApp + " id: " + id);

                    PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);
                    policiesFiles.execute("package", deployApp, id, sessionToken);

                    broadcastReceivedLog(MQTT_SEND, "Install package", "name: " + deployApp + " id: " + id);

                    // return the status of the task
                    sendTaskStatus(taskId, FEEDBACK_RECEIVED);
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", installPackage", ex.getMessage());
                    broadcastReceivedLog(ERROR, "Error on getActiveSessionToken", ex.getMessage());

                    // return the status of the task
                    sendTaskStatus(taskId, FEEDBACK_FAILED);
                }
            }

            @Override
            public void onError(int type, String error) {
                FlyveLog.e(this.getClass().getName() + ", installPackage", error);
                broadcastReceivedLog(String.valueOf(type), ERROR, error);

                // return the status of the task
                sendTaskStatus(taskId, FEEDBACK_FAILED);
            }
        });


    }

    /**
     * Files
     * {"file":[{"deployFile":"%SDCARD%/","id":"1","version":"1","taskId":"1"}]}
     */
    public void downloadFile(final String deployFile, final String id, final String versionCode, final String taskId) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);

                if("true".equals(policiesFiles.execute("file", deployFile, id, sessionToken))) {
                    FlyveLog.d("File was stored on: " + deployFile);
                    broadcastReceivedLog(MQTT_SEND, "File was stored on", deployFile);

                    // return the status of the task
                    sendTaskStatus(taskId, FEEDBACK_DONE);
                }
            }

            @Override
            public void onError(int type, String error) {
                FlyveLog.e(this.getClass().getName() + ", downloadFile", error);
                broadcastReceivedLog(String.valueOf(type), "Error on applicationOnDevices", error);

                // return the status of the task
                sendTaskStatus(taskId, FEEDBACK_FAILED);
            }
        });
    }

    public void removeFile(String taskId, String removeFile) {
        try {
            PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);
            policiesFiles.removeFile(removeFile);

            FlyveLog.d("Remove file: " + removeFile);
            broadcastReceivedLog(MQTT_SEND, "Remove file", removeFile);

            // return the status of the task
            sendTaskStatus(taskId, FEEDBACK_DONE);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", removeFile", ex.getMessage());

            // return the status of the task
            sendTaskStatus(taskId, FEEDBACK_FAILED);
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
     * Send PING to the MQTT server
     * payload: !
     */
    public void sendKeepAlive() {
        String topic = mTopic + "/Status/Ping";
        String payload = "!";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(MQTT_SEND, "PING", "ID: " + token.getMessageId());
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", sendKeepAlive", ex.getMessage());
            broadcastReceivedLog(ERROR, "Error on sendKeepAlive", ex.getMessage());
        }
    }

    /**
     * Send INVENTORY to the MQTT server
     * payload: XML FusionInventory
     */
    public void sendInventory(String payload) {
        String topic = mTopic + "/Status/Inventory";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);

            // send broadcast
            broadcastReceivedLog(MQTT_SEND, "Send Inventory", "ID: " + token.getMessageId());
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", sendInventory", ex.getMessage());

            // send broadcast
            broadcastReceivedLog(ERROR, "Error on sendKeepAlive", ex.getMessage());
        }
    }

    public void sendTaskStatus(String taskId, String status) {
        String topic = mTopic + "/Status/Task/" + taskId;
        byte[] encodedPayload;
        try {
            String payload = "{ \"status\": \"" + status + "\" }";

            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);

            // send broadcast
            broadcastReceivedLog(MQTT_SEND, "Send Inventory", "ID: " + token.getMessageId());
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", sendTaskStatus", ex.getMessage());

            // send broadcast
            broadcastReceivedLog(ERROR, "Error on sendKeepAlive", ex.getMessage());
        }
    }

    /**
     * Send the Status version of the agent
     * payload: {"version": "0.99.0"}
     */
    public void sendStatusVersion() {
        String topic = mTopic + "/FlyvemdmManifest/Status/Version";
        String payload = "{\"version\":\"" + BuildConfig.VERSION_NAME + "\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(MQTT_SEND, "Send Status Version", "ID: " + token.getMessageId());
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", sendStatusVersion",ex.getMessage());
            broadcastReceivedLog(ERROR, "Error on sendStatusVersion", ex.getMessage());
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
     * Send the GPS information to MQTT
     * payload: {"latitude":"10.2485486","longitude":"-67.5904498","datetime":1499364642}
     */
    public void sendGPS() {

        FastLocationProvider fastLocationProvider = new FastLocationProvider();
        Boolean isAvailable = fastLocationProvider.getLocation(context, new FastLocationProvider.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                String topic = mTopic + "/Status/Geolocation";

                if(location == null) {
                    // if the GPS not response then send this character ?
                    FlyveLog.e(this.getClass().getName() + ", sendGPS", "without location yet...");
                    try {
                        byte[] encodedPayload;
                        String payload = "{\"datetime\":" + Helpers.getUnixTime() + ",\"gps\":\"off\"}";
                        encodedPayload = payload.getBytes(UTF_8);
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(topic, message);
                    } catch (Exception ex) {
                        FlyveLog.e(this.getClass().getName() + ", sendGPS", "Fail sending the ? payload");
                    }

                } else {
                    FlyveLog.d("lat: " + location.getLatitude() + " lon: " + location.getLongitude());

                    try {
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());

                        JSONObject jsonGPS = new JSONObject();

                        jsonGPS.put("latitude", latitude);
                        jsonGPS.put("longitude", longitude);
                        jsonGPS.put("datetime", Helpers.getUnixTime());

                        String payload = jsonGPS.toString();
                        byte[] encodedPayload;

                        encodedPayload = payload.getBytes(UTF_8);
                        MqttMessage message = new MqttMessage(encodedPayload);
                        IMqttDeliveryToken token = client.publish(topic, message);

                        // send broadcast
                        broadcastReceivedLog(MQTT_SEND, "Sended Geolocation", "ID: " + token.getMessageId());
                    } catch (Exception ex) {
                        FlyveLog.e(this.getClass().getName() + ", sendGPS", ex.getMessage());
                        broadcastReceivedLog(ERROR, "Error on GPS location", ex.getMessage());
                    }
                }
            }
        });

        if(!isAvailable) {
            String topic = mTopic + "/Status/Geolocation";
            try {
                byte[] encodedPayload;
                String payload = "{\"datetime\":" + Helpers.getUnixTime() + ",\"gps\":\"off\"}";
                encodedPayload = payload.getBytes(UTF_8);
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic, message);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", sendGPS", "Fail sending the ? payload");
            }

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
