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
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.flyvemdm.MQTTNotifierActivity;
import com.teclib.flyvemdm.MainActivity;
import com.teclib.flyvemdm.R;


public class MQTTNotificationService extends Service {

    MQTTNotificationService.MQTTNotificationServiceReceiver MQTTNotificationServiceReceiver;

    @Override
    public void onCreate() {
        MQTTNotificationServiceReceiver = new MQTTNotificationServiceReceiver();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(MQTTService.MQTT_STATUS_INTENT);
        filter.addAction(MQTTService.MQTT_STOP_INTENT);
        registerReceiver(MQTTNotificationServiceReceiver, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(MQTTNotificationServiceReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public class MQTTNotificationServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferenceMQTT sharedPreferenceMQTT = new SharedPreferenceMQTT();
            if(!getSettingsPreferences()) {
                if (intent.getAction().equals(MQTTService.MQTT_STATUS_INTENT)) {
                    boolean receive = sharedPreferenceMQTT.getStatus(context);
                    if (receive) {
                        CustomNotificationConnected(getString(R.string.stork_connect));
                    } else {
                        CustomNotificationNoConnected(getString(R.string.stork_unconnect));
                    }
                }
                if(intent.getAction().equals("com.teclib.service.STOP")){
                    cancelNotification(context,1);
                }
            }
            else {
                cancelNotification(context,1);
            }
        }
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    public void CustomNotificationConnected(String title) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_mqtt_connect);

        String strtitle = getString(R.string.app_name);

        Intent intent = new Intent(this, MQTTNotifierActivity.class);

        intent.putExtra("title", strtitle);
        intent.putExtra("text", title);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_white_stork)
                .setTicker(getString(R.string.enrolement_string))
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setContent(remoteViews);

        remoteViews.setImageViewResource(R.id.imagenotileft, R.mipmap.ic_notification_connect);
        remoteViews.setTextViewText(R.id.title, getString(R.string.app_name));
        remoteViews.setTextViewText(R.id.text, title);

        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(1, builder.build());

    }

    public void CustomNotificationNoConnected(String title) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification_mqtt_no_connect);

        String strtitle = getString(R.string.app_name);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("title", strtitle);
        intent.putExtra("text", title);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_white_stork)
                .setTicker(getString(R.string.enrolement_string))
                .setOngoing(true)
                .setContentIntent(pIntent)
                .setContent(remoteViews);

        remoteViews.setImageViewResource(R.id.imagenotileft, R.mipmap.ic_notification_no_connect);

        remoteViews.setTextViewText(R.id.title, getString(R.string.app_name));
        remoteViews.setTextViewText(R.id.text, title);

        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(1, builder.build());

    }

    private boolean getSettingsPreferences() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        boolean notification = prefs.getBoolean("notification", false);

        return notification;

    }

}
