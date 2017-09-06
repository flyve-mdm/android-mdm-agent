/*
 *   Copyright © 2017 Teclib. All rights reserved.
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
 * @copyright Copyright © 2017 Teclib. All rights reserved.
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
	 * @param boolean the status
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
	 * @param string the Manifest version
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
	 * @param string the url
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
	 * @param string the user token
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
	 * @param string the invitation token
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
	 * @param string the session token
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
	 * @param string the profile ID
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
	 * @param string the agent ID
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
	 * @param string the broker
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
	 * @param string the port
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
	 * @param string the TLS
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
	 * @param string the topic
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
	 * @param string the MQTT user
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
	 * @param string the MQTT password
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
	 * @param string the certificate
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
	 * @param string the name
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
	 * @param string the computer ID
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
	 * @param string the entities ID
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
	 * @param string the Fleets ID
	 */
	public void setPluginFlyvemdmFleetsId(String pluginFlyvemdmFleetsId) {
		setData("plugin_flyvemdm_fleets_id", pluginFlyvemdmFleetsId);
	}

	/**
	 * Set the state of the Connectivity of the Wifi
	 * @param boolean the state of the connectivity, true if disabled, false otherwise
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
	 * @param boolean the state of the connectivity, true if disabled, false otherwise
	 */
	public void setConnectivityBluetoothDisable(boolean disable) {
		setData("ConnectivityBluetoothDisable", String.valueOf(disable));
	}

	public boolean getConnectivityBluetoothDisable() {
		return Boolean.valueOf(getData("ConnectivityBluetoothDisable"));
	}

	public void setConnectivityGPSDisable(boolean disable) {
		setData("ConnectivityGPSDisable", String.valueOf(disable));
	}

	public boolean getConnectivityGPSDisable() {
		return Boolean.valueOf(getData("ConnectivityGPSDisable"));
	}

	public void setEasterEgg(boolean enable) {
		setData("easterEgg", String.valueOf(enable));
	}

	public boolean getEasterEgg() {
		return Boolean.valueOf(getData("easterEgg"));
	}

	private String getData(String key){
		String data = "";
		SharedPreferences sp = getSettings();
		if(sp != null) {
			data = sp.getString(key, null);
		}
		return data;
	}

	private void setData(String key, String value) {
		SharedPreferences sp = getSettings();
		if(sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(key, value );
			editor.apply();
		}
	}

	public void clearSettings(){
		SharedPreferences sp = getSettings();
		if(sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.clear();
			editor.apply();
		}
	}

	public void deleteKeyCache(String llave){
		SharedPreferences sp = getSettings();
		if(sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.remove(llave);
			editor.apply();
		}
	}
}
