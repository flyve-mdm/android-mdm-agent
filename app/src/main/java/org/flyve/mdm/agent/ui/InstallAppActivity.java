package org.flyve.mdm.agent.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.utils.FlyveLog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_install_app);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id = extras.getString("APP_ID");
            String appPath = extras.getString("APP_PATH");

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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else if (Build.VERSION.SDK_INT < 16) {
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
        } else if (Build.VERSION.SDK_INT < 24) {
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        } else { // Android N
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(uri);
            // grant READ permission for this content Uri
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        switch (requestCode) {
            case APP_INSTALL_REQUEST:
                if (resultCode == RESULT_OK) {
                    FlyveLog.d("Package Installation Success");
                } else if (resultCode == RESULT_FIRST_USER) {
                    FlyveLog.e("Package Installation Cancelled by USER");
                } else {
                    FlyveLog.e("Something went wrong - INSTALLATION FAILED");
                }
        }
    }

}
