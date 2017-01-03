/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android.
 *
 * Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.teclib.api.FlyveLog;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity {
    private String link;
    private String sendLink;

    Button _signupButton;
    EditText _nameText,_emailText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            link = extras.getString("link");
        }
        else{
            Uri data = getIntent().getData();
            link=data.toString();
            getIntent().setData(null);
        }
        FlyveLog.d(link);

        _signupButton = (Button)findViewById(R.id.button);
        _nameText = (EditText)findViewById(R.id.editText);
        _emailText = (EditText)findViewById(R.id.editText2);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Enrolement");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
       // String password = _passwordText.getText().toString();

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(link);
            jsonObj.put("serial",Build.SERIAL);
            jsonObj.put("name",name);
            jsonObj.put("email",email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendLink = jsonObj.toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess(sendLink);
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 30);
    }

    public void onSignupSuccess(String url) {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent LoginIntent = new Intent(this.getBaseContext(), HTTPActivity.class);
        LoginIntent.putExtra("link", url);
        LoginIntent.putExtra("previous","LoginActivity");
        startActivity(LoginIntent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
      //  String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError(getString(R.string.login_name_length));
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.login_email_check));
            valid = false;
        } else {
            _emailText.setError(null);
        }
/*
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("entre 4 et 10 caractères alphanumériques");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
*/
        return valid;
    }


}