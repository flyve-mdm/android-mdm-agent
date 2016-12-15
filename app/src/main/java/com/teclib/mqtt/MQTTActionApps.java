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

package com.teclib.mqtt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.teclib.api.AppInfo;
import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;
import com.teclib.service.NotificationRemoveService;
import com.teclib.flyvemdm.DownloadTask;

public class MQTTActionApps {

    Context mContext;
    private SharedPreferenceAction mSharedPreferenceAction;
    private AppInfo appinfo;
    private boolean isRemoveApp = false;

    public MQTTActionApps(Context context){
        mContext = context;
    }

    public void install(JSONObject jsonObject) throws JSONException {

        //download APK
        appinfo = new AppInfo(mContext);

        String idlist;
        String packageNamelist;
        String versionCode;

        idlist = jsonObject.getString("id");
        packageNamelist = jsonObject.getString("deployApp");
        versionCode = jsonObject.getString("versionCode");

        if(!appinfo.isInstall(packageNamelist,versionCode)){
            new DownloadTask(mContext).execute("application",idlist, packageNamelist);
        }
    }

    public void remove(JSONObject jsonObject) throws JSONException {

        mSharedPreferenceAction = new SharedPreferenceAction();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = mContext.getPackageManager().queryIntentActivities( mainIntent, 0);


        boolean isApp = false;
        for (Object object : pkgAppsList)
        {
            ResolveInfo info = (ResolveInfo) object;
            String strPackageName  = info.activityInfo.applicationInfo.packageName.toString();

            if(strPackageName.equals(jsonObject.getString("removeApp"))) {
                FlyveLog.d("getView: applications name =  " + strPackageName);
                isApp = true;
            }
        }

        if(isApp){
            isRemoveApp = true;
            mSharedPreferenceAction.saveApksRemove(mContext,jsonObject.getString("removeApp"));
        }


        if(isRemoveApp){
            Intent intent = new Intent(mContext, NotificationRemoveService.class);
            mContext.startService(intent);
        }
    }
}
