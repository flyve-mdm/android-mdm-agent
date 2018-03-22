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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.ConnectivityAdapter;
import org.flyve.mdm.agent.data.PoliciesData;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentConnectivity extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_connectivity, null);

        ListView lst = v.findViewById(R.id.lst);
        loadData(lst);

        return v;
    }

    private void loadData(ListView lst) {

        PoliciesData cache = new PoliciesData(FragmentConnectivity.this.getContext());

        ArrayList arr = new ArrayList<HashMap<String, Boolean>>();

        HashMap<String, String> map = new HashMap<>();
        map.put("description", getResources().getString(R.string.disable_airplane_mode));
        map.put("disable", String.valueOf(cache.getConnectivityAirplaneModeDisable()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.disable_bluetooth));
        map.put("disable", String.valueOf(cache.getConnectivityBluetoothDisable()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.disable_gps));
        map.put("disable", String.valueOf(cache.getConnectivityGPSDisable()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.disable_mobile_line));
        map.put("disable", String.valueOf(cache.getConnectivityMobileLineDisable()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.disable_wifi));
        map.put("disable", String.valueOf(cache.getConnectivityWifiDisable()));
        arr.add(map);

        lst.setAdapter( new ConnectivityAdapter(FragmentConnectivity.this.getActivity(), arr));

    }

}
