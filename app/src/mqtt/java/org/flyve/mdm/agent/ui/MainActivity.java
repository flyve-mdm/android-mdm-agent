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

package org.flyve.mdm.agent.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.DrawerAdapter;
import org.flyve.mdm.agent.core.mqtt.MqttModel;
import org.flyve.mdm.agent.data.localstorage.AppData;
import org.flyve.mdm.agent.policies.PoliciesAsyncTask;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.policies.manager.AndroidPolicies;
import org.flyve.policies.manager.DeviceLockedController;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private ListView lstDrawer;
    private ArrayList<HashMap<String, String>> arrDrawer;
    private HashMap<String, String> selectedItem;
    private TextView txtToolbarTitle;
    private Intent mServiceIntent;
    private AppData cache;

    /**
     * Called when the activity is starting 
     * @param savedInstanceState if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start MQTT
        globalStartMQTT();

        cache = new AppData(this);

         // Setup the DrawerLayout and NavigationView
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        mDrawerLayout = findViewById(R.id.drawerLayout);

        lstDrawer = findViewById(R.id.lstNV);
        lstDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawers();
            selectedItem = arrDrawer.get(position);
            loadFragment(selectedItem, "");
            }
        });

        mFragmentManager = getSupportFragmentManager();

        // Setup Drawer Toggle of the Toolbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        // check if come from notification like DeployApp
        int menuItemSelected = 0;
        String extra = "";
        String type = getIntent().getStringExtra("From");
        if (type != null) {
            switch (type) {
                case "DeployApp":
                case "RemoveApp":
                    menuItemSelected = 1;
                    extra = "DeployApp";
                    break;
                case "PasswordPolicy":
                    AndroidPolicies androidPolicies = new AndroidPolicies(MainActivity.this, FlyveAdminReceiver.class);
                    androidPolicies.enablePassword(true, "", MainActivity.class);
            }
        }

        loadListDrawer(menuItemSelected, extra);
        checkNotifications();

        PoliciesAsyncTask.sendStatusbyHttp(MainActivity.this, true);
    }

    private void checkNotifications() {
        DeviceLockedController pwd = new DeviceLockedController(this);
        if(pwd.isDeviceScreenLocked()) {
            try {
                NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1009);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", checkNotifications", ex.getMessage());
            }
        }
    }

    /**
     * if you need restart MQTT connection you need call this method
     */
    public void globalStartMQTT() {
        // ------------------
        // MQTT SERVICE
        // ------------------
        mServiceIntent = MQTTService.start( this.getApplicationContext() );
    }

    /**
     * Loads the Fragment
     * @param item
     */
    private void loadFragment(HashMap<String, String> item, String extra) {

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        txtToolbarTitle.setText(item.get("name").toUpperCase());

        // Information
        if (item.get("id").equals("1")) {
            FragmentInformation f = new FragmentInformation();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // Activity
        if (item.get("id").equals("2")) {
            FragmentActivity f = new FragmentActivity();
            f.setup(extra);
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

    /**
     * Load the list drawer
     */
    public void loadListDrawer(int menuItemSelected, String extra) {

        arrDrawer = new ArrayList<>();

        // Information
        HashMap<String, String> map = new HashMap<>();
        map.put("id", "1");
        map.put("name", getResources().getString(R.string.drawer_information));
        map.put("img", "ic_info");
        arrDrawer.add(map);

        // Activity
        map = new HashMap<>();
        map.put("id", "2");
        map.put("name", getResources().getString(R.string.drawer_activity));
        map.put("img", "ic_log");
        map.put("separator", "true");
        arrDrawer.add(map);

        // Feedback
        map = new HashMap<>();
        map.put("id", "8");
        map.put("name", getResources().getString(R.string.drawer_feedback));
        map.put("img", "ic_feedback");
        arrDrawer.add(map);

        // Help
        map = new HashMap<>();
        map.put("id", "4");
        map.put("name", getResources().getString(R.string.drawer_help));
        map.put("img", "ic_help");
        arrDrawer.add(map);

        // Configuration
        map = new HashMap<>();
        map.put("id", "7");
        map.put("name", getResources().getString(R.string.drawer_configuration));
        map.put("img", "ic_config");
        arrDrawer.add(map);

        // About
        map = new HashMap<>();
        map.put("id", "5");
        map.put("name", getResources().getString(R.string.drawer_about));
        map.put("img", "ic_about");
        arrDrawer.add(map);

        // if easterEgg is active
        if(cache.getEasterEgg()) {

            // MQTT Configuration
            map = new HashMap<>();
            map.put("id", "3");
            map.put("name", getResources().getString(R.string.drawer_mqtt_config));
            map.put("img", "ic_config");
            arrDrawer.add(map);

            // Test Policies
            map = new HashMap<>();
            map.put("id", "6");
            map.put("name", getResources().getString(R.string.drawer_test_policies));
            map.put("img", "ic_config");
            arrDrawer.add(map);

        }


        try {
            // lad adapter
            DrawerAdapter adapter = new DrawerAdapter(this, arrDrawer);
            lstDrawer.setAdapter(adapter);

            // Select Information on load //
            selectedItem = arrDrawer.get(menuItemSelected);
            loadFragment(selectedItem, extra);
        } catch(Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", loadListDrawer", ex.getMessage());
        }
    }
}
