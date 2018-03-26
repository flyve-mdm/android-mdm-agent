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

package org.flyve.mdm.agent.services;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.os.Build;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.MqttData;
import org.flyve.mdm.agent.data.PoliciesData;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.FastLocationProvider;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class PoliciesController {

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
    private PoliciesData cache;
    private MqttData mqttCache;
    private String mTopic;

    public PoliciesController(Context context, MqttAndroidClient client) {
        this.client = client;
        this.context = context;
        cache = new PoliciesData(context);
        mqttCache = new MqttData(context);
        mTopic = mqttCache.getTopic();
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
            mqttCache.setManifestVersion(version);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Subscribe to the topic
     * When come from MQTT has a format like this {"subscribe":[{"topic":"/2/fleet/22"}]}
     */
    public void subscribe(final String channel) {
        String[] topics = addTopic(channel);

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
            FlyveLog.e(ex.getMessage());
        }

        String str = Arrays.toString(topics);
        FlyveLog.i("Topics: " + str + " Qos: " + qos.length);

        try {
            IMqttToken subToken = client.subscribe(topics, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    FlyveLog.d("Subscribed");
                    broadcastReceivedLog(Helpers.broadCastMessage("TOPIC", "Subscribed", channel));
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
                    FlyveLog.e("ERROR on subscribe: " + errorMessage);
                    broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on subscribe", errorMessage));
                }
            });
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Create Device Inventory
     * Example {"query": "inventory"}
     */
    public void createInventory() {
        InventoryTask inventoryTask = new InventoryTask(this.context, "FlyveMDM-Agent_v1.0");
        inventoryTask.getXML(new InventoryTask.OnTaskCompleted() {
            @Override
            public void onTaskSuccess(String data) {
                FlyveLog.xml(data);

                // send inventory to MQTT
                sendInventory(data);

                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Inventory", "Inventory Send"));
            }

            @Override
            public void onTaskError(Throwable error) {
                FlyveLog.e(error.getMessage());
                //send broadcast
                broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on createInventory", error.getMessage()));
            }
        });
    }

    /**
     * TLS
     * Example "useTLS": "true|false", "taskId": "25"
     */
    public void useTLS(Boolean enable) {
        try {
            MqttData cache = new MqttData(context);

            if(enable) {
                cache.setTls("1");

                // stop service
                ((Service) context).stopSelf();

                // restart MQTT connection with this new parameters
                MQTTService.start(MDMAgent.getInstance());
            } else {
                cache.setTls("0");

                // stop service
                ((Service) context).stopSelf();

                // restart MQTT connection with this new parameters
                MQTTService.start(MDMAgent.getInstance());
            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Lock Device
     * Example {"lock":"now|unlock"}
     */
    public void lockDevice(Boolean lock) {
        try {
            PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);

            if(lock) {
                mdm.lockScreen();
                mdm.lockDevice();
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Lock", "Device Lock"));
            } else {
                Helpers.sendBroadcast("unlock", "org.flyvemdm.finishlock", context);
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on lockDevice", ex.getMessage()));
            FlyveLog.e(ex.getCause().getMessage());
        }
    }

    /**
     * FLEET Camera
     * Example {"camera":[{"disableCamera":"true"}]}
     */
    public void disableCamera(Boolean disable) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.disableCamera(disable);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Camera", "Camera is disable: " + disable));
    }

    public void disableUI(JSONObject json) {
        try {
            JSONArray jsonConnectivities = json.getJSONArray("connectivity");
            for (int i = 0; i <= jsonConnectivities.length(); i++) {
                JSONObject jsonConnectivity = jsonConnectivities.getJSONObject(i);

                if (jsonConnectivity.has("disableScreenCapture")) {
                    boolean disable = jsonConnectivity.getBoolean("disableScreenCapture");
                    new PoliciesDeviceManager(context).disableCaptureScreen(disable);
                }

                if (jsonConnectivity.has("disableStatusBar")) {
                    boolean disable = jsonConnectivity.getBoolean("disableStatusBar");
                    new PoliciesDeviceManager(context).disableCaptureScreen(disable);
                }

            }
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    public void disableBluetooth(boolean disable) {
        try {
            cache.setConnectivityBluetoothDisable(disable);
            PoliciesConnectivity.disableBluetooth(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Bluetooth", "Bluetooth is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on Bluetooth", ex.getMessage()));
        }
    }

    public void disableWifi(boolean disable) {
        try {
            cache.setConnectivityWifiDisable(disable);
            PoliciesConnectivity.disableWifi(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Wifi", "Wifi is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on Wifi", ex.getMessage()));
        }
    }

    public void disableGPS(boolean disable) {
        try {
            cache.setConnectivityGPSDisable(disable);
            PoliciesConnectivity.disableGps(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "GPS", "GPS is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on GPS", ex.getMessage()));
        }
    }

    public void disableRoaming(boolean disable) {
        try {
            cache.setConnectivityRoamingDisable(disable);
            PoliciesConnectivity.disableRoaming(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Roaming", "Roaming is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on Roaming", ex.getMessage()));
        }
    }

    public void disableAirplaneMode(boolean disable) {
        try {
            cache.setConnectivityAirplaneModeDisable(disable);
            PoliciesConnectivity.disableAirplaneMode(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "AirplaneMode", "AirplaneMode is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on AirplaneMode", ex.getMessage()));
        }
    }

    public void disableMobileLine(boolean disable) {
        try {
            cache.setConnectivityMobileLineDisable(disable);
            PoliciesConnectivity.disableMobileLine(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "MobileLine", "MobileLine is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on MobileLine", ex.getMessage()));
        }
    }

    public void disableNFC(boolean disable) {
        try {
            cache.setConnectivityNFCDisable(disable);
            PoliciesConnectivity.disableNFC(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "NFC", "NFC is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on NFC", ex.getMessage()));
        }
    }

    public void disableHostpotTethering(boolean disable) {
        try {
            cache.setConnectivityHostpotTetheringDisable(disable);
            PoliciesConnectivity.disableHostpotTethering(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "HostpotTethering", "HostpotTethering is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on HostpotTethering", ex.getMessage()));
        }
    }

    public void disableSmsMms(boolean disable) {
        // to change the behavior of this policy check
        // SMSReceiver

        try {
            cache.setConnectivitySmsMmsDisable(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "SMS", "SMS is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on SMS", ex.getMessage()));
        }
    }

    public void disableAllUsbFileTransferProtocols(boolean disable) {
        try {
            cache.setConnectivityUsbFileTransferProtocolsDisable(disable);
            PoliciesConnectivity.disableAllUsbFileTransferProtocols(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "UsbFileTransferProtocols", "UsbFileTransferProtocols is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on UsbFileTransferProtocols", ex.getMessage()));
        }
    }

    public void disableADBUsbFileTransferProtocols(boolean disable) {
        try {
            cache.setConnectivityADBUsbFileTransferProtocolsDisable(disable);
            PoliciesConnectivity.disableADBUsbFileTransferProtocols(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "UsbFileTransferProtocols ADB", "UsbFileTransferProtocols is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on UsbFileTransferProtocols ADB", ex.getMessage()));
        }
    }

    public void disablePTPUsbFileTransferProtocols(boolean disable) {
        try {
            cache.setConnectivityPTPUsbFileTransferProtocolsDisable(disable);
            PoliciesConnectivity.disablePTPUsbFileTransferProtocols(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "UsbFileTransferProtocols PTP", "UsbFileTransferProtocols is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on UsbFileTransferProtocols PTP", ex.getMessage()));
        }
    }

    public void disableMTPUsbFileTransferProtocols(boolean disable) {
        try {
            cache.setConnectivityMTPUsbFileTransferProtocolsDisable(disable);
            PoliciesConnectivity.disableMTPUsbFileTransferProtocols(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "UsbFileTransferProtocols MTP", "UsbFileTransferProtocols is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on UsbFileTransferProtocols MTP", ex.getMessage()));
        }
    }

    public void disableStatusBar(boolean disable) {
        try {
            new PoliciesDeviceManager(context).disableStatusBar(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "status bar", "status bar is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on status bar", ex.getMessage()));
        }
    }

    public void disableSpeakerphone(boolean disable) {
        try {
            new PoliciesDeviceManager(context).disableSpeakerphone(disable);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Speaker phone", "Speaker phone is disable: " + disable));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on Speaker phone", ex.getMessage()));
        }
    }

    public void disableScreenCapture(boolean disable) {
        try {
            if(Build.VERSION.SDK_INT >= 21) {
                new PoliciesDeviceManager(context).disableCaptureScreen(disable);
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Screen Capture", "Screen Capture is disable: " + disable));
            } else {
                FlyveLog.i("Screen capture policy is available on devices with api equals or mayor than 21");
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on Screen Capture", ex.getMessage()));
        }
    }

    public void resetPassword(String newPassword) {
        try {
            if(!newPassword.isEmpty()) {
                new PoliciesDeviceManager(context).resetPassword(newPassword);
                broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Reset Password", "Reset Password : ****"));
            } else {
                broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on Reset Password", "the new password is empty"));
            }
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on Reset Password", ex.getMessage()));
        }
    }

    public void removePackage(String packageName) {
        FlyveLog.d("Remove package: " + packageName);

        PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);
        policiesFiles.removeApk(packageName);
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

                    PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);
                    policiesFiles.execute("package", deployApp, id, sessionToken);

                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Install package", "name: " + deployApp + " id: " + id));
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on getActiveSessionToken", ex.getMessage()));
                }
            }

            @Override
            public void onError(String error) {
                FlyveLog.e(error);
                broadcastReceivedLog(Helpers.broadCastMessage(ERROR, ERROR, error));
                broadcastReceivedLog("Application fail: " + error);
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
                PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);

                if("true".equals(policiesFiles.execute("file", deployFile, id, sessionToken))) {
                    FlyveLog.d("File was stored on: " + deployFile);
                    broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "File was stored on", deployFile));
                }
            }

            @Override
            public void onError(String error) {
                FlyveLog.e(error);
                broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on applicationOnDevices", error));
            }
        });
    }

    public void removeFile(String removeFile) {
        PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);
        policiesFiles.removeFile(removeFile);

        FlyveLog.d("Remove file: " + removeFile);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Remove file", removeFile));
    }


    /**
     * FLEET encryption
     * Example {"encryption":[{"storageEncryption":"false"}]}
     */
    public void storageEncryption(Boolean enable) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.storageEncryptionDevice(enable);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "storage Encryption", "storage Encryption: " + enable));
    }

    public void passwordEnabled(String taskId) {
        try {
            PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
            mdm.enablePassword();
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordEnabled", "true"));

            // return the status of the task
            sendTaskStatus(taskId, FEEDBACK_DONE);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // return the status of the task
            sendTaskStatus(taskId, FEEDBACK_FAILED);
        }
    }

    public void passwordQuality(String quality) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordQuality(quality);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordQuality", quality));
    }

    public void passwordMinLength(int length) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordLength(length);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordMinLength", String.valueOf(length)));
    }

    public void passwordMinNonLetter(int length) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordMinimumNonLetter(length);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordMinNonLetter", String.valueOf(length)));
    }

    public void passwordMinLetter(int length) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordMinimumLetters(length);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordMinLetter", String.valueOf(length)));
    }

    public void passwordMinNumeric(int minimum) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordMinimumNumeric(minimum);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordMinNumeric", String.valueOf(minimum)));
    }

    public void passwordMinSymbols(int minimum) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordMinimumSymbols(minimum);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordMinSymbols", String.valueOf(minimum)));
    }

    public void passwordMinLowerCase(int minimum) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordMinimumLowerCase(minimum);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordMinLowerCase", String.valueOf(minimum)));
    }

    public void passwordMinUpperCase(int minimum) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setPasswordMinimumUpperCase(minimum);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "passwordMinUpperCase", String.valueOf(minimum)));
    }

    public void maximumFailedPasswordsForWipe(int maximum) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setMaximumFailedPasswordsForWipe(maximum);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "MaximumFailedPasswordsForWipe", String.valueOf(maximum)));
    }

    public void maximumTimeToLock(int maximum) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.setMaximumTimeToLock(maximum);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "maximumTimeToLock", String.valueOf(maximum)));
    }

    /**
     * Erase all device data include SDCard
     */
    public void wipe() {
        try {
            PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
            mdm.wipe();
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Wipe", "Wipe success"));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on wipe", ex.getMessage()));
        }
    }

    /**
     * Unenroll the device
     */
    public boolean unenroll() {
        // Send message with unenroll
        String topic = mTopic + "/Status/Unenroll";
        String payload = "{\"unenroll\": \"unenrolled\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Unenroll", "Unenroll success"));

            // clear cache
            cache.clearSettings();

            // send message
            Helpers.sendBroadcast(Helpers.broadCastMessage("action", "open", "splash"), Helpers.BROADCAST_MSG, this.context);

            // show offline
            Helpers.sendBroadcast(false, Helpers.BROADCAST_STATUS, this.context);

            return true;
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on unenroll", ex.getMessage()));
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
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "PING", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendKeepAlive", ex.getMessage()));
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
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Inventory", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // send broadcast
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendKeepAlive", ex.getMessage()));
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
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Inventory", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());

            // send broadcast
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendKeepAlive", ex.getMessage()));
        }
    }

    public void disableCreateVpnProfiles(Boolean disable) {
        PoliciesDeviceManager mdm = new PoliciesDeviceManager(this.context);
        mdm.disableVPN(disable);
        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "maximumTimeToLock", String.valueOf(disable)));
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
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Status Version", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendStatusVersion", ex.getMessage()));
        }
    }

    /**
     * Send the Status version of the agent
     * payload: {"online": "true"}
     */
    public void sendOnlineStatus(Boolean status) {
        String topic = mTopic + "/Status/Online";
        String payload = "{\"online\": \"" + Boolean.toString( status ) + "\"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes(UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            IMqttDeliveryToken token = client.publish(topic, message);
            broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Online Status", "ID: " + token.getMessageId()));
        } catch (Exception ex) {
            broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on sendStatusVersion", ex.getMessage()));
        }
    }

    /**
     * Send the GPS information to MQTT
     * payload: {"latitude":"10.2485486","longitude":"-67.5904498","datetime":1499364642}
     */
    public void sendGPS() {

        new FastLocationProvider().getLocation(context, new FastLocationProvider.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if(location == null) {
                    FlyveLog.d("without location yet...");
                } else {
                    FlyveLog.d("lat: " + location.getLatitude() + " lon: " + location.getLongitude());

                    try {
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());

                        JSONObject jsonGPS = new JSONObject();

                        jsonGPS.put("latitude", latitude);
                        jsonGPS.put("longitude", longitude);
                        jsonGPS.put("datetime", Helpers.getUnixTime());

                        String topic = mTopic + "/Status/Geolocation";
                        String payload = jsonGPS.toString();
                        byte[] encodedPayload;

                        encodedPayload = payload.getBytes(UTF_8);
                        MqttMessage message = new MqttMessage(encodedPayload);
                        IMqttDeliveryToken token = client.publish(topic, message);

                        // send broadcast
                        broadcastReceivedLog(Helpers.broadCastMessage(MQTT_SEND, "Send Geolocation", "ID: " + token.getMessageId()));
                    } catch (Exception ex) {
                        FlyveLog.e(ex.getMessage());
                        broadcastReceivedLog(Helpers.broadCastMessage(ERROR, "Error on GPS location", ex.getMessage()));
                    }
                }
            }
        });
    }

    /**
     * Broadcast the received log
     * @param message
     */
    private void broadcastReceivedLog(String message){
        // write log file
        FlyveLog.f(message, FlyveLog.FILE_NAME_LOG);
    }

}
