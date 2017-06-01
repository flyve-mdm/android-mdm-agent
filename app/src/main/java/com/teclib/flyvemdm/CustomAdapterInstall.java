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


import android.content.Context;
import android.content.Intent;

import android.content.pm.ResolveInfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class CustomAdapterInstall extends BaseAdapter {

    ArrayList<DataModelInstall> listArray;
    private SharedPreferenceAction msharedPreferenceAction;
    private Set<String> mapks;
    private Context mcontext;
    private String[] msplitResult;
    private String[] msplitPackageName;
    private String[] msplitResultFinal;
    private boolean[] minstalledApp;

    public static String [] prgmNameList;

    public CustomAdapterInstall(Context context) {

        listArray = new ArrayList<DataModelInstall>(20); //limit 20 applications in a configuration

        msharedPreferenceAction = new SharedPreferenceAction();
        mcontext = context;

        mapks = msharedPreferenceAction.getApks(mcontext);

        prgmNameList = mapks.toArray(new String[mapks.size()]);

        msplitResult = new String[mapks.size()];
        msplitPackageName = new String[mapks.size()];

        minstalledApp = new boolean[mapks.size()];

        //initialize tab
        for(int i = 0; i < minstalledApp.length; i++){
            minstalledApp[i] = false;
        }

        if(Arrays.toString(mapks.toArray()).equals("[null]")){
            FlyveLog.i("0 application found");
            return;
        }

        for (int i = 0; i < prgmNameList.length; i++) {
            msplitResult[i] = prgmNameList[i].split(";;")[0];
            msplitPackageName[i] = prgmNameList[i].split(";;")[2];
        }

        for(int i = 0; i < msplitResult.length; i++){
            msplitResultFinal = msplitResult[i].split("/");
            for(int j = 0 ; j < msplitResultFinal.length; j++){
                if(msplitResultFinal[j].contains(".apk")){
                    listArray.add(new DataModelInstall(msplitResultFinal[j].toString(), msplitPackageName[i]));
                }
            }
        }
    }

    @Override
    public int getCount() {
        return listArray.size();    // total number of elements in the list
    }

    @Override
    public Object getItem(int i) {
        return listArray.get(i);    // single item in the list
    }

    @Override
    public long getItemId(int i) {
        return i;                   // index number
    }

    public boolean[] getInstallApps() {
        return minstalledApp;
    }

    @Override
    public View getView(int index, View view, final ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.apks_list, parent, false);
        }
        final DataModelInstall dataModelInstall = listArray.get(index);

        TextView textView = (TextView) view.findViewById(R.id.textView1);
        textView.setText(dataModelInstall.getApk());

        Button button = (Button) view.findViewById(R.id.button_install_list);
        button.setText("install");

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List pkgAppsList = mcontext.getPackageManager().queryIntentActivities( mainIntent, 0);

        for (Object object : pkgAppsList)
        {
            ResolveInfo info = (ResolveInfo) object;
            String strPackageName  = info.activityInfo.applicationInfo.packageName.toString();

            if(strPackageName.equals(dataModelInstall.getPackageName())) {
                FlyveLog.i("Applications name =  " + strPackageName);
                button.setText("Already installed");
                button.setEnabled(false);
                minstalledApp[index]=true;
            }

        }

        if(areAllTrue(minstalledApp)) {
            FlyveLog.i("All app installed");
            TabApplications.isInstalled = true;
            TabApplications.getInstance().Refresh();

        }

        // button click listener
        // this chunk of code will run, if user click the button
        // because, we set the click listener on the button only

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlyveLog.d("Apk: " + dataModelInstall.getApk());
                for(int i = 0 ; i < prgmNameList.length; i++){
                    if(prgmNameList[i].contains(dataModelInstall.getApk())){
                        TabApplications.getInstance().install(prgmNameList[i].split(";;")[0]);
                    }
                }
            }
        });
        return view;
    }

    public static boolean areAllTrue(boolean[] minstalledApp)
    {
        for(boolean check : minstalledApp) if(!check) return false;
        return true;
    }

}
