/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.utils.FlyveLog;

public class MqttReceiver extends BroadcastReceiver {

    private MQTTService mqttService;

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mqttService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MQTTService.LocalBinder mLocalBinder = (MQTTService.LocalBinder)service;
            mqttService = mLocalBinder.getServerInstance();
        }
    };


    /**
     * It starts the MQTT service
     * @param context in which the receiver is running
     * @param intent being received
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent mIntent = new Intent(context, MQTTService.class);
            context.getApplicationContext().bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", onReceive", ex.getMessage());
        }
    }
}
