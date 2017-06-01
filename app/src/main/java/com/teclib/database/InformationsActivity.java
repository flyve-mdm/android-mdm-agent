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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.teclib.service.MQTTService;
import com.teclib.flyvemdm.R;

/**
 * Created by dlarget on 11/07/16.
 */
public class InformationsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static String name;
    private static int id;
    private static int computer_id;
    private SharedPreferenceSettings sharedPreferenceSettings;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.informations);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        context = getBaseContext();
        sharedPreferenceSettings = new SharedPreferenceSettings();

        name = sharedPreferenceSettings.getNameID(context);
        id = sharedPreferenceSettings.getId(context);
        computer_id = sharedPreferenceSettings.getComputerId(context);

        Preference pref_id = (Preference) findPreference(getString(R.string.pref_id));
        pref_id.setSummary(Integer.toString(id));

        Preference pref_name = (Preference) findPreference(getString(R.string.pref_name));
        pref_name.setSummary(name);

        Preference pref_computer_id = (Preference) findPreference(getString(R.string.pref_computer_id));
        pref_computer_id.setSummary(Integer.toString(computer_id));
    }

    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MQTTService.MQTT_STATUS_INTENT);
        sendBroadcast(broadcastIntent);

    }
}
