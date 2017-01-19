/*
 * Copyright (C) 2010 The Android Open Source Project
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


package com.teclib.api;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import com.teclib.database.SharedPreferencePolicies;
import com.teclib.flyvemdm.R;
import com.teclib.service.NotificationPasswordPolicies;

import java.util.ArrayList;

/**
 * This activity provides a comprehensive UI for exploring and operating the DevicePolicyManager
 * api.  It consists of two primary modules:
 * <p>
 * 1:  A device policy controller, implemented here as a series of preference fragments.  Each
 * one contains code to monitor and control a particular subset of device policies.
 * <p>
 * 2:  A DeviceAdminReceiver, to receive updates from the DevicePolicyManager when certain aspects
 * of the device security status have changed.
 */
public class DeviceAdmin extends Activity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    DevicePolicyManager mDPM;
    ComponentName mDeviceAdmin;
    SharedPreferencePolicies sharedPreferencePolicies;

    protected static Context mContext = null;


    protected void onCreate(Bundle savedInstanceState) {
        FlyveLog.d("DeviceAdmin onCreate");
        super.onCreate(savedInstanceState);
        mContext = this;
        // Prepare to work with the DPM
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(mContext, AdminReceiver.class);
        sharedPreferencePolicies = new SharedPreferencePolicies();
    }

    protected void onStart() {
        super.onStart();

        if(!isActiveAdmin()) {
            FlyveLog.d("onStart: Active DeviceAdmin mode");
            activeDeviceManagement();
        } else {
            CheckPasswordPolicies();
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                FlyveLog.d("onStart: Mode DeviceAdmin active");
                boolean isPoliciesPasswordChanged = false;
                ArrayList<String> Argslist =  getIntent().getStringArrayListExtra("ControllerArgs");

                FlyveLog.d("Controller args: %s", Argslist.toString());
                if(Argslist.contains("init")){
                    initPolicies();
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("passwordQuality")){
                    setPasswordQuality();
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("passwordMinLetters")){
                    setPasswordMinumimLetters(sharedPreferencePolicies.getMinLetters(mContext));
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("passwordMinLowerCase")){
                    setPasswordMinimumLowerCase(sharedPreferencePolicies.getMinLowerCase(mContext));
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("passwordMinUpperCase")){
                    setPasswordMinimumUpperCase(sharedPreferencePolicies.getMinUpperCase(mContext));
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("passwordMinNonLetter")){
                    setPasswordMinimumNonLetter(sharedPreferencePolicies.getMinNonLetter(mContext));
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("passwordMinNumeric")){
                    setPasswordMinimumNumeric(sharedPreferencePolicies.getMinNumeric(mContext));
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("passwordMinLength")){
                    setPasswordLength();
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("MaximumFailedPasswordsForWipe")){
                    setMaximumFailedPasswordsForWipe(sharedPreferencePolicies.getMaxFailedWipe(mContext));
                }
                if(Argslist.contains("MaximumTimeToLock")){
                    setMaximumTimeToLock(sharedPreferencePolicies.getMaxTimeToLock(mContext));
                }
                if(Argslist.contains("passwordMinSymbols")){
                    setPasswordMinimumSymbols(sharedPreferencePolicies.getMinSymbols(mContext));
                    isPoliciesPasswordChanged = true;
                }
                if(Argslist.contains("encryption")){
                    setStorageEncryption(sharedPreferencePolicies.getEncryption(mContext));
                }
                if(Argslist.contains("camera")){
                    disableCamera(sharedPreferencePolicies.getCameraStatus(mContext));
                }
                if(Argslist.contains("lock")){
                }
                if(Argslist.contains("wipe")){
                    WipeDevice();
                }
                if(isPoliciesPasswordChanged==true) {
                    CheckPasswordPolicies();
                }
            }
        }
        finish();
        return;
    }

    private void initPolicies() {
        setPasswordLength();
        setPasswordQuality();
        setPasswordMinumimLetters(sharedPreferencePolicies.getMinLetters(mContext));
        setPasswordMinimumLowerCase(sharedPreferencePolicies.getMinLowerCase(mContext));
        setPasswordMinimumUpperCase(sharedPreferencePolicies.getMinUpperCase(mContext));
        setPasswordMinimumNonLetter(sharedPreferencePolicies.getMinNonLetter(mContext));
        setPasswordMinimumNumeric(sharedPreferencePolicies.getMinNumeric(mContext));
        setMaximumFailedPasswordsForWipe(sharedPreferencePolicies.getMaxFailedWipe(mContext));
        setMaximumTimeToLock(sharedPreferencePolicies.getMaxTimeToLock(mContext));
        setPasswordMinimumSymbols(sharedPreferencePolicies.getMinSymbols(mContext));
        setStorageEncryption(sharedPreferencePolicies.getEncryption(mContext));
        disableCamera(sharedPreferencePolicies.getCameraStatus(mContext));
    }

    private void CheckPasswordPolicies() {
        FlyveLog.d("CheckPasswordPolicies");
        FlyveLog.d(mDPM.isActivePasswordSufficient());
        FlyveLog.d(mDPM.getPasswordMinimumLength(mDeviceAdmin));

        if (!mDPM.isActivePasswordSufficient()) {
            // Triggers password change screen in Settings.
            Intent intent = new Intent(this, NotificationPasswordPolicies.class);
            startService(intent);
        }
    }


    private void WipeDevice() {
        try {
            mDPM.wipeData(0);
        } catch(Exception e) {
            FlyveLog.e(e.getMessage());
        }
    }

    private void setPasswordLength() {
        int length = sharedPreferencePolicies.getMinLength(mContext);
        FlyveLog.d("setPasswordLength: " + length);
        mDPM.setPasswordMinimumLength(mDeviceAdmin, length);
    }


    private void setPasswordQuality() {
        String quality = sharedPreferencePolicies.getQualityPassword(mContext);
        switch(quality) {
            case "PASSWORD_QUALITY_NUMERIC":
                FlyveLog.d("switch: PASSWORD_QUALITY_NUMERIC");
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
                break;
            case "PASSWORD_QUALITY_ALPHABETIC":
                FlyveLog.d("switch: PASSWORD_QUALITY_ALPHABETIC");
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
                break;
            case "PASSWORD_QUALITY_ALPHANUMERIC":
                FlyveLog.d("switch: PASSWORD_QUALITY_ALPHANUMERIC");
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);
                break;
            case "PASSWORD_QUALITY_COMPLEX":
                FlyveLog.d("switch: PASSWORD_QUALITY_COMPLEX");
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
                break;
            case "PASSWORD_QUALITY_SOMETHING":
                FlyveLog.d("switch: PASSWORD_QUALITY_SOMETHING");
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
                break;
            case "PASSWORD_QUALITY_UNSPECIFIED":
                FlyveLog.d("switch: PASSWORD_QUALITY_UNSPECIFIED");
                mDPM.setPasswordQuality(mDeviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
                break;
            default:

        }

    }

    private void setPasswordMinumimLetters(int minLetters) {
        FlyveLog.d("setPasswordMinumimLetters:  " + minLetters);
        mDPM.setPasswordMinimumUpperCase(mDeviceAdmin, minLetters);
    }

    private void setPasswordMinimumLowerCase(int minLowerCase) {
        FlyveLog.d("setPasswordMinimumLowerCase:  " + minLowerCase);
        mDPM.setPasswordMinimumLowerCase(mDeviceAdmin, minLowerCase);
    }

    private void setPasswordMinimumUpperCase(int minUpperCase) {
        FlyveLog.d("setPasswordMinimumUpperCase:  " + minUpperCase);
        mDPM.setPasswordMinimumUpperCase(mDeviceAdmin, minUpperCase);
    }

    private void setPasswordMinimumNonLetter(int minNonLetter) {
        FlyveLog.d("setPasswordMinimumNonLetter: " + minNonLetter);
        mDPM.setPasswordMinimumNonLetter(mDeviceAdmin, minNonLetter);
    }

    private void setPasswordMinimumNumeric(int minNumeric) {
        FlyveLog.d("setPasswordMinimumNumeric:  " + minNumeric);
        mDPM.setPasswordMinimumNumeric(mDeviceAdmin, minNumeric);
    }

    private void setPasswordMinimumSymbols(int minSymbols) {
        FlyveLog.d("setPasswordMinimumSymbols:  " + minSymbols);
        mDPM.setPasswordMinimumSymbols(mDeviceAdmin, minSymbols);
    }

    private void setMaximumFailedPasswordsForWipe(int maxFailed) {
        FlyveLog.d("setMaximumFailedPasswordsForWipe:  " + maxFailed);
        mDPM.setMaximumFailedPasswordsForWipe(mDeviceAdmin, maxFailed);
    }

    private void setMaximumTimeToLock(long timeMs) {
        FlyveLog.d("setMaximumTimeToLock:  " + timeMs);
        mDPM.setMaximumTimeToLock(mDeviceAdmin, timeMs);
    }

    private void lockNow() {
        FlyveLog.d("lockNow:");
        mDPM.lockNow();
    }

    private void disableCamera(boolean isDisable) {
        FlyveLog.d("disableCamera:");
        mDPM.setCameraDisabled(mDeviceAdmin, isDisable);
    }

    private void setStorageEncryption(boolean isEncryption) {
        int status = mDPM.getStorageEncryptionStatus();
        int isEncrypt = mDPM.setStorageEncryption(mDeviceAdmin, isEncryption);
        FlyveLog.d("status: " + status);
        FlyveLog.d("setStorageEncryption: " + isEncrypt);
        if(isEncryption){

        }
        if(status != 3) {
            FlyveLog.d("status different de 3: ");
            // Intent intent = new Intent(getBaseContext(), NotificationPolicies.class);
            // getBaseContext().startService(intent);
        }

    }


    protected boolean activeDeviceManagement() {
        if(!isActiveAdmin()) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    this.getString(R.string.add_admin_extra_app_text));

            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            return true;
        } else {
            return true;
        }
    }

    /**
     * Helper to determine if we are an active admin
     */
    private boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdmin);
    }


    /**
     * Sample implementation of a DeviceAdminReceiver.  Your controller must provide one,
     * although you may or may not implement all of the methods shown here.
     * <p>
     * All callbacks are on the UI thread and your implementations should not engage in any
     * blocking operations, including disk I/O.
     */
    public static class AdminReceiver extends DeviceAdminReceiver {

        @Override
        public void onEnabled(Context context, Intent intent) {
            FlyveLog.d("onEnabled: ");
            FlyveLog.i(context.getString(R.string.admin_receiver_status_enabled));
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            FlyveLog.d("onDisableRequested: ");
            return context.getString(R.string.admin_receiver_status_disable_warning);
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            // Called when the app is about to be deactivated as a device administrator.
            // Deletes previously stored password policy.
            FlyveLog.d("onDisabled: ");
            FlyveLog.i(context.getString(R.string.admin_receiver_status_disabled));
        }

        @Override
        public void onPasswordChanged(Context context, Intent intent) {
            FlyveLog.d("onPasswordChanged: ");
            FlyveLog.i(context.getString(R.string.admin_receiver_status_pw_changed));
            DevicePolicyManager localDPM = (DevicePolicyManager) context
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            // Create the same Explicit Intent
            FlyveLog.d(localDPM.isActivePasswordSufficient());
            if (localDPM.isActivePasswordSufficient()){
                Intent i = new Intent("NotifyServiceActionKillNotification");
                context.sendBroadcast(i);
            }
        }

        @Override
        public void onPasswordFailed(Context context, Intent intent) {
            FlyveLog.d("onPasswordFailed: ");
            FlyveLog.i(context.getString(R.string.admin_receiver_status_pw_failed));
        }

        @Override
        public void onPasswordSucceeded(Context context, Intent intent) {
            FlyveLog.d("onPasswordSucceeded: ");
            FlyveLog.i(context.getString(R.string.admin_receiver_status_pw_succeeded));
        }

        @Override
        public void onPasswordExpiring(Context context, Intent intent) {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            long expr = dpm.getPasswordExpiration(
                    new ComponentName(context, AdminReceiver.class));
            long delta = expr - System.currentTimeMillis();
            boolean expired = delta < 0L;
            String message = context.getString(expired ?
                    R.string.expiration_status_past : R.string.expiration_status_future);
            FlyveLog.i(message);
        }

    }


}
