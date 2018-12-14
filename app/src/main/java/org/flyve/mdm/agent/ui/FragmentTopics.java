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
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.TopicsAdapter;
import org.flyve.mdm.agent.data.database.TopicsData;
import org.flyve.mdm.agent.data.database.entity.Topics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentTopics extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_topics, null);

        final ListView lst = v.findViewById(R.id.lst);
        loadData(lst);

        final SwipeRefreshLayout swipeLayout = v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
                loadData(lst);
            }
        });

        return v;
    }

    private void loadData(ListView lst) {
        List<Topics> arrTopics = new TopicsData(FragmentTopics.this.getContext()).getAllTopics();

        ArrayList arr = new ArrayList<HashMap<String, Boolean>>();

        if(arrTopics.isEmpty()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("topic", "");
            map.put("status", "");
            arr.add(map);
        } else {
            for (int i = 0; i < arrTopics.size(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("topic", String.valueOf(arrTopics.get(i).topic));
                map.put("status", String.valueOf(arrTopics.get(i).status));
                arr.add(map);
            }
        }

        lst.setAdapter( new TopicsAdapter(FragmentTopics.this.getActivity(), arr));
    }
}
