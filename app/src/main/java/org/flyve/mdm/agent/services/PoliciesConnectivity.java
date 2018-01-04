package org.flyve.mdm.agent.services;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.FlyveLog;

import java.io.DataOutputStream;
import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;

/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @date      8/11/17
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class PoliciesConnectivity {

    private PoliciesConnectivity() {}

    private static void executecmd(String[] cmds) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        }
        catch (Exception ex){
            FlyveLog.d(ex.getMessage());
        }
    }

    public static void disableGps(boolean disable) {
        String provider = Settings.Secure.getString(MDMAgent.getInstance().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(provider.trim().equals("")){
            return;
        }

        if(disable) {
            String[] cmds = {"cd /system/bin", "settings put secure location_providers_allowed -gps", "settings put secure location_providers_allowed -network" };
            executecmd(cmds);
        } else {
            String[] cmds = {"cd /system/bin", "settings put secure location_providers_allowed +gps", "settings put secure location_providers_allowed +network" };
            executecmd(cmds);
        }
    }

    public static void disableRoaming(boolean disable){
        if(Build.VERSION.SDK_INT>=21) {
            new PoliciesDeviceManager(MDMAgent.getInstance()).disableRoaming(disable);
        } else {
            // ROOT OPTION
            String value = "0"; // disable
            if (disable) {
                value = "1"; // disable
            }

            String[] cmds = {"cd /system/bin", "settings put global data_roaming " + value};
            executecmd(cmds);
        }
    }

    public static void disableNFC(boolean disable) {
        String value = "enable";
        if(disable) {
            value = "disable";
        }

        String[] cmds = {"svc nfc " + value};
        executecmd(cmds);
    }

    public static void disableMobileLine(boolean disable) {
        if(disable) {
            try {
                TelephonyManager tm = (TelephonyManager) MDMAgent.getInstance().getApplicationContext().getSystemService(TELEPHONY_SERVICE);

                Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
                m1.setAccessible(true);
                Object iTelephony = m1.invoke(tm);

                Method m2 = iTelephony.getClass().getDeclaredMethod("setRadio", boolean.class);

                m2.invoke(iTelephony, false);
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }
    }

    public static void disableAirplaneMode(boolean disable) {
        String value = "1"; // enable
        if(disable) {
            value = "0"; // disable

            String[] cmds = {"cd /system/bin", "settings put global airplane_mode_on " + value};
            executecmd(cmds);
        }
    }

    public static void disableHostpotTethering(boolean disable) {
        disableWifi(disable);
        disableBluetooth(disable);
    }

    public static void disableUsbFileTransferProtocols(boolean disable) {
        String value = "mtp,ptp,adb";
        if(disable) {
            value = "none";
        }

        String[] cmds = {"setprop persist.sys.usb.config " + value};

        executecmd(cmds);
    }

    public static void disableBluetooth(boolean disable) {
        if(disable) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter!=null && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public static void disableWifi(boolean disable) {
        WifiManager wifiManager = (WifiManager) MDMAgent.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager!=null && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(!disable);
        }
    }

    public static void disableSMS(boolean disable) {


    }

}
