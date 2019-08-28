package org.flyve.mdm.agent.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.flyve.mdm.agent.MessagePolicies;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.ui.PushPoliciesActivity;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessageService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        //
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        final String topic = remoteMessage.getData().get("topic");
        if(topic==null) {
            Helpers.storeLog("fcm", "topic null", "topic cannot be null");
            return;
        }

        final String message = remoteMessage.getData().get("message");
        if(message==null) {
            Helpers.storeLog("fcm", "message null", "message cannot be null");
            return;
        }

        final String body;
        if(remoteMessage.getNotification() == null || remoteMessage.getNotification().getBody().equals("")) {
            body = "Please sync your device";
        } else {
            body = remoteMessage.getNotification().getBody();
        }

        sendNotification(topic, message, body);

        // sometimes the messages are a lot and the notification
        // manager cannot handled, need some delay to do it
        SystemClock.sleep(1000);
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     */

    private void sendNotification(String topic, String message, String body) {
        String debugInfo = "Notification (body): " + body + "\n" + "Notification (message): " + message + "\n" + "Notification (topic): " + topic;
        FlyveLog.d(debugInfo);
        MessagePolicies messagePolicies = new MessagePolicies();
        messagePolicies.messageArrived(MDMAgent.getInstance(), topic, message);
    }

    private int getID() {
        Date now = new Date();
        return Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
    }
}
