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

import com.teclib.api.AndroidShell;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

public class AppManagementActivity extends Activity{

    //adb shell am start -a android.intent.action.MAIN -n
    // com.XXX.xxx/com.XXX.xxx.Main --es STRING_PAR_NAME stringParameterValue

    // am start -a android.intent.action.MAIN -n org.fdroid.fdroid/org.fdroid.fdroid.UPKDeployActivity --es UPKfilePath An.stop-1.5.upk
    // --es UPKfilePAth PATHUPK
    public static Context mContext = null;
    private SharedPreferenceAction mSharedPreferenceAction;

    @SuppressLint("SdCardPath")
    public void onStart(){

        super.onStart();
        mContext = this;
        mSharedPreferenceAction = new SharedPreferenceAction();

        String Token = "";
        FlyveLog.v("UPKDeploy Activity launched -- from MDM");
        if(getIntent().getAction() != null && getIntent().getAction().equals("android.intent.action.MAIN")){

            FlyveLog.v("UPKDeploy Activity intent exists");
            Token  = "1";
            Set<String> apks = mSharedPreferenceAction.getUpks(mContext);
            for(Iterator<String> it = apks.iterator(); it.hasNext(); ) {
                String apk = it.next();
                AppManagementTask task = new AppManagementTask(this, this, apk,true,Token);
                task.start();
            }

            finish();
        }
    }

    public void onCreate(){

    }


}
