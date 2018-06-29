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

package org.flyve.mdm.agent.data.localstorage;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {

	private static final String SHARED_PREFS_FILE = "FlyveHMPrefs";
	private Context mContext;

	/**
	 * Constructor
	 * @param context
	 */
	public LocalStorage(Context context){
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
	 * Get the data matching the given argument
	 * @param key
	 * @return string the data
	 */
	protected String getData(String key){
		String data = "";
		SharedPreferences sp = getSettings();
		if(sp != null) {
			data = sp.getString(key, null);
			if(data==null) {
				data = "";
			}
		}
		return data;
	}

	/**
	 * Set the data given in the argument to the Shared Preferences
	 * @param key
	 * @param value
	 */
	protected void setData(String key, String value) {
		SharedPreferences sp = getSettings();
		if(sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(key, value );
			editor.apply();
		}
	}

	/**
	 * Remove all the values from the preferences
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
