/*
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

package com.teclib.mqtt;

import android.content.Context;
import com.teclib.database.SharedPreferenceConnectivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MQTTActionConnectivity {

    Context mContext;
    private SharedPreferenceConnectivity mSharedPreferenceConnectivity;

    public MQTTActionConnectivity(Context context){
        mContext = context;
    }

    public void SaveValues(JSONObject jsonObject) throws JSONException {
        JSONArray connectivity;
        connectivity = jsonObject.getJSONArray("connectivity");

        mSharedPreferenceConnectivity = new SharedPreferenceConnectivity();
        for(int i = 0; i < connectivity.length(); i++){
            if(connectivity.getJSONObject(i).has("disableWifi")){
                boolean wifi = connectivity.getJSONObject(i).getBoolean("disableWifi");
                mSharedPreferenceConnectivity.saveWifi(mContext, wifi);
            }
            if(connectivity.getJSONObject(i).has("disableBluetooth")){
                boolean bluetooth = connectivity.getJSONObject(i).getBoolean("disableBluetooth");
                mSharedPreferenceConnectivity.saveBluetooth(mContext,bluetooth);
            }
            if(connectivity.getJSONObject(i).has("disableGPS")){
                boolean geoloc = connectivity.getJSONObject(i).getBoolean("disableGPS");
                mSharedPreferenceConnectivity.saveGeoloc(mContext, geoloc);
            }

        }
    }

}
