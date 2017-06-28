/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @author    Rafael Hernandez
 * @date      02/06/2017
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.teclib.data.DataStorage;
import com.teclib.services.MQTTService;

/**
 * This is the main activity of the app
 */
public class MainActivity extends Activity {

    private IntentFilter mIntent;
    private Intent mServiceIntent;
    private TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ------------------
        // MQTT SERVICE
        // ------------------
        MQTTService mMQTTService = new MQTTService();
        mServiceIntent = new Intent(MainActivity.this, mMQTTService.getClass());
        // Start the service
        if (!isMyServiceRunning(mMQTTService.getClass())) {
            startService(mServiceIntent);
        }

        tvMsg = (TextView) findViewById(R.id.tvMsg);

        Button btnUnenroll = (Button) findViewById(R.id.btnClear);
        btnUnenroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataStorage cache = new DataStorage(MainActivity.this);
                cache.clearSettings();
            }
        });

        Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    protected void onPause() {
        // unregister the broadcast
        if(mIntent != null) {
            unregisterReceiver(broadcastReceiver);
            mIntent = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        // register the broadcast
        super.onResume();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("flyve.mqtt.msg"));
    }

    @Override
    protected void onDestroy() {
        // stop the service
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    /**
     * Check if the service is running
     * @param serviceClass Class
     * @return boolean
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", Boolean.toString( true ));
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", Boolean.toString( false ));
        return false;
    }

    /**
     * broadcastReceiver instance that receive all the message from Broadcast
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("message");  //get the type of message from MyGcmListenerService 1 - lock or 0 -Unlock
        tvMsg.setText( type );
        }
    };
}
