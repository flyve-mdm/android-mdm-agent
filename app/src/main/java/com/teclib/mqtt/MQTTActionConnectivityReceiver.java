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

package com.teclib.mqtt;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceConnectivity;


public class MQTTActionConnectivityReceiver extends BroadcastReceiver{

    SharedPreferenceConnectivity mSharedPreferenceConnectivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        FlyveLog.d("network receiver");
        mSharedPreferenceConnectivity = new SharedPreferenceConnectivity();

        //WIFI
        if (action.equals("android.net.wifi.STATE_CHANGE")) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info != null && info.isAvailable()) {

                if (mSharedPreferenceConnectivity.getWifi(context)) {

                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                }
            }
        }

        // Bluetooth
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                if (mSharedPreferenceConnectivity.getBluetooth(context)) {
                    bluetoothAdapter.disable();
                }
            }
        }
    }
}
