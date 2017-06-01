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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.teclib.api.GPSTracker;
import com.teclib.service.MQTTService;
import com.teclib.service.NotificationGPSActivation;


public class ActiveGPSActivity extends Activity {

    private Context mContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
    }

    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        GPSTracker mGPS = new GPSTracker(this);
        super.onDestroy();
        if(!mGPS.canGetLocation){
            Intent intent = new Intent(mContext,NotificationGPSActivation.class);
            mContext.startService(intent);
        }
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        GPSTracker mGPS = new GPSTracker(this);
        super.onActivityResult(requestCode, resultCode, data);
        if(mGPS.canGetLocation){
            Intent mqtt = new Intent(mContext, MQTTService.class);
            mqtt.setAction(MQTTService.ACTION_GPS);
            mContext.startService(mqtt);
            finish();
        }
        else{
            finish();
        }
    }

}
