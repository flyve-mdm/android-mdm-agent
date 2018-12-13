package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.flyve.mdm.agent.MessagePolicies;
import org.flyve.mdm.agent.R;

public class PushPoliciesActivity extends AppCompatActivity {

    private Button btnClose;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_policies);

        progressBar = findViewById(R.id.progressBar);

        String message = getIntent().getStringExtra("message");
        String topic = getIntent().getStringExtra("topic");

        TextView txtPolicies = findViewById(R.id.txtPolicies);
        txtPolicies.setText(message);

        MessagePolicies messagePolicies = new MessagePolicies();
        messagePolicies.messageArrived(PushPoliciesActivity.this, topic, message);

        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PushPoliciesActivity.this.finish();
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnClose.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, 5000);
    }
}
