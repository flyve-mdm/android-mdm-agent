package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.ConnectivityHelper;

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
 * @date      25/11/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FragmentTestPolicies extends Fragment {

    private DataStorage cache;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_test_policies, container, false);

        cache = new DataStorage(FragmentTestPolicies.this.getContext());

        Switch swGPS = (Switch) v.findViewById(R.id.swGPS);

        swGPS.setChecked(cache.getConnectivityGPSDisable());
        swGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityGPSDisable(isChecked);
                if(isChecked) {
                    ConnectivityHelper.disableGps(isChecked);
                }
            }
        });

        Switch swAirplane = (Switch) v.findViewById(R.id.swAirplane);
        swAirplane.setChecked(cache.getConnectivityAirplaneModeDisable());
        swAirplane.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityAirplaneModeDisable(isChecked);
                ConnectivityHelper.disableAirplaneMode(isChecked);
            }
        });

        Switch swBluetooth = (Switch) v.findViewById(R.id.swBluetooth);
        swBluetooth.setChecked(cache.getConnectivityBluetoothDisable());
        swBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityBluetoothDisable(isChecked);
                if(isChecked) {
                    ConnectivityHelper.disableBluetooth(isChecked);
                }
            }
        });

        Switch swWifi = (Switch) v.findViewById(R.id.swWifi);
        swWifi.setChecked(cache.getConnectivityWifiDisable());
        swWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityWifiDisable(isChecked);
                if(isChecked) {
                    ConnectivityHelper.disableWifi(isChecked);
                }
            }
        });

        Switch swNFC = (Switch) v.findViewById(R.id.swNFC);
        swNFC.setChecked(cache.getConnectivityRoamingDisable());
        swNFC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityNFCDisable(isChecked);
                if(isChecked) {
                    ConnectivityHelper.disableNFC(isChecked);
                }
            }
        });

        Switch swHostpot = (Switch) v.findViewById(R.id.swHostpot);
        swHostpot.setChecked(cache.getConnectivityHostpotTetheringDisable());
        swHostpot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityHostpotTetheringDisable(isChecked);
                if(isChecked) {
                    ConnectivityHelper.disableHostpotTethering(isChecked);
                }
            }
        });

        Switch swMobileLine = (Switch) v.findViewById(R.id.swMobileLine);
        swMobileLine.setChecked(cache.getConnectivityMobileLineDisable());
        swMobileLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityMobileLineDisable(isChecked);
                if(isChecked) {
                    ConnectivityHelper.disableMobileLine(isChecked);
                }
            }
        });

        Switch swUsbOnTheGo = (Switch) v.findViewById(R.id.swUsbOnTheGo);
        swUsbOnTheGo.setChecked(cache.getConnectivityUsbFileTransferProtocolsDisable());
        swUsbOnTheGo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cache.setConnectivityUsbFileTransferProtocolsDisable(isChecked);
                if(isChecked) {
                    ConnectivityHelper.disableUsbFileTransferProtocols(isChecked);
                }
            }
        });

        return v;
    }

    }
