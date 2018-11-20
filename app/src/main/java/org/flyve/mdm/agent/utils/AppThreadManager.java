package org.flyve.mdm.agent.utils;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
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
 * @date      19/9/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.flyve.mdm.agent.data.database.ApplicationData;
import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.services.PoliciesController;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppThreadManager {

    private static AppThreadManager singleton;
    private MqttAndroidClient client;

    private List<JSONObject> item;

    public static AppThreadManager getAppThreadManager(MqttAndroidClient client) {
        if(singleton==null) {
            singleton = new AppThreadManager(client);
        }
        return singleton;
    }

    private AppThreadManager(MqttAndroidClient client) {
        this.item = new ArrayList<>();
        this.client = client;
    }

    public void add(Context context, JSONObject jsonObj) {
        if(!item.isEmpty()) {
            for (int i = 0; i < item.size(); i++) {
                if (item.get(i).toString().trim().contains(jsonObj.toString().trim())) {
                    return;
                }
            }
        }

        item.add(jsonObj);
        process(context);
    }

    public void finishProcess(Context context) {
        if(!item.isEmpty()) {
            item.remove(0);
        }

        FlyveLog.i("Finish Processing");
        process(context);

    }

    public void process(Context context) {
        if(!item.isEmpty()) {

            FlyveLog.i("Processing item 0 from item size -> " + item.size());
            JSONObject jsonObj = item.get(0);

            try {
                String deployApp = jsonObj.getString("deployApp");
                String id = jsonObj.getString("id");
                String versionCode = jsonObj.getString("versionCode");
                String taskId = jsonObj.getString("taskId");

                ApplicationData apps = new ApplicationData(context);
                Application[] appsArray = apps.getApplicationsById(id);

                // check if the app exists with same version or older
                Boolean bDownload = true;
                if(appsArray.length>0 && Integer.parseInt(versionCode) >= Integer.parseInt(appsArray[0].appVersionCode)) {
                    bDownload = false;
                }

                if(bDownload) {
                    // execute the policy
                    PoliciesController policiesController = new PoliciesController(context, this.client);
                    policiesController.installPackage(deployApp, id, versionCode, taskId);
                } else {
                    finishProcess(context);
                }
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            }
        }
    }
}
