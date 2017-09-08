/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @date      02/06/2017
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.ui;

import android.app.ActivityManager;
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
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.utils.FlyveLog;

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
    private DataStorage cache;

    /**
     * Perform the final clenup before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        // stop the service
        stopService(mServiceIntent);
        FlyveLog.i("onDestroy!");

        super.onDestroy();
    }

    /**
     * Called when the activity is starting 
     * @param Bundle if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cache = new DataStorage(this);

         // Setup the DrawerLayout and NavigationView
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        lstDrawer = (ListView) findViewById(R.id.lstNV);
        lstDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawers();
            selectedItem = arrDrawer.get(position);
            loadFragment(selectedItem);
            }
        });

        mFragmentManager = getSupportFragmentManager();

        // Setup Drawer Toggle of the Toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        loadListDrawer();

        // ------------------
        // MQTT SERVICE
        // ------------------
        MQTTService mMQTTService = new MQTTService();
        mServiceIntent = new Intent(this, mMQTTService.getClass());
        // Start the service
        if (!isMyServiceRunning(mMQTTService.getClass())) {
            startService(mServiceIntent);
        }
    }

    /**
     * Check if the service is running
     * @param serviceClass Class
     * @return boolean
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                FlyveLog.i ("isMyServiceRunning?", Boolean.toString( true ));
                return true;
            }
        }
        FlyveLog.i ("isMyServiceRunning?", Boolean.toString( false ));
        return false;
    }

    private void loadFragment(HashMap<String, String> item) {

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        txtToolbarTitle.setText(item.get("name").toUpperCase());

        // Information
        if (item.get("id").equals("1")) {
            FragmentInformation f = new FragmentInformation();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // Log
        if (item.get("id").equals("2")) {
            FragmentLog f = new FragmentLog();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }

        // Feedback
        if (item.get("id").equals("3")) {
            return;
        }

        // Help
        if (item.get("id").equals("4")) {
            FragmentHelp f = new FragmentHelp();
            fragmentTransaction.replace(R.id.containerView, f).commit();
            return;
        }
    }

    public void loadListDrawer() {

        arrDrawer = new ArrayList<>();

        // Information
        HashMap<String, String> map = new HashMap<>();
        map.put("id", "1");
        map.put("name", getResources().getString(R.string.drawer_information));
        map.put("img", "ic_info");
        arrDrawer.add(map);

        // if easterEgg is active
        if(cache.getEasterEgg()) {

            // Log
            map = new HashMap<>();
            map.put("id", "2");
            map.put("name", getResources().getString(R.string.drawer_log));
            map.put("img", "ic_log");
            arrDrawer.add(map);

        }

        // Feedback
        map = new HashMap<>();
        map.put("id", "3");
        map.put("name", getResources().getString(R.string.drawer_feedback));
        map.put("img", "ic_feedback");
        map.put("separator", "true");
        arrDrawer.add(map);

        // Help
        map = new HashMap<>();
        map.put("id", "4");
        map.put("name", getResources().getString(R.string.drawer_help));
        map.put("img", "ic_help");
        arrDrawer.add(map);

        try {
            // lad adapter
            DrawerAdapter adapter = new DrawerAdapter(this, arrDrawer);
            lstDrawer.setAdapter(adapter);

            // Select Information on load //
            selectedItem = arrDrawer.get(0);
            loadFragment(selectedItem);
        } catch(Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }
}
