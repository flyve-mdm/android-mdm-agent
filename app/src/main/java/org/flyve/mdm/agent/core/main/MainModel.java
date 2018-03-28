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

package org.flyve.mdm.agent.core.main;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.DrawerAdapter;
import org.flyve.mdm.agent.data.AppData;
import org.flyve.mdm.agent.services.DeviceLockedController;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.ui.FragmentAbout;
import org.flyve.mdm.agent.ui.FragmentActivity;
import org.flyve.mdm.agent.ui.FragmentConfiguration;
import org.flyve.mdm.agent.ui.FragmentFeedback;
import org.flyve.mdm.agent.ui.FragmentHelp;
import org.flyve.mdm.agent.ui.FragmentInformation;
import org.flyve.mdm.agent.ui.FragmentMQTTConfig;
import org.flyve.mdm.agent.ui.FragmentTestPolicies;
import org.flyve.mdm.agent.utils.FlyveLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainModel implements Main.Model {

    private Main.Presenter presenter;
    private ArrayList<HashMap<String, String>> arrDrawer;
    private Intent mServiceIntent;

    public void startMQTTService(Context context) {
        mServiceIntent = MQTTService.start(context);
    }

    public void closeMQTTService(Context context) {
        if(mServiceIntent!=null) {
            context.stopService(mServiceIntent);
        }
    }

    public void checkNotifications(Context context) {
        DeviceLockedController pwd = new DeviceLockedController(context);
        if(pwd.isDeviceScreenLocked()) {
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1009);
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }
        }
    }


    public MainModel(Main.Presenter presenter) {
        this.presenter = presenter;
    }

    public void onClickItem(FragmentManager fragmentManager, android.support.v7.widget.Toolbar toolbar, Map<String, String> item) {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        toolbar.setTitle(item.get("name"));

        // Information
        if (item.get("id").equals("1")) {
            FragmentInformation f = new FragmentInformation();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // Activity
        if (item.get("id").equals("2")) {
            FragmentActivity f = new FragmentActivity();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // MQTT Configuration
        if (item.get("id").equals("3")) {
            FragmentMQTTConfig f = new FragmentMQTTConfig();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // Test Policies
        if (item.get("id").equals("6")) {
            FragmentTestPolicies f = new FragmentTestPolicies();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // Help
        if (item.get("id").equals("4")) {
            FragmentHelp f = new FragmentHelp();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // About
        if (item.get("id").equals("5")) {
            FragmentAbout f = new FragmentAbout();
            fragmentTransaction.replace(R.id.containerView, f).commit();
        }

        // Configuration
        if (item.get("id").equals("7")) {
            FragmentConfiguration f = new FragmentConfiguration();
            fragmentTransaction.replace(R.id.containerView, f).commit();
        }

        // Feedback
        if (item.get("id").equals("8")) {
            FragmentFeedback f = new FragmentFeedback();
            fragmentTransaction.replace(R.id.containerView, f).commit();
        }
    }

    public List<HashMap<String, String>> getMenuItem() {
        return arrDrawer;
    }

    public Map<String, String> setupDrawer(Activity activity, ListView lst) {
        arrDrawer = new ArrayList<>();

        // Information
        HashMap<String, String> map = new HashMap<>();
        map.put("id", "1");
        map.put("name", activity.getResources().getString(R.string.drawer_information));
        map.put("img", "ic_info");
        arrDrawer.add(map);

        // if easterEgg is active
        if(new AppData(activity).getEasterEgg()) {

            // MQTT Configuration
            map = new HashMap<>();
            map.put("id", "3");
            map.put("name", activity.getResources().getString(R.string.drawer_mqtt_config));
            map.put("img", "ic_config");
            arrDrawer.add(map);

            // Test Policies
            map = new HashMap<>();
            map.put("id", "6");
            map.put("name", activity.getResources().getString(R.string.drawer_test_policies));
            map.put("img", "ic_config");
            arrDrawer.add(map);

        }

        // Activity
        map = new HashMap<>();
        map.put("id", "2");
        map.put("name", activity.getResources().getString(R.string.drawer_activity));
        map.put("img", "ic_log");
        map.put("separator", "true");
        arrDrawer.add(map);

        // Feedback
        map = new HashMap<>();
        map.put("id", "8");
        map.put("name", activity.getResources().getString(R.string.drawer_feedback));
        map.put("img", "ic_feedback");
        arrDrawer.add(map);

        // Help
        map = new HashMap<>();
        map.put("id", "4");
        map.put("name", activity.getResources().getString(R.string.drawer_help));
        map.put("img", "ic_help");
        arrDrawer.add(map);

        // Configuration
        map = new HashMap<>();
        map.put("id", "7");
        map.put("name", activity.getResources().getString(R.string.drawer_configuration));
        map.put("img", "ic_config");
        arrDrawer.add(map);

        // About
        map = new HashMap<>();
        map.put("id", "5");
        map.put("name", activity.getResources().getString(R.string.drawer_about));
        map.put("img", "ic_about");
        arrDrawer.add(map);

        try {
            // load adapter
            DrawerAdapter adapter = new DrawerAdapter(activity, arrDrawer);
            lst.setAdapter(adapter);

            // Select Information on load //
            return arrDrawer.get(0);
        } catch(Exception ex) {
            FlyveLog.e(ex.getMessage());
        }

        return null;
    }
}
