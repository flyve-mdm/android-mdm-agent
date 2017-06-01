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

import java.util.HashSet;
import java.util.Set;

public class SharedPreferenceSettings {

    private static final String PREFS_NAME = "SETTINGS_PREFS";

    private static final String PREFS_PROTOCOL_KEY = "protocol";
    private static final String PREFS_ID_KEY = "id";
    private static final String PREFS_COMPUTER_ID_KEY = "computers_id";
    private static final String PREFS_ENTITIES_ID_KEY = "entities_id";
    private static final String PREFS_FLEETS_ID_KEY = "fleets_id";
    private static final String PREFS_NAME_ID_KEY = "name_id";
    private static final String PREFS_USER_TOKEN_KEY = "user_token";
    private static final String PREFS_API_SERVER_KEY = "api_server";

    public SharedPreferenceSettings() {
        super();
    }

    public void saveApiServer(Context context, String api_server) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(PREFS_API_SERVER_KEY, api_server);
        editor.apply();
    }

    public void saveUserToken(Context context, String usertoken) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(PREFS_USER_TOKEN_KEY, usertoken);
        editor.apply();
    }

    public void saveProtocol(Context context, String protocol) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(PREFS_PROTOCOL_KEY, protocol);
        editor.apply();
    }

    public void saveNameID(Context context, String name_id) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(PREFS_NAME_ID_KEY, name_id);
        editor.apply();
    }

    public void saveEntitiesId(Context context, int entities_id) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_ENTITIES_ID_KEY, entities_id);
        editor.apply();
    }

    public void saveId(Context context, int id) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_ID_KEY, id);
        editor.apply();
    }

    public void saveComputersId(Context context, int computer_id) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_COMPUTER_ID_KEY, computer_id);
        editor.apply();
    }

    public void saveFleetId(Context context, int computer_id) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_COMPUTER_ID_KEY, computer_id);
        editor.apply();
    }

    public String getApiServer(Context context) {
        SharedPreferences settings;
        String api_server;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_API_SERVER_KEY)) {
            api_server = settings.getString(PREFS_API_SERVER_KEY,null);
        }
        else {
            api_server = null;
        }
        return api_server;
    }

    public String getUserToken(Context context) {
        SharedPreferences settings;
        String user_token;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_USER_TOKEN_KEY)) {
            user_token = settings.getString(PREFS_USER_TOKEN_KEY,null);
        }
        else {
            user_token = null;
        }
        return user_token;
    }

    public String getNameID(Context context) {
        SharedPreferences settings;
        String name_id;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_NAME_ID_KEY)) {
            name_id = settings.getString(PREFS_NAME_ID_KEY,null);
        }
        else {
            name_id = null;
        }
        return name_id;
    }


    public int getId(Context context) {
        SharedPreferences settings;
        int id;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_ID_KEY)) {
            id = settings.getInt(PREFS_ID_KEY, 0);
        }
        else {
            id = 0;
        }
        return id;
    }

    public int getEntitiesId(Context context) {
        SharedPreferences settings;
        int id;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_ENTITIES_ID_KEY)) {
            id = settings.getInt(PREFS_ENTITIES_ID_KEY,0);
        }
        else {
            id = 0;
        }
        return id;
    }

    public int getComputerId(Context context) {
        SharedPreferences settings;
        int computer_id;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_COMPUTER_ID_KEY)) {
            computer_id = settings.getInt(PREFS_COMPUTER_ID_KEY,0);
        }
        else {
            computer_id = 0;
        }
        return computer_id;
    }

    public int getFleetId(Context context) {
        SharedPreferences settings;
        int fleet_id;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_FLEETS_ID_KEY)) {
            fleet_id = settings.getInt(PREFS_FLEETS_ID_KEY,0);
        }
        else {
            fleet_id = 0;
        }
        return fleet_id;
    }


    public String getProtocol(Context context) {
        SharedPreferences settings;
        String status;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.contains(PREFS_PROTOCOL_KEY)) {
            status = settings.getString(PREFS_PROTOCOL_KEY, "null");
        }
        else {
            status = null;
        }
        return status;

    }

    public void removeProtocol(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(PREFS_PROTOCOL_KEY);
        editor.apply();
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }


}
