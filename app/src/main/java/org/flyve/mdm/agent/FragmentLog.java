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

package org.flyve.mdm.agent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.flyve.mdm.agent.adapter.LogAdapter;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 * This is the main activity of the app
 */
public class FragmentLog extends Fragment {

    private TextView txtMessage;
    private TextView txtTitle;
    private ArrayList<HashMap<String, String>> arr_data;
    private LogAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_log, container, false);

        txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        txtTitle = (TextView) v.findViewById(R.id.txtTitle);

        arr_data = new ArrayList<>();

        ListView lst = (ListView) v.findViewById(R.id.lst);
        mAdapter = new LogAdapter(FragmentLog.this.getActivity(), arr_data);
        lst.setAdapter(mAdapter);

        loadLogFile();

        return v;
    }

    private void addLine(String line) {
        try {
            HashMap<String, String> map = new HashMap<>();

            JSONObject json = new JSONObject(line);

            map.put("type", json.getString("type"));
            map.put("title", json.getString("title"));
            map.put("body", json.getString("body"));
            map.put("date", json.getString("date"));

            arr_data.add(map);
            Collections.reverse(arr_data);
            mAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            FlyveLog.d("ERROR" + ex.getMessage());
        }
    }

    /**
     * Load Log from files
     */
    private void loadLogFile() {
        File file = new File("/sdcard/FlyveMDM/" + FlyveLog.FILE_NAME_LOG);
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                addLine(line);
            }
            br.close();
        }
        catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
        finally {
            if(fr!=null) {
                try {
                    fr.close();
                }catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                }
            }
        }
    }
}
