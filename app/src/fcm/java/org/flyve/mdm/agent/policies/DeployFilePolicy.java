package org.flyve.mdm.agent.policies;

import android.content.Context;

import org.flyve.mdm.agent.core.enrollment.EnrollmentHelper;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

public class DeployFilePolicy extends BasePolicies {

    public static final String POLICY_NAME = "deployFile";

    public DeployFilePolicy(Context context) {
        super(context, POLICY_NAME);
    }

    @Override
    protected boolean process() {
        try {
            JSONObject jsonObj = new JSONObject(message);

            if(jsonObj.has(POLICY_NAME)) {
                final String deployFile = jsonObj.getString(POLICY_NAME);
                final String id = jsonObj.getString("id");
                final String versionCode = jsonObj.getString("version");
                final String taskId = jsonObj.getString("taskId");

                // execute the policy
                EnrollmentHelper sToken = new EnrollmentHelper(this.context);
                sToken.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
                    @Override
                    public void onSuccess(String sessionToken) {
                        FlyveLog.d("Install file: " + deployFile + " id: " + id);
                        PoliciesFiles policiesFiles = new PoliciesFiles(context);
                        policiesFiles.execute("file", deployFile, id, sessionToken, taskId);
                    }

                    @Override
                    public void onError(int type, String error) {
                        FlyveLog.e(this.getClass().getName() + ", downloadFile", error);
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