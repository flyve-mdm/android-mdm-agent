package org.flyve.mdm.agent.policies;

import android.content.Context;

import org.flyve.mdm.agent.PoliciesController;
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
            String deployApp = jsonObj.getString("deployApp");
            String id = jsonObj.getString("id");
            String versionCode = jsonObj.getString("versionCode");
            String taskId = jsonObj.getString("taskId");

            ApplicationData apps = new ApplicationData(context);
            Application[] appsArray = apps.getApplicationsById(id);

            // check if the app exists with same version or older
            Boolean bDownload = true;
            if(appsArray.length>0 && Integer.parseInt(versionCode) >= Integer.parseInt(appsArray[0].appVersionCode)) {
                bDownload = false;
            }

            if(bDownload) {
                // execute the policy
                PoliciesController policiesController = new PoliciesController(context);
                policiesController.installPackage(deployApp, id, versionCode, taskId);
            }

            return true;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            return false;
        }
    }
}