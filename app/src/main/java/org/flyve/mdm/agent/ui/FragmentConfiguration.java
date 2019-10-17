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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.localstorage.AppData;
import org.flyve.mdm.agent.data.localstorage.LocalStorage;
import org.flyve.mdm.agent.data.database.MqttData;

public class FragmentConfiguration extends Fragment {

    private AppData cache;

    /**
     * Create the Fragment view
     * @param inflater the object that can be used to inflate any views in the fragment
     * @param container the parent View the fragment's UI should be attached to
     * @param savedInstanceState this fragment is being re-constructed from a previous saved state
     * @return View the View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_configuration, container, false);

        cache = new AppData(FragmentConfiguration.this.getContext());

        Switch swNotifications = v.findViewById(R.id.swNotifications);
        swNotifications.setChecked(cache.getDisableNotification());
        swNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cache.setDisableNotification(b);
            }
        });

        Switch swConnectionNotification = v.findViewById(R.id.swConnectionNotification);
        swConnectionNotification.setChecked(cache.getEnableNotificationConnection());
        swConnectionNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cache.setEnableNotificationConnection(b);
            }
        });

        Switch swDarkTheme = v.findViewById(R.id.swDarkTheme);
        swDarkTheme.setChecked(cache.getDarkTheme());
        swDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cache.setDarkTheme(b);
            }
        });

        Button btnClear = v.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FragmentConfiguration.this.getContext());

                builder.setTitle(R.string.danger);
                builder.setMessage(R.string.erase_all_data_question);

                builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        LocalStorage localStorage = new LocalStorage(FragmentConfiguration.this.getContext());
                        localStorage.clearSettings();

                        new MqttData(FragmentConfiguration.this.getContext()).deleteAll();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return v;
    }
}
