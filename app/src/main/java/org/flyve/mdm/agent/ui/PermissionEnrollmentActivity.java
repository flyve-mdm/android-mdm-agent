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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.core.permission.Permission;
import org.flyve.mdm.agent.core.permission.PermissionPresenter;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class PermissionEnrollmentActivity extends Activity implements Permission.View {
    private static final int REQUEST_EXIT = 1;
    private LinearLayout lnButtons;
    private Button btnPermission;
    private Permission.Presenter presenter;
    private String inventory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_enrollment);

        presenter = new PermissionPresenter(this);

        lnButtons = findViewById(R.id.lnButtons);

        btnPermission = findViewById(R.id.btnPermission);
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermission();
                } else {
                    presenter.generateInventory(PermissionEnrollmentActivity.this);
                }
            }
        });

        Button btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.showDialogShare(PermissionEnrollmentActivity.this);
            }
        });

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnrollmentActivity.inventory = PermissionEnrollmentActivity.this.inventory;
                Intent miIntent = new Intent(PermissionEnrollmentActivity.this, EnrollmentActivity.class);
                PermissionEnrollmentActivity.this.startActivityForResult(miIntent, REQUEST_EXIT);
            }
        });

        TextView txtVersion = findViewById(R.id.txtVersion);
        txtVersion.setText(MDMAgent.getCompleteVersion());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EXIT && resultCode == RESULT_OK) {
            this.finish();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(PermissionEnrollmentActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                1);
    }

    @Override
    public void showSnackError(int type, String message) {
        Helpers.snack(this, getResources().getString(R.string.error_message_with_number, String.valueOf(type), message), this.getResources().getString(R.string.snackbar_close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public void inventorySuccess(String inventory) {
        lnButtons.setVisibility(View.VISIBLE);
        btnPermission.setVisibility(View.GONE);
        this.inventory = inventory;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED
                ) {
                    presenter.generateInventory(PermissionEnrollmentActivity.this);
                } else {
                    presenter.showSnackError(CommonErrorType.PERMISSION_ONREQUESTPERMISSIONSRESULT, getString(R.string.permission_error_result));
                }
            }
        }
    }


}
