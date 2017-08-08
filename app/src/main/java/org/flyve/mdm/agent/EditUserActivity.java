package org.flyve.mdm.agent;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.InputValidatorHelper;
import org.flyve.mdm.agent.utils.MultipleEditText;

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
public class EditUserActivity extends AppCompatActivity {

    private TextView txtMessage;
    private EditText editName;
    private EditText editLastName;
    private DataStorage cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        cache = new DataStorage(EditUserActivity.this);

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
        editName.setText( cache.getUserFirstName() );

        editLastName = (EditText) findViewById(R.id.editLastName);
        editLastName.setText( cache.getUserLastName() );

//        editPhone.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        editPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    validateForm();
//                    return true;
//                }
//                return false;
//            }
//        });

        // Multiples Emails
        LinearLayout lnEmails = (LinearLayout) findViewById(R.id.lnEmails);
        MultipleEditText editEmail = new MultipleEditText(this, lnEmails, getResources().getString(R.string.email));
        editEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editEmail.setSpinnerArray(R.array.email_array);
        lnEmails.addView( editEmail.createEditText() );

        // 3 Phones
        LinearLayout lnPhones = (LinearLayout) findViewById(R.id.lnPhones);
        MultipleEditText editPhone = new MultipleEditText(this, lnPhones, getResources().getString(R.string.phone));
        editPhone.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        editPhone.setLimit(3);
        editPhone.setSpinnerArray(R.array.phone_array);
        lnPhones.addView( editPhone.createEditText() );

        // Language
        Spinner spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLanguage.setAdapter(adapter);

        // Button Register
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
        cache.setUserFirstName( editName.getText().toString() );
        cache.setUserLastName( editLastName.getText().toString() );

        Helpers.snack( EditUserActivity.this, "Saved" );
    }

    /**
     * Send information to validateForm
     */
    private void validateForm() {
        StringBuilder errMsg = new StringBuilder("Please fix the following errors and try again.\n\n");
        txtMessage.setText("");

        // Hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        //Validate and Save
        boolean allowSave = true;

        String name = editName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();

        // Name
        if (InputValidatorHelper.isNullOrEmpty(name)) {
            errMsg.append("- First name should not be empty.\n");
            allowSave = false;
        }

        // Last name
        if (InputValidatorHelper.isNullOrEmpty(lastName)) {
            errMsg.append("- Last name should not be empty.\n");
            allowSave = false;
        }

        if(allowSave){
            save();
        } else {
            txtMessage.setText(errMsg);
        }
    }
}
