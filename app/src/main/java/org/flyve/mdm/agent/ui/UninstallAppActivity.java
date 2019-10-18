/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class UninstallAppActivity extends Activity {

    private static final int APP_UNINSTALL_REQUEST = 2020;
    private String mPackage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.mPackage = extras.getString("PACKAGE");
            if(Helpers.isPackageInstalled(UninstallAppActivity.this, mPackage)) {
                try {
                    uninstallApk(mPackage);
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", onCreate", ex.getMessage());
                }
            }else{
                FlyveLog.e(this.getClass().getName() + ", onActivityResult", "Uninstallation failed or is uninstalled");
            }
        } else {
            finish();
        }
    }

    /**
     * Install the Android Package
     * @param mPackage to uninstall
     */
    public void uninstallApk(String mPackage) {

        // https://code.google.com/p/android/issues/detail?id=205827
        //        if ((Build.VERSION.SDK_INT < 24)
        //                && (!uri.getScheme().equals("file"))) {
        //            throw new RuntimeException(getString(R.string.packageinstaller_android_n_support));
        //        }

        Intent intent = new Intent();
        // Note regarding EXTRA_NOT_UNKNOWN_SOURCE:
        // works only when being installed as system-app
        // https://code.google.com/p/android/issues/detail?id=42253

        if (Build.VERSION.SDK_INT < 14) {
            intent.setAction(Intent.ACTION_DELETE);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("package:"+mPackage), "application/vnd.android.package-archive");
        } else if (Build.VERSION.SDK_INT < 16) {
            intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:"+mPackage));
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
        } else if (Build.VERSION.SDK_INT < 24) {
            intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:"+mPackage));
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        } else { // Android N
            intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:"+mPackage));
            // grant READ permission for this content Uri
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        }

        try {
            startActivityForResult(intent, APP_UNINSTALL_REQUEST);
        } catch (ActivityNotFoundException e) {
            FlyveLog.e(this.getClass().getName() + ", installApk", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case APP_UNINSTALL_REQUEST:
                if (resultCode == RESULT_OK) {
                    FlyveLog.d("Package Uninstall Success");
                } else {
                    FlyveLog.e(this.getClass().getName() + ", onActivityResult", "Uninstallation failed or is already uninstalled");
                }
                break;
        }

        finish();
    }

}
