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

package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;

import java.util.List;


public class AppInfo {

    private Context mContext;

    /**
     * Constructor
     *
     * @param context context
     */
    public AppInfo(Context context) {
        mContext = context;
    }

    /**
     * check if an application is installed
     *
     * @param packageName an application package name
     * @param versionCode an application version code
     * @return true if app is installed
     */
    public boolean isInstall(String packageName, String versionCode) {

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<PackageInfo> applications = mContext.getPackageManager()
                .getInstalledPackages(0);
        for(PackageInfo info : applications) {

            String name = info.packageName;
            int version = info.versionCode;

            if(packageName.equals(name) && version == Integer.parseInt(versionCode)) {
                FlyveLog.d("isInstall: return true");
                return true;
            }
        }
        return false;
    }

    /**
     * check if an application is installed
     *
     * @param packageName an application package name
     * @return true if app is installed
     */
    public boolean isInstall(String packageName) {

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<PackageInfo> applications = mContext.getPackageManager()
                .getInstalledPackages(0);
        for(PackageInfo info : applications) {

            String name = info.packageName;

            if(packageName.equals(name)) {
                FlyveLog.d("isInstall: return true");
                return true;
            }
        }
        return false;
    }



}
