/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * this file is part of flyve-mdm-android-agent
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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flyvemdm.inventory.categories.Hardware;

import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.AndroidCryptoProvider;
import org.flyve.mdm.agent.utils.EnrollmentHelper;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.InputValidatorHelper;
import org.json.JSONObject;

import java.net.URLEncoder;


/**
 * Register the agent to the platform
 */
public class EnrollmentActivity extends AppCompatActivity {

    private ProgressBar pb;
    private ProgressBar pbx509;
    private DataStorage cache;
    private TextView txtMessage;
    private EditText editName;
    private EditText editLastName;
    private EditText editEmail;
    private EditText editPhone;
    private ImageView btnRegister;

    private static Handler uiHandler;

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    private static void runOnUI(Runnable runnable) {
        uiHandler.post(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        pb = (ProgressBar) findViewById(R.id.progressBar);
        pbx509 = (ProgressBar) findViewById(R.id.progressBarX509);

        cache = new DataStorage( EnrollmentActivity.this );

        txtMessage = (TextView) findViewById(R.id.txtMessage);

        editName = (EditText) findViewById(R.id.editName);
        editLastName = (EditText) findViewById(R.id.editLastName);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPhone = (EditText) findViewById(R.id.editPhone);
        editPhone.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    enroll();
                    return true;
                }
                return false;
            }
        });

        btnRegister = (ImageView) findViewById(R.id.btnSave);
        btnRegister.setEnabled(false);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enroll();
            }
        });

        // start creating a certificated
        createX509cert();
    }

    /**
     * Send information to enroll
     */
    private void enroll() {
        StringBuilder errMsg = new StringBuilder("Please fix the following errors and try again.\n\n");
        txtMessage.setText("");

        //Validate and Save
        boolean allowSave = true;

        // block fields on form
        enableFields(false);

        String email = editEmail.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();

        // Email
        if (InputValidatorHelper.isNullOrEmpty(name)) {
            errMsg.append("- First name should not be empty.\n");
            allowSave = false;
        }

        // First name
        if (InputValidatorHelper.isNullOrEmpty(lastName)) {
            errMsg.append("- Last name should not be empty.\n");
            allowSave = false;
        }

        // Last name
        if (email.equals("") || !InputValidatorHelper.isValidEmail(email)) {
            errMsg.append("- Invalid email address.\n");
            allowSave = false;
        }

        if(allowSave){
            sendEnroll();
        } else {
            txtMessage.setText(errMsg);
        }
    }

    private void enableFields(Boolean enable) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.userData);
        for (View view : ll.getTouchables()){
            if (view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setEnabled(enable);
                editText.setFocusable(enable);
                editText.setFocusableInTouchMode(enable);
            }
        }
    }

    /**
     * Create X509 certificate
     */
    private void createX509cert() {
        pbx509.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                try {
                    AndroidCryptoProvider createCertif = new AndroidCryptoProvider(getBaseContext());
                    createCertif.generateRequest(new AndroidCryptoProvider.generateCallback() {
                        @Override
                        public void onGenerate(final boolean work) {
                            EnrollmentActivity.runOnUI(new Runnable() {
                                public void run() {
                                pbx509.setVisibility(View.GONE);
                                if(work) {
                                    btnRegister.setEnabled(true);
                                }
                                }
                            });
                        }
                    });
                } catch (Exception ex) {
                    pbx509.setVisibility(View.GONE);
                    txtMessage.setText("Error creating certificate X509");
                    FlyveLog.e(ex.getMessage());
                }
            }
        }).start();
    }

    private void sendEnroll() {
        try {
            pb.setVisibility(View.VISIBLE);

            AndroidCryptoProvider csr = new AndroidCryptoProvider(EnrollmentActivity.this.getBaseContext());
            String requestCSR = "";
            if( csr.getlCsr() != null ) {
                requestCSR = URLEncoder.encode(csr.getlCsr(), "UTF-8");
            }

            JSONObject payload = new JSONObject();

            payload.put("_email", editEmail.getText().toString());
            payload.put("_invitation_token", cache.getInvitationToken());
            payload.put("_serial", Helpers.getDeviceSerial());
            payload.put("_uuid", new Hardware(EnrollmentActivity.this).getUUID());
            payload.put("csr", requestCSR);
            payload.put("firstname", editName.getText().toString());
            payload.put("lastname", editLastName.getText().toString());
            payload.put("phone", editPhone.getText().toString());
            payload.put("version", BuildConfig.VERSION_NAME);

            EnrollmentHelper enroll = new EnrollmentHelper(EnrollmentActivity.this);
            enroll.enrollment(payload, new EnrollmentHelper.enrollCallback() {
                @Override
                public void onSuccess(String data) {
                    pb.setVisibility(View.GONE);

                    // Store user information
                    cache.setUserFirstName(editName.getText().toString());
                    cache.setUserLastName(editLastName.getText().toString());
                    cache.setUserEmail(editEmail.getText().toString());
                    cache.setUserPhone(editPhone.getText().toString());

                    openMain();
                }

                @Override
                public void onError(String error) {
                    pb.setVisibility(View.GONE);
                    enableFields(true);
                    txtMessage.setText(error);
                }
            });
        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            enableFields(true);
            txtMessage.setText( ex.getMessage() );
            FlyveLog.e( ex.getMessage() );
        }
    }

    /**
     * Open the main activity
     */
    private void openMain() {
        Intent miIntent = new Intent(EnrollmentActivity.this, MainActivity.class);
        EnrollmentActivity.this.startActivity(miIntent);
        EnrollmentActivity.this.finish();
    }
}
