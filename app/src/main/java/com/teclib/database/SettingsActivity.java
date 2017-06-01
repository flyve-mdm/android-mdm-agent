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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.teclib.service.MQTTService;
import com.teclib.flyvemdm.R;


public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        Preference button = (Preference)findPreference(getString(R.string.pref_information));

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                Intent intentSettings = new Intent(getBaseContext(),
                        InformationsActivity.class);
                startActivity(intentSettings);
                return true;
            }
        });
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
