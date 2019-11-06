package org.flyve.mdm.agent.core.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.data.database.TopicsData;
import org.flyve.mdm.agent.data.localstorage.AppData;
import org.flyve.mdm.agent.policies.PoliciesAsyncTask;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MqttHelper extends Service  {
    private static MqttHelper instance = null;

    IBinder mBinder = new LocalBinder();
    public MqttAndroidClient mqttAndroidClient;
    public String mBroker;
    public String mPort;
    public String mUser;
    public String mPassword;
    public String mTopic;
    public String mTLS;
    public String clientId;
    public String protocol;
    public Context context;
    private Timer reconnectionTimer;
    private int reconnectionCounter;
    private boolean alreadyRun = false;


    public static boolean isInstanceCreated() {
        return instance != null;
    }


    @Override
    public void onCreate() {
        MqttData cache = new MqttData(getApplicationContext());
        context = getApplicationContext();
        mBroker = cache.getBroker();
        mPort = cache.getPort();
        mUser = cache.getMqttUser();
        mPassword = cache.getMqttPasswd();
        mTopic = cache.getTopic();
        mTLS = cache.getTls();
        reconnectionTimer = null;

        protocol = "tcp";
        // TLS is active change protocol
        if(mTLS.equals("1")) {
            protocol = "ssl";
        }

        instance = this;
    }

    @Override
    public void onDestroy() {
        if(mqttAndroidClient != null && mqttAndroidClient.isConnected()){
            try {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                FlyveLog.d("MQTTHelper CanDisconnect  "+e.getMessage());

            }
        }
        alreadyRun = false;
        instance = null;
        FlyveLog.d("MQTTHelper OnDestroy "+this.getClass().getName());
        super.onDestroy();
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling the method startService(Intent)
     * https://developer.android.com/reference/android/app/Service.html#START_STICKY Documentation of the Constant
     *
     * @param intent supplied to start the service
     * @param flags the additional data about this start request
     * @param startId a unique integer representing this specific request to start
     * @return constant START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!alreadyRun){
            construct(getApplicationContext());
            alreadyRun = true;
        }
        return START_STICKY;
    }


    private void construct(final Context context){


        FlyveLog.d("MQTTHelper connect ");
        if(mqttAndroidClient == null) {
            clientId = MqttClient.generateClientId();
            FlyveLog.d("MQTT Generate client ID " + clientId);

            mqttAndroidClient = new MqttAndroidClient(context, protocol + "://" + mBroker + ":" + mPort, clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) { }

                @Override
                public void connectionLost(Throwable throwable) {
                    FlyveLog.d("MQTT connection lost");
                    setStatus(false);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    FlyveLog.d("MQTT message arrive -> "+mqttMessage.toString() + " with qos "+ mqttMessage.getQos());
                    MqttHelper.this.messageArrived(context,topic,mqttMessage);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { }
            });

        }

        connect();
    }

    private void connect(){

        MqttData cache = new MqttData(context);
        final String mUser = cache.getMqttUser();
        final String mPassword = cache.getMqttPasswd();
        final String mTopic = cache.getTopic();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setPassword(mPassword.toCharArray());
        options.setUserName(mUser);
        options.setCleanSession(false);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setConnectionTimeout(MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT);
        options.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
        options.setAutomaticReconnect(true);

        // Create a testament to send when MQTT connection is down
        String will = "{ \"online\": false }";
        options.setWill(mTopic + "/Status/Online", will.getBytes(), 0, true);

        // set all the topics on database to unconnected
        new TopicsData(context).clearTopics();
        FlyveLog.d("MQTT Clear all Topics");


        try {
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {


                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    FlyveLog.d("MQTT connection succes");
                    // Everything ready waiting for message
                    MqttController policiesController = new MqttController(context, mqttAndroidClient);

                    // main topic
                    String topic = mTopic + "/#";
                    policiesController.subscribe(topic);

                    // subscribe to manifest
                    policiesController.subscribe("FlyvemdmManifest/Status/Version");

                    setStatus(true);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    FlyveLog.d("MQTT Failed to connect to: " + protocol + "://" + mBroker + ":" + mPort + " "+exception.toString());
                    setStatus(false);
                }
            });

        } catch (MqttException ex){
            setStatus(false);
            ex.printStackTrace();
        }
    }


    public void messageArrived(Context context, String topic, MqttMessage message) {
        int priority = topic.contains("fleet") ? 0 : 1;

        String messageBody = new String(message.getPayload());
        MqttController mqttController = new MqttController(context, mqttAndroidClient);

        if(topic.isEmpty()) {
            // exit if the topic if empty
            return;
        }

        // Delete policy information
        if(messageBody.contains("default")) {
            try {
                String taskId = new JSONObject(messageBody).getString("taskId");
                new PoliciesData(context).removeValue(taskId);
                FlyveLog.i("Deleting policy " + message + " - " + topic);
            } catch (Exception ex) {
                FlyveLog.e("fcm", "error deleting policy " + message + " - " + topic, ex.getMessage());
            }
            return;
        }

        //Command/Policies
        new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.POLICIES, topic, messageBody, this.mqttAndroidClient);

        // Command/Ping
        if(topic.toLowerCase().contains("ping")) {
            new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.PING, topic,messageBody, this.mqttAndroidClient);
        }

        // Command/Geolocate
        if(topic.toLowerCase().contains("geolocate")) {
            new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.GEOLOCATE, topic,messageBody, this.mqttAndroidClient);
        }

        // Command/Inventory
        if(topic.toLowerCase().contains("inventory")) {
            new PoliciesAsyncTask().execute(context, PoliciesAsyncTask.INVENTORY, topic,messageBody, this.mqttAndroidClient);
        }

        // Command/Wipe
        if(topic.toLowerCase().contains("wipe")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.WIPE, topic,messageBody);
        }

        // Command/Unenroll
        if(topic.toLowerCase().contains("unenroll")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.UNENROLL, topic,messageBody);
        }

        // Command/Lock
        if(topic.toLowerCase().contains("lock")) {
            new PoliciesAsyncTask().execute(context,PoliciesAsyncTask.LOCK, topic,messageBody);
        }

        // Command/Subscribe
        if(topic.toLowerCase().contains("subscribe")) {
            try {
                JSONObject jsonObj = new JSONObject(messageBody);

                if(jsonObj.has("subscribe")) {
                    JSONArray jsonTopics = jsonObj.getJSONArray("subscribe");
                    for(int i=0; i<jsonTopics.length();i++) {
                        JSONObject jsonTopic = jsonTopics.getJSONObject(i);

                        String channel = jsonTopic.getString("topic")+"/#";
                        if(channel == null || channel.contains("null")) {
                            mqttController.unsubscribe();
                        }else{
                            mqttController.subscribe(channel);
                        }
                    }
                }
            } catch (Exception ex) {
                showDetailError(context, CommonErrorType.MQTT_SUBSCRIBE, ex.getMessage());
            }
        }

    }

    private void setStatus(Boolean isConnected){

        // reconnect
        if(!isConnected) {
            reconnectionTimer = new Timer();
            reconnectionTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(mqttAndroidClient.isConnected()) {
                        setStatus(true);
                    } else {
                        FlyveLog.d("Reconnection finish");
                        Helpers.storeLog("MQTT", "Reconnection Success", "");
                        reconnectionTimer.cancel();
                        reconnectionTimer = null;
                    }
                }
            }, 0, 15000);


        } else {
            // send via http the status connected
            PoliciesAsyncTask.sendStatusbyHttp(context, true);
        }

        AppData cache = new AppData(context);
        cache.setOnlineStatus(isConnected);

        Helpers.sendBroadcast(isConnected, Helpers.BROADCAST_STATUS, context);
    }

    public void showDetailError(Context context, int type, String message) {
        FlyveLog.e(this.getClass().getName() + ", showDetailError", context.getResources().getString(R.string.error_message_with_number, String.valueOf(type), message));
    }


    /**
     * Return the communication channel to the service
     * @param intent that was used to bind to this service
     * @return IBinder null if clients cannot bind to the service
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public MqttHelper getServerInstance() {
            return MqttHelper.this;
        }
    }
}

