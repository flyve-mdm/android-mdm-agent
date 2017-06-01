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

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceConnectivity;
import com.teclib.service.MQTTService;

import java.util.ArrayList;


public class MQTTAction extends BroadcastReceiver {

    private SharedPreferenceConnectivity mSharedPreferenceConnectivity;
    private ArrayList<String> mIntentArgs;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(MQTTService.MQTT_MSG_RECEIVED_INTENT)) {
            String Jsonreceive = intent.getStringExtra(MQTTService.MQTT_MSG_RECEIVED_MSG);
            mSharedPreferenceConnectivity = new SharedPreferenceConnectivity();

            try {
                ExecuteCommands(context, Jsonreceive);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected()) {

                    if(mSharedPreferenceConnectivity.getWifi(context)) {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        wifiManager.setWifiEnabled(false);
                    }
                }
            }
            catch (Exception e)
            {
                FlyveLog.e(e.getMessage());
                e.printStackTrace();
            }

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                if (mSharedPreferenceConnectivity.getBluetooth(context)) {
                    bluetoothAdapter.disable();
                }
            }
        }
    }

    private void ExecuteCommands(final Context mContext , String message) throws JSONException {
        JSONObject jsonObj = new JSONObject(message);

        if(jsonObj.has("application")){
            JSONArray checkInstall = new JSONArray();
            checkInstall = jsonObj.getJSONArray("application");

            for(int i = 0; i < checkInstall.length(); i++){
                if(checkInstall.getJSONObject(i).has("removeApp")){
                    FlyveLog.d("uninstall apps");
                    MQTTActionApps mqttActionApps = new MQTTActionApps(mContext);
                    mqttActionApps.remove(checkInstall.getJSONObject(i));
                }
                else if(checkInstall.getJSONObject(i).has("deployApp")){
                    FlyveLog.d("install apps");
                    MQTTActionApps mqttActionApps = new MQTTActionApps(mContext);
                    mqttActionApps.install(checkInstall.getJSONObject(i));
                }
            }
        }

        if(jsonObj.has("file")){
            JSONArray checkInstall = new JSONArray();
            checkInstall = jsonObj.getJSONArray("file");

            for(int i = 0; i < checkInstall.length(); i++){
                if(checkInstall.getJSONObject(i).has("removeFile")){
                    FlyveLog.d("delete file");
                    MQTTActionFiles mqttActionFiles = new MQTTActionFiles(mContext);
                    mqttActionFiles.delete(checkInstall.getJSONObject(i));
                }
                else if(checkInstall.getJSONObject(i).has("deployFile")){
                    FlyveLog.d("download file");
                    MQTTActionFiles mqttActionFiles = new MQTTActionFiles(mContext);
                    mqttActionFiles.download(checkInstall.getJSONObject(i));
                }
            }
        }
        if(jsonObj.has("launcher")){
            MQTTActionLauncher mqttActionLauncher = new MQTTActionLauncher(mContext);
            mqttActionLauncher.forward(jsonObj);
        }
        if(jsonObj.has("wipe")){
            mIntentArgs = new ArrayList<String>();
            mIntentArgs.add("wipe");

            Intent intentone = new Intent(mContext.getApplicationContext(), com.teclib.api.DeviceAdmin.class);
            intentone.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intentone.putStringArrayListExtra("ControllerArgs", mIntentArgs);
            mContext.startActivity(intentone);

        }

        if(jsonObj.has("connectivity")){
            MQTTActionConnectivity mqttactionConnectivity = new MQTTActionConnectivity(mContext);
            mqttactionConnectivity.SaveValues(jsonObj);
        }
        if(jsonObj.has("policies")){
            MQTTActionPolicies mqttactionPolicies = new MQTTActionPolicies(mContext);
            mqttactionPolicies.SetPolicies(jsonObj);
        }
        if(jsonObj.has("camera")){
            MQTTActionPolicies mqttactionPolicies = new MQTTActionPolicies(mContext);
            mqttactionPolicies.SetPolicies(jsonObj);
        }
        if(jsonObj.has("encryption")){
            MQTTActionPolicies mqttactionPolicies = new MQTTActionPolicies(mContext);
            mqttactionPolicies.SetPolicies(jsonObj);
        }
        if(jsonObj.has("lock")){
            MQTTActionPolicies mqttactionPolicies = new MQTTActionPolicies(mContext);
            mqttactionPolicies.SetPolicies(jsonObj);
        }
    }
}