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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.teclib.database.SharedPreferenceAction;
import com.teclib.service.NotificationRemoveService;

import java.util.Arrays;
import java.util.Set;

public class RemoveApplicationActivity extends Activity {
    
    private static RemoveApplicationActivity sRemoveApplicationCheckActivity;
    private ListView mlv;
    private Context mContext;
    public static boolean isInstalled;
    private Button finish_button;
    private SharedPreferenceAction sharedPreferenceAction;
    private Set<String> isEmptyApks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_application_check);
        isInstalled = false;

        mContext=this;
        sRemoveApplicationCheckActivity = this;

        sharedPreferenceAction = new SharedPreferenceAction();

        mlv=(ListView) findViewById(R.id.listView);

        CustomAdapterRemove customAdapterRemove = new CustomAdapterRemove(mContext);

        mlv.setAdapter(customAdapterRemove);
        finish_button = (Button) findViewById(R.id.finish_btn);
        Refresh();



        final Button button = (Button) findViewById(R.id.finish_btn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sharedPreferenceAction.removeApksRemove(mContext);
                finish();
                Intent i = new Intent("NotifyServiceRemoveAction");
                sendBroadcast(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Reload the view
        CustomAdapterRemove customAdapterRemove = new CustomAdapterRemove(mContext);
        mlv.setAdapter(customAdapterRemove);
        if(isInstalled){
            finish_button.setVisibility(View.VISIBLE);
        }
        else{
            finish_button.setVisibility(View.INVISIBLE);
        }
    }

    protected void onResume(){
        super.onResume();
        Refresh();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isEmptyApks = sharedPreferenceAction.getApksRemove(mContext);
        if(!Arrays.toString(isEmptyApks.toArray()).equals("[null]")){
            Intent intent = new Intent(mContext, NotificationRemoveService.class);
            mContext.startService(intent);
        }
        finish();
    }


    public static RemoveApplicationActivity getInstance() {
        return sRemoveApplicationCheckActivity;
    }


    public void Refresh(){
        if(isInstalled){
               finish_button.setVisibility(View.VISIBLE);
        }
        else{
              finish_button.setVisibility(View.INVISIBLE);
        }
    }

}
