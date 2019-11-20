package org.flyve.mdm.agent.policies;

import android.content.Context;

import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.data.database.ApplicationData;
import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

public class DeployAppPolicy extends BasePolicies {

    public static final String POLICY_NAME = "deployApp";

    public DeployAppPolicy(Context context) {
        super(context, POLICY_NAME);
    }

    @Override
    protected boolean process() {
        try {
            JSONObject jsonObj = new JSONObject(message);
            final String deployApp = jsonObj.getString("deployApp");
            final String id = jsonObj.getString("id");
            final String versionCode = jsonObj.getString("versionCode");
            final String taskId = jsonObj.getString("taskId");

            ApplicationData apps = new ApplicationData(context);
            Application[] appsArray = apps.getApplicationsById(id);

            // check if the app exists with same version or older
            Boolean bDownload = true;
            if(appsArray.length > 0 && Integer.parseInt(versionCode) <= Integer.parseInt(appsArray[0].appVersionCode)) {
                bDownload = false;
            }

            if(bDownload) {

                EnrollmentHelper sToken = new EnrollmentHelper(this.context);
                sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
                    @Override
                    public void onSuccess(String sessionToken) {
                        try {
                            FlyveLog.d("Download package: " + deployApp + " id: " + id);
                            PoliciesFiles policiesFiles = new PoliciesFiles(context);
                            policiesFiles.execute("package", deployApp, id, sessionToken, taskId, versionCode);
                        } catch (Exception ex) {
                            FlyveLog.e(this.getClass().getName() + ", installPackage", ex.getMessage());
                        }
                    }

                    @Override
                    public void onError(int type, String error) {
                        FlyveLog.e(this.getClass().getName() + ", installPackage", error);
                    }
                });
            }

            return true;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            return false;
        }
    }
}