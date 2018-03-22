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

package org.flyve.mdm.agent.data;

import android.content.Context;

public class PoliciesData extends LocalStorage {

    /**
     * Constructor
     *
     * @param context
     */
    public PoliciesData(Context context) {
        super(context);
    }

    /**
     * Set the state of the Connectivity of the Wifi
     * @param disable the state of the connectivity, true if disabled, false otherwise
     */
    public void setConnectivityWifiDisable(boolean disable) {
        setData("ConnectivityWifiDisable", String.valueOf(disable));
    }

    /**
     * Get the state of the Connectivity of the Wifi
     * @return boolean the state of the connectivity, true if disabled, false otherwise
     */
    public boolean getConnectivityWifiDisable() {
        return Boolean.valueOf(getData("ConnectivityWifiDisable"));
    }

    /**
     * Set the state of the Connectivity of the Bluetooth
     * @param disable the state of the connectivity, true if disabled, false otherwise
     */
    public void setConnectivityBluetoothDisable(boolean disable) {
        setData("ConnectivityBluetoothDisable", String.valueOf(disable));
    }

    /**
     * Get the state of the Connectivity of the Bluetooth
     * @return boolean the state of the connectivity, true if disabled, false otherwise
     */
    public boolean getConnectivityBluetoothDisable() {
        return Boolean.valueOf(getData("ConnectivityBluetoothDisable"));
    }

    /**
     * Set the state of the Connectivity of the GPS
     * @param disable the state of the connectivity, true if disabled, false otherwise
     */
    public void setConnectivityGPSDisable(boolean disable) {
        setData("ConnectivityGPSDisable", String.valueOf(disable));
    }

    /**
     * Set the state of the Roaming enable / disable
     * @param disable
     */
    public void setConnectivityRoamingDisable(boolean disable) {
        setData("ConnectivityRoamingDisable", String.valueOf(disable));
    }

    /**
     * Set the state of the Airplane mode disable
     * @param disable
     */
    public void setConnectivityAirplaneModeDisable(boolean disable) {
        setData("ConnectivityAirplaneModeDisable", String.valueOf(disable));
    }

    /**
     * Set the state of the Radio FM enable / disable
     * @param disable
     */
    public void setConnectivityRadioFMDisable(boolean disable) {
        setData("ConnectivityRadioFMDisable", String.valueOf(disable));
    }

    /**
     * Set the state of the Mobile Line enable / disable
     * @param disable
     */
    public void setConnectivityMobileLineDisable(boolean disable) {
        setData("ConnectivityMobileLineDisable", String.valueOf(disable));
    }

    /**
     * Set the state of the NFC enable / disable
     * @param disable
     */
    public void setConnectivityNFCDisable(boolean disable) {
        setData("ConnectivityNFCDisable", String.valueOf(disable));
    }

    /**
     * Set the state of the Hostpot Tethering  enable / disable
     * @param disable
     */
    public void setConnectivityHostpotTetheringDisable(boolean disable) {
        setData("ConnectivityHostpotTetheringDisable", String.valueOf(disable));
    }

    /**
     * Set SMS / MMS  enable / disable
     * @param disable
     */
    public void setConnectivitySmsMmsDisable(boolean disable) {
        setData("ConnectivitySmsMmsDisable", String.valueOf(disable));
    }

    /**
     * Set Usb File Transfer Protocols  enable / disable
     * @param disable
     */
    public void setConnectivityUsbFileTransferProtocolsDisable(boolean disable) {
        setData("ConnectivityUsbFileTransferProtocolsDisable", String.valueOf(disable));
    }

    public void setConnectivityADBUsbFileTransferProtocolsDisable(boolean disable) {
        setData("ConnectivityADBUsbFileTransferProtocolsDisable", String.valueOf(disable));
    }

    public void setConnectivityPTPUsbFileTransferProtocolsDisable(boolean disable) {
        setData("ConnectivityPTPUsbFileTransferProtocolsDisable", String.valueOf(disable));
    }

    public void setConnectivityMTPUsbFileTransferProtocolsDisable(boolean disable) {
        setData("ConnectivityMTPUsbFileTransferProtocolsDisable", String.valueOf(disable));
    }

    public void setdisableSpeakerphone(boolean disable) {
        setData("ConnectivityDisableSpeakerphone", String.valueOf(disable));
    }

    public Boolean getdisableSpeakerphone() {
        return Boolean.valueOf(getData("ConnectivityDisableSpeakerphone"));
    }

    public Boolean getConnectivityADBUsbFileTransferProtocolsDisable() {
        return Boolean.valueOf(getData("ConnectivityADBUsbFileTransferProtocolsDisable"));
    }

    public Boolean getConnectivityPTPUsbFileTransferProtocolsDisable() {
        return Boolean.valueOf(getData("ConnectivityPTPUsbFileTransferProtocolsDisable"));
    }

    public Boolean getConnectivityMTPUsbFileTransferProtocolsDisable() {
        return Boolean.valueOf(getData("ConnectivityMTPUsbFileTransferProtocolsDisable"));
    }

