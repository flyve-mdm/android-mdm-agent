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

package org.flyve.mdm.agent;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import org.flyve.mdm.agent.adapter.LogAdapter;
import org.flyve.mdm.agent.services.MQTTService;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This is the main activity of the app
 */
public class MainActivity extends Activity {

    private IntentFilter mIntent;
    private Intent mServiceIntent;
    private TextView tvMsg;
    private TextView tvStatus;
    private ArrayList<HashMap<String, String>> arr_data;
    LogAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.teclib.flyvemdm.R.layout.activity_main);

        // ------------------
        // MQTT SERVICE
        // ------------------
        MQTTService mMQTTService = new MQTTService();
        mServiceIntent = new Intent(MainActivity.this, mMQTTService.getClass());
        // Start the service
        if (!isMyServiceRunning(mMQTTService.getClass())) {
            startService(mServiceIntent);
        }

        tvMsg = (TextView) findViewById(com.teclib.flyvemdm.R.id.tvMsg);
        tvStatus = (TextView) findViewById(com.teclib.flyvemdm.R.id.tvStatus);

        arr_data = new ArrayList<HashMap<String, String>>();


        ListView lst = (ListView) findViewById(com.teclib.flyvemdm.R.id.lst);
        mAdapter = new LogAdapter(MainActivity.this, arr_data);
        lst.setAdapter(mAdapter);

    }

    @Override
    protected void onPause() {
        // unregister the broadcast
        if(mIntent != null) {
            unregisterReceiver(broadcastReceivedMessage);
            unregisterReceiver(broadcastReceivedLog);
            unregisterReceiver(broadcastServiceStatus);
            mIntent = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        // register the broadcast
        super.onResume();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceivedMessage, new IntentFilter("flyve.mqtt.msg"));
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceivedLog, new IntentFilter("flyve.mqtt.log"));
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastServiceStatus, new IntentFilter("flyve.mqtt.status"));
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
     * broadcastReceiverMessage instance that receive all the message from MQTTService
     */
    private BroadcastReceiver broadcastReceivedLog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");  //get the type of message from MyGcmListenerService 1 - lock or 0 -Unlock
            tvMsg.setText( msg );

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("message", msg);

            arr_data.add(map);
            mAdapter.notifyDataSetChanged();
        }
    };

    /**
     * broadcastReceiverMessage instance that receive all the message from MQTTService
     */
    private BroadcastReceiver broadcastReceivedMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("message");  //get the type of message from MyGcmListenerService 1 - lock or 0 -Unlock
        tvMsg.setText( msg );
        }
    };

    /**
     * broadcastServiceStatus instance that receive service status from MQTTService
     */
    private BroadcastReceiver broadcastServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("message");  //get the type of message from MyGcmListenerService 1 - lock or 0 -Unlock
        HashMap<String, String> map = new HashMap<String, String>();

        if(Boolean.parseBoolean(msg)) {
            tvStatus.setText("Online");
            map.put("message", "Online");
        } else {
            tvStatus.setText("Offline");
            map.put("message", "Offline");
        }

        arr_data.add(map);
        mAdapter.notifyDataSetChanged();
        }
    };
}
