/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android.
 *
 * Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teclib.database.SharedPreferenceMQTT;


public class TabMyDevice extends Fragment {

    FragmentActivity listener;
    private SharedPreferenceMQTT sharedPreferenceMQTT;
    private Context mContext;

    // This event fires 1st, before creation of fragment or any views
    // The onAttach method is called when the Fragment instance is associated with an Activity.
    // This does not mean the Activity is fully initialized.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.listener = (FragmentActivity) context;
        }
        mContext = context;

    }

    public TabMyDevice() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_fragment_device, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        String PhoneModel = android.os.Build.MODEL;
        String PhoneSerial = Build.SERIAL;
        sharedPreferenceMQTT = new SharedPreferenceMQTT();
        String MQTTServer = sharedPreferenceMQTT.getServer(mContext);

        // Set the Text to try this out
        TextView serial = (TextView) view.findViewById(R.id.serial);
        TextView model = (TextView) view.findViewById(R.id.model);
        TextView server = (TextView) view.findViewById(R.id.server);

        serial.setText("Serial : " + PhoneSerial);
        model.setText("Model :" + PhoneModel);
        server.setText("Server :" + MQTTServer);
    }
}