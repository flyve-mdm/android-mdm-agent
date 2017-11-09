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
import org.flyve.mdm.agent.utils.GpsHelper;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.SystemHelper;

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

            // Disable / Enable Roaming
            if(cache.getRoaming()!=null || !cache.getRoaming().equals("")) {
                SystemHelper.disableRoaming(cache.getConnectivityRoamingDisable());
            }

            // Disable / Enable Mobile line
            if(cache.getMobileLine()!=null || !cache.getMobileLine().equals("")) {
                SystemHelper.disableMobileLine(cache.getConnectivityMobileLineDisable());
            }
        }

        // Manage WIFI
        if ("android.net.wifi.STATE_CHANGE".equalsIgnoreCase(action) || "android.net.wifi.WIFI_STATE_CHANGED".equalsIgnoreCase(action)) {
            FlyveLog.i("is Online: %s", Helpers.isOnline(context));
            if(Helpers.isOnline(context)) {
                MQTTService.start( context );
            }

            // Disable / Enable Hostpot
            if(cache.getHostpotTethering()!=null || !cache.getHostpotTethering().equals("")) {
                SystemHelper.disableHostpotTethering(context, cache.getConnectivityHostpotTetheringDisable());
            }
        }

        // Manage Bluetooth
        if ("android.bluetooth.adapter.action.STATE_CHANGED".equalsIgnoreCase(action)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled() && cache.getConnectivityBluetoothDisable()) {
                bluetoothAdapter.disable();
            }
        }

        // Manage NFC
        if("android.nfc.extra.ADAPTER_STATE".equalsIgnoreCase(action) && !cache.getNFC().equals("")) {
            SystemHelper.disableNFC(cache.getConnectivityNFCDisable());
        }

        // Manage location
        if("android.location.PROVIDERS_CHANGED".equalsIgnoreCase(action)) {
            /*
             *  Turn off GPS need system app
             *  To install apk on system/app with adb on root device
             *
             *   -------------------------------------------
             *   $adb shell
             *   $su
             *   $mount -o rw,remount /system
             *   -------------------------------------------
             *
             *   If apk is on external sdcard
             *
             *   # for Android 4.3 or newest
             *   # move the apk to /system/priv-app
             *   mv /storage/sdcard1/file.apk /system/priv-app
             *
             *   # older Android devices
             *   # move apk to /system/app
             *   mv /storage/sdcard1/file.apk /system/app
             *
             *   # change file permission to execute
             *   chmod 644 file.apk
             *
             *   # exit and reboot the device to take change
             *   adb reboot
             */

            boolean disable = cache.getConnectivityGPSDisable();
            GpsHelper.disableGps(disable);

            FlyveLog.i("Location providers change: " + disable);
        }
    }
}
