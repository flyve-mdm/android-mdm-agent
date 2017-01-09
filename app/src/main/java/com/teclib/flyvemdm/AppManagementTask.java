package com.teclib.flyvemdm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.teclib.api.AndroidShell;
import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dlarget on 06/01/17.
 */

public class AppManagementTask extends Thread {

    public static String upkFile = "";
    public static Context mContext = null;
    private SharedPreferenceAction mSharedPreferenceAction;
    static final int REQUEST_INSTALL = 0;
    public static boolean isFromMDM = false;
    private static int token_id = -1;
    private static String repoaddress = "";
    private static final String LOG_TAG = "uhuru-store";
    private Activity mActivity;
    private AndroidShell mExec;

    public AppManagementTask(Context xCtx, Activity activity, String file, boolean isfromMDMAgent, String Token)
    {

        isFromMDM = isfromMDMAgent;
        upkFile = file;
        mContext = xCtx;
        mActivity = activity;

        FlyveLog.i("New AppManagementTask");

        if(isFromMDM){
            // Permet d'éviter d'eventuelles injections -- On veut s'assurer que c'est bien un entier
            token_id = Integer.parseInt(Token);
            FlyveLog.i("Token : "+ token_id);
        }

        FlyveLog.i("FilePath : "+ upkFile);

    }

    public void run() {
        installApk(upkFile, mActivity);
        mActivity = null;
        return;

    }

    private void installApk(String file, Activity activity) {

        String returnCommand = null;


            FlyveLog.d(file);
            //returnCommand = mExec.execSh("am start -a android.intent.action.MAIN -n org.fdroid.fdroid/org.fdroid.fdroid.UPKDeployActivity --es UPKfilePath " + apk + " " + "--es Token " + "1");
            FlyveLog.d("AppManagementTask");

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setDataAndType(Uri.parse("file://" + file),
                    "application/vnd.android.package-archive");
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("isFromMDM", true);//false
            intent.putExtra("UPKFilePath", file);
            intent.putExtra("repoaddress", "");// ""
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra("token_id", 1);
            mActivity.startActivityForResult(intent, REQUEST_INSTALL);
            // delete this after test, execSh is public now, delete
         //   returnCommand = mExec.execSh("rm " + file);
         //   FlyveLog.d(returnCommand);
    }

    public static String getUpkDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/.fdroid/");
        return System.getenv("EXTERNAL_STORAGE") + "/.fdroid/";
    }

    /**
     * @return SDCARD directory with apk folder
     */
    public static String getApkDir() throws Exception {
        FlyveLog.d(System.getenv("EXTERNAL_STORAGE") + "/apk");
        return System.getenv("EXTERNAL_STORAGE") + "/apk";
    }

    /**
     * Remove downloaded application after installation
     */
    public final void executeRemoveApks() throws Exception {
        File fileOrDirectory = new File(getApkDir());
        if(fileOrDirectory.isDirectory())
            for(File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     * Remove downloaded application after installation
     */
    public final void executeRemoveUpks() throws Exception {
        File fileOrDirectory = new File(getUpkDir());
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

}

