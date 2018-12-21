package org.flyve.mdm.agent.utils;

/*
 *   Copyright  2018 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
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
 * @date      1/5/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.flyve.inventory.InventoryTask;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.data.database.MqttData;

public class Inventory {

    // MDM Agent/0.0.0
    public static final String APP_VERSION = "MDM Agent/" + BuildConfig.VERSION_NAME;

    public void getXMLInventory(Context context, InventoryTask.OnTaskCompleted callback) {
        InventoryTask inventoryTask = new InventoryTask(context, APP_VERSION, true);
        MqttData cache = new MqttData(context);

        String invitationToken = cache.getInvitationToken();
        if(!invitationToken.isEmpty()){
            String tag = "invitation_" + invitationToken;
            FlyveLog.i(tag);
            inventoryTask.setTag(tag);
        }

        inventoryTask.getXML(callback);
    }

    public void getJSONInventory(Context context, InventoryTask.OnTaskCompleted callback) {
        InventoryTask inventoryTask = new InventoryTask(context, APP_VERSION, true);
        MqttData cache = new MqttData(context);

        String invitationToken = cache.getInvitationToken();
        if(!invitationToken.isEmpty()){
            String tag = "invitation_" + invitationToken;
            FlyveLog.i(tag);
            inventoryTask.setTag(tag);
        }

        inventoryTask.getJSON(callback);
    }
}
