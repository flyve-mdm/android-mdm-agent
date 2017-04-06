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
import org.json.JSONException;
import org.json.JSONObject;

import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;
import com.teclib.flyvemdm.AsyncTaskCallbackInterface;
import com.teclib.flyvemdm.DownloadTask;
import java.util.Set;

public class MQTTActionFiles implements AsyncTaskCallbackInterface {

    Context mContext;
    SharedPreferenceAction sharedPreferenceAction;

    public MQTTActionFiles(Context context){
        mContext = context;
    }

    public void download(JSONObject jsonObject) throws JSONException {
        //download file
        FlyveLog.d(jsonObject.getString("deployFile"));

        new DownloadTask(mContext, this).execute("file",jsonObject.getString("id"),jsonObject.getString("deployFile"));
    }

    public void delete(JSONObject jsonObject) throws JSONException {
        //delete file
        FlyveLog.d(jsonObject.getString("removeFile"));
        sharedPreferenceAction = new SharedPreferenceAction();
        Set<String> listFiles = sharedPreferenceAction.getFiles(mContext);

        for(int i = 0 ; i < listFiles.size() ; i++){
            FlyveLog.d("delete file = " + listFiles.toArray()[i]);
        }

    }

    @Override
    public void onSuccess(String status) {

    }

    @Override
    public void onFailure(Exception e) {

    }
}