    /**
     * Get the state of the Connectivity of the GPS
     * @return boolean the state of the connectivity, true if disabled, false otherwise
     */
    public boolean getConnectivityGPSDisable() {
        return Boolean.valueOf(getData("ConnectivityGPSDisable"));
    }

    public String getWifi() {
        return getData("ConnectivityWifiDisable");
    }

    public String getRoaming() {
        return getData("ConnectivityRoamingDisable");
    }

    public String getNFC() {
        return getData("ConnectivityNFCDisable");
    }

    public String getMobileLine() {
        return getData("ConnectivityMobileLineDisable");
    }

    public String getHostpotTethering() {
        return getData("ConnectivityHostpotTetheringDisable");
    }

    public String getUsbFileTransferProtocols() {
        return getData("ConnectivityUsbFileTransferProtocolsDisable");
    }

    public String getAirplaneMode() {
        return getData("ConnectivityAirplaneModeDisable");
    }

    /**
     * Get the state of the Roaming enable / disable
     */
    public boolean getConnectivityRoamingDisable() {
        return Boolean.valueOf(getData("ConnectivityRoamingDisable"));
    }

    /**
     * Get the state of the Radio FM enable / disable
     */
    public boolean getConnectivityRadioFMDisable() {
        return Boolean.valueOf(getData("ConnectivityRadioFMDisable"));
    }

    /**
     * Get the state of the Mobile Line enable / disable
     */
    public boolean getConnectivityMobileLineDisable() {
        return Boolean.valueOf(getData("ConnectivityMobileLineDisable"));
    }

    /**
     * Get the state of the NFC enable / disable
     */
    public boolean getConnectivityNFCDisable() {
        return Boolean.valueOf(getData("ConnectivityNFCDisable"));
    }

    /**
     * Get the state of the Hostpot Tethering  enable / disable
     */
    public boolean getConnectivityHostpotTetheringDisable() {
        return Boolean.valueOf(getData("ConnectivityHostpotTetheringDisable"));
    }

    /**
     * Get the state of the Hostpot Tethering  enable / disable
     */
    public boolean getConnectivityUsbFileTransferProtocolsDisable() {
        return Boolean.valueOf(getData("ConnectivityUsbFileTransferProtocolsDisable"));
    }

    /**
     * Get the state of the Airplane Mode  enable / disable
     */
    public boolean getConnectivityAirplaneModeDisable() {
        return Boolean.valueOf(getData("ConnectivityAirplaneModeDisable"));
    }

    /**
     * Get the state of SMS / MMS  enable / disable
     */
    public boolean getConnectivitySmsMmsDisable() {
        return Boolean.valueOf(getData("ConnectivitySmsMmsDisable"));
    }

    public void setStorageEncryptionDevice(boolean enable) {
        setData("StorageEncryptionDevice", String.valueOf(enable));
    }

    public boolean getStorageEncryptionDevice() {
        return Boolean.valueOf(getData("StorageEncryptionDevice"));
    }

    public void setDisableCamera(boolean enable) {
        setData("DisableCamera", String.valueOf(enable));
    }

    public boolean getDisableCamera() {
        return Boolean.valueOf(getData("DisableCamera"));
    }

    public void setPasswordLength(String value) {
        setData("PasswordLength", value);
    }

    public String getPasswordLength() {
        return getData("PasswordLength");
    }

    public void setPasswordQuality(String value) {
        setData("PasswordQuality", value);
    }

    public String getPasswordQuality() {
        return getData("PasswordQuality");
    }

    public void setPasswordMinimumLetters(String value) {
        setData("PasswordMinimumLetters", value);
    }

    public String getPasswordMinimumLetters() {
        return getData("PasswordMinimumLetters");
    }

    public void setPasswordMinimumLowerCase(String value) {
        setData("PasswordMinimumLowerCase", value);
    }

    public String getPasswordMinimumLowerCase() {
        return getData("PasswordMinimumLowerCase");
    }

    public void setPasswordMinimumUpperCase(String value) {
        setData("PasswordMinimumUpperCase", value);
    }

    public String getPasswordMinimumUpperCase() {
        return getData("PasswordMinimumUpperCase");
    }

    public void setPasswordMinimumNonLetter(String value) {
        setData("PasswordMinimumNonLetter", value);
    }

    public String getPasswordMinimumNonLetter() {
        return getData("PasswordMinimumNonLetter");
    }

    public void setPasswordMinimumNumeric(String value) {
        setData("PasswordMinimumNumeric", value);
    }

    public String getPasswordMinimumNumeric() {
        return getData("PasswordMinimumNumeric");
    }

    public void setPasswordMinimumSymbols(String value) {
        setData("PasswordMinimumSymbols", value);
    }

    public String getPasswordMinimumSymbols() {
        return getData("PasswordMinimumSymbols");
    }

    public void setMaximumFailedPasswordsForWipe(String value) {
        setData("MaximumFailedPasswordsForWipe", value);
    }

    public String getMaximumFailedPasswordsForWipe() {
        return getData("MaximumFailedPasswordsForWipe");
    }

    public void setMaximumTimeToLock(String value) {
        setData("MaximumTimeToLock", value);
    }

    public String getMaximumTimeToLock() {
        return getData("MaximumTimeToLock");
    }

}
