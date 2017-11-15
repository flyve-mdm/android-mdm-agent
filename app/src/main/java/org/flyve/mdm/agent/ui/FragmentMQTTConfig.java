package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.Helpers;

/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
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
 * @date      31/7/17
 * @copyright Copyright (C) 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FragmentMQTTConfig extends Fragment {

    private DataStorage cache;

    /**
     * Instantiate the user interface view
     * @param inflater the object that can be used to inflate any views in the fragment
     * @param container the parent view the fragment's UI should be attached to
     * @param savedInstanceState this fragment is being re-constructed from a previous saved state
     * @return View the view for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_mqtt_configuration, container, false);

        cache = new DataStorage(FragmentMQTTConfig.this.getContext());

        final String mBroker = cache.getBroker();
        final String mPort = cache.getPort();
        final String mUser = cache.getMqttuser();
        final String mPassword = cache.getMqttpasswd();
        final String mTopic = cache.getTopic();

        final EditText editBroker = (EditText) v.findViewById(R.id.editBroker);
        editBroker.setText( mBroker );

        final EditText editPort = (EditText) v.findViewById(R.id.editPort);
        editPort.setText( mPort );

        final EditText editUser = (EditText) v.findViewById(R.id.editUser);
        editUser.setText( mUser );

        final EditText editPassword = (EditText) v.findViewById(R.id.editPassword);
        editPassword.setText( mPassword );

        final EditText editTopic = (EditText) v.findViewById(R.id.editTopic);
        editTopic.setText( mTopic );

        FloatingActionButton btnSave = (FloatingActionButton) v.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cache.setBroker( editBroker.getText().toString() );
                cache.setPort( editPort.getText().toString() );
                cache.setMqttuser( editUser.getText().toString() );
                cache.setMqttpasswd( editPassword.getText().toString() );
                cache.setTopic( editTopic.getText().toString() );

                // restart MQTT connection with this new parameters
                ((MainActivity)FragmentMQTTConfig.this.getActivity()).globalStartMQTT();

                Helpers.snack(FragmentMQTTConfig.this.getActivity(), getString(R.string.mqtt_save_message));
            }
        });

        return v;
    }
}
