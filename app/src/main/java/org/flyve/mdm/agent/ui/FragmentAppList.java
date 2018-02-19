package org.flyve.mdm.agent.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.ApplicationsAdapter;
import org.flyve.mdm.agent.room.database.AppDataBase;
import org.flyve.mdm.agent.room.entity.Application;

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

    private ListView lst;
    private ProgressBar pb;
    private AppDataBase dataBase;
    private Application[] apps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_list, container, false);

        dataBase = AppDataBase.getAppDatabase(this.getActivity());

        pb = v.findViewById(R.id.progressBar);

        final SwipeRefreshLayout swipeLayout = v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                loadData();
            }
        });

        lst = v.findViewById(R.id.lst);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Application app = apps[i];

                // Pending
                if(app.appStatus.equals("1")) {
                    Intent intent = new Intent(FragmentAppList.this.getContext(), InstallAppActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("APP_ID", app.appId);
                    intent.putExtra("APP_PATH", app.appPath);
                    FragmentAppList.this.getContext().startActivity(intent);
                }
            }
        });

        loadData();

        return v;
    }

    private void loadData() {

        pb.setVisibility(View.VISIBLE);

        apps = dataBase.applicationDao().loadAll();

        ApplicationsAdapter mAdapter = new ApplicationsAdapter(this.getActivity(), apps);
        lst.setAdapter(mAdapter);

        pb.setVisibility(View.GONE);
    }
}