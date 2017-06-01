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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class CustomAdapterRemove extends BaseAdapter {

    private static final String TAG = CustomAdapterRemove.class.getSimpleName();
    ArrayList<DataModelRemove> listArray;
    private SharedPreferenceAction msharedPreferenceAction;
    private Set<String> mapks;
    private Context mContext;
    private boolean[] mremovedApp;

    public static String [] prgmNameList;

    public CustomAdapterRemove(Context context) {

        listArray = new ArrayList<DataModelRemove>(20); //limit 20 applications in a configuration

        msharedPreferenceAction = new SharedPreferenceAction();
        mContext = context;

        mapks = msharedPreferenceAction.getApksRemove(mContext);

        prgmNameList = mapks.toArray(new String[mapks.size()]);

        mremovedApp = new boolean[mapks.size()];

        //initialize tab
        for(int i = 0; i < mremovedApp.length; i++){
            mremovedApp[i] = false;
        }

        for(int i = 0 ; i < prgmNameList.length ; i++) {
            listArray.add(new DataModelRemove(prgmNameList[i].toString()));
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

    public boolean[] getRemoveApps() {
        return mremovedApp;
    }

    @Override
    public View getView(int index, View view, final ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.apks_list, parent, false);
        }
        final DataModelRemove dataModelRemove = listArray.get(index);

        TextView textView = (TextView) view.findViewById(R.id.textView1);
        textView.setText(dataModelRemove.getPackageName());

        Button button = (Button) view.findViewById(R.id.button_install_list);
        button.setText("Remove");
        button.setEnabled(false);


        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List pkgAppsList = mContext.getPackageManager().queryIntentActivities( mainIntent, 0);

        for (Object object : pkgAppsList)
        {
            ResolveInfo info = (ResolveInfo) object;
            String strPackageName  = info.activityInfo.applicationInfo.packageName.toString();

            if(strPackageName.equals(dataModelRemove.getPackageName())) {
                FlyveLog.i("Applications name =  " + strPackageName);
               // button.setText("Already installed");
                button.setEnabled(true);
                mremovedApp[index]=true;
            }
        }

        for(int i = 0; i < mremovedApp.length; i++){
            FlyveLog.i("removedApps = " + mremovedApp[i]);
        }
        if(areAllTrue(mremovedApp)) {
            FlyveLog.i("All app removed");
            RemoveApplicationActivity.isInstalled = true;
            RemoveApplicationActivity.getInstance().Refresh();
        }

        // button click listener
        // this chunk of code will run, if user click the button
        // because, we set the click listener on the button only

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(dataModelRemove.getPackageName());
            }
        });

        return view;
    }

    public int remove(String file){
        Uri packageUri = Uri.parse("package:"+file);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(uninstallIntent);
        } catch (ActivityNotFoundException e) {
            FlyveLog.e(e.getMessage());
            return 0;
        }

        return 1;
    }


    public static boolean areAllTrue(boolean[] minstalledApp)
    {
        for(boolean check : minstalledApp) if(check) return false;
        return true;
    }




}
