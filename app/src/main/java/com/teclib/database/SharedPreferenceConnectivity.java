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

package com.teclib.database;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceConnectivity {

    private static final String PREFS_NAME = "CONNECTIVITY_PREFS";
    private static final String PREFS_WIFI_KEY = "wifi";
    private static final String PREFS_BLUETOOTH_KEY = "bluetooth";
    private static final String PREFS_GEOLOC_KEY = "geolocation";

    public SharedPreferenceConnectivity() {
        super();
    }


    public void saveWifi(Context context, boolean value) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(PREFS_WIFI_KEY, value);
        editor.apply();
    }

    public void saveBluetooth(Context context, boolean value) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(PREFS_BLUETOOTH_KEY, value);
        editor.apply();
    }
    // Value 0 is empty
    // Value 1 receive gps order but gps disable
    // Value 2 active gps but not send
    // Value 3 gps position send
    public void saveGeoloc(Context context, boolean value) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(PREFS_GEOLOC_KEY, value);
        editor.apply();
    }

    public boolean getWifi(Context context) {
        SharedPreferences settings;
        boolean status;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_WIFI_KEY)) {
            status = settings.getBoolean(PREFS_WIFI_KEY, false);
        }
        else {
            status = false;
        }
        return status;
    }

    public boolean getBluetooth(Context context) {
        SharedPreferences settings;
        boolean status;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_BLUETOOTH_KEY)) {
            status = settings.getBoolean(PREFS_BLUETOOTH_KEY,false);
        }
        else {
            status = false;
        }
        return status;
    }

    public boolean getGeoloc(Context context) {
        SharedPreferences settings;
        boolean status;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_GEOLOC_KEY)) {
            status = settings.getBoolean(PREFS_GEOLOC_KEY,false);
        }
        else {
            status = false;
        }
        return status;
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

}
