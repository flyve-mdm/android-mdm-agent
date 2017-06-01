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

public class SharedPreferencePolicies {

    private static final String PREFS_NAME = "POLICIES_PREFS";
    private static final String PREFS_PASSWORD_QUALITY_KEY = "password_quality";
    private static final String PREFS_PASSWORD_MIN_LETTERS_KEY = "password_minimum_letters";
    private static final String PREFS_PASSWORD_MIN_LOWER_CASE_KEY = "password_minimum_lowercase";
    private static final String PREFS_PASSWORD_MIN_UPPER_CASE_KEY = "password_minimum_uppercase";
    private static final String PREFS_PASSWORD_MIN_NON_LETTERS_KEY = "password_minimum_nonletters";
    private static final String PREFS_PASSWORD_MIN_NUMERIC_KEY = "password_minimum_numeric";
    private static final String PREFS_PASSWORD_MIN_LENGTH_KEY = "password_minimum_length";
    private static final String PREFS_PASSWORD_MIN_SYMBOLS_KEY = "password_minimum_symbols";
    private static final String PREFS_MAX_TIME_TO_LOCK_KEY = "maximum_time_tolock";
    private static final String PREFS_PASSWORD_MAX_FAILED_WIPE_KEY = "password_maximum_failed_wipe";
    private static final String PREFS_DISABLE_CAMERA_KEY = "disable_camera";
    private static final String PREFS_ENABLE_STORAGE_ENCRYPTION_KEY = "enable_storage_encryption";

    public SharedPreferencePolicies() {
        super();
    }

    public void savePasswordQuality(Context context, String quality) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(PREFS_PASSWORD_QUALITY_KEY, quality);
        editor.apply();
    }


    public void savePasswordMinLetters(Context context, int minLetters) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MIN_LETTERS_KEY, minLetters);
        editor.apply();
    }

    public void savePasswordMinLowerCase(Context context, int minLowerCase) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MIN_LOWER_CASE_KEY, minLowerCase);
        editor.apply();
    }

    public void savePasswordMinUpperCase(Context context, int minUpperCase) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MIN_UPPER_CASE_KEY, minUpperCase);
        editor.apply();
    }

    public void savePasswordMinNonLetters(Context context, int minNonLetters) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MIN_NON_LETTERS_KEY, minNonLetters);
        editor.apply();
    }

    public void savePasswordMinNumeric(Context context, int minNumeric) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MIN_NUMERIC_KEY, minNumeric);
        editor.apply();
    }

    public void savePasswordMinLength(Context context, int minLength) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MIN_LENGTH_KEY, minLength);
        editor.apply();
    }

    public void savePasswordMaxWipe(Context context, int maxWipe) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MAX_FAILED_WIPE_KEY, maxWipe);
        editor.apply();
    }

    public void savePasswordMinSymbols(Context context, int minSymbols) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(PREFS_PASSWORD_MIN_SYMBOLS_KEY, minSymbols);
        editor.apply();
    }

    public void saveMaxTimeToLock(Context context, Long maxTimeToLock) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putLong(PREFS_MAX_TIME_TO_LOCK_KEY, maxTimeToLock);
        editor.apply();
    }

    public void saveDisableCamera(Context context, boolean disableCamera) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(PREFS_DISABLE_CAMERA_KEY, disableCamera);
        editor.apply();
    }

    public void saveEnableStorageEncryption(Context context, boolean enableStorageEncryption) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(PREFS_ENABLE_STORAGE_ENCRYPTION_KEY, enableStorageEncryption);
        editor.apply();
    }

    public String getQualityPassword(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String text;

        if (settings.contains(PREFS_PASSWORD_QUALITY_KEY)) {
            text = settings.getString(PREFS_PASSWORD_QUALITY_KEY, null);
        }
        else {
            text = "null";
        }
        return text;
    }

    public int getMinLetters(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MIN_LETTERS_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MIN_LETTERS_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public int getMinLowerCase(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MIN_LOWER_CASE_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MIN_LOWER_CASE_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public int getMinUpperCase(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MIN_LOWER_CASE_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MIN_LOWER_CASE_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public int getMinNonLetter(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MIN_NON_LETTERS_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MIN_NON_LETTERS_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public int getMinNumeric(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MIN_NUMERIC_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MIN_NUMERIC_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public int getMinLength(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MIN_LENGTH_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MIN_LENGTH_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public int getMaxFailedWipe(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MAX_FAILED_WIPE_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MAX_FAILED_WIPE_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public Long getMaxTimeToLock(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Long text;

        if (settings.contains(PREFS_MAX_TIME_TO_LOCK_KEY)) {
            text = settings.getLong(PREFS_MAX_TIME_TO_LOCK_KEY, 0L);
        }
        else {
            text = 0L;
        }
        return text;
    }

    public boolean getEncryption(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean text;

        text = settings.contains(PREFS_ENABLE_STORAGE_ENCRYPTION_KEY) && settings.getBoolean(PREFS_ENABLE_STORAGE_ENCRYPTION_KEY, false);
        return text;
    }

    public int getMinSymbols(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int text;

        if (settings.contains(PREFS_PASSWORD_MIN_SYMBOLS_KEY)) {
            text = settings.getInt(PREFS_PASSWORD_MIN_SYMBOLS_KEY, 0);
        }
        else {
            text = 0;
        }
        return text;
    }

    public boolean getCameraStatus(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean text;

        text = settings.contains(PREFS_DISABLE_CAMERA_KEY) && settings.getBoolean(PREFS_DISABLE_CAMERA_KEY, false);
        return text;
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();
    }
}
