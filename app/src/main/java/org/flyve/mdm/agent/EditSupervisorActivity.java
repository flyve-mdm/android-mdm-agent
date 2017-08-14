package org.flyve.mdm.agent;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.flyve.mdm.agent.core.supervisor.SupervisorController;
import org.flyve.mdm.agent.core.supervisor.SupervisorModel;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.InputValidatorHelper;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
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
 * @date      31/7/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class EditSupervisorActivity extends AppCompatActivity {

    private TextView txtMessage;
    private EditText editName;
    private EditText editEmail;
    private SupervisorModel supervisor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);

        supervisor = new SupervisorController(EditSupervisorActivity.this).getCache();

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

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText( "" );

        txtMessage = (TextView) findViewById(R.id.txtMessage);

        editName = (EditText) findViewById(R.id.editName);
        editName.setText( supervisor.getName() );

        editEmail = (EditText) findViewById(R.id.editEmail);
        editEmail.setEnabled(false);
        editEmail.setText( supervisor.getEmail() );
        editEmail.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validateForm();
                    return true;
                }
                return false;
            }
        });


        ImageView btnRegister = (ImageView) findViewById(R.id.btnSave);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });
    }

    /**
     * Storage information
     */
    private void save() {

        SupervisorModel model = new SupervisorModel();
        SupervisorController controller = new SupervisorController(EditSupervisorActivity.this);

        model.setName( editName.getText().toString() );
        model.setEmail( editEmail.getText().toString() );

        controller.save(model);

        Helpers.snack( EditSupervisorActivity.this, getResources().getString(R.string.saved) );
}

    /**
     * Send information to validateForm
     */
    private void validateForm() {
        StringBuilder errMsg = new StringBuilder(getResources().getString(R.string.validate_error));
        txtMessage.setText("");

        // Hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Validate and Save
        boolean allowSave = true;

        String email = editEmail.getText().toString().trim();
        String name = editName.getText().toString().trim();

        // Email
        if (InputValidatorHelper.isNullOrEmpty(email)) {
            errMsg.append(getResources().getString(R.string.validate_email));
            allowSave = false;
        }

        // Organization
        if (InputValidatorHelper.isNullOrEmpty(name)) {
            errMsg.append(getResources().getString(R.string.validate_organization));
            allowSave = false;
        }

        if(allowSave){
            save();
        } else {
            txtMessage.setText(errMsg);
        }
    }
}
