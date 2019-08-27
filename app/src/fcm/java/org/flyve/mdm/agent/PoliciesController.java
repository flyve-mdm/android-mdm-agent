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

package org.flyve.mdm.agent;

import android.content.Context;

import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONObject;

public class PoliciesController {

    private static final String ERROR = "ERROR";
    private static final String MQTT_SEND = "MQTT Send";
    private static final String UTF_8 = "UTF-8";

    public static final String FEEDBACK_PENDING = "pending";
    public static final String FEEDBACK_RECEIVED = "received";
    public static final String FEEDBACK_DONE = "done";
    public static final String FEEDBACK_FAILED = "failed";
    public static final String FEEDBACK_CANCELED = "canceled";
    public static final String FEEDBACK_WAITING = "waiting";

    private String status;


    private Context context;
    private String url;

    public PoliciesController(Context context) {
        this.context = context;

        Routes routes = new Routes(context);
        MqttData cache = new MqttData(context);
        url = routes.pluginFlyvemdmAgent(cache.getAgentId());
    }



    /**
     * Application
     */
    public void installPackage(final String deployApp, final String id, final String versionCode, final String taskId) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                try {
                    FlyveLog.d("Install package: " + deployApp + " id: " + id);
                    PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);
                    policiesFiles.execute("package", deployApp, id, sessionToken);
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", installPackage", ex.getMessage());
                    Helpers.broadCastMessage(ERROR, "Error on getActiveSessionToken", ex.getMessage());

                    // return the status of the task
                    MessagePolicies.sendTaskStatusbyHttp(context, FEEDBACK_FAILED, taskId);
                }
            }

            @Override
            public void onError(int type, String error) {
                FlyveLog.e(this.getClass().getName() + ", installPackage", error);
                Helpers.broadCastMessage(String.valueOf(type), ERROR, error);

                // return the status of the task
                MessagePolicies.sendTaskStatusbyHttp(context, FEEDBACK_FAILED, taskId);
            }
        });


    }

    public void removePackage(String taskId, String packageName) {
        try {
            PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);
            policiesFiles.removeApk(packageName.trim(), taskId);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", removePackage", ex.getMessage());
            // return the status of the task
            MessagePolicies.sendTaskStatusbyHttp(context, FEEDBACK_FAILED, taskId);
        }

    }

    /**
     * Files
     * {"file":[{"deployFile":"%SDCARD%/","id":"1","version":"1","taskId":"1"}]}
     */
    public void downloadFile(final String deployFile, final String id, final String versionCode, final String taskId) {

        EnrollmentHelper sToken = new EnrollmentHelper(this.context);
        sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String sessionToken) {
                FlyveLog.d("Install file: " + deployFile + " id: " + id);
                PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);
                policiesFiles.execute("file", deployFile, id, sessionToken, taskId);
            }

            @Override
            public void onError(int type, String error) {
                FlyveLog.e(this.getClass().getName() + ", downloadFile", error);
                Helpers.broadCastMessage(String.valueOf(type), "Error on applicationOnDevices", error);

                // return the status of the task
                MessagePolicies.sendTaskStatusbyHttp(context, FEEDBACK_FAILED, taskId);
            }
        });
    }

    public void removeFile(String taskId, String removeFile, Context context) {
        try {
            PoliciesFiles policiesFiles = new PoliciesFiles(PoliciesController.this.context);
            policiesFiles.removeFile(removeFile, taskId);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", removeFile", ex.getMessage());
            // return the status of the task
            MessagePolicies.sendTaskStatusbyHttp(context, FEEDBACK_FAILED, taskId);
        }
    }

}
