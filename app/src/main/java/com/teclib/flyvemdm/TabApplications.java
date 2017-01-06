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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;

import java.io.File;


public class TabApplications extends Fragment {

    private static TabApplications sTabApplications;
    private ListView mlv;
    private TextView mTextview;
    private Context mContext;
    public static boolean isInstalled;
    private Button finish_button;
    private SharedPreferenceAction sharedPreferenceAction;
    private AppManagementActivity appManagementActivity;

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

    public TabApplications() {
        // Required empty public constructor
    }

    public static TabApplications getInstance() {
        return sTabApplications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_install_application_check, container, false);

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        isInstalled = false;
        sTabApplications = this;

        sharedPreferenceAction = new SharedPreferenceAction();

        mlv=(ListView) view.findViewById(R.id.listView);
        mTextview = (TextView) view.findViewById(R.id.information);

        mTextview.setVisibility(View.INVISIBLE);

        CustomAdapterInstall customAdapterInstall = new CustomAdapterInstall(mContext);

        mlv.setAdapter(customAdapterInstall);
        finish_button = (Button) view.findViewById(R.id.finish_btn);
        Refresh();

        final Button button = (Button) view.findViewById(R.id.finish_btn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sharedPreferenceAction.removeApks(mContext);
                try {
                    //TODO gestion suppression app
                   // appManagementActivity.executeRemoveApks();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getActivity().finish();

                Intent i = new Intent("NotifyInstallServiceAction");
                getActivity().sendBroadcast(i);
            }
        });

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }


    @Override
    public void onStart() {
        super.onStart();

        CustomAdapterInstall customAdapterInstall = new CustomAdapterInstall(mContext);
        mlv.setAdapter(customAdapterInstall);
        if(isInstalled){
            testFinish();
            finish_button.setVisibility(View.VISIBLE);
        }
        else{
            finish_button.setVisibility(View.INVISIBLE);
        }
    }

    public void Refresh(){
        if(isInstalled){
            testFinish();
            finish_button.setVisibility(View.VISIBLE);
        }
        else{
            finish_button.setVisibility(View.INVISIBLE);
        }
    }

    public void testFinish(){
        sharedPreferenceAction.removeApks(mContext);

        try {
            //TODO gestion suppression app
            //appManagementActivity.executeRemoveApks();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().finish();

        Intent i = new Intent("NotifyInstallServiceAction");
        getActivity().sendBroadcast(i);
    }

/*
    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach: ");
        super.onDetach();
        this.listener = null;
    }
*/

    public void install(String file){
        File apk = new File(file);
        FlyveLog.i("Install : " + file);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk),
                "application/vnd.android.package-archive");

        startActivityForResult(intent, 1);

    }

}
