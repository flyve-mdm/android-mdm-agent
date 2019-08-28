package org.flyve.mdm.agent.policies;

import android.content.Context;

import org.flyve.mdm.agent.PoliciesController;
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
                String deployFile = jsonObj.getString(POLICY_NAME);
                String id = jsonObj.getString("id");
                String versionCode = jsonObj.getString("version");
                String taskId = jsonObj.getString("taskId");

                // execute the policy
                PoliciesController policiesController = new PoliciesController(context);
                policiesController.downloadFile(deployFile, id, versionCode, taskId);
            }

            return true;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            return false;
        }
    }
}