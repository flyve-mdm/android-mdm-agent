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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.localstorage.UserData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class PreviewUserActivity extends AppCompatActivity {

    private static final int EDIT_USER = 101;

    /**
     * Called when the activity is starting
     * It shows the UI with the User information
     * @param savedInstanceState if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preview);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", onCreate", ex.getMessage());
            }

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        loadData();

        ImageView btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditUser();
            }
        });
    }

    /**
     * Called when the activity will start interacting with the user
     */
    @Override
    public void onResume() {
        super.onResume();

        loadData();
    }

    /**
     * Loads the information of the User
     */
    private void loadData() {

        try {
            UserData user = new UserData(PreviewUserActivity.this);

            if (user == null) {
                return;
            }

            ImageView imgPhoto = findViewById(R.id.imgPhoto);
            if (user.getPicture() != null && !user.getPicture().equals("")) {
                imgPhoto.setImageBitmap(Helpers.stringToBitmap(user.getPicture()));
            }

            LinearLayout lnEmail = findViewById(R.id.lnEmails);
            lnEmail.removeAllViews();

            for (int i = 0; i < user.getEmails().size(); i++) {
                TextView txtEmail = new TextView(PreviewUserActivity.this);
                txtEmail.setText(user.getEmails().get(i).getEmail());
                lnEmail.addView(txtEmail);
            }

            TextView txtFirstName = findViewById(R.id.txtFirstName);
            txtFirstName.setText(user.getFirstName() + " " + user.getLastName());

            TextView txtPhone = findViewById(R.id.txtPhone);
            if (user.getPhone() != null && !user.getPhone().equals("")) {
                txtPhone.setText(user.getPhone());
                txtPhone.setVisibility(View.VISIBLE);
            } else {
                txtPhone.setVisibility(View.GONE);
            }

            TextView txtPhoneMobile = findViewById(R.id.txtPhoneMobile);
            if (user.getMobilePhone() != null && !user.getMobilePhone().equals("")) {
                txtPhoneMobile.setText(user.getMobilePhone());
                txtPhoneMobile.setVisibility(View.VISIBLE);
            } else {
                txtPhoneMobile.setVisibility(View.GONE);
            }

            TextView txtPhone2 = findViewById(R.id.txtPhone2);
            if (user.getPhone2() != null && !user.getPhone2().equals("")) {
                txtPhone2.setText(user.getPhone2());
                txtPhone2.setVisibility(View.VISIBLE);
            } else {
                txtPhone2.setVisibility(View.GONE);
            }

            TextView txtLanguage = findViewById(R.id.txtLanguage);
            txtLanguage.setText(user.getLanguage());

            TextView txtAdministrativeNumber = findViewById(R.id.txtAdministrativeNumber);
            txtAdministrativeNumber.setText(user.getAdministrativeNumber());
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", loadData", ex.getMessage());
        }
    }

    /**
     * Open Edit User Activity
     */
    private void openEditUser() {
        Intent intent = new Intent(PreviewUserActivity.this, EditUserActivity.class);
        PreviewUserActivity.this.startActivityForResult(intent, EDIT_USER);
    }
}
