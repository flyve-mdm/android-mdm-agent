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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.LogAdapter;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.LogFileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 * This is the main activity of the app
 */
public class FragmentLog extends Fragment  {

    private TextView txtMessage;
    private TextView txtTitle;
    private ArrayList<HashMap<String, String>> arr_data;
    private ListView lst;
    private ProgressBar pb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_log, container, false);

        txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        txtTitle = (TextView) v.findViewById(R.id.txtTitle);
        pb = (ProgressBar) v.findViewById(R.id.progressBar);

        FloatingActionButton btnDelete = (FloatingActionButton) v.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlyveLog.clearLog( FlyveLog.FILE_NAME_LOG );
                arr_data.clear();
                loadLogFile();
            }
        });

        arr_data = new ArrayList<>();

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                arr_data.clear();
                loadLogFile();
            }
        });

        lst = (ListView) v.findViewById(R.id.lst);
        loadLogFile();

        return v;
    }

    /**
     * Load Log from files
     */
    private void loadLogFile() {
        pb.setVisibility(View.VISIBLE);

        LogFileReader.loadLog(FlyveLog.FILE_NAME_LOG, new LogFileReader.LogFileCallback() {
            @Override
            public void onSuccess(ArrayList<HashMap<String, String>> data) {
                pb.setVisibility(View.GONE);

                // order data last first
                Collections.reverse(data);
                arr_data = data;
                if(arr_data.isEmpty()) {
                    txtMessage.setText(getResources().getString(R.string.without_data_to_show));
                } else {
                    txtMessage.setText("");
                }

                LogAdapter mAdapter = new LogAdapter(FragmentLog.this.getActivity(), arr_data);
                lst.setAdapter(mAdapter);
            }

            @Override
            public void onError(String error) {
                pb.setVisibility(View.GONE);
                txtMessage.setText(getResources().getString(R.string.we_can_not_show_the_log));
            }
        });
    }
}
