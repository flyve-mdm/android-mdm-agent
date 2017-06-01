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


public class SharedPreferenceAction {

    private static final String PREFS_NAME = "ACTION_PREFS";
    private static final String PREFS_APKS_KEY = "apk_list";
    private static final String PREFS_UPKS_KEY = "upk_list";
    private static final String PREFS_APKS_REMOVE_KEY = "apk_remove_list";
    private static final String PREFS_FILES_KEY = "file_list";

    public SharedPreferenceAction() {
        super();
    }

    /**
     * save downloaded file path
     *
     * @param context android context
     * @param files file path
     */
    public void saveFiles(Context context, String files) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Set<String> myStrings = new HashSet<>(settings.getStringSet(PREFS_FILES_KEY, new HashSet<String>()));

        myStrings.add(files);
        editor.putStringSet(PREFS_FILES_KEY, myStrings);
        editor.apply();
    }

    /**
     * save downloaded apk path
     *
     * @param context android context
     * @param apks apk path
     */
    public void saveApks(Context context, String apks) {

        SharedPreferences settings  = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor  = settings.edit();

        Set<String> myStrings = new HashSet<>(settings.getStringSet(PREFS_APKS_KEY, new HashSet<String>()));

        myStrings.add(apks);
        editor.putStringSet(PREFS_APKS_KEY, myStrings);
        editor.apply();
    }

    /**
     * save downloaded upk path
     *
     * @param context android context
     * @param upks upk path
     */
    public void saveUpks(Context context, String upks) {

        SharedPreferences settings  = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor  = settings.edit();

        Set<String> myStrings = new HashSet<>(settings.getStringSet(PREFS_UPKS_KEY, new HashSet<String>()));

        myStrings.add(upks);
        editor.putStringSet(PREFS_UPKS_KEY, myStrings);
        editor.apply();
    }

    /**
     * save apk package name we need to delete
     *
     * @param context android context
     * @param apks upk path
     */
    public void saveApksRemove(Context context, String apks) {

        SharedPreferences settings  = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor  = settings.edit();

        Set<String> myStrings = new HashSet<>(settings.getStringSet(PREFS_APKS_REMOVE_KEY, new HashSet<String>()));

        myStrings.add(apks);
        editor.putStringSet(PREFS_APKS_REMOVE_KEY, myStrings);
        editor.apply();
    }

    /**
     * read apk list we need to install from sharedPreferences
     *
     * @param context android context
     * @return list of apk path we need to install
     */
    public Set<String> getApks(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> text;

        if (settings.contains(PREFS_APKS_KEY)) {
            text = new HashSet<>(settings.getStringSet(PREFS_APKS_KEY, new HashSet<String>()));
        }
        else {
            text = new HashSet<>();
            text.add("null");
        }
        return text;

    }

    /**
     * read upk list we need to install from sharedPreferences
     *
     * @param context android context
     * @return list of upk path we need to install
     */
    public Set<String> getUpks(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> text;

        if (settings.contains(PREFS_UPKS_KEY)) {
            text = new HashSet<>(settings.getStringSet(PREFS_UPKS_KEY, new HashSet<String>()));
        }
        else {
            text = new HashSet<>();
            text.add("null");
        }
        return text;

    }

    /**
     * read apk list we need to remove from sharedPreferences
     *
     * @param context android context
     * @return list of apk package name we need to remove
     */
    public Set<String> getApksRemove(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> text;

        if (settings.contains(PREFS_APKS_REMOVE_KEY)) {
            text = new HashSet<>(settings.getStringSet(PREFS_APKS_REMOVE_KEY, new HashSet<String>()));
        }
        else {
            text = new HashSet<>();
            text.add("null");
        }
        return text;

    }

    /**
     * read file downloaded list
     *
     * @param context android context
     * @return list downloaded files
     */
    public Set<String> getFiles(Context context) {
        SharedPreferences settings;
        Set<String> text;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.contains(PREFS_FILES_KEY)) {
            text = new HashSet<>(settings.getStringSet(PREFS_FILES_KEY, new HashSet<String>()));
        }
        else {
            text = new HashSet<>();
            text.add("null");
        }
        return text;

    }

    /**
     * remove file path in sharedPreference
     *
     * @param context android context
     */
    public void removeFiles(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(PREFS_FILES_KEY);
        editor.apply();
    }

    /**
     * remove apks path when apks are installed
     *
     * @param context android context
     */
    public void removeApks(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(PREFS_APKS_KEY);
        editor.apply();
    }

    /**
     * remove upks path when upks are installed
     *
     * @param context android context
     */
    public void removeUpks(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(PREFS_UPKS_KEY);
        editor.apply();
    }

    /**
     * remove apks package name when apks are uninstalled
     *
     * @param context android context
     */
    public void removeApksRemove(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(PREFS_APKS_REMOVE_KEY);
        editor.apply();
    }

    /**
     * delete all data
     *
     * @param context android context
     */
    public void clearSharedPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }

}
