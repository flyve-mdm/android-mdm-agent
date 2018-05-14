package org.flyve.mdm.agent.data;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
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
 * @date      8/4/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.flyve.mdm.agent.room.database.AppDataBase;
import org.flyve.mdm.agent.room.entity.Policies;

import java.util.List;

public class PoliciesData {

    private static final String PASSWORD_ENABLED = "passwordEnabled";
    private static final String PASSWORD_QUALITY = "passwordQuality";
    private static final String PASSWORD_MINIMUM_LENGTH = "passwordMinimumLength";
    private static final String PASSWORD_MINIMUM_LOWER_CASE = "passwordMinimumLowerCase";
    private static final String PASSWORD_MINIMUM_UPPER_CASE = "passwordMinimumUpperCase";
    private static final String PASSWORD_MINIMUM_NON_LETTER = "passwordMinimumNonLetter";
    private static final String PASSWORD_MINIMUM_LETTERS = "passwordMinimumLetters";
    private static final String PASSWORD_MINIMUM_NUMERIC = "passwordMinimumNumeric";
    private static final String PASSWORD_MINIMUM_SYMBOLS = "passwordMinimumSymbols";
    private static final String MAXIMUM_FAILED_PASSWORDS_FOR_WIPE = "maximumFailedPasswordsForWipe";
    private static final String MAXIMUM_TIME_TO_LOCK = "maximumTimeToLock";
    private static final String STORAGE_ENCRYPTION = "storageEncryption";
    private static final String DISABLE_CAMERA = "disableCamera";
    private static final String DISABLE_BLUETOOTH = "disableBluetooth";
    private static final String DISABLE_SCREEN_CAPTURE = "disableScreenCapture";
    private static final String DISABLE_AIRPLANE_MODE = "disableAirplaneMode";
    private static final String DISABLE_GPS = "disableGPS";
    private static final String DISABLE_HOSTPOT_TETHERING = "disableHostpotTethering";
    private static final String DISABLE_ROAMING = "disableRoaming";
    private static final String DISABLE_WIFI = "disableWifi";
    private static final String USE_TLS = "useTLS";
    private static final String DISABLE_MOBILE_LINE = "disableMobileLine";
    private static final String DISABLE_NFC = "disableNFC";
    private static final String DISABLE_STATUSBAR = "disableStatusbar";
    private static final String DISABLE_USB_MTP = "disableUsbMtp";
    private static final String DISABLE_USB_PTP = "disableUsbPtp";
    private static final String DISABLE_USB_ADB = "disableUsbAdb";
    private static final String DISABLE_SPEAKER_PHONE = "disableSpeakerphone";
    private static final String DISABLE_VPN = "disableVPN";
    private static final String DISABLE_SMS_MMS = "disableSmsMms";

    private AppDataBase dataBase;

    public PoliciesData(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
    }

    public boolean getPasswordEnabled() {
        return getBooleanValue(PASSWORD_ENABLED);
    }

    public boolean setPasswordEnabled(Boolean enable, int priority) {
        return setBooleanValue(PASSWORD_ENABLED, enable, priority);
    }

    public String getPasswordQuality() {
        return getStringValue(PASSWORD_QUALITY);
    }

    public String setPasswordQuality(String value, int priority) {
        return setStringValue(PASSWORD_QUALITY, value, priority);
    }

    public int getPasswordMinimumLength() {
        return getIntValue(PASSWORD_MINIMUM_LENGTH);
    }

    public int setPasswordMinimumLength(int value, int priority) {
        return setIntValue(PASSWORD_MINIMUM_LENGTH, value, priority);
    }

    public int getPasswordMinimumLowerCase() {
        return getIntValue(PASSWORD_MINIMUM_LOWER_CASE);
    }

    public void setPasswordMinimumLowerCase(int value, int priority) {
        setIntValue(PASSWORD_MINIMUM_LOWER_CASE, value, priority);
    }

    public int getPasswordMinimumUpperCase() {
        return getIntValue(PASSWORD_MINIMUM_UPPER_CASE);
    }

    public int setPasswordMinimumUpperCase(int value, int priority) {
        return setIntValue(PASSWORD_MINIMUM_UPPER_CASE, value, priority);
    }

    public int getPasswordMinimumNonLetter() {
        return getIntValue(PASSWORD_MINIMUM_NON_LETTER);
    }

    public int setPasswordMinimumNonLetter(int value, int priority) {
        return setIntValue(PASSWORD_MINIMUM_NON_LETTER, value, priority);
    }

    public int getPasswordMinimumLetters() {
        return getIntValue(PASSWORD_MINIMUM_LETTERS);
    }

    public int setPasswordMinimumLetters(int value, int priority) {
        return setIntValue(PASSWORD_MINIMUM_LETTERS, value, priority);
    }

    public int getPasswordMinimumNumeric() {
        return getIntValue(PASSWORD_MINIMUM_NUMERIC);
    }

    public int setPasswordMinimumNumeric(int value, int priority) {
        return setIntValue(PASSWORD_MINIMUM_NUMERIC, value, priority);
    }

    public int getPasswordMinimumSymbols() {
        return getIntValue(PASSWORD_MINIMUM_SYMBOLS);
    }

    public int setPasswordMinimumSymbols(int value, int priority) {
        return setIntValue(PASSWORD_MINIMUM_SYMBOLS, value, priority);
    }

    public int getMaximumFailedPasswordsForWipe() {
        return getIntValue(MAXIMUM_FAILED_PASSWORDS_FOR_WIPE);
    }

    public int setMaximumFailedPasswordsForWipe(int value, int priority) {
        return setIntValue(MAXIMUM_FAILED_PASSWORDS_FOR_WIPE, value, priority);
    }

    public int getMaximumTimeToLock() {
        return getIntValue(MAXIMUM_TIME_TO_LOCK);
    }

    public int setMaximumTimeToLock(int value, int priority) {
        return setIntValue(MAXIMUM_TIME_TO_LOCK, value, priority);
    }

    public boolean getStorageEncryption() {
        return getBooleanValue(STORAGE_ENCRYPTION);
    }

    public boolean setStorageEncryption(boolean value, int priority) {
        return setBooleanValue(STORAGE_ENCRYPTION, value, priority);
    }

    public boolean getDisableCamera() {
        return getBooleanValue(DISABLE_CAMERA);
    }

    public boolean setDisableCamera(boolean value, int priority) {
        return setBooleanValue(DISABLE_CAMERA, value, priority);
    }

    public boolean getDisableBluetooth() {
        return getBooleanValue(DISABLE_BLUETOOTH);
    }

    public boolean setDisableBluetooth(boolean value, int priority) {
        return setBooleanValue(DISABLE_BLUETOOTH, value, priority);
    }

    public boolean getDisableScreenCapture() {
        return getBooleanValue(DISABLE_SCREEN_CAPTURE);
    }

    public boolean setDisableScreenCapture(boolean value, int priority) {
        return setBooleanValue(DISABLE_SCREEN_CAPTURE, value, priority);
    }

    public boolean getDisableAirplaneMode() {
        return getBooleanValue(DISABLE_AIRPLANE_MODE);
    }

    public boolean setDisableAirplaneMode(boolean value, int priority) {
        return setBooleanValue(DISABLE_AIRPLANE_MODE, value, priority);
    }

    public boolean getDisableGPS() {
        return getBooleanValue(DISABLE_GPS);
    }

    public boolean setDisableGPS(boolean value, int priority) {
        return setBooleanValue(DISABLE_GPS, value, priority);
    }

    public boolean getDisableHostpotTethering() {
        return getBooleanValue(DISABLE_HOSTPOT_TETHERING);
    }

    public boolean setDisableHostpotTethering(boolean value, int priority) {
        return setBooleanValue(DISABLE_HOSTPOT_TETHERING, value, priority);
    }

    public boolean getDisableRoaming() {
        return getBooleanValue(DISABLE_ROAMING);
    }

    public boolean setDisableRoaming(boolean value, int priority) {
        return setBooleanValue(DISABLE_ROAMING, value, priority);
    }

    public boolean getDisableWifi() {
        return getBooleanValue(DISABLE_WIFI);
    }

    public boolean setDisableWifi(boolean value, int priority) {
        return setBooleanValue(DISABLE_WIFI, value, priority);
    }

    public boolean getUseTLS() {
        return getBooleanValue(USE_TLS);
    }

