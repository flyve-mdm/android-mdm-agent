/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
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
 * @date      02/06/2017
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.EnrollmentHelper;
import org.json.JSONObject;

public class StartEnrollmentActivity extends Activity {

    private RelativeLayout btnEnroll;
    private TextView txtMessage;
    private TextView txtTitle;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_enrollment);

        DataStorage cache = new DataStorage( StartEnrollmentActivity.this );

        txtMessage = (TextView) findViewById(R.id.txtMessage);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        pb = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        Uri data = intent.getData();

        String deepLinkData = Helpers.base64decode(data.getQueryParameter("data"));

        String url;
        String userToken;
        String invitationToken;
        String deepLinkErrorMessage = getResources().getString(R.string.ERROR_DEEP_LINK);

        try {
            JSONObject jsonLink = new JSONObject(deepLinkData);

            if(jsonLink.has("url")) {
                url = jsonLink.getString("url");
            } else {
                deepLinkErrorMessage = "URL " + deepLinkErrorMessage;
                txtMessage.setText(deepLinkErrorMessage);
                return;
            }

            if(jsonLink.has("user_token")) {
                userToken = jsonLink.getString("user_token");
            } else {
                deepLinkErrorMessage = "USER " + deepLinkErrorMessage;
                txtMessage.setText(deepLinkErrorMessage);
                return;
            }

            if(jsonLink.has("invitation_token")) {
                invitationToken = jsonLink.getString("invitation_token");
            } else {
                deepLinkErrorMessage = "TOKEN " + deepLinkErrorMessage;
                txtMessage.setText(deepLinkErrorMessage);
                return;
            }

            cache.setUrl(url);
            cache.setUserToken(userToken);
            cache.setInvitationToken(invitationToken);

        } catch (Exception ex) {
            FlyveLog.e( ex.getMessage() );
            txtMessage.setText(deepLinkErrorMessage);
            return;
        }

        btnEnroll = (RelativeLayout) findViewById(R.id.btnEnroll);
        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEnroll.setVisibility(View.GONE);
                txtMessage.setText(getResources().getString(R.string.please_wait));
                pb.setVisibility(View.VISIBLE);

                EnrollmentHelper sessionToken = new EnrollmentHelper(StartEnrollmentActivity.this);
                sessionToken.getActiveSessionToken(new EnrollmentHelper.enrollCallBack() {
                    @Override
                    public void onSuccess(String data) {
                        btnEnroll.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.GONE);
                        txtMessage.setText("");
                        txtTitle.setText(getResources().getString(R.string.start_enroll));

                        // Active EnrollmentHelper Token is stored on cache
                        openActivity();
                    }

                    @Override
                    public void onError(String error) {
                        btnEnroll.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.GONE);
                        txtMessage.setText(error);
                        txtTitle.setText(getResources().getString(R.string.fail_enroll));
                    }
                });

            }
        });
    }

    /**
     * Open activity
     */
    private void openActivity() {
        Intent miIntent = new Intent(StartEnrollmentActivity.this, EnrollmentActivity.class);
        StartEnrollmentActivity.this.startActivity(miIntent);
    }
}
