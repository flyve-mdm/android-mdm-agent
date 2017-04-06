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

import java.util.List;

import com.teclib.api.AppInfo;
import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;
import com.teclib.flyvemdm.AsyncTaskCallbackInterface;
import com.teclib.service.NotificationRemoveService;
import com.teclib.flyvemdm.DownloadTask;

public class MQTTActionApps extends MQTTAction implements AsyncTaskCallbackInterface {

    Context mContext;
    private SharedPreferenceAction mSharedPreferenceAction;
    private AppInfo appinfo;
    private boolean isRemoveApp = false;
    private JSONObject mApplicationAction;

    public MQTTActionApps(Context context){
        mContext = context;
    }

    public void install(JSONObject jsonObject) throws JSONException {
        mApplicationAction = jsonObject;

        //download APK
        appinfo = new AppInfo(mContext);

        String idlist = jsonObject.getString("id");
        String packageNamelist = jsonObject.getString("deployApp");
        String versionCode = jsonObject.getString("versionCode");

        if (!appinfo.isInstall(packageNamelist, versionCode)) {
            mTaskFeedback = new JSONArray();
            new DownloadTask(mContext, this).execute("application", idlist, packageNamelist);
        }
    }

    public void remove(JSONObject jsonObject) throws JSONException {

        mSharedPreferenceAction = new SharedPreferenceAction();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = mContext.getPackageManager().queryIntentActivities( mainIntent, 0);


        boolean isApp = false;
        mTaskFeedback = new JSONArray();
        for (Object object : pkgAppsList) {
            ResolveInfo info = (ResolveInfo) object;
            String strPackageName  = info.activityInfo.applicationInfo.packageName;

            if (strPackageName.equals(jsonObject.getString("removeApp"))) {
                FlyveLog.d("getView: applications name =  " + strPackageName);
                isApp = true;
            }
        }

        if (isApp){
            isRemoveApp = true;
            mSharedPreferenceAction.saveApksRemove(mContext,jsonObject.getString("removeApp"));
        }


        if (isRemoveApp) {
            Intent intent = new Intent(mContext, NotificationRemoveService.class);
            mContext.startService(intent);
        }
    }

    @Override
    public void onSuccess(String status) {
        addTaskToFeedback(mApplicationAction, status);
        sendTaskFeedback();
    }

    @Override
    public void onFailure(Exception e) {
        addTaskToFeedback(mApplicationAction, e.getMessage());
        sendTaskFeedback();
        FlyveLog.e(e.getMessage());
    }
}
