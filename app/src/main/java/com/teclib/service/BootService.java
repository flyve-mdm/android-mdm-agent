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
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;
import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.flyvemdm.MainActivity;
import com.teclib.flyvemdm.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class BootService extends Service {

    private static Thread mThread = null;
    private SharedPreferenceMQTT mSharedPreferenceMQTT;
    private SharedPreferenceAction mSharedPreferenceAction;
    private Set<String> mIsEmptyApks;
    private Set<String> mIsEmptyRemoveApks;
    private Context mContext;
    private ArrayList<String> mIntentArgs;

    public BootService() { }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void onCreate()
    {
        SurveillanceRunnable Surveillance = new SurveillanceRunnable();
        mThread = new Thread(Surveillance);
        mThread.start();

        mContext = this.getBaseContext();
        mIntentArgs = new ArrayList<String>();
        //verification de l'enrolement
        mSharedPreferenceMQTT = new SharedPreferenceMQTT();
        mSharedPreferenceAction = new SharedPreferenceAction();
        String server = mSharedPreferenceMQTT.getServer(mContext);
        FlyveLog.d(server);

        if ("null".equals(server)){
            CustomNotification(0);
        }
        else {
            mIntentArgs.add("init");
            Intent DeviceAdmin = new Intent(this.getBaseContext(), com.teclib.api.DeviceAdmin.class);
            DeviceAdmin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            DeviceAdmin.putStringArrayListExtra("ControllerArgs", mIntentArgs);
            mContext.startActivity(DeviceAdmin);
        }

        mIsEmptyApks = mSharedPreferenceAction.getApks(mContext);

        FlyveLog.d(Arrays.toString(mIsEmptyApks.toArray()).toString());

        if(!"[null]".equals(Arrays.toString(mIsEmptyApks.toArray()))){
          //  Intent intent = new Intent(mContext,NotificationInstallService.class);
          //  mContext.startService(intent);
        }


        mIsEmptyRemoveApks = mSharedPreferenceAction.getApksRemove(mContext);
        if(!"[null]".equals(Arrays.toString(mIsEmptyRemoveApks.toArray()))){
            Intent intentRemove = new Intent(mContext,NotificationRemoveService.class);
            mContext.startService(intentRemove);
        }
    }

    public void onDestroy() {
        if (mThread !=null)
        {
            mThread.isInterrupted();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent,flags,startId);
    }

    public class SurveillanceRunnable implements Runnable {
        private boolean bThreadExec=false;

        @Override
        public void run() {
            bThreadExec=true;
            do {
                try {
                    Thread.sleep(10000);
                    if(!"null".equals(mSharedPreferenceMQTT.getServer(mContext))){
                        bThreadExec=false;
                        Intent mqtt = new Intent(mContext, MQTTService.class);
                        mqtt.setAction(MQTTService.ACTION_START);
                        startService(mqtt);
                        onDestroy();
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    bThreadExec = false;
                }
            }while (bThreadExec);

            stopForeground(true);
        }
    }

    public void CustomNotification(int status) {
        if(status==1){
            //enrolement ok
        }
        else {
            RemoteViews remoteViews = new RemoteViews(getPackageName(),
                    R.layout.notification_enrolment);

            String strTitle = getString(R.string.app_name);
            String strText = getString(R.string.enrolement_string);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("title", strTitle);
            intent.putExtra("text", strText);

            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_white_stork)
                    .setTicker(getString(R.string.enrolement_string))
                    .setOngoing(true)
                    .setContentIntent(pIntent)
                    .setContent(remoteViews);

            remoteViews.setImageViewResource(R.id.imagenotileft, R.mipmap.ic_notification_enrolment);
            remoteViews.setTextViewText(R.id.title, getString(R.string.app_name));
            remoteViews.setTextViewText(R.id.text, getString(R.string.enrolement_string));

            NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationmanager.notify(1, builder.build());
        }

    }


}
