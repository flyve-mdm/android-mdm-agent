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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import com.teclib.api.FlyveLog;
import com.teclib.launcher.ReplyInterface;


public class MQTTActionLauncher {

    public static final String TAG = "MQTTActionLauncher";
    Context mContext;

    public MQTTActionLauncher(Context context){
        mContext = context;
    }

    public String getJSONString(String label, Object obj) {
        JSONObject jsonObj = new JSONObject();
        String newJSONStringLauncher = null;

        try {
            jsonObj.put(label, obj);
            newJSONStringLauncher = jsonObj.toString();
        } catch (JSONException e) {
            FlyveLog.e("JSON String launcher", e);
        }
        return newJSONStringLauncher;
    }

    public void forward(JSONObject jsonObject) throws JSONException {

        JSONObject jsonLauncher = (JSONObject) jsonObject.get("launcher");

        if (jsonLauncher.has("code")){
            String code = (String) jsonLauncher.get("code");
            switch (code){
                case "start":
                    start(jsonObject);
                    break;
                case "update":
                    update(jsonObject);
                    break;
                case "unlock":
                    unlock(jsonObject);
                    break;
            }
        }
    }

    private void start(JSONObject jsonObject){
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, jsonObject.toString());
        intent.setType("text/plain");
        intent.setAction(ReplyInterface.sACTION_STARTJSON);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try{
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            FlyveLog.e("start activity", e);
        }
    }

    private void update(JSONObject jsonObject){
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, jsonObject.toString());
        intent.setType("text/plain");
        intent.setAction(ReplyInterface.sACTION_SENDJSON);
        mContext.sendBroadcast(intent);
    }

    private void unlock(JSONObject jsonObject){
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, jsonObject.toString());
        intent.setType("text/plain");
        intent.setAction(ReplyInterface.sACTION_SENDJSON);
        mContext.sendBroadcast(intent);
    }
}
