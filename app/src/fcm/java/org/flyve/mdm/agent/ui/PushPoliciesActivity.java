package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.flyve.mdm.agent.MessagePolicies;
import org.flyve.mdm.agent.R;

public class PushPoliciesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_policies);

        String message = getIntent().getStringExtra("message");
        String topic = getIntent().getStringExtra("topic");

        TextView txtPolicies = findViewById(R.id.txtPolicies);
        txtPolicies.setText(message);

        MessagePolicies messagePolicies = new MessagePolicies();
        messagePolicies.messageArrived(PushPoliciesActivity.this, topic, message);

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PushPoliciesActivity.this.finish();
            }
        });
    }
}
