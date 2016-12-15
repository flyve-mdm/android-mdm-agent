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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.teclib.database.SharedPreferenceConnectivity;
import com.teclib.database.SharedPreferencePolicies;

import java.util.ArrayList;
import java.util.List;


public class TabSecurityPolicies extends Fragment {

    private static TabSecurityPolicies sTabApplications;
    private ListView mlv;
    private Context mContext;
    private SharedPreferencePolicies msharedPreferencePolicies;
    private SharedPreferenceConnectivity msharedPreferenceConnectivity;
    private Resources mRessources;
    private Button apply_button;

    private String mSecurity_encryption;
    private String mSecurity_camera;
    private String mSecurity_wifi;
    private String mSecurity_bluetooth;
    private List<String> mSecurityList;

    FragmentActivity listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.listener = (FragmentActivity) context;
        }
        mContext = context;
    }

    public TabSecurityPolicies() {

    }

    public static TabSecurityPolicies getInstance() {
        return sTabApplications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_fragment_security, container, false);
    }

    @TargetApi(11)
    private int getDeviceEncryptionStatus() {

        int status = DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED;

        if (Build.VERSION.SDK_INT >= 11) {
            final DevicePolicyManager dpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm != null) {
                status = dpm.getStorageEncryptionStatus();
            }
        }

        return status;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        msharedPreferencePolicies = new SharedPreferencePolicies();

        apply_button = (Button) view.findViewById(R.id.apply_policies);
        apply_button.setVisibility(View.INVISIBLE);
        apply_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent intent = new Intent(DevicePolicyManager.ACTION_START_ENCRYPTION);
                mContext.startActivity(intent);
            }
        });

        if(msharedPreferencePolicies.getEncryption(mContext)){
            apply_button.setVisibility(View.VISIBLE);
            if(getDeviceEncryptionStatus()==3){
                apply_button.setVisibility(View.INVISIBLE);
            }
        }

        mlv = (ListView)getActivity().findViewById(R.id.securitylist);
        msharedPreferencePolicies = new SharedPreferencePolicies();
        msharedPreferenceConnectivity = new SharedPreferenceConnectivity();
        mRessources = getResources();
        mSecurityList = new ArrayList<String>();

        mSecurity_encryption = String.format(mRessources.getString(R.string.security_encryption), msharedPreferencePolicies.getEncryption(mContext));
        mSecurity_camera = String.format(mRessources.getString(R.string.security_camera), msharedPreferencePolicies.getCameraStatus(mContext));
        mSecurity_wifi = String.format(mRessources.getString(R.string.security_wifi), msharedPreferenceConnectivity.getWifi(mContext));
        mSecurity_bluetooth = String.format(mRessources.getString(R.string.security_bluetooth), msharedPreferenceConnectivity.getBluetooth(mContext));

        mSecurityList.add(mSecurity_encryption);
        mSecurityList.add(mSecurity_camera);
        mSecurityList.add(mSecurity_wifi);
        mSecurityList.add(mSecurity_bluetooth);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mSecurityList);
        mlv.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

}
