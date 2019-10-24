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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;

import org.flyve.policies.utils.FlyveLog;
import org.flyve.policies.utils.Helpers;

import static android.app.admin.DevicePolicyManager.WIPE_EXTERNAL_STORAGE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;

public class AndroidPolicies {

    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;
    private Context context;

    public AndroidPolicies(Context context, Class<?> adminReceiver) {
        this.context = context;
        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(context, adminReceiver);
    }

    @TargetApi(21)
    public void disableRoaming(boolean disable) {
        if(Build.VERSION.SDK_INT >= 21) {
            try {
                mDPM.setGlobalSetting(mDeviceAdmin, Settings.Global.DATA_ROAMING, disable ? "0" : "1");
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", disableRoaming", ex.getMessage());
            }
        } else {
            FlyveLog.i("Disable roaming policy is available on devices with api equals or mayor than 21");
        }
    }

    @TargetApi(21)
    public void disableCaptureScreen(boolean disable) {
        if(Build.VERSION.SDK_INT >= 21) {
            try {
                mDPM.setScreenCaptureDisabled(mDeviceAdmin, disable);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", disableCaptureScreen", ex.getMessage());
            }
        } else {
            FlyveLog.i("Screen capture policy is available on devices with api equals or mayor than 21");
        }
    }

    @TargetApi(24)
    public void disableVPN(Boolean disable) {
        if(Build.VERSION.SDK_INT >= 24) {
            try {
                mDPM.setAlwaysOnVpnPackage(mDeviceAdmin, null, !disable);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", disableVPN", ex.getMessage());
            }
        } else {
            FlyveLog.i("VPN policy is available on devices with api equals or mayor than 24");
        }
    }

    @TargetApi(23)
    public void disableStatusBar(boolean disable) {
        if(Build.VERSION.SDK_INT >= 23) {
            try {
                mDPM.setStatusBarDisabled(mDeviceAdmin, disable);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", disableStatusBar", ex.getMessage());
            }
        }
    }

    public void enablePassword(boolean enable, String typeRecommended, Class<?> mainActivity) {
        if(enable) {
            DeviceLockedController pwd = new DeviceLockedController(context);
            if (pwd.isDeviceScreenLocked()) {
                try {
                    if (!mDPM.isActivePasswordSufficient()) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", enablePassword", ex.getMessage());
                }
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                String type = typeRecommended.equals("PASSWORD_PASSWD") ? "Password" : "PIN Password";
                Helpers.sendToNotificationBar(context, 1009, "MDM Agent", "Please create a " + type, true, mainActivity, "PasswordPolicy");
            }
        }
    }

    public void reboot() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            pm.reboot(null);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", reboot", ex.getMessage());
        }
    }

    public void resetPassword(String newPassword) {
        mDPM.resetPassword(newPassword, 0);
    }

    /**
     * Erase all data of the device
     */
    public void wipe() {
        mDPM.wipeData(WIPE_EXTERNAL_STORAGE);
    }

    /**
     * Launch lock activity
     */
    public void lockScreen(Class<?> lockActivity, Context context) {
        if ( Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(context, lockActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * Lock the device now
     */
    public void lockDevice() {
        mDPM.lockNow();
    }

    /**
     * Lock the device now
     */
    public void unlockDevice() {
        PowerManager.WakeLock screenLock = ((PowerManager)context.getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AndroidPolicies:unlock");
        screenLock.acquire();
        screenLock.release();
    }


    /**
     * Request to user encrypt files
     */
    public void storageEncryptionDeviceRequest() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_START_ENCRYPTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(intent);
    }

    /**
     * Encrypt Storage from dashboard
     * @param isEncryption boolean
     */
    public void storageEncryptionDevice(boolean isEncryption) {
        int status = mDPM.getStorageEncryptionStatus();
        FlyveLog.d("status: " + status);

        if(isEncryption && status == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE) {
            // the data is already encrypted
            return;
        }

        if(isEncryption && status == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING) {
            // the encryption is working
            return;
        }

        if (status != DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
            // encrypt file mute
            // 3 = ENCRYPTION_STATUS_ACTIVE
            // 1 = ENCRYPTION_STATUS_INACTIVE
            // 5 = ENCRYPTION_STATUS_ACTIVE_PER_USER
            // 4 = ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY
            // 2 = ENCRYPTION_STATUS_ACTIVATING
            int isEncrypt = mDPM.setStorageEncryption(mDeviceAdmin, isEncryption);
            FlyveLog.d("setStorageEncryption: " + isEncrypt);
        } else {
            FlyveLog.d("Storage Encryption unsupported");
        }
    }

    /**
     * Disable the possibility to use the camera
     * @param disable boolean true | false
     */
    public void disableCamera(boolean disable) {
        mDPM.setCameraDisabled(mDeviceAdmin, disable);
    }

    /**
     * Set password length
     * @param length int
     */
    public void setPasswordLength(int length) {
        if(mDPM.getPasswordMinimumLength(mDeviceAdmin)!=length){
            FlyveLog.d("PasswordLength: " + length);
            mDPM.setPasswordMinimumLength(mDeviceAdmin, length);
        }
    }

    /**
     * Set password quality
     * @param quality String quality type
     */
    public void setPasswordQuality(String quality) {
         if("PASSWORD_QUALITY_NUMERIC".equalsIgnoreCase(quality)) {
             FlyveLog.d("switch: PASSWORD_QUALITY_NUMERIC");
             mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
         }

         if("PASSWORD_QUALITY_ALPHABETIC".equalsIgnoreCase(quality)) {
            FlyveLog.d("switch: PASSWORD_QUALITY_ALPHABETIC");
            mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
        }

        if("PASSWORD_QUALITY_ALPHANUMERIC".equalsIgnoreCase(quality)) {
            FlyveLog.d("switch: PASSWORD_QUALITY_ALPHANUMERIC");
            mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);
        }

        if("PASSWORD_QUALITY_COMPLEX".equalsIgnoreCase(quality)) {
            FlyveLog.d("switch: PASSWORD_QUALITY_COMPLEX");
            mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
        }

        if("PASSWORD_QUALITY_SOMETHING".equalsIgnoreCase(quality)) {
            FlyveLog.d("switch: PASSWORD_QUALITY_SOMETHING");
            mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
        }

        if("PASSWORD_QUALITY_UNSPECIFIED".equalsIgnoreCase(quality)) {
            FlyveLog.d("switch: PASSWORD_QUALITY_UNSPECIFIED");
            mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        }
    }

    /**
     * Set Password minumim letters
     * @param minLetters int
     */
    public void setPasswordMinimumLetters(int minLetters) {
        if(mDPM.getPasswordMinimumLetters(mDeviceAdmin)!=minLetters) {
            FlyveLog.d("PasswordMinimumLetters:  " + minLetters);

            if (mDPM.getPasswordQuality(mDeviceAdmin) != DevicePolicyManager.PASSWORD_QUALITY_COMPLEX) {
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
            }

            mDPM.setPasswordMinimumLetters(mDeviceAdmin, minLetters);
        }
    }

    /**
     * set Password Minimum Lower Case
     * @param minLowerCase int
     */
    public void setPasswordMinimumLowerCase(int minLowerCase) {
        if(mDPM.getPasswordMinimumLowerCase(mDeviceAdmin)!=minLowerCase) {
            FlyveLog.d("setPasswordMinimumLowerCase:  " + minLowerCase);

            if (mDPM.getPasswordQuality(mDeviceAdmin) != DevicePolicyManager.PASSWORD_QUALITY_COMPLEX) {
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
            }

            mDPM.setPasswordMinimumLowerCase(mDeviceAdmin, minLowerCase);
        }
    }

    /**
     * set Password Minimum Upper Case
     * @param minUpperCase int
     */
    public void setPasswordMinimumUpperCase(int minUpperCase) {
        if(mDPM.getPasswordMinimumUpperCase(mDeviceAdmin)!=minUpperCase) {
            FlyveLog.d("setPasswordMinimumUpperCase:  " + minUpperCase);

            if (mDPM.getPasswordQuality(mDeviceAdmin) != DevicePolicyManager.PASSWORD_QUALITY_COMPLEX) {
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
            }

            mDPM.setPasswordMinimumUpperCase(mDeviceAdmin, minUpperCase);
        }
    }

    /**
     * set Password Minimum Non Letter
     * @param minNonLetter int
     */
    public void setPasswordMinimumNonLetter(int minNonLetter) {
        if(mDPM.getPasswordMinimumNonLetter(mDeviceAdmin)!=minNonLetter) {
            FlyveLog.d("setPasswordMinimumNonLetter: " + minNonLetter);

            if (mDPM.getPasswordQuality(mDeviceAdmin) != DevicePolicyManager.PASSWORD_QUALITY_COMPLEX) {
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
            }

            mDPM.setPasswordMinimumNonLetter(mDeviceAdmin, minNonLetter);
        }
    }

    /**
     * set Password Minimum Numeric
     * @param minNumeric int
     */
    public void setPasswordMinimumNumeric(int minNumeric) {
        if(mDPM.getPasswordMinimumNumeric(mDeviceAdmin)!=minNumeric) {
            FlyveLog.d("setPasswordMinimumNumeric:  " + minNumeric);

            if (mDPM.getPasswordQuality(mDeviceAdmin) != DevicePolicyManager.PASSWORD_QUALITY_COMPLEX) {
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
            }

            mDPM.setPasswordMinimumNumeric(mDeviceAdmin, minNumeric);
        }
    }

    /**
     * set Password Minimum Symbols
     * @param minSymbols int
     */
    public void setPasswordMinimumSymbols(int minSymbols) {
        if(mDPM.getPasswordMinimumSymbols(mDeviceAdmin)!=minSymbols) {
            FlyveLog.d("setPasswordMinimumSymbols:  " + minSymbols);

            if (mDPM.getPasswordQuality(mDeviceAdmin) != DevicePolicyManager.PASSWORD_QUALITY_COMPLEX) {
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
            }

            mDPM.setPasswordMinimumSymbols(mDeviceAdmin, minSymbols);
        }
    }

    /**
     * set Maximum Failed Passwords For Wipe
     * @param maxFailed int
     */
    public void setMaximumFailedPasswordsForWipe(int maxFailed) {
        if(mDPM.getMaximumFailedPasswordsForWipe(mDeviceAdmin)!=maxFailed) {
            FlyveLog.d("setMaximumFailedPasswordsForWipe:  " + maxFailed);
            mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, maxFailed);
        }
    }

    /**
     * set Maximum Time To Lock
     * @param timeMs
     */
    public void setMaximumTimeToLock(long timeMs) {
        if(mDPM.getMaximumTimeToLock(mDeviceAdmin)!=timeMs) {
            FlyveLog.d("setMaximumTimeToLock:  " + timeMs);
            mDPM.setMaximumTimeToLock(mDeviceAdmin, timeMs);
        }
    }
}
