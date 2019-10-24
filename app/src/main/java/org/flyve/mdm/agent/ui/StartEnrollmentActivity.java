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

package org.flyve.mdm.agent.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.core.deeplink.Deeplink;
import org.flyve.mdm.agent.core.deeplink.DeeplinkPresenter;
import org.flyve.mdm.agent.core.deeplink.DeeplinkSchema;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.utils.Helpers;

public class StartEnrollmentActivity extends Activity implements Deeplink.View {

    private static final int REQUEST_EXIT = 1;

    private Deeplink.Presenter presenter;
    private RelativeLayout btnEnroll;
    private TextView txtMessage;
    private TextView txtTitle;
    private ProgressBar pb;

    /**
     * Called when the activity is starting
     * It shows the UI to start the enrollment
     * @param savedInstanceState if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_enrollment);

        sendBroadcast();

        presenter = new DeeplinkPresenter(this);

        // check if broker is on cache open the main activity
        MqttData cache = new MqttData( StartEnrollmentActivity.this );

        if(!cache.getBroker().isEmpty()) {
            openMain();
        }

        TextView txtIntro = findViewById(R.id.txtIntro);
        txtIntro.setText( Html.fromHtml(StartEnrollmentActivity.this.getResources().getString(R.string.walkthrough_step_1)) );
        txtIntro.setMovementMethod(LinkMovementMethod.getInstance());
        txtMessage = findViewById(R.id.txtMessage);
        txtTitle = findViewById(R.id.txtTitle);
        pb = findViewById(R.id.progressBar);

        // get the deeplink
        String deeplink = "";
        Intent intent = getIntent();
        Uri data = null;

        // come from QR scan
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            String str = bundle.getString("data");
            if (str != null) {
                data = Uri.parse(str);
            }
        }

        // come from deeplink
        if(data==null) {
            data = intent.getData();
        }

        try {
            deeplink = data.getQueryParameter("data");
        } catch (Exception ex) {
            presenter.showSnackError(CommonErrorType.DEEPLINK_GETQUERYPARAMETER, ex.getMessage());
        }

        TextView txtVersion = findViewById(R.id.txtVersion);
        txtVersion.setText(MDMAgent.getCompleteVersion());

        presenter.lint(StartEnrollmentActivity.this, deeplink);

        btnEnroll = findViewById(R.id.btnEnroll);
        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(OptionsEnrollmentActivity.needOptions()){
                    Intent miIntent = new Intent(StartEnrollmentActivity.this, OptionsEnrollmentActivity.class);
                    StartEnrollmentActivity.this.startActivity(miIntent);
                    StartEnrollmentActivity.this.finish();
                }else{
                    Intent miIntent = new Intent(StartEnrollmentActivity.this, PermissionEnrollmentActivity.class);
                    StartEnrollmentActivity.this.startActivity(miIntent);
                    StartEnrollmentActivity.this.finish();
                }



            }
        });
    }

    /**
     * Send a Broadcast with the Close Action
     */
    public void sendBroadcast() {
        //send broadcast
        Intent in = new Intent();
        in.setAction("flyve.ACTION_CLOSE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
    }

    /**
     * Open the main activity
     */
    private void openMain() {
        Intent intent = new Intent(StartEnrollmentActivity.this, MainActivity.class);
        StartEnrollmentActivity.this.startActivity(intent);
        StartEnrollmentActivity.this.finish();
    }

    /**
     * Shows an error message
     * @param message
     */
    @Override
    public void showSnackError(int type, String message) {
        txtTitle.setText(getResources().getString(R.string.fail_enroll));
        txtMessage.setText(getResources().getString(R.string.error_message_with_number, String.valueOf(type), message));
        Helpers.snack(this, getResources().getString(R.string.error_message_with_number, String.valueOf(type), message), this.getResources().getString(R.string.snackbar_close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public void lintSuccess(DeeplinkSchema deeplinkSchema) {
        presenter.saveMQTTConfig(StartEnrollmentActivity.this, deeplinkSchema.getUrl(), deeplinkSchema.getUserToken(), deeplinkSchema.getInvitationToken());
        presenter.saveSupervisor(StartEnrollmentActivity.this, deeplinkSchema.getName(), deeplinkSchema.getPhone(), deeplinkSchema.getWebsite(), deeplinkSchema.getEmail());

        TextView txtURL = findViewById(R.id.txtURL);
        txtURL.setText(getResources().getString(R.string.deeplink_url, deeplinkSchema.getUrl().substring(0,15)));

        TextView txtInvitationToken = findViewById(R.id.txtInvitationToken);
        txtInvitationToken.setText(getResources().getString(R.string.deeplink_invitation_token, deeplinkSchema.getInvitationToken().substring(0,15)));

        TextView txtUserToken = findViewById(R.id.txtUserToken);
        txtUserToken.setText(getResources().getString(R.string.deeplink_user_token, deeplinkSchema.getUserToken().substring(0,15)));
    }

    @Override
    public void openEnrollSuccess() {
        btnEnroll.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
        txtMessage.setText(getResources().getString(R.string.success));
        txtTitle.setText(getResources().getString(R.string.start_enroll));
    }

    @Override
    public void openEnrollFail() {
        btnEnroll.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
    }

    /**
     * Called when the launched activity exits
     * @param requestCode the request code originally supplied, it identifies who this result came from
     * @param resultCode the result code returned
     * @param data an intent which can return result data to the caller
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EXIT && resultCode == RESULT_OK) {
            this.finish();
        }
    }
}
