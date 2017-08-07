package org.flyve.mdm.agent;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.InputValidatorHelper;

import java.util.ArrayList;
import java.util.List;

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
    private EditText editPhone;
    private List<EditText> editEmailList = new ArrayList<>();
    private LinearLayout lnEmails;
    private DataStorage cache;
    private int emailIndex = 0;

    private EditText editEmail(int id) {
        EditText editText = new EditText(this);
        editText.setId(id);
        editText.setHint(getResources().getString(R.string.email));
        editText.setTag("");
        editText.setBackgroundColor(Color.WHITE);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(!"used".equalsIgnoreCase(v.getTag().toString())) {
                    v.setTag("used");
                    lnEmails.addView(editEmail(++emailIndex));
                }
                return false;
            }
        });

        Drawable img = getResources().getDrawable( R.drawable.ic_mail );
        img.setBounds( 0, 0, 60, 60 );
        editText.setCompoundDrawables( img, null, null, null );

        editEmailList.add(editText);
        return editText;
    }

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

        editPhone = (EditText) findViewById(R.id.editPhone);
        editPhone.setText( cache.getUserPhone() );

        editPhone.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validateForm();
                    return true;
                }
                return false;
            }
        });

        lnEmails = (LinearLayout) findViewById(R.id.lnEmails);
        lnEmails.addView( editEmail(emailIndex) );

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
        cache.setUserPhone( editPhone.getText().toString() );

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
