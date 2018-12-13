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
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class MqttPoliciesController {

    private static final String ERROR = "ERROR";
    private static final String MQTT_SEND = "MQTT Send";
    private static final String UTF_8 = "UTF-8";

    private static final String FEEDBACK_PENDING = "pending";
    private static final String FEEDBACK_RECEIVED = "received";
    private static final String FEEDBACK_DONE = "done";
    private static final String FEEDBACK_FAILED = "failed";
    private static final String FEEDBACK_CANCELED = "canceled";
    private static final String FEEDBACK_WAITING = "waiting";


    private Context context;
    private String url;

    public MqttPoliciesController(Context context) {
        this.context = context;

        Routes routes = new Routes(context);
        MqttData cache = new MqttData(context);
        url = routes.pluginFlyvemdmAgent(cache.getAgentId());
    }


    public void removePackage(String taskId, String packageName) {
        try {
            PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);
            policiesFiles.removeApk(packageName.trim());

            // return the status of the task

            MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_DONE);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", removePackage", ex.getMessage());

            // return the status of the task
            MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_FAILED);
        }
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

                    PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);
                    policiesFiles.execute("package", deployApp, id, sessionToken);

                    Helpers.broadCastMessage(MQTT_SEND, "Install package", "name: " + deployApp + " id: " + id);

                    // return the status of the task
                    MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_RECEIVED);
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", installPackage", ex.getMessage());
                    Helpers.broadCastMessage(ERROR, "Error on getActiveSessionToken", ex.getMessage());

                    // return the status of the task
                    MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_FAILED);
                }
            }

            @Override
            public void onError(int type, String error) {
                FlyveLog.e(this.getClass().getName() + ", installPackage", error);
                Helpers.broadCastMessage(String.valueOf(type), ERROR, error);

                // return the status of the task
                MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_FAILED);
            }
        });


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
                PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);

                if("true".equals(policiesFiles.execute("file", deployFile, id, sessionToken))) {
                    FlyveLog.d("File was stored on: " + deployFile);
                    Helpers.broadCastMessage(MQTT_SEND, "File was stored on", deployFile);

                    // return the status of the task
                    MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_DONE);
                }
            }

            @Override
            public void onError(int type, String error) {
                FlyveLog.e(this.getClass().getName() + ", downloadFile", error);
                Helpers.broadCastMessage(String.valueOf(type), "Error on applicationOnDevices", error);

                // return the status of the task
                MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_FAILED);
            }
        });
    }

    public void removeFile(String taskId, String removeFile) {
        try {
            PoliciesFiles policiesFiles = new PoliciesFiles(MqttPoliciesController.this.context);
            policiesFiles.removeFile(removeFile);

            FlyveLog.d("Remove file: " + removeFile);
            Helpers.broadCastMessage(MQTT_SEND, "Remove file", removeFile);

            // return the status of the task
            MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_DONE);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", removeFile", ex.getMessage());

            // return the status of the task
            MessagePolicies.pluginHttpResponse(context, url, FEEDBACK_FAILED);
        }
    }

}
