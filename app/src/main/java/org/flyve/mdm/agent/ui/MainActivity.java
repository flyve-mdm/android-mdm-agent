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
import org.flyve.mdm.agent.utils.Helpers;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Main.View {

    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private ListView lst;
    private android.support.v7.widget.Toolbar toolbar;
    private Main.Presenter presenter;

    @Override
    public void onDestroy() {
        // Stop MQTT service
        presenter.closeMQTTService(MainActivity.this);
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

        toolbar = findViewById(R.id.toolbar);
        lst = findViewById(R.id.lst);
        mDrawerLayout = findViewById(R.id.drawerLayout);

        // start MQTT service
        presenter.startMQTTService(MainActivity.this);

        mFragmentManager = getSupportFragmentManager();

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                HashMap<String, String> selectedItem = presenter.getMenuItem().get(position);
                presenter.onClickItem(mFragmentManager, toolbar, selectedItem);
            }
        });

        loadMenu();

        // This method check if we can close any persistent notification
        presenter.checkNotifications(MainActivity.this);
    }

    // This method is implemented to reload the menu from outside this class too
    public void loadMenu() {
        Map<String, String> menuItem = presenter.setupDrawer(MainActivity.this, lst);
        presenter.onClickItem(mFragmentManager, toolbar, menuItem);
    }

    @Override
    public void showError(String message) {
        Helpers.snack(MainActivity.this, message);
    }
}
