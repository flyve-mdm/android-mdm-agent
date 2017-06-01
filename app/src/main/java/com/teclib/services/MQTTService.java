package com.teclib.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.teclib.data.DataStorage;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import javax.net.ssl.SSLContext;

/**
 * Created by rafaelhernandez on 09/05/2016.
 */
public class MQTTService extends Service implements MqttCallback {

    private String TAG = "MQTT";
    private MqttAndroidClient client;
    private DataStorage cache;

    private String mBroker = "";
    private String mPort = "";
    private String mUser = "";
    private String mPassword = "";
    private String mTopic = "";

    public MQTTService(Context applicationContext) {
        super();
        Log.i("START", "SERVICE MQTT");
    }

    public MQTTService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("com.teclib.RestartMQTT");
        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void connect() {

        cache = new DataStorage(this.getApplicationContext());

        mBroker = cache.getVariablePermanente("broker");
        mPort = "8883"; //cache.getVariablePermanente("port");
        mUser = "rafa"; //cache.getVariablePermanente("agent_id");
        mPassword = "azlknvjkfbsdklfdsgfd"; //cache.getVariablePermanente("mqttpasswd");
        mTopic = cache.getVariablePermanente("topic");

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "ssl://" + mBroker + ":" + mPort,
                clientId);

        client.setCallback( this );

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setPassword(mPassword.toCharArray());
            options.setUserName(mUser);
            options.setCleanSession(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setConnectionTimeout(0);

            try {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);

                options.setSocketFactory(sslContext.getSocketFactory());

                Log.d("Flyve", "ssl socket factory created from flyve ca");
            } catch (Exception ex) {
                Log.e("Flyve","error while building ssl mqtt cnx", ex);
            }

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    suscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                }
            });
        }
        catch (MqttException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "Connection fail " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "Topic " + topic);
        Log.d(TAG, "Message " + message.getPayload());

        String messageBody;
        messageBody = new String(message.getPayload());

        try {
            JSONObject jsonObj = new JSONObject(messageBody);

            // KeepAlive
            if (jsonObj.has("query")) {
                if ("Ping".equals(jsonObj.getString("query"))) {

                    Intent in = new Intent();
                    in.putExtra("message", "PING!");
                    in.setAction("NOW");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

                    sendKeepAlive();
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete ");
    }

    private void sendKeepAlive() {
        String topic = mTopic + "/Status/Ping";
        String payload = "!";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            Log.d(TAG, "payload sended");
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR: " + e.getMessage());
        }
    }

    private void suscribe() {
        String topic = mTopic + "/#";
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Log.d(TAG, "suscribed");

                    Intent in = new Intent();
                    in.putExtra("message", "suscribed");
                    in.setAction("NOW");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Log.d(TAG, "ERROR: " + exception.getMessage());

                    Intent in = new Intent();
                    in.putExtra("message", "ERROR: " + exception.getMessage());
                    in.setAction("NOW");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}