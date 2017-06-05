/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   com.teclib.data is part of flyve-mdm-android
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
 * @date      02/06/2017
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package com.teclib.data;

import android.content.Context;
import android.content.SharedPreferences;

public class DataStorage {

	private final String SHARED_PREFS_FILE = "FlyveHMPrefs";
	private Context mContext;

	/**
	 * Constructor 
	 * @param context
	 */
	public DataStorage(Context context){
		mContext = context;
	}

	/**
	 * Obtiene las preferencias del setting
	 * @return tipo de datos SharedPreferences
	 */
	private SharedPreferences getSettings(){
		if (mContext != null) {
		return mContext.getSharedPreferences(SHARED_PREFS_FILE, 0);
		} else {
			return null;
		}
	}

	public String getUrl() {
		return getData("url");
	}

	public void setUrl(String url) {
		setData("url", url);
	}

	public String getUser_token() {
		return getData("user_token");
	}

	public void setUser_token(String user_token) {
		setData("user_token", user_token);
	}

	public String getInvitation_token() {
		return getData("invitation_token");
	}

	public void setInvitation_token(String invitation_token) {
		setData("invitation_token", invitation_token);
	}

	public String getSession_token() {
		return getData("session_token");
	}

	public void setSession_token(String session_token) {
		setData("session_token", session_token);
	}

	public String getProfile_id() {
		return getData("profile_id");
	}

	public void setProfile_id(String profile_id) {
		setData("profile_id", profile_id);
	}

	public String getAgent_id() {
		return getData("agent_id");
	}

	public void setAgent_id(String agent_id) {
		setData("agent_id", agent_id);
	}

	public String getBroker() {
		return getData("broker");
	}

	public void setBroker(String broker) {
		setData("broker", broker);
	}

	public String getPort() {
		return getData("port");
	}

	public void setPort(String port) {
		setData("port", port);
	}

	public String getTls() {
		return getData("tls");
	}

	public void setTls(String tls) {
		setData("tls", tls);
	}

	public String getTopic() {
		return getData("topic");
	}

	public void setTopic(String topic) {
		setData("topic", topic);
	}

	public String getMqttuser() {
		return getData("mqttuser");
	}

	public void setMqttuser(String mqttuser) {
		setData("mqttuser", mqttuser);
	}

	public String getMqttpasswd() {
		return getData("mqttpasswd");
	}

	public void setMqttpasswd(String mqttpasswd) {
		setData("mqttpasswd", mqttpasswd);
	}

	public String getCertificate() {
		return getData("certificate");
	}

	public void setCertificate(String certificate) {
		setData("certificate", certificate);
	}

	public String getName() {
		return getData("name");
	}

	public void setName(String name) {
		setData("name", name);
	}

	public String getComputers_id() {
		return getData("computers_id");
	}

	public void setComputers_id(String computers_id) {
		setData("computers_id", computers_id);
	}

	public String getEntities_id() {
		return getData("entities_id");
	}

	public void setEntities_id(String entities_id) {
		setData("entities_id", entities_id);
	}

	public String getPlugin_flyvemdm_fleets_id() {
		return getData("plugin_flyvemdm_fleets_id");
	}

	public void setPlugin_flyvemdm_fleets_id(String plugin_flyvemdm_fleets_id) {
		setData("plugin_flyvemdm_fleets_id", plugin_flyvemdm_fleets_id);
	}

	private String getData(String key){
		return getSettings().getString(key, null);
	}

	private void setData(String key, String value){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.putString(key, value );
			editor.apply();
		}
	}

	public void clearSettings(){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.clear();
			editor.apply();
		}
	}

	public void deleteKeyCache(String llave){
		if(getSettings() != null) {
			SharedPreferences.Editor editor = getSettings().edit();
			editor.remove(llave);
			editor.apply();
		}
	}
}
