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

	protected String getData(String key){
		String data = "";
		SharedPreferences sp = getSettings();
		if(sp != null) {
			data = sp.getString(key, null);
		}
		return data;
	}

	protected void setData(String key, String value) {
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
