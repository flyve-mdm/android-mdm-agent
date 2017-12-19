package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.adapter.PoliciesAdapter;
import org.flyve.mdm.agent.data.DataStorage;

import java.util.ArrayList;
import java.util.HashMap;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
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
 * @date      18/12/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FragmentPolicies extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate be_calendario_fragment and setup Views.
         */
        View v = inflater.inflate(R.layout.fragment_policies, null);

        ListView lst = (ListView) v.findViewById(R.id.lst);
        loadData(lst);

        return v;
    }

    private void loadData(ListView lst) {

        DataStorage cache = new DataStorage(FragmentPolicies.this.getContext());

        ArrayList arr = new ArrayList<HashMap<String, Boolean>>();

        HashMap<String, String> map = new HashMap<>();
        map.put("description", getResources().getString(R.string.storage_encryption_device));
        map.put("value", String.valueOf(cache.getStorageEncryptionDevice()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.disable_camera));
        map.put("value", String.valueOf(cache.getDisableCamera()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_length));
        map.put("value", String.valueOf(cache.getPasswordLength()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_quality));
        map.put("value", String.valueOf(cache.getPasswordQuality()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_minimum_letters));
        map.put("value", String.valueOf(cache.getPasswordMinimumLetters()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_minimum_lower_case));
        map.put("value", String.valueOf(cache.getPasswordMinimumLowerCase()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_minimum_upper_case));
        map.put("value", String.valueOf(cache.getPasswordMinimumUpperCase()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_minimum_non_letter));
        map.put("value", String.valueOf(cache.getPasswordMinimumNonLetter()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_minimum_numeric));
        map.put("value", String.valueOf(cache.getPasswordMinimumNumeric()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.password_minimum_symbols));
        map.put("value", String.valueOf(cache.getPasswordMinimumSymbols()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.maximum_failed_passwords_for_wipe));
        map.put("value", String.valueOf(cache.getMaximumFailedPasswordsForWipe()));
        arr.add(map);

        map = new HashMap<>();
        map.put("description", getResources().getString(R.string.maximum_time_to_lock));
        map.put("value", String.valueOf(cache.getMaximumTimeToLock()));
        arr.add(map);

        lst.setAdapter( new PoliciesAdapter(FragmentPolicies.this.getActivity(), arr));

    }

}
