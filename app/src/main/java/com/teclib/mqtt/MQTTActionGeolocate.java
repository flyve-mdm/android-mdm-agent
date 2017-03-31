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
 * @author    Thierry Bugier Pineau
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.mqtt;

import com.teclib.api.FlyveLog;
import com.teclib.api.GPSTracker;
import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.flyvemdm.MainApplication;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MQTTActionGeolocate {
    public void sendGPS() throws JSONException {
        MainApplication application = MainApplication.getInstance();
        MqttAndroidClient client = application.getMQTTService().getClient();
        if (client != null) {
            String gpsLoc;
            double test = 0.0;
            GPSTracker GPS = new GPSTracker(application.getApplicationContext());
            GPS.getLocation();

            FlyveLog.i("sendGPS: " + "Lat = " + GPS.getLatitude() + "Lon = " + GPS.getLongitude());
            JSONObject jsonGPS = new JSONObject();

            if (Double.compare(test, GPS.getLatitude()) == 0
                    && Double.compare(test, GPS.getLongitude()) == 0) {
                jsonGPS.put("latitude", "na");
                jsonGPS.put("longitude", "na");
            } else {
                jsonGPS.put("latitude", GPS.getLatitude());
                jsonGPS.put("longitude", GPS.getLongitude());
            }
            jsonGPS.put("datetime", GetUnixTime());

            gpsLoc = jsonGPS.toString();

            MqttMessage messageGps = new MqttMessage(gpsLoc.getBytes());
            messageGps.setQos(0);
            String serialTopic = new SharedPreferenceMQTT().getSerialTopic(application.getBaseContext())[0];

            try {
                client.publish(serialTopic + "/Status/Geolocation", messageGps);
                FlyveLog.d("Message published" + gpsLoc);
            } catch (Exception ex) {
                FlyveLog.e("mqtt exception on gps message publish", ex);
            }
        }
    }

    protected int GetUnixTime() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        int utc = (int) (now / 1000);
        return (utc);
    }
}
