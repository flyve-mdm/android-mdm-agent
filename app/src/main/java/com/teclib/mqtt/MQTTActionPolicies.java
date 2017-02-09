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
import android.content.Intent;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferencePolicies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MQTTActionPolicies {

    private JSONObject jPoliciesObject;
    Context mContext;
    private SharedPreferencePolicies mSharedPreferencePolicies;

    private String mPasswordQuality;
    private int mPasswordMinLetters;
    private int mPasswordMinLowerCase;
    private int mPasswordMinUpperCase;
    private int mPasswordMinNonLetter;
    private int mPasswordMinNumeric;
    private int mPasswordMinLength;
    private int mMaximumFailedPasswordsForWipe;
    private long mMaximumTimeToLock;
    private int mPasswordMinSymbols;

    private boolean mEncryption;
    private boolean mCamera;

    private ArrayList<String> mIntentArgs;

    public MQTTActionPolicies(Context context) {
        mSharedPreferencePolicies = new SharedPreferencePolicies();
        mContext = context;
    }

    public void SetPolicies(JSONObject jsonObject) throws JSONException {

        mIntentArgs = new ArrayList<String>();
        if (jsonObject.has("policies")) {
            jPoliciesObject = new JSONObject();
            JSONArray jPolicies = jsonObject.getJSONArray("policies");

            for (int i = 0; i < jPolicies.length(); i++) {

                jPoliciesObject = jPolicies.getJSONObject(i);

                if (jPoliciesObject.has("passwordQuality")) {

                    mPasswordQuality = jPoliciesObject.getString("passwordQuality");
                    String passwordQualitySave = mSharedPreferencePolicies.getQualityPassword(mContext);

                    if (passwordQualitySave != null) {

                        if (!(passwordQualitySave.equals(mPasswordQuality))) {
                            FlyveLog.d("SetPolicies: mPasswordQuality = " + mPasswordQuality);
                            mSharedPreferencePolicies.savePasswordQuality(mContext, mPasswordQuality);
                            mIntentArgs.add("passwordQuality");
                        }
                    }
                }

                if (jPoliciesObject.has("passwordMinLetters")) {

                    mPasswordMinLetters = Integer.parseInt(jPoliciesObject.getString("passwordMinLetters"));
                    int passwordMinLetters = mSharedPreferencePolicies.getMinLetters(mContext);

                    if (!(passwordMinLetters == mPasswordMinLetters)) {
                        FlyveLog.d("SetPolicies: mPasswordMinLetters = " + mPasswordMinLetters);
                        mSharedPreferencePolicies.savePasswordMinLetters(mContext, mPasswordMinLetters);
                        mIntentArgs.add("passwordMinLetters");
                    }
                }

                if (jPoliciesObject.has("passwordMinLowerCase")) {

                    mPasswordMinLowerCase = Integer.parseInt(jPoliciesObject.getString("passwordMinLowerCase"));
                    int passwordMinLowerCase = mSharedPreferencePolicies.getMinLowerCase(mContext);

                    if (!(passwordMinLowerCase == mPasswordMinLowerCase)) {
                        FlyveLog.d("SetPolicies: mPasswordMinLowerCase = " + mPasswordMinLowerCase);
                        mSharedPreferencePolicies.savePasswordMinLowerCase(mContext, mPasswordMinLowerCase);
                        mIntentArgs.add("passwordMinLowerCase");
                    }
                }

                if (jPoliciesObject.has("passwordMinUpperCase")) {

                    mPasswordMinUpperCase = Integer.parseInt(jPoliciesObject.getString("passwordMinUpperCase"));
                    int passwordMinUpperCase = mSharedPreferencePolicies.getMinUpperCase(mContext);

                    if (!(passwordMinUpperCase == mPasswordMinUpperCase)) {
                        FlyveLog.d("SetPolicies: mPasswordMinUpperCase = " + mPasswordMinUpperCase);
                        mSharedPreferencePolicies.savePasswordMinUpperCase(mContext, mPasswordMinUpperCase);
                        mIntentArgs.add("passwordMinUpperCase");
                    }
                }

                if (jPoliciesObject.has("passwordMinNonLetter")) {

                    mPasswordMinNonLetter = Integer.parseInt(jPoliciesObject.getString("passwordMinNonLetter"));
                    int passwordMinNonLetter = mSharedPreferencePolicies.getMinNonLetter(mContext);

                    if (!(passwordMinNonLetter == mPasswordMinNonLetter)) {
                        FlyveLog.d("SetPolicies: mPasswordMinNonLetter = " + mPasswordMinNonLetter);
                        mSharedPreferencePolicies.savePasswordMinNonLetters(mContext, mPasswordMinNonLetter);
                        mIntentArgs.add("passwordMinNonLetter");
                    }
                }

                if (jPoliciesObject.has("passwordMinNumeric")) {

                    mPasswordMinNumeric = Integer.parseInt(jPoliciesObject.getString("passwordMinNumeric"));
                    int passwordMinNumeric = mSharedPreferencePolicies.getMinNumeric(mContext);

                    if (!(passwordMinNumeric == mPasswordMinNumeric)) {
                        FlyveLog.d("SetPolicies: mPasswordMinNumeric = " + mPasswordMinNumeric);
                        mSharedPreferencePolicies.savePasswordMinNumeric(mContext, mPasswordMinNumeric);
                        mIntentArgs.add("passwordMinNumeric");
                    }
                }

                if (jPoliciesObject.has("passwordMinLength")) {
                    mPasswordMinLength = Integer.parseInt(jPoliciesObject.getString("passwordMinLength"));
                    int passwordMinLength = mSharedPreferencePolicies.getMinLength(mContext);

                    if (!(passwordMinLength == mPasswordMinLength)) {
                        FlyveLog.d("SetPolicies: mPasswordMinLength = " + mPasswordMinLength);
                        mSharedPreferencePolicies.savePasswordMinLength(mContext, mPasswordMinLength);
                        mIntentArgs.add("passwordMinLength");
                    }
                }


                if (jPoliciesObject.has("MaximumFailedPasswordsForWipe")) {
                    mMaximumFailedPasswordsForWipe = Integer.parseInt(jPoliciesObject.getString("MaximumFailedPasswordsForWipe"));
                    int maximumFailedPasswordsForWipe = mSharedPreferencePolicies.getMaxFailedWipe(mContext);

                    if (!(maximumFailedPasswordsForWipe == mMaximumFailedPasswordsForWipe)) {
                        FlyveLog.d("SetPolicies: mMaximumFailedPasswordsForWipe = " + mMaximumFailedPasswordsForWipe);
                        mSharedPreferencePolicies.savePasswordMaxWipe(mContext, mMaximumFailedPasswordsForWipe);
                        mIntentArgs.add("MaximumFailedPasswordsForWipe");
                    }
                }

                if (jPoliciesObject.has("MaximumTimeToLock")) {
                    mMaximumTimeToLock = Long.parseLong(jPoliciesObject.getString("MaximumTimeToLock"));
                    Long maximumFailedPasswordsForWipe = mSharedPreferencePolicies.getMaxTimeToLock(mContext);

                    if (!(maximumFailedPasswordsForWipe == mMaximumTimeToLock)) {
                        FlyveLog.d("SetPolicies: mMaximumTimeToLock = " + mMaximumTimeToLock);
                        mSharedPreferencePolicies.saveMaxTimeToLock(mContext, mMaximumTimeToLock);
                        mIntentArgs.add("MaximumTimeToLock");
                    }
                }

                if (jPoliciesObject.has("passwordMinSymbols")) {

                    mPasswordMinSymbols = Integer.parseInt(jPoliciesObject.getString("passwordMinSymbols"));
                    int passwordMinSymbols = mSharedPreferencePolicies.getMinSymbols(mContext);

                    if (!(passwordMinSymbols == mPasswordMinSymbols)) {
                        FlyveLog.d("SetPolicies: mPasswordMinSymbols = " + mPasswordMinSymbols);
                        mSharedPreferencePolicies.savePasswordMinSymbols(mContext, mPasswordMinSymbols);
                        mIntentArgs.add("passwordMinSymbols");
                    }
                }
            }
        }

        if (jsonObject.has("encryption")) {
            JSONArray jEncryption = jsonObject.getJSONArray("encryption");

            mEncryption = Boolean.valueOf(jEncryption.getJSONObject(0).getString("storageEncryption"));
            Boolean encryption = mSharedPreferencePolicies.getEncryption(mContext);

            if (!(mEncryption == encryption)) {
                FlyveLog.d("SetPolicies: mEncryption = " + mSharedPreferencePolicies.getMinSymbols(mContext));
                mSharedPreferencePolicies.saveEnableStorageEncryption(mContext, mEncryption);
                mIntentArgs.add("encryption");
            }
        }

        if (jsonObject.has("camera")) {
            JSONArray jCamera = jsonObject.getJSONArray("camera");

            mCamera = Boolean.valueOf(jCamera.getJSONObject(0).getString("disableCamera"));
            Boolean camera = mSharedPreferencePolicies.getEncryption(mContext);
            FlyveLog.d("SetPolicies mCamera = " + mCamera);
            FlyveLog.d("SetPolicies camera = " + camera);
            if (!(mCamera == camera)) {
                FlyveLog.d("SetPolicies: Camera = " + mSharedPreferencePolicies.getCameraStatus(mContext));
                mSharedPreferencePolicies.saveDisableCamera(mContext, mCamera);
                mIntentArgs.add("camera");
            }

        }

        if (jsonObject.has("lock")) {
            mIntentArgs.add("lock");
            if(!mIntentArgs.isEmpty()){
                Intent intentone = new Intent(mContext.getApplicationContext(), com.teclib.api.DeviceAdmin.class);
                intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intentone.putStringArrayListExtra("ControllerArgs", mIntentArgs);
                mContext.startActivity(intentone);
            }
            return;
        }

        if(!mIntentArgs.isEmpty()){
            FlyveLog.d("SetPolicies mIntentArgs = " + mIntentArgs.toString());
            Intent intentone = new Intent(mContext.getApplicationContext(), com.teclib.api.DeviceAdmin.class);
            intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intentone.putStringArrayListExtra("ControllerArgs", mIntentArgs);
            mContext.startActivity(intentone);
        }
    }
}
