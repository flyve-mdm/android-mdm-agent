package com.teclib.flyvemdm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.teclib.api.FlyveLog;

/**
 * Created by dlarget on 06/01/17.
 */

public class AppManagementTask extends Thread {

    private String upkFile = "";
    static final int REQUEST_INSTALL = 0;
    private boolean isFromMDM = false;
    private static int tokenId = -1;
    private Activity mActivity;

    public AppManagementTask(Context xCtx, Activity activity, String file, boolean isfromMDMAgent, String token)
    {
        isFromMDM = isfromMDMAgent;
        upkFile = file;
        mActivity = activity;

        FlyveLog.d("New AppManagementTask");
        if(isFromMDM){
            tokenId = Integer.parseInt(token);
            FlyveLog.d("Token : "+ tokenId);
        }

        FlyveLog.d("FilePath : "+ upkFile);

    }

    @Override
    public void run() {
        installApk(upkFile);
        mActivity = null;
        return;

    }

    private void installApk(String file) {
            FlyveLog.d(file);
            FlyveLog.d("AppManagementTask");

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setDataAndType(Uri.parse("file://" + file),
                    "application/vnd.android.package-archive");
            intent.putExtra("isFromMDM", true);
            intent.putExtra("UPKFilePath", file);
            intent.putExtra("repoaddress", "");
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra("token_id", 1);
            mActivity.startActivityForResult(intent, REQUEST_INSTALL);
    }


}

