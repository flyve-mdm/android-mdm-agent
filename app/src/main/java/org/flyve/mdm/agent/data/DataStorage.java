/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @date      02/06/2017
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.data;

import android.content.Context;
import android.content.SharedPreferences;

public class DataStorage {

	private static final String SHARED_PREFS_FILE = "FlyveHMPrefs";
	private Context mContext;

	/**
	 * Constructor 
	 * @param context
	 */
	public DataStorage(Context context){
		mContext = context;
	}

	/**
	 * Get preference from setting
	 * @return SharedPreferences
	 */
	private SharedPreferences getSettings(){
		if (mContext != null) {
		return mContext.getSharedPreferences(SHARED_PREFS_FILE, 0);
		} else {
			return null;
		}
	}

	/**
	 * Get the online status
	 * @return boolean the value represented by the string
	 */
	public boolean getOnlineStatus() {
		return Boolean.valueOf(getData("onlineStatus"));
	}

	/**
	 * Set the online status
	 * @param status enable / disable
	 */
	public void setOnlineStatus(boolean status) {
		setData("onlineStatus", String.valueOf(status));
	}

	/**
	 * Get the version of the Manifest
	 * @return string the Manifest version
	 */
	public String getManifestVersion() {
		return getData("manifestVersion");
	}

	/**
	 * Set the version of the Manifest
	 * @param version number of the version
	 */
	public void setManifestVersion(String version) {
		setData("manifestVersion", version);
	}

	/**
	 * Get the URL
	 * @return string the URL
	 */
	public String getUrl() {
		return getData("url");
	}

	/**
	 * Set the URL
	 * @param url
	 */
	public void setUrl(String url) {
		setData("url", url);
	}

	/**
	 * Get the token of the user
	 * @return string the user token
	 */
	public String getUserToken() {
		return getData("user_token");
	}

	/**
	 * Set the token of the user
	 * @param userToken
	 */
	public void setUserToken(String userToken) {
		setData("user_token", userToken);
	}

	/**
	 * Get the invitation token
	 * @return string the invitation token
	 */
	public String getInvitationToken() {
		return getData("invitation_token");
	}

	/**
	 * Set the invitation token
	 * @param invitationToken
	 */
	public void setInvitationToken(String invitationToken) {
		setData("invitation_token", invitationToken);
	}

	/**
	 * Get the session token
	 * @return string the session token
	 */
	public String getSessionToken() {
		return getData("session_token");
	}

	/**
	 * Set the session token
	 * @param sessionToken
	 */
	public void setSessionToken(String sessionToken) {
		setData("session_token", sessionToken);
	}

	/**
	 * Get the ID of the profile
	 * @return string the profile ID
	 */
	public String getProfileId() {
		return getData("profile_id");
	}

	/**
	 * Set the ID of the profile
	 * @param profileId
	 */
	public void setProfileId(String profileId) {
		setData("profile_id", profileId);
	}

	/**
	 * Get the ID of the agent
	 * @return string the agent ID
	 */
	public String getAgentId() {
		return getData("agent_id");
	}

	/**
	 * Set the ID of the agent
	 * @param agentId
	 */
	public void setAgentId(String agentId) {
		setData("agent_id", agentId);
	}

	/**
	 * Get the broker
	 * @return string the broker
	 */
	public String getBroker() {
		return getData("broker");
	}

	/**
	 * Set the broker
	 * @param broker
	 */
	public void setBroker(String broker) {
		setData("broker", broker);
	}

	/**
	 * Get the port
	 * @return string the port
	 */
	public String getPort() {
		return getData("port");
	}

	/**
	 * Set the port
	 * @param port
	 */
	public void setPort(String port) {
		setData("port", port);
	}

	/**
	 * Get the Transport Layer Security (TLS)
	 * @return string the TLS
	 */
	public String getTls() {
		return getData("tls");
	}

	/**
	 * Set the Transport Layer Security (TLS)
	 * @param tls
	 */
	public void setTls(String tls) {
		setData("tls", tls);
	}

	/**
	 * Get the topic
	 * @return string the topic
	 */
	public String getTopic() {
		return getData("topic");
	}

	/**
	 * Set the topic 
	 * @param topic
	 */
	public void setTopic(String topic) {
		setData("topic", topic);
	}

	/**
	 * Get the user of the Message Queue Telemetry Transport (MQTT)
	 * @return string the MQTT user
	 */
	public String getMqttuser() {
		return getData("mqttuser");
	}

	/**
	 * Set the user of the Message Queue Telemetry Transport (MQTT)
	 * @param mqttuser
	 */
	public void setMqttuser(String mqttuser) {
		setData("mqttuser", mqttuser);
	}

	/**
	 * Get the password of the Message Queue Telemetry Transport (MQTT)
	 * @return the MQTT password
	 */
	public String getMqttpasswd() {
		return getData("mqttpasswd");
	}

	/**
	 * Set the password of the Message Queue Telemetry Transport (MQTT)
	 * @param mqttpasswd
	 */
	public void setMqttpasswd(String mqttpasswd) {
		setData("mqttpasswd", mqttpasswd);
	}

	/**
	 * Get the Certificate
	 * @return string the certificate
	 */
	public String getCertificate() {
		return getData("certificate");
	}

	/**
	 * Set the Certificate
	 * @param certificate
	 */
	public void setCertificate(String certificate) {
		setData("certificate", certificate);
	}

	/**
	 * Get the name
	 * @return string the name
	 */
	public String getName() {
		return getData("name");
	}

	/**
	 * Set the name
	 * @param name
	 */
	public void setName(String name) {
		setData("name", name);
	}

	/**
	 * Get the ID of the computer
	 * @return string the computer ID
	 */
	public String getComputersId() {
		return getData("computers_id");
	}

	/**
	 * Set the ID of the computer
	 * @param computersId
	 */
	public void setComputersId(String computersId) {
		setData("computers_id", computersId);
	}

	/**
	 * Get the ID of the entities
	 * @return string the entities ID
	 */
	public String getEntitiesId() {
		return getData("entities_id");
	}

	/**
	 * Set the ID of the entities
	 * @param entitiesId
	 */
	public void setEntitiesId(String entitiesId) {
		setData("entities_id", entitiesId);
	}

	/**
	 * Get the ID of the Fleets of the Flyve MDM plugin
	 * @return string the Fleets ID
	 */
	public String getPluginFlyvemdmFleetsId() {
		return getData("plugin_flyvemdm_fleets_id");
	}

	/**
	 * Set the ID of the Fleets of the Flyve MDM plugin
	 * @param pluginFlyvemdmFleetsId
	 */
	public void setPluginFlyvemdmFleetsId(String pluginFlyvemdmFleetsId) {
		setData("plugin_flyvemdm_fleets_id", pluginFlyvemdmFleetsId);
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
	 * Set the state of the Airplane mode enable / disable
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

	/**
	 * Get the state of the Connectivity of the GPS
	 * @return boolean the state of the connectivity, true if disabled, false otherwise
	 */
	public boolean getConnectivityGPSDisable() {
		return Boolean.valueOf(getData("ConnectivityGPSDisable"));
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

	public String getSmsMms() {
		return getData("ConnectivitySmsMmsDisable");
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

	/**
	 * Set the state of the Easter Egg
	 * @param enable the state, true if enabled, false otherwise
	 */
	public void setEasterEgg(boolean enable) {
		setData("easterEgg", String.valueOf(enable));
	}

	/**
	 * Get the state of the Easter Egg
	 * @return boolean the state, true if enabled, false otherwise
	 */
	public boolean getEasterEgg() {
		return Boolean.valueOf(getData("easterEgg"));
	}

	/**
	 * Get the data matching the given argument
	 * @param key
	 * @return string the data
	 */
	private String getData(String key){
		String data = "";
		SharedPreferences sp = getSettings();
		if(sp != null) {
			data = sp.getString(key, null);
		}
		return data;
	}

	/**
	 * Set the data given in the argument to the Shared Preferences
	 * @param key
	 * @param value
	 */
	private void setData(String key, String value) {
		SharedPreferences sp = getSettings();
		if(sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(key, value );
			editor.apply();
		}
	}

	/**
	 * Removes all the values from the preferences
	 */
	public void clearSettings(){
		SharedPreferences sp = getSettings();
		if(sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.clear();
			editor.apply();
		}
	}

	/**
	 * Remove the key cache
	 * @param key value to remove
	 */
	public void deleteKeyCache(String key){
		SharedPreferences sp = getSettings();
		if(sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.remove(key);
			editor.apply();
		}
	}
}
