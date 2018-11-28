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

package org.flyve.policies.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import org.flyve.policies.utils.FlyveLog;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

public class CustomPolicies {

    private Context context;
    
    public CustomPolicies(Context context) {
        this.context = context;
    }

    private void executecmd(String[] cmds) {
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

    public void disableAlarm(boolean disable) {
        AudioManager aManager = (AudioManager) context.getApplicationContext().getSystemService(AUDIO_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if(disable) {
                    aManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                } else {
                    aManager.setStreamVolume(AudioManager.STREAM_ALARM, 100, 0);
                }
            } else {
                //media
                aManager.setStreamMute(AudioManager.STREAM_ALARM, disable);
            }
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
        }

    }

    public void disableSpeakerphone(final boolean disable) {
        // This policy is called when a call phone is running
        // review CustomPhoneStateLister to extends or
        // MQTTConnectivityReceiver for listener implementation

        final Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(disable);
                FlyveLog.d("incoming_call: speaker: " + disable);
            }
        }, 500);
    }

    public void disableGps(boolean disable) {
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
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

    public void disableRoaming(boolean disable){
        // ROOT OPTION
        String value = "0"; // disable
        if (disable) {
            value = "1"; // disable
        }

        String[] cmds = {"cd /system/bin", "settings put global data_roaming " + value};
        executecmd(cmds);
    }

    public void disableNFC(boolean disable) {
        String value = "enable";
        if(disable) {
            value = "disable";
        }

        String[] cmds = {"svc nfc " + value};
        executecmd(cmds);
    }

    public void disableMobileLine(boolean disable) {
        if(disable) {
            try {
                TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(TELEPHONY_SERVICE);

                Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
                m1.setAccessible(true);
                Object iTelephony = m1.invoke(tm);

                Method m2 = iTelephony.getClass().getDeclaredMethod("setRadio", boolean.class);

                m2.invoke(iTelephony, false);
            } catch (Exception ex) {
                FlyveLog.e(CustomPolicies.class.getClass().getName() + ", disableMobileLine", ex.getMessage());
            }
        }
    }

    public void disableAirplaneMode(boolean disable) {
        String value = "1"; // enable
        if(disable) {
            value = "0"; // disable

            String[] cmds = {"cd /system/bin", "settings put global airplane_mode_on " + value};
            executecmd(cmds);
        }
    }

    public void disableHostpotTethering(boolean disable) {
        disableWifi(disable);
        disableBluetooth(disable);
    }

    public void disableAllUsbFileTransferProtocols(boolean disable) {
        String value = "mtp,ptp,adb";
        if(disable) {
            value = "none";
        }

        String[] cmds = {"setprop persist.sys.usb.config " + value};

        executecmd(cmds);
    }

    public void disableADBUsbFileTransferProtocols(boolean disable) {
        String value = "adb";
        if(disable) {
            value = "none";
        }

        String[] cmds = {"setprop persist.sys.usb.config " + value};

        executecmd(cmds);
    }

    public void disablePTPUsbFileTransferProtocols(boolean disable) {
        String value = "ptp";
        if(disable) {
            value = "none";
        }

        String[] cmds = {"setprop persist.sys.usb.config " + value};

        executecmd(cmds);
    }

    public void disableMTPUsbFileTransferProtocols(boolean disable) {
        String value = "mtp";
        if(disable) {
            value = "none";
        }

        String[] cmds = {"setprop persist.sys.usb.config " + value};

        executecmd(cmds);
    }

    public void disableBluetooth(boolean disable) {
        if(disable) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter!=null && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public void disableWifi(boolean disable) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager!=null && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(!disable);
        }
    }

    public void disableSounds(int streamType, Boolean disable) {
        AudioManager aManager = (AudioManager)context.getApplicationContext().getSystemService(AUDIO_SERVICE);

        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int direction = disable ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_SAME;
                aManager.adjustStreamVolume(streamType, direction, 0);
            } else {
                //media
                aManager.setStreamMute(streamType, disable);
            }
        } catch (Exception ex) {
            FlyveLog.e(CustomPolicies.class.getClass().getName() + ", disableSounds", ex.getMessage());
        }
    }

}
