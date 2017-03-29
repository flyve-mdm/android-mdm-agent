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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.android.service.MqttAndroidClient;

import com.teclib.flyvemdm.MainApplication;
import com.teclib.database.SharedPreferenceMQTT;

public class MQTTActionPing {
    public void sendPing() throws MqttException {
        MainApplication application = MainApplication.getInstance();
        MqttAndroidClient client = application.getMQTTService().getClient();
        if (client != null) {
            String serialTopic = new SharedPreferenceMQTT().getSerialTopic(application.getBaseContext())[0];

            MqttMessage message = new MqttMessage("!".getBytes());
            message.setQos(0);
            client.publish(serialTopic + "/Status/Ping", message);
        }
    }
}
