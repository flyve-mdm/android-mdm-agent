package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.policies.PasswordEnablePolicy;
import org.flyve.mdm.agent.utils.FlyveLog;

public class PushPoliciesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_policies);

        String payload = getIntent().getStringExtra("payload");

        PasswordEnablePolicy airplaneModePolicy = new PasswordEnablePolicy(getApplicationContext());
        airplaneModePolicy.setMqttEnable(false);
        airplaneModePolicy.setValue(true);
        airplaneModePolicy.setPriority(1);
        FlyveLog.d("Password policy: " + payload);

        try {
            airplaneModePolicy.execute();
        } catch (Exception ex) {
            FlyveLog.d(ex.getMessage());
        }
    }
}
