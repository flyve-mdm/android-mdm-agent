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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.FilesAdapter;
import org.flyve.mdm.agent.data.database.FileData;
import org.flyve.mdm.agent.data.database.entity.File;
import org.flyve.mdm.agent.utils.FlyveLog;

public class FragmentFileList extends Fragment {

    private ListView lst;
    private ProgressBar pb;
    private File[] files;
    private TextView txtNoData;

    @Override
    public void onResume(){
        super.onResume();
        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_list, container, false);

        pb = v.findViewById(R.id.progressBar);
        txtNoData = v.findViewById(R.id.txtNoData);

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
                File file = files[i];
                FlyveLog.d(file.fileName);
            }
        });

        loadData();

        return v;
    }

    private void loadData() {

        pb.setVisibility(View.VISIBLE);

        files = new FileData(FragmentFileList.this.getContext()).getAllFiles();

        if(files.length>0) {
            FilesAdapter mAdapter = new FilesAdapter(FragmentFileList.this.getActivity(), files);
            lst.setAdapter(mAdapter);
            txtNoData.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.VISIBLE);
        }

        pb.setVisibility(View.GONE);
    }
}