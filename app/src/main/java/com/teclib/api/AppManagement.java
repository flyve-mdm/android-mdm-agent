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

package com.teclib.api;

import android.content.Context;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import com.teclib.database.SharedPreferenceAction;

public class AppManagement {

    private SharedPreferenceAction mSharedPreferenceAction;
    private Context mContext;
    private AndroidShell mExec;

    /**
     * Constructor
     *
     * @param context context
     */
    public AppManagement(Context context) {
        mSharedPreferenceAction = new SharedPreferenceAction();
        mContext = context;
        mExec = new AndroidShell();
    }


    /**
     * @return SDCARD directory with apk folder
     */
    public static String getDataDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/apk");
        return System.getenv("EXTERNAL_STORAGE") + "/apk";
    }

    /**
     * Remove downloaded application after installation
     */
    public final void executeRemoveApks() throws Exception {
        File fileOrDirectory = new File(getDataDir());
        if(fileOrDirectory.isDirectory())
            for(File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    void DeleteRecursive(File fileOrDirectory) {

        if(fileOrDirectory.isDirectory())
            for(File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();

    }

    /**
     * Install an UPK on Uhuru phone
     */
    public final void executeInstallUpk() {
        String returnCommand = null;
        Set<String> apks = mSharedPreferenceAction.getUpks(mContext);

        for(Iterator<String> it = apks.iterator(); it.hasNext(); ) {
            String apk = it.next();
            FlyveLog.d(apk);
            returnCommand = mExec.execSh("am start -a android.intent.action.MAIN -n org.fdroid.fdroid/org.fdroid.fdroid.UPKDeployActivity --es UPKfilePath " + apk + " " + "--es Token " + "1");
            FlyveLog.d(returnCommand);

            returnCommand = mExec.execSh("rm " + apk);
            FlyveLog.d(returnCommand);

        }
        mSharedPreferenceAction.removeUpks(mContext);
    }

}
