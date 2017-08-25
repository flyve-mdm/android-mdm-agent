package org.flyve.mdm.agent.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.user.UserController;
import org.flyve.mdm.agent.core.user.UserModel;
import org.flyve.mdm.agent.utils.Helpers;

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
 * @date      16/8/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

public class PreviewUserActivity extends AppCompatActivity {

    private static final int EDIT_USER = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preview);

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

        loadData();

        ImageView btnEdit = (ImageView) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditUser();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData();
    }

    private void loadData() {
        UserModel user = new UserController(PreviewUserActivity.this).getCache();

        if(user==null) {
            return;
        }

        ImageView imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        if(user.getPicture() != null && !user.getPicture().equals("")) {
            imgPhoto.setImageBitmap(Helpers.StringToBitmap(user.getPicture()));
        }

        LinearLayout lnEmail = (LinearLayout) findViewById(R.id.lnEmails);
        lnEmail.removeAllViews();

        for(int i = 0; i < user.getEmails().size(); i++) {
            TextView txtEmail = new TextView( PreviewUserActivity.this );
            txtEmail.setText( user.getEmails().get(i).getEmail() );
            lnEmail.addView(txtEmail);
        }

        TextView txtFirstName = (TextView) findViewById(R.id.txtFirstName);
        txtFirstName.setText( user.getFirstName() + " " + user.getLastName() );

        TextView txtPhone = (TextView) findViewById(R.id.txtPhone);
        if(!user.getPhone().equals("")) {
            txtPhone.setText(user.getPhone());
            txtPhone.setVisibility(View.VISIBLE);
        } else {
            txtPhone.setVisibility(View.GONE);
        }

        TextView txtPhoneMobile = (TextView) findViewById(R.id.txtPhoneMobile);
        if(!user.getMobilePhone().equals("")) {
            txtPhoneMobile.setText(user.getMobilePhone());
            txtPhoneMobile.setVisibility(View.VISIBLE);
        } else {
            txtPhoneMobile.setVisibility(View.GONE);
        }

        TextView txtPhone2 = (TextView) findViewById(R.id.txtPhone2);
        if(!user.getMobilePhone().equals("")) {
            txtPhone2.setText(user.getPhone2());
            txtPhone2.setVisibility(View.VISIBLE);
        } else {
            txtPhone2.setVisibility(View.GONE);
        }

        TextView txtLanguage = (TextView) findViewById(R.id.txtLanguage);
        txtLanguage.setText( user.getLanguage() );

        TextView txtAdministrativeNumber = (TextView) findViewById(R.id.txtAdministrativeNumber);
        txtAdministrativeNumber.setText( user.getAdministrativeNumber() );
    }

    /**
     * Open Edit User Activity
     */
    private void openEditUser() {
        Intent intent = new Intent(PreviewUserActivity.this, EditUserActivity.class);
        PreviewUserActivity.this.startActivityForResult(intent, EDIT_USER);
    }
}