    public boolean setUseTLS(boolean value, int priority) {
        return setBooleanValue(USE_TLS, value, priority);
    }

    public boolean getDisableMobileLine() {
        return getBooleanValue(DISABLE_MOBILE_LINE);
    }

    public boolean setDisableMobileLine(boolean value, int priority) {
        return setBooleanValue(DISABLE_MOBILE_LINE, value, priority);
    }

    public boolean getDisableNFC() {
        return getBooleanValue(DISABLE_NFC);
    }

    public boolean setDisableNFC(boolean value, int priority) {
        return setBooleanValue(DISABLE_NFC, value, priority);
    }

    public boolean getDisableStatusbar() {
        return getBooleanValue(DISABLE_STATUSBAR);
    }

    public boolean setDisableStatusbar(boolean value, int priority) {
        return setBooleanValue(DISABLE_STATUSBAR, value, priority);
    }

    public boolean getDisableUsbMtp() {
        return getBooleanValue(DISABLE_USB_MTP);
    }

    public boolean setDisableUsbMtp(boolean value, int priority) {
        return setBooleanValue(DISABLE_USB_MTP, value, priority);
    }

    public boolean getDisableUsbPtp() {
        return getBooleanValue(DISABLE_USB_PTP);
    }

    public boolean setDisableUsbPtp(boolean value, int priority) {
        return setBooleanValue(DISABLE_USB_PTP, value, priority);
    }

    public boolean getDisableUsbAdb() {
        return getBooleanValue(DISABLE_USB_ADB);
    }

    public boolean setDisableUsbAdb(boolean value, int priority) {
        return setBooleanValue(DISABLE_USB_ADB, value, priority);
    }

    public boolean getDisableSpeakerphone() {
        return getBooleanValue(DISABLE_SPEAKER_PHONE);
    }

    public boolean setDisableSpeakerphone(boolean value, int priority) {
        return setBooleanValue(DISABLE_SPEAKER_PHONE, value, priority);
    }

    public boolean getDisableVPN() {
        return getBooleanValue(DISABLE_VPN);
    }

    public boolean setDisableVPN(boolean value, int priority) {
        return setBooleanValue(DISABLE_VPN, value, priority);
    }

    public boolean getDisableSmsMms() {
        return getBooleanValue(DISABLE_SMS_MMS);
    }

    public boolean setDisableSmsMms(boolean value, int priority) {
        return setBooleanValue(DISABLE_SMS_MMS, value, priority);
    }

    private String getStringValue(String policyName) {
        List<Policies> arrPolicies = dataBase.PoliciesDao().getPolicyByName(policyName);
        if(!arrPolicies.isEmpty()) {
            return arrPolicies.get(0).value;
        } else {
            return "";
        }
    }

    private String setStringValue(String policyName, String value, int priority) {
        if(dataBase.PoliciesDao().getPolicyBy(policyName, priority).isEmpty()) {
            Policies policies = new Policies();
            policies.policyName = policyName;
            policies.value = value;
            policies.priority = priority;
            dataBase.PoliciesDao().insert(policies);
        } else {
            Policies policies = dataBase.PoliciesDao().getPolicyBy(policyName, priority).get(0);
            policies.value = value;
            dataBase.PoliciesDao().update(policies);
        }

        // Return the priority value
        Policies policies = dataBase.PoliciesDao().getPolicyByName(policyName).get(0);
        return policies.value;
    }

    private boolean getBooleanValue(String policyName) {
        String value = getStringValue(policyName);
        if(value.equals("")) {
            value = "false";
        }
        return Boolean.valueOf(value);
    }

    private Boolean setBooleanValue(String policyName, Boolean enable, int priority) {
        String data = setStringValue(policyName, String.valueOf(enable), priority);
        if(data.equals("")) {
            data = "false";
        }
        return Boolean.valueOf(data);
    }

    private int getIntValue(String policyName) {
        String value = getStringValue(policyName);
        if(value.equals("")) {
            value = "0";
        }
        return Integer.parseInt(value);
    }

    private int setIntValue(String policyName, int value, int priority) {
        String data = setStringValue(policyName, String.valueOf(value), priority);
        if(data.equals("")) {
            data = "0";
        }
        return Integer.parseInt(data);
    }

}
