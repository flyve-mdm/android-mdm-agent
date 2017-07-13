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
import org.flyve.mdm.agent.utils.Session;
import org.json.JSONObject;

public class StartEnrollmentActivity extends Activity {

    private RelativeLayout btnEnroll;
    private TextView tvStatus;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_enrollment);

        DataStorage cache = new DataStorage( StartEnrollmentActivity.this );

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        pb = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        Uri data = intent.getData();

        String deepLinkData = Helpers.base64decode(data.getQueryParameter("data"));

        String url = "";
        String userToken = "";
        String invitationToken = "";

        try {
            JSONObject jsonLink = new JSONObject(deepLinkData);

            if(jsonLink.has("url")) {
                url = jsonLink.getString("url");
            }

            if(jsonLink.has("user_token")) {
                userToken = jsonLink.getString("user_token");
            }

            if(jsonLink.has("invitation_token")) {
                invitationToken = jsonLink.getString("invitation_token");
            }

            cache.setUrl(url);
            cache.setUserToken(userToken);
            cache.setInvitationToken(invitationToken);

        } catch (Exception ex) {
            FlyveLog.e( ex.getMessage() );
        }

        btnEnroll = (RelativeLayout) findViewById(R.id.btnEnroll);
        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEnroll.setVisibility(View.GONE);
                tvStatus.setText(getResources().getString(R.string.please_wait));
                pb.setVisibility(View.VISIBLE);

                Session sessionToken = new Session(StartEnrollmentActivity.this);
                sessionToken.getActiveSessionToken(new Session.sessionCallback() {
                    @Override
                    public void onSuccess(String data) {
                        btnEnroll.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.GONE);
                        tvStatus.setText("");

                        // Active Session Token is stored on cache
                        openActivity();
                    }

                    @Override
                    public void onError(String error) {
                        btnEnroll.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.GONE);
                        tvStatus.setText(error);
                    }
                });

            }
        });
    }

    /**
     * Open activity
     */
    private void openActivity() {
        Intent miIntent = new Intent(StartEnrollmentActivity.this, RegisterActivity.class);
        StartEnrollmentActivity.this.startActivity(miIntent);
    }
}
