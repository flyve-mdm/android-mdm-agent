/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.policies.AirplaneModePolicy;
import org.flyve.mdm.agent.policies.BluetoothPolicy;
import org.flyve.mdm.agent.policies.GPSPolicy;
import org.flyve.mdm.agent.policies.HostpotTetheringPolicy;
import org.flyve.mdm.agent.policies.MobileLinePolicy;
import org.flyve.mdm.agent.policies.NFCPolicy;
import org.flyve.mdm.agent.policies.RoamingPolicy;
import org.flyve.mdm.agent.policies.WifiPolicy;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.policies.manager.CustomPolicies;

/**
 * Receive broadcast from android.net.wifi.STATE_CHANGE and android.bluetooth.adapter.action.STATE_CHANGED
 * on AndroidManifest.xml
 */
public class MQTTConnectivityReceiver extends BroadcastReceiver {

    /**
     * It is called when it receives information about the state of the connectivity of the WIFI, Bluetooth and GPS
     * @param context in which the receiver is running
     * @param intent being received
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        FlyveLog.d("Connectivity receiver: " + action);

        PoliciesData cache = new PoliciesData(context);
        CustomPolicies customPolicies = new CustomPolicies(context);

        if(action==null) {
            return;
        }

        try {
            // TELEPHONY MANAGER class object to register one listner
            TelephonyManager tmgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            //Create Listener
            CustomPhoneStateListener phoneListener = new CustomPhoneStateListener();

            // Register listener for LISTEN_CALL_STATE
            tmgr.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", onReceive", ex.getMessage());
        }

        if(action.contains("USB")) {
            FlyveLog.d("USB Device Attached");
            customPolicies.disableAllUsbFileTransferProtocols( true );
        }

        if("android.net.conn.CONNECTIVITY_CHANGE".equalsIgnoreCase(action)) {
            FlyveLog.i("is Online: %s", Helpers.isOnline(context));

            // Disable / Enable Roaming
            Boolean policy = Helpers.boolFromString(cache.getValue(RoamingPolicy.POLICY_NAME).value);
            if(policy) {
                customPolicies.disableRoaming(policy);
            }

            // Disable / Enable Mobile line
            policy = Helpers.boolFromString(cache.getValue(MobileLinePolicy.POLICY_NAME).value);
            if(policy) {
                customPolicies.disableMobileLine(policy);
            }
        }

        if("android.intent.action.AIRPLANE_MODE".equalsIgnoreCase(action)) {
            // Disable / Enable Airplane Mode
            Boolean policy = Helpers.boolFromString(cache.getValue(AirplaneModePolicy.POLICY_NAME).value);
            if(policy) {
                customPolicies.disableAirplaneMode(policy);
            }
        }

        // Manage WIFI
        if ("android.net.wifi.STATE_CHANGE".equalsIgnoreCase(action) || "android.net.wifi.WIFI_STATE_CHANGED".equalsIgnoreCase(action)) {
            FlyveLog.i("is Online: %s", Helpers.isOnline(context));

            // Disable / Enable Hostpot
            Boolean policy = Helpers.boolFromString(cache.getValue(HostpotTetheringPolicy.POLICY_NAME).value);
            if(policy) {
                customPolicies.disableWifi(policy);
            }

            // Disable / Enable Wifi
            policy = Helpers.boolFromString(cache.getValue(WifiPolicy.POLICY_NAME).value);
            if(policy) {
                customPolicies.disableHostpotTethering(policy);
            }
        }

        // Manage Bluetooth
        if ("android.bluetooth.adapter.action.STATE_CHANGED".equalsIgnoreCase(action)) {
            Boolean policy = Helpers.boolFromString(cache.getValue(BluetoothPolicy.POLICY_NAME).value);
            if(policy) {
                customPolicies.disableBluetooth(policy);
            }
        }

        // Manage NFC
        if("android.nfc.extra.ADAPTER_STATE".equalsIgnoreCase(action)) {
            Boolean policy = Helpers.boolFromString(cache.getValue(NFCPolicy.POLICY_NAME).value);
            if(policy) {
                customPolicies.disableNFC(policy);
            }
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

            boolean disable = Helpers.boolFromString(cache.getValue(GPSPolicy.POLICY_NAME).value);
            customPolicies.disableGps(disable);
            FlyveLog.i("Location providers change: " + disable);
        }
    }
}
