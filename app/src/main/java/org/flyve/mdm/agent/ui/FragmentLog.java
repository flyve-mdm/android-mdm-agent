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
import org.flyve.mdm.agent.data.database.MDMLogData;
import org.flyve.mdm.agent.data.database.entity.MDMLog;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentLog extends Fragment  {

    private TextView txtMessage;
    private List<HashMap<String, String>> arrData;
    private ListView lst;
    private ProgressBar pb;
    private MDMLogData logsData;

    /**
     * Called to have the fragments instantiate its user interface View
     * @param inflater the object that can be used to inflate any views
     * @param container the parent View
     * @param savedInstanceState if non-null, this fragment is being reconstructed from a previous saved state
     * @return View the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_log, container, false);

        txtMessage = v.findViewById(R.id.txtMessage);
        pb = v.findViewById(R.id.progressBar);

        logsData = new MDMLogData(FragmentLog.this.getContext());

        FloatingActionButton btnDelete = v.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logsData.deleteAll();
                arrData.clear();
                loadLogFile();
            }
        });

        arrData = new ArrayList<>();

        final SwipeRefreshLayout swipeLayout = v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                arrData.clear();
                loadLogFile();
            }
        });

        lst = v.findViewById(R.id.lst);
        loadLogFile();

        return v;
    }

    /**
     * Load Log from files
     */
    private void loadLogFile() {
        pb.setVisibility(View.VISIBLE);

        MDMLog[] logs = logsData.getAllLogs();
        for(MDMLog log : logs) {
            HashMap<String, String> data = addLine(log.description);
            if(data!=null) {
                arrData.add(data);
            }
        }

        if(arrData.isEmpty()) {
            txtMessage.setText(getResources().getString(R.string.without_data_to_show));
        } else {
            txtMessage.setText("");
        }

        LogAdapter mAdapter = new LogAdapter(FragmentLog.this.getActivity(), arrData);
        lst.setAdapter(mAdapter);
        pb.setVisibility(View.GONE);
    }

    private HashMap<String, String> addLine(String line) {
        try {
            HashMap<String, String> map = new HashMap<>();

            JSONObject json = new JSONObject(line);

            map.put("type", json.getString("type"));
            map.put("title", json.getString("title"));
            map.put("body", json.getString("body"));
            map.put("date", json.getString("date"));

            return map;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", addLine", "ERROR: " + line + " - " + ex.getMessage());
        }

        return null;
    }

}
