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
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.teclib.api.FlyveLog;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.io.File;


@ReportsCrashes(
        formKey = "",
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON
        //mode = ReportingInteractionMode.DIALOG,
        //sharedPreferencesName = "com.teclib.flyvemdm_preferences",
        //sharedPreferencesMode = Context.MODE_PRIVATE,
        //resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
        //resDialogText = R.string.crash_dialog_text,
        //resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
        //resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        //resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
        //resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class MainApplication extends Application {

    private static MainApplication instance;
    protected Boolean isDebuggable;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ACRA.init(this);
        Logger
                .init("FlyveMDM")                 // default PRETTYLOGGER or use just init()
                .methodCount(2)                 // default 2
                .hideThreadInfo()               // default shown
                .logLevel(LogLevel.FULL)        // default LogLevel.FULL Put LogLevel.NONE in release
                .methodOffset(0);             // default 0

        isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
        SharedPreferences acra = PreferenceManager.getDefaultSharedPreferences(this);
        if(!acra.getString("server","null").equals("null"))
            acraInit();
    }

    public static MainApplication getInstance(){
        return instance;
    }

    protected void acraInit(){
        SharedPreferences acra = PreferenceManager.getDefaultSharedPreferences(this);
        String entity = acra.getString("server", "null");
        String login = acra.getString("login","null");
        String password = acra.getString("password","null");

        ACRAConfiguration config = ACRA.getNewDefaultConfig(this);
        config.setFormUri("http://reporting.demo.flyve.org/"+entity+"/_design/acra-storage/_update/report");
        config.setFormUriBasicAuthLogin(login);
        config.setFormUriBasicAuthPassword(password);
        ACRA.setConfig(config);
        ACRA.init(this);
    }

    public void clearApplicationData() {

        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                    FlyveLog.d(fileName);
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }

    public void killApp() {
        FlyveLog.d("Kill app");
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public Boolean getIsDebuggable() {
        return isDebuggable;
    }

}
