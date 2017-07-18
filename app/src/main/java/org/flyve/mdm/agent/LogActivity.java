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
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ListView;
import android.widget.TextView;

import org.flyve.mdm.agent.adapter.LogAdapter;
import org.flyve.mdm.agent.security.FlyveAdminReceiver;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This is the main activity of the app
 */
public class LogActivity extends Activity {

    private IntentFilter mIntent;
    private Intent mServiceIntent;
    private TextView txtMessage;
    private TextView txtTitle;
    private ArrayList<HashMap<String, String>> arr_data;
    LogAdapter mAdapter;

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    ComponentName mDeviceAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // Device Admin
        mDeviceAdmin = new ComponentName(this, FlyveAdminReceiver.class);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "EXPLANATION");
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);

        // ------------------
        // MQTT SERVICE
        // ------------------
        MQTTService mMQTTService = new MQTTService();
        mServiceIntent = new Intent(LogActivity.this, mMQTTService.getClass());
        // Start the service
        if (!isMyServiceRunning(mMQTTService.getClass())) {
            startService(mServiceIntent);
        }

        txtMessage = (TextView) findViewById(R.id.txtMessage);
        txtTitle = (TextView) findViewById(R.id.txtTitle);

        arr_data = new ArrayList<HashMap<String, String>>();

        ListView lst = (ListView) findViewById(R.id.lst);
        mAdapter = new LogAdapter(LogActivity.this, arr_data);
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
        LocalBroadcastManager.getInstance(LogActivity.this).registerReceiver(broadcastReceivedMessage, new IntentFilter("flyve.mqtt.msg"));
        LocalBroadcastManager.getInstance(LogActivity.this).registerReceiver(broadcastReceivedLog, new IntentFilter("flyve.mqtt.log"));
        LocalBroadcastManager.getInstance(LogActivity.this).registerReceiver(broadcastServiceStatus, new IntentFilter("flyve.mqtt.status"));
    }

    @Override
    protected void onDestroy() {
        // stop the service
        stopService(mServiceIntent);
        FlyveLog.i("onDestroy!");
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
                FlyveLog.i ("isMyServiceRunning?", Boolean.toString( true ));
                return true;
            }
        }
        FlyveLog.i ("isMyServiceRunning?", Boolean.toString( false ));
        return false;
    }

    /**
     * broadcastReceiverMessage instance that receive all the message from MQTTService
     */
    private BroadcastReceiver broadcastReceivedLog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");

            try {
                HashMap<String, String> map = new HashMap<String, String>();

                JSONObject json = new JSONObject(msg);

                map.put("type", json.getString("type"));
                map.put("title", json.getString("title"));
                map.put("body", json.getString("body"));
                map.put("date", json.getString("date"));

                arr_data.add(map);
                mAdapter.notifyDataSetChanged();
            } catch (Exception ex) {
                FlyveLog.d("ERROR" + ex.getMessage());
            }
        }
    };

    /**
     * broadcastReceiverMessage instance that receive all the message from MQTTService
     */
    private BroadcastReceiver broadcastReceivedMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("message");
        txtTitle.setText(msg);
        }
    };

    /**
     * broadcastServiceStatus instance that receive service status from MQTTService
     */
    private BroadcastReceiver broadcastServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("message");

        if(Boolean.parseBoolean(msg)) {
            txtMessage.setText("Online");
        } else {
            txtMessage.setText("Offline");
        }
        }
    };
}
