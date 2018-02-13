package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.room.database.AppDataBase;
import org.flyve.mdm.agent.room.entity.Application;

import java.util.HashMap;
import java.util.List;

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
 * @date      9/2/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FragmentAppList extends Fragment {

    private List<HashMap<String, String>> arrData;
    private ListView lst;
    private ProgressBar pb;
    private AppDataBase dataBase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_list, container, false);

        dataBase = AppDataBase.getAppDatabase(this.getActivity());

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                arrData.clear();
                loadData();
            }
        });

        lst = (ListView) v.findViewById(R.id.lst);

        loadData();

        return v;

    }

    private void makeFakeData() {
        if (dataBase == null) {
            return;
        }

        Application[] apps = new Application[4];
        apps[0] = appsInstance("1", "Flyve", "org.flyve.mdm");
        apps[1] = appsInstance("2", "Play Store", "com.google.play");
        apps[2] = appsInstance("3", "Inventory", "org.flyve.inventory");
        apps[3] = appsInstance("4", "Telegram", "com.telegram");



    }

    private Application appsInstance(String id, String appName, String packageName) {
        Application apps = new Application();

        apps.appId = id;
        apps.appName = appName;
        apps.appPackage = packageName;

        dataBase.ApplicationDao().insert(apps);

        return apps;
    }

    private void loadData() {

        if(dataBase.ApplicationDao().loadAll().length <= 0) {
            makeFakeData();
        }

        Application apps[] = dataBase.ApplicationDao().loadAll();


    }
}