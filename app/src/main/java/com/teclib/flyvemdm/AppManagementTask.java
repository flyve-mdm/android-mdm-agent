package com.teclib.flyvemdm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.teclib.api.AndroidShell;
import com.teclib.api.FlyveLog;
import com.teclib.database.SharedPreferenceAction;

import java.io.File;

/**
 * Created by dlarget on 06/01/17.
 */

public class AppManagementTask extends Thread {

    private String upkFile = "";
    private Context mContext = null;
    static final int REQUEST_INSTALL = 0;
    private boolean isFromMDM = false;
    private static int token_id = -1;
    private Activity mActivity;

    public AppManagementTask(Context xCtx, Activity activity, String file, boolean isfromMDMAgent, String Token)
    {
        isFromMDM = isfromMDMAgent;
        upkFile = file;
        mContext = xCtx;
        mActivity = activity;

        FlyveLog.d("New AppManagementTask");
        if(isFromMDM){
            token_id = Integer.parseInt(Token);
            FlyveLog.d("Token : "+ token_id);
        }

        FlyveLog.d("FilePath : "+ upkFile);

    }

    public void run() {
        installApk(upkFile, mActivity);
        mActivity = null;
        return;

    }

    private void installApk(String file, Activity activity) {
            FlyveLog.d(file);
            FlyveLog.d("AppManagementTask");

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setDataAndType(Uri.parse("file://" + file),
                    "application/vnd.android.package-archive");
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("isFromMDM", true);
            intent.putExtra("UPKFilePath", file);
            intent.putExtra("repoaddress", "");
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra("token_id", 1);
            mActivity.startActivityForResult(intent, REQUEST_INSTALL);
    }


}

