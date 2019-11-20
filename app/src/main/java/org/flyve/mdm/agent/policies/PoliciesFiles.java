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

package org.flyve.mdm.agent.policies;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.data.database.setup.AppDataBase;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.StorageFolder;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class PoliciesFiles extends AsyncTask<String, Integer, Boolean> {

    private Context context;
    private Routes routes;
    private String taskId;
    private String type;
    private String status;



    /**
     * This constructor loads the context of the current class
     * @param context of the class
     */
    public PoliciesFiles(Context context) {
        this.context = context;
        this.routes = new Routes(context);
    }

    /**
     * Thread to download files or package on background
     * @param args args[0] = "file|package",
     *             args[1] = "file path (/storage/sdcard/documents)"
     *             args[2] = "file id on the server"
     *             args[3] = "valid session token"
     *             args[4] = "taskId"
     * @return
     */
    @Override
    protected Boolean doInBackground(String... args) {

        // check values
        if(args[0].isEmpty() ||
                args[1].isEmpty() ||
                args[2].isEmpty() ||
                args[3].isEmpty() ||
                args[4].isEmpty()) {
            return false;
        }

        this.type = args[0];
        this.taskId = args[4];
        this.status = BasePolicies.FEEDBACK_WAITING;

        if(type.equals("file")) {
            downloadFile(args[1], args[2], args[3], args[4]);
        } else if (type.equals("package")) {
            downloadApk(args[1], args[2], args[3],args[4],args[5]);
        }
        return true;
    }

    protected void onPostExecute(Boolean result) {
        if(result){
            BasePolicies.sendTaskStatusbyHttp(this.context, this.status, this.taskId);
        }
    }


    /**
     * Download and save file from Id to path
     * @param path String path to save the file on device
     * @param id String Id from
     * @param sessionToken
     */
    public void downloadFile(String path, String id, String sessionToken, String taskId) {

        //prevent CPU from going off if the user presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        String filePath = "";
        try {
            filePath = new StorageFolder(context).convertPath(path);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", downloadFile", ex.getMessage());
            this.status = BasePolicies.FEEDBACK_FAILED;
        }

        final String url = routes.pluginFlyvemdmFile(id);
        String completeFilePath = download(url, filePath, sessionToken, taskId);

        if(!completeFilePath.isEmpty()){
            this.status = BasePolicies.FEEDBACK_DONE;
        }else{
            this.status = BasePolicies.FEEDBACK_FAILED;
        }
    }

    /**
     * Download, save and install app
     * @param appName String package of the app
     * @param id String Id from
     * @param sessionToken
     */
    public void downloadApk(String appName, String id, String sessionToken,  String taskId, String versionCode) {

        //prevent CPU from going off if the user presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        String filePath = "";
        try {
            filePath = new StorageFolder(context).getApkDir();
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", downloadApk", ex.getMessage());
            this.status = BasePolicies.FEEDBACK_FAILED;
        }

        final String url = routes.pluginFlyvemdmPackage(id);
        String completeFilePath = download(url, filePath, sessionToken, taskId);
        if(completeFilePath.isEmpty()) {
            this.status = BasePolicies.FEEDBACK_FAILED;
        } else {
            if(Helpers.isSystemApp(context).equalsIgnoreCase("1")) {
                // Silently for System apps
                this.status = BasePolicies.FEEDBACK_WAITING;
                Helpers.installApkSilently(completeFilePath);
            } else {
                // Regular app
                this.status = BasePolicies.FEEDBACK_WAITING;
                Helpers.installApk(context, id, completeFilePath, taskId, versionCode);
            }
        }
    }

    /**
     * Download file from url to start need a fresh sessionToken
     * @param url String url to download the file
     * @param path String path to save
     * @return String complete path with name of the file
     */
    private String download(final String url, final String path, String sessionToken, String taskId) {
        HashMap<String, String> header = new HashMap();
        header.put("Session-Token", sessionToken);

        String data = ConnectionHTTP.getSyncWebData(url, "GET",header);
        if(data.contains("ERROR")) {
            Helpers.sendToNotificationBar(context, context.getResources().getString(R.string.download_file_fail));
            FlyveLog.e(this.getClass().getName() + ", download", data + "\n" + url);
        } else {
            try {
                JSONObject jsonObjDownload = new JSONObject(data);
                return getFile(jsonObjDownload, path, url, data, sessionToken, taskId);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", download", ex.getMessage() + "\n" + url);
                return "";
            }
        } // endif Exception
        return "";
    }

    private String getFile(JSONObject jsonObjDownload, String path, String url, String data, String sessionToken, String taskId) {

        String fileName = "";

        try {
            // Both has name
            if (jsonObjDownload.has("name")) {
                fileName = jsonObjDownload.getString("name");
            }

            // is APK / UPK
            if (jsonObjDownload.has("dl_filename")) {
                fileName = jsonObjDownload.getString("package_name");
                if(jsonObjDownload.getString("dl_filename").contains(".apk")){
                    fileName = fileName + ".apk";
                }else{
                    fileName = fileName + ".upk";
                }

            }

            // validating if folder exists or create
            new File(path).mkdirs();

            // validating if file exists
            String filePath = path + fileName;
            File file = new File(filePath);


            if (file.exists()) {

                //if file return absolute patch
                if(type.equals("file")){
                    FlyveLog.i("File exists: " + filePath);
                    addFile(file, fileName, taskId);
                    return file.getAbsolutePath();
                //if package remove file and donwload new version
                }else{
                    FlyveLog.i("Package exists: " + filePath + "Let's remove it to download new version");
                    try {
                        //remove package from device
                        String realPath = new StorageFolder(context).convertPath(filePath);
                        File fileToRemove = new File(realPath);
                        if(fileToRemove.delete()){
                            FlyveLog.d("Successful removal of package : " + filePath);
                        }else{
                            FlyveLog.d("Can't remove Package : " + filePath);
                        }
                    } catch (Exception ex) {
                        FlyveLog.e(this.getClass().getName() + ", removeApk", ex.getMessage());
                    }

                }

            }

            Boolean isSave = ConnectionHTTP.getSyncFile(url, filePath , sessionToken, new ConnectionHTTP.ProgressCallback() {
                @Override
                public void progress(int value) {
                    publishProgress(value);
                }
            });

            if (isSave) {
                publishProgress(100);
                FlyveLog.i(context.getString(R.string.download_file_ready) + file.getAbsolutePath());

                addFile(file, fileName, taskId);

                return file.getAbsolutePath();
            } else {
                publishProgress(100);
                FlyveLog.e(this.getClass().getName() + ", getFile", "Download fail: " + data + "\n" + url);

                return "";
            }

        } catch(Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", getFile", ex.getMessage()+ "\n" + url);
            return "";
        }
    }

    private void addFile(File file, String fileName, String taskId) {
        org.flyve.mdm.agent.data.database.entity.File dataFile = new org.flyve.mdm.agent.data.database.entity.File();
        dataFile.fileName = fileName;
        dataFile.filePath = file.getAbsolutePath();
        dataFile.taskid = taskId;

        AppDataBase dataBase = AppDataBase.getAppDatabase(context);
        dataBase.FileDao().deleteByName(fileName);
        dataBase.FileDao().insert(dataFile);
    }

    /**
     * Remove the file according to the given path
     * @param filePath
     * @return boolean true if file deleted, false otherwise
     */
    public void removeFile(String filePath, final String taskId) {
        this.taskId = taskId;
        try {
            String realPath = new StorageFolder(context).convertPath(filePath);
            File file = new File(realPath);
            if(file.delete()){
                FlyveLog.d("Remove file: " + filePath);

                //remove file from DB
                AppDataBase dataBase = AppDataBase.getAppDatabase(context);
                dataBase.FileDao().deleteByFilePath(file.getAbsolutePath());

                this.status = BasePolicies.FEEDBACK_DONE;
            }else{
                this.status = BasePolicies.FEEDBACK_FAILED;
            }
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", removeFile", ex.getMessage() + "\n" + filePath);
            this.status = BasePolicies.FEEDBACK_FAILED;
        }
        BasePolicies.sendTaskStatusbyHttp(this.context, this.status, this.taskId);
    }

    /**
     * Uninstall the Android Package
     * @param mPackage to uninstall
     * @return int if it succeed 1, otherwise 0
     */
    public void removeApk(String mPackage, final String taskId){
        this.taskId = taskId;
        this.status = BasePolicies.FEEDBACK_WAITING;

        //update taskID from DAO app
        AppDataBase dataBase = AppDataBase.getAppDatabase(context);
        Application[] appsArray = dataBase.applicationDao().getApplicationByPackageName(mPackage);
        //app installed by agent
        if(appsArray.length == 1) {
            dataBase.applicationDao().updateTaskId(mPackage, this.taskId);
        }

        //is priv app uninstall silently
        if(Helpers.isSystemApp(context).equalsIgnoreCase("1")) {
            // Silently for System apps
            this.status = BasePolicies.FEEDBACK_WAITING;
            Helpers.uninstallApkSilently(mPackage);
        } else {
            //use activity to install apk
            this.status = BasePolicies.FEEDBACK_WAITING;
            Helpers.uninstallApk(context,mPackage);
        }
        BasePolicies.sendTaskStatusbyHttp(this.context, this.status, this.taskId);
    }

}
