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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.main.Main;
import org.flyve.mdm.agent.core.main.MainPresenter;
import org.flyve.mdm.agent.services.DeviceLockedController;
import org.flyve.mdm.agent.services.MQTTService;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Main.View {

    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private ListView lst;
    private HashMap<String, String> selectedItem;
    private Intent mServiceIntent;
    private Main.Presenter presenter;
    private android.support.v7.widget.Toolbar toolbar;

    /**
     * Perform the final clenup before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        // stop the service
        if(mServiceIntent!=null) {
            stopService(mServiceIntent);
        }

        FlyveLog.i("onDestroy!");

        super.onDestroy();
    }

    /**
     * Called when the activity is starting 
     * @param savedInstanceState if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainPresenter(this);

        // start MQTT
        globalStartMQTT();

         // Setup the DrawerLayout and NavigationView
        mDrawerLayout = findViewById(R.id.drawerLayout);

        // Setup Drawer Toggle of the Toolbar
        toolbar = findViewById(R.id.toolbar);

        lst = findViewById(R.id.lst);

        mFragmentManager = getSupportFragmentManager();

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                selectedItem = presenter.getMenuItem().get(position);
                presenter.onClickItem(mFragmentManager, toolbar, selectedItem);
            }
        });

        loadMenu();

        checkNotifications();
    }

    public void loadMenu() {
        Map<String, String> menuItem = presenter.setupDrawer(MainActivity.this, lst);
        presenter.onClickItem(mFragmentManager, toolbar, menuItem);
    }

    private void checkNotifications() {
        DeviceLockedController pwd = new DeviceLockedController(this);
        if(pwd.isDeviceScreenLocked()) {
            try {
                NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1009);
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
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

    @Override
    public void showError(String message) {
        Helpers.snack(MainActivity.this, message);
    }
}
