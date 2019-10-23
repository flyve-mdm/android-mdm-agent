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

package org.flyve.mdm.agent.core.mqtt;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface mqtt {

    interface View {

    }

    interface Presenter {
        // Views

        // Models
        MqttAndroidClient getMqttClient();
        Boolean isConnected();
        void connect(Context context, MqttCallback callback);
        void connectionLost(Context context, MqttCallback callback, String message);
        void messageArrived(Context context, String topic, MqttMessage message);
        void showDetailError(Context context, int type, String message);
        void onDestroy(Context context);
        void deliveryComplete(Context context, IMqttDeliveryToken token);
    }

    interface Model {
        MqttAndroidClient getMqttClient();
        Boolean isConnected();
        void connect(Context context, MqttCallback callback);
        void connectionLost(Context context, MqttCallback callback, String message);
        void messageArrived(Context context, String topic, MqttMessage message);
        void showDetailError(Context context, int type, String message);
        void onDestroy(Context context);
        void deliveryComplete(Context context, IMqttDeliveryToken token);
    }
}
