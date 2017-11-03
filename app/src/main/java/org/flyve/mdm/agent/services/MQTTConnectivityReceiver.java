/*
 * Copyright (C) 2016-2017 Teclib'
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

package org.flyve.mdm.agent.services;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

/**
 * Receive broadcast from android.net.wifi.STATE_CHANGE and android.bluetooth.adapter.action.STATE_CHANGED
 * on AndroidManifest.xml
 */
public class MQTTConnectivityReceiver extends BroadcastReceiver {

    /**
     * It is called when it receives information about the state of the connectivity of the WIFI, Bluetooth and GPS
     * @param Context the context in which the receiver is running
     * @param Intent the intent being received
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        FlyveLog.d("Connectivity receiver: " + action);

        DataStorage cache = new DataStorage(context);

        if("android.net.conn.CONNECTIVITY_CHANGE".equalsIgnoreCase(action)) {
            FlyveLog.i("is Online: %s", Helpers.isOnline(context));
            if(Helpers.isOnline(context)) {
                MQTTService.start( context );
            }
        }

        // Manage WIFI
        if ("android.net.wifi.STATE_CHANGE".equalsIgnoreCase(action) || "android.net.wifi.WIFI_STATE_CHANGED".equalsIgnoreCase(action)) {
            FlyveLog.i("is Online: %s", Helpers.isOnline(context));
            if(Helpers.isOnline(context)) {
                MQTTService.start( context );
            }
        }

        // Manage Bluetooth
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled() && cache.getConnectivityBluetoothDisable()) {
                bluetoothAdapter.disable();
            }
        }

        // Manage location
        if("android.location.PROVIDERS_CHANGED".equalsIgnoreCase(action)) {
            /*
             * Turn off GPS need system app for any api Android under 23 and
             * user app for API 23 or above
             * check this https://developer.android.com/reference/android/Manifest.permission.html#WRITE_SETTINGS
             * check if the app is installed on /system or /data to manage GPS settings
             *
             * “Write settings” permission not granted marshmallow android
             *  https://stackoverflow.com/questions/39224303/write-settings-permission-not-granted-marshmallow-android/39224511#39224511
             *
             * Can't get WRITE_SETTINGS permission
             * https://stackoverflow.com/questions/32083410/cant-get-write-settings-permission
             */

            boolean disable = cache.getConnectivityGPSDisable();
            FlyveLog.i("Location providers change: " + disable);
        }
    }
}
