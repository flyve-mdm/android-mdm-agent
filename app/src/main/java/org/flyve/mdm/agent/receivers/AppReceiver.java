package org.flyve.mdm.agent.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.data.database.setup.AppDataBase;
import org.flyve.mdm.agent.policies.BasePolicies;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.StorageFolder;

import java.io.File;

public class AppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String mPackage = intent.getData().getEncodedSchemeSpecificPart();

        switch (intent.getAction()){
            case Intent.ACTION_INSTALL_PACKAGE:
            case Intent.ACTION_PACKAGE_ADDED:
                if(isPackageInstalled(mPackage,context.getPackageManager())){
                    onInstallApp(mPackage, context);
                }
                break;

            case Intent.ACTION_PACKAGE_FULLY_REMOVED:
                if(!isPackageInstalled(mPackage,context.getPackageManager())){
                    onRemoveApp(mPackage, context);
                }
                break;

        }

    }

    public void onInstallApp(String mPackage, Context context){

        //retrieve data from applicationDAO
        AppDataBase dataBase = AppDataBase.getAppDatabase(context);
        Application[] appsArray = dataBase.applicationDao().getApplicationByPackageName(mPackage);

        //app installed by agent update internal status and send status to flyveMDM
        if(appsArray.length == 1) {
            dataBase.applicationDao().updateStatus(Integer.toString(appsArray[0].id), "2");
            BasePolicies.sendTaskStatusbyHttp(context, BasePolicies.FEEDBACK_DONE, appsArray[0].taskId);
        }
    }

    public void onRemoveApp(String mPackage, Context context){
        String taskId = "0";
        AppDataBase dataBase = AppDataBase.getAppDatabase(context);

        //retrieve taskId from applicationDAO
        Application[] appsArray = dataBase.applicationDao().getApplicationByPackageName(mPackage);

        //app installed by agent
        if(appsArray.length == 1){
            taskId = appsArray[0].taskId;
            FlyveLog.d("Retrieve TaskId -> " + appsArray[0].taskId);

            //remove file from DB
            FlyveLog.d("Remove File from DB -> "+ mPackage);
            dataBase.FileDao().deleteByName(mPackage+".apk");
            dataBase.FileDao().deleteByName(mPackage+".upk");

            //remove app from DB
            FlyveLog.d("Remove App from DB -> "+ mPackage);
            dataBase.applicationDao().deleteByPackageName(mPackage);

            //try to remove Package from device
            String filePath = "";
            try {
                filePath = new StorageFolder(context).getApkDir();
                filePath = filePath + mPackage +".apk";
                // validating if file exists
                File fileApk = new File(filePath);
                if (!fileApk.exists()) {
                    filePath = filePath + mPackage +".upk";
                }

                //remove package from device
                String realPath = new StorageFolder(context).convertPath(filePath);
                File file = new File(realPath);
                if(file.delete()){
                    FlyveLog.d("Successful removal of package : " + filePath);
                }else{
                    FlyveLog.d("Can't remove Package : " + filePath);
                }
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", removeApk", ex.getMessage());
            }

            BasePolicies.sendTaskStatusbyHttp(context, BasePolicies.FEEDBACK_DONE, taskId);
        }

    }

    private boolean isPackageInstalled(String mPackage, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(mPackage, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
