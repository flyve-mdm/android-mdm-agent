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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.teclib.database.SharedPreferencePolicies;

import java.util.ArrayList;
import java.util.List;


public class TabPasswordPolicies extends Fragment {

    private static TabPasswordPolicies sTabApplications;
    private ListView mlv;
    private Context mContext;
    private SharedPreferencePolicies msharedPreferencePolicies;
    private Resources mRessources;

    private String mPassword_quality;
    private String mPassword_minimum_length;
    private String mPassword_minimum_numeric;
    private String mPassword_minimum_letters;
    private String mPassword_minimum_symbols;
    private String mPassword_minimum_non_letters;
    private String mPassword_minimum_upper_case;
    private String mPassword_minimum_lower_case;
    private String mPassword_minimum_failed_before_wipe;
    private String mPassword_minimum_before_wipe;
    private String mPassword_minimum_before_lock;
    private List<String> mpaswwordList;

    FragmentActivity listener;

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

    public TabPasswordPolicies() {
        // Required empty public constructor
    }

    public static TabPasswordPolicies getInstance() {
        return sTabApplications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.tab_fragment_policies, container, false);

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mlv = (ListView)getActivity().findViewById(R.id.passwordlist);
        msharedPreferencePolicies = new SharedPreferencePolicies();
        mRessources = getResources();
        mpaswwordList = new ArrayList<String>();
        mPassword_quality = String.format(mRessources.getString(R.string.password_quality), msharedPreferencePolicies.getQualityPassword(mContext));
        mPassword_minimum_length = String.format(mRessources.getString(R.string.password_minimum_length), msharedPreferencePolicies.getMinLength(mContext));
        mPassword_minimum_numeric = String.format(mRessources.getString(R.string.password_minimum_numeric), msharedPreferencePolicies.getMinNumeric(mContext));
        mPassword_minimum_letters = String.format(mRessources.getString(R.string.password_minimum_letters), msharedPreferencePolicies.getMinLetters(mContext));
        mPassword_minimum_symbols = String.format(mRessources.getString(R.string.password_minimum_symbols), msharedPreferencePolicies.getMinSymbols(mContext));
        mPassword_minimum_non_letters = String.format(mRessources.getString(R.string.password_minimum_non_letters), msharedPreferencePolicies.getMinNonLetter(mContext));
        mPassword_minimum_upper_case = String.format(mRessources.getString(R.string.password_minimum_upper_case), msharedPreferencePolicies.getMinUpperCase(mContext));
        mPassword_minimum_lower_case = String.format(mRessources.getString(R.string.password_minimum_lower_case), msharedPreferencePolicies.getMinLowerCase(mContext));
        mPassword_minimum_failed_before_wipe = String.format(mRessources.getString(R.string.password_minimum_failed_before_wipe), msharedPreferencePolicies.getMaxFailedWipe(mContext));
        mPassword_minimum_before_lock = String.format(mRessources.getString(R.string.password_minimum_before_lock), msharedPreferencePolicies.getMaxTimeToLock(mContext));

        mpaswwordList.add(mPassword_quality);
        mpaswwordList.add(mPassword_minimum_length);
        mpaswwordList.add(mPassword_minimum_numeric);
        mpaswwordList.add(mPassword_minimum_letters);
        mpaswwordList.add(mPassword_minimum_symbols);
        mpaswwordList.add(mPassword_minimum_non_letters);
        mpaswwordList.add(mPassword_minimum_upper_case);
        mpaswwordList.add(mPassword_minimum_lower_case);
        mpaswwordList.add(mPassword_minimum_failed_before_wipe);
        mpaswwordList.add(mPassword_minimum_before_lock);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, mpaswwordList);
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
