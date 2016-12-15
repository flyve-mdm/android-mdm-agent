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
import android.content.SharedPreferences.Editor;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferenceMQTT {

    private static final String PREFS_NAME = "MQTT_PREFS";
    private static final String PREFS_SERVER_KEY = "server";
    private static final String PREFS_SERIAL_TOPICS_KEY = "mqtt_topic_serial";
    private static final String PREFS_TOPICS_KEY = "mqtt_topic_list";
    private static final String PREFS_PORT_KEY = "mqtt_port";
    private static final String PREFS_TLS_KEY = "mqtt_tls";
    private static final String PREFS_PASSWORD_KEY = "mqtt_password";
    private static final String PREFS_STATUS_KEY = "mqtt_status";
    private static final String PREFS_INVENTORY_KEY = "is_inventory";

    public SharedPreferenceMQTT() {
        super();
    }

    public void saveIsInventory(Context context, boolean isSend) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.putBoolean(PREFS_INVENTORY_KEY, isSend);
        editor.apply();
    }

    public void savePassword(Context context, String password) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.putString(PREFS_PASSWORD_KEY, password);
        editor.apply();
    }

    public void saveStatus(Context context, boolean status) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.putBoolean(PREFS_STATUS_KEY, status);
        editor.apply();
    }


    public void saveSerialTopic(Context context, String serialTopic) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.putString(PREFS_SERIAL_TOPICS_KEY, serialTopic);
        editor.apply();
    }

    public void saveAdress(Context context, String server) {
        SharedPreferences settings  = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.putString(PREFS_SERVER_KEY, server);
        editor.apply();
    }

    public void savePort(Context context, String port) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.putString(PREFS_PORT_KEY, port);
        editor.apply();
    }

    public void saveTLS(Context context, String ssl) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.putString(PREFS_TLS_KEY, ssl);
        editor.apply();
    }

    public void saveTopics(Context context, String topics) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        Set<String> myStrings = new HashSet<>(settings.getStringSet(PREFS_TOPICS_KEY, new HashSet<String>()));

        myStrings.add(topics);
        editor.putStringSet(PREFS_TOPICS_KEY, myStrings);
        editor.apply();

    }



    public Set<String> getTopics(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> text;

        text = new HashSet<>(settings.getStringSet(PREFS_TOPICS_KEY, new HashSet<String>()));
        return text;
    }

    public String[] getSerialTopic(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String[] text = new String[1];

        if (settings.contains(PREFS_SERIAL_TOPICS_KEY)) {
            text[0] = settings.getString(PREFS_SERIAL_TOPICS_KEY, null);
        }
        else {
            text[0] = "";
        }
        return text;
    }

    public String getPassword(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String text;

        if (settings.contains(PREFS_PASSWORD_KEY)) {
            text = settings.getString(PREFS_PASSWORD_KEY, null);
        }
        else {
            text = "null";
        }
        return text;
    }

    public boolean getStatus(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean text;

        if (settings.contains(PREFS_STATUS_KEY)) {
            text = settings.getBoolean(PREFS_STATUS_KEY, false);
        }
        else {
            text = false;
        }
        return text;
    }

    public boolean getIsInventory(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean text;

        if (settings.contains(PREFS_INVENTORY_KEY)) {
            text = settings.getBoolean(PREFS_INVENTORY_KEY, false);
        }
        else {
            text = false;
        }
        return text;
    }

    public String getServer(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String text;

        if (settings.contains(PREFS_SERVER_KEY)) {
            text = settings.getString(PREFS_SERVER_KEY, null);
        }
        else {
            text = "null";
        }
        return text;
    }

    public String getPort(Context context) {
        SharedPreferences settings;
        String text;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.contains(PREFS_PORT_KEY)) {
            text = settings.getString(PREFS_PORT_KEY, null);
        }
        else {
            text = "null";
        }
        return text;
    }

    public String getTLS(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String text;

        if (settings.contains(PREFS_TLS_KEY)) {
            text = settings.getString(PREFS_TLS_KEY, null);
        }
        else {
            text = "null";
        }
        return text;
    }


    public Set<String> getAllTopics(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> text = null;

        text = new HashSet<>(settings.getStringSet(PREFS_TOPICS_KEY, new HashSet<String>()));
        text.add(settings.getString(PREFS_SERIAL_TOPICS_KEY, null));
        return text;
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

    public void removeTopics(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.remove(PREFS_TOPICS_KEY);
        editor.apply();
    }


    public void removeValue(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Editor editor = settings.edit();

        editor.remove(PREFS_SERVER_KEY);
        editor.remove(PREFS_TOPICS_KEY);
        editor.remove(PREFS_PORT_KEY);
        editor.remove(PREFS_TLS_KEY);
        editor.apply();
    }
}
