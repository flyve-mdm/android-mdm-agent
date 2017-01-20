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
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.teclib.database.SettingsActivity;
import com.teclib.database.SharedPreferenceMQTT;

public class MQTTNotifierActivity extends AppCompatActivity {
    public static final String TAG = "MQTTNotifierActivity";
    final static String ACTION = "MQTTNotifierActivity";
    private Context mContext;
    MQTTNotifierActivityReceiver mqttNotifierActivityReceiver;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    private ViewPager viewPager;
    private RelativeLayout clickedLayout;
    private Toolbar toolbar;
    private SharedPreferenceMQTT sharedPreferenceMQTT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_notifier);

        mContext = this;
        toolbar = (Toolbar) findViewById(R.id.flyve_toolbar);
        sharedPreferenceMQTT = new SharedPreferenceMQTT();
        mqttNotifierActivityReceiver = new MQTTNotifierActivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(mqttNotifierActivityReceiver, intentFilter);


        Intent DeviceAdmin = new Intent(this.getBaseContext(), com.teclib.api.DeviceAdmin.class);
        startActivity(DeviceAdmin);


        if (toolbar != null) {
            setSupportActionBar(toolbar);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.btn_return_http_fail);
        }

        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        this.drawerToggle = new ActionBarDrawerToggle(this,this.drawerLayout,0,0);
        this.drawerLayout.setDrawerListener(this.drawerToggle);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);


        String[] tabs = { "Device", getString(R.string.app_picker_name), "Password policies", "Security policies" };

        // Adding Tabs
        for (String tab_name : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        onNewIntent(getIntent());

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(tab.getPosition());
                if (clickedLayout != null){
                    clickedLayout.setBackground(null);
                }
                switch (position) {
                    case 0:
                        toolbar.setSubtitle("Device");
                        clickedLayout = (RelativeLayout) findViewById(R.id.drawer_sys);
                        break;
                    case 1:
                        toolbar.setSubtitle(R.string.app_picker_name);
                        clickedLayout = (RelativeLayout) findViewById(R.id.drawer_apps);
                        break;
                    case 2:
                        toolbar.setSubtitle("Password policies");
                        clickedLayout = (RelativeLayout) findViewById(R.id.drawer_login);
                        break;
                    case 3:
                        toolbar.setSubtitle("Security policies");
                        clickedLayout = (RelativeLayout) findViewById(R.id.drawer_stats);
                        break;
                    default:
                        toolbar.setSubtitle("");
                        break;
                }
                clickedLayout.setBackgroundColor(getResources().getColor(R.color.bleu));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mqttNotifierActivityReceiver);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mqtt_notifier, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.uninstall:
                Uri packageUri = Uri.parse("package:com.teclib.flyvemdm");
                Intent uninstallIntent =
                        new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                startActivity(uninstallIntent);
                return true;
                */
            case R.id.settings:
                Intent intentSettings = new Intent(mContext,
                        SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void onClickDrawer(View v){
        switch (v.getId()){
            case R.id.drawer_sys:
                viewPager.setCurrentItem(0,true);
                break;
            case R.id.drawer_apps:
                viewPager.setCurrentItem(1, true);
                break;
            case R.id.drawer_login:
                viewPager.setCurrentItem(2, true);
                break;
            case R.id.drawer_stats:
                viewPager.setCurrentItem(3, true);
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);

    }

    public class MQTTNotifierActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }

}