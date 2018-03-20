package org.flyve.mdm.agent.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.room.database.AppDataBase;
import org.flyve.mdm.agent.room.entity.Application;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

import java.io.File;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      15/2/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class InstallAppActivity extends Activity {

    private static final int APP_INSTALL_REQUEST = 1010;
    private String id;
    private String appPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_install_app);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            id = extras.getString("APP_ID");
            appPath = extras.getString("APP_PATH");

            installApk(appPath);
        } else {
            finish();
        }

    }

    /**
     * Install the Android Package
     * @param file to install
     */
    public void installApk(String file) {

        FlyveLog.d(file);
        Uri uri = Uri.fromFile(new File(file));
        if (uri == null) {
            throw new RuntimeException("Set the data uri to point to an apk location!");
        }
        // https://code.google.com/p/android/issues/detail?id=205827
        if ((Build.VERSION.SDK_INT < 24)
                && (!uri.getScheme().equals("file"))) {
            throw new RuntimeException("PackageInstaller < Android N only supports file scheme!");
        }
        if ((Build.VERSION.SDK_INT >= 24)
                && (!uri.getScheme().equals("content"))) {
            throw new RuntimeException("PackageInstaller >= Android N only supports content scheme!");
        }

        Intent intent = new Intent();

        // Note regarding EXTRA_NOT_UNKNOWN_SOURCE:
        // works only when being installed as system-app
        // https://code.google.com/p/android/issues/detail?id=42253

        if (Build.VERSION.SDK_INT < 14) {
            intent.setAction(Intent.ACTION_VIEW);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else if (Build.VERSION.SDK_INT < 16) {
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(uri);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
        } else if (Build.VERSION.SDK_INT < 24) {
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(uri);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        } else { // Android N
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(uri);
            // grant READ permission for this content Uri
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        }

        try {
            startActivityForResult(intent, APP_INSTALL_REQUEST);
        } catch (ActivityNotFoundException e) {
            FlyveLog.e(e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String status = "1"; // pending
        switch (requestCode) {
            case APP_INSTALL_REQUEST:
                if (resultCode == RESULT_OK) {
                    FlyveLog.d("Package Installation Success");
                    status = "2"; // installed
                } else {
                    FlyveLog.d("Installation failed");
                }
        }

        try {
            PackageManager packageManager = InstallAppActivity.this.getPackageManager();

            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(appPath, 0);
            packageInfo.applicationInfo.sourceDir = appPath;
            packageInfo.applicationInfo.publicSourceDir = appPath;

            String appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
            String appPackage = packageInfo.packageName;

            AppDataBase dataBase = AppDataBase.getAppDatabase(InstallAppActivity.this);
            if(dataBase.applicationDao().getApplicationById(id).length <= 0) {
                Application apps = new Application();

                apps.appId = id;
                apps.appName = appName;
                apps.appPath = appPath;
                apps.appStatus = status; // 1 pending | 2 installed
                apps.appPackage = appPackage;

                dataBase.applicationDao().insert(apps);
            } else {
                // update status to installed
                dataBase.applicationDao().updateStatus(id, "2");
            }

            if(status.equals("2")) {
                // remove notifications if exists
                NotificationManager notificationManager = (NotificationManager) InstallAppActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Integer.parseInt(id));

            } else {
                Helpers.sendToNotificationBar(InstallAppActivity.this, Integer.parseInt(id), getString(R.string.app_pending_to_install), appName, true, MainActivity.class);
            }

        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }

        finish();
    }

}
