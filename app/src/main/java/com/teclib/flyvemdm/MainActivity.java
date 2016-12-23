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

package com.teclib.flyvemdm;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.database.SharedPreferenceSettings;
import org.fusioninventory.Agent;
import com.teclib.service.BootService;

import org.json.JSONException;
import org.json.JSONObject;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends FragmentActivity {

    private static String TAG = "MainActivity";
    private SharedPreferenceMQTT msharedPreferenceMQTT = new SharedPreferenceMQTT();
    private SharedPreferenceSettings msharedPreferenceSettings = new SharedPreferenceSettings();

    String apiUrl = null;
    String apiUserToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main_portrait);
            FlyveLog.d("PORTRAIT");
        } else {
            setContentView(R.layout.activity_main_paysage);
            FlyveLog.d("PAYSAGE");
        }

        View someView = findViewById(R.id.view);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.status_text));

        Intent inventoryService = new Intent(this.getBaseContext(), Agent.class);
        this.getBaseContext().startService(inventoryService);

        Intent monServiceIntent = new Intent(this.getBaseContext(), BootService.class);
        this.getBaseContext().startService(monServiceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent DeviceAdmin = new Intent(this.getBaseContext(), com.teclib.api.DeviceAdmin.class);
        startActivity(DeviceAdmin);

        if (!msharedPreferenceMQTT.getSerialTopic(getBaseContext())[0].isEmpty()) {
             Intent intent = new Intent(getApplicationContext(), MQTTNotifierActivity.class);
             startActivity(intent);
             finish();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void scanNow(View view) {
        new IntentIntegrator(this).initiateScan();
    }
/*
    public void dnsNow(View view) {
        Log.d(TAG, "On dnsNow .....");
        Intent intent = new Intent(getApplicationContext(),DNSActivity.class);
        startActivity(intent);
        finish();
    }
*/


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if(result.getContents() == null) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        } else {
                String contents = result.getContents();
                FlyveLog.d(contents);
                try {
                    JSONObject jsonObj = new JSONObject(contents);
                    if(jsonObj.has("url")) {
                        Uri uri = Uri.parse(jsonObj.getString("url"));
                        apiUrl = jsonObj.getString("url");
                        apiUserToken = jsonObj.getString("user_token");
                        String protocol = uri.getScheme();
                        msharedPreferenceSettings.saveProtocol(getBaseContext(),protocol);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent LoginIntent = new Intent(this.getBaseContext(), LoginActivity.class);
                LoginIntent.putExtra("link", contents);
                startActivity(LoginIntent);
                finish();
        }
    }
    
}



