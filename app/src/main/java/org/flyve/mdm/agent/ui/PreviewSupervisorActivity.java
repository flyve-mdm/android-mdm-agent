package org.flyve.mdm.agent.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.supervisor.SupervisorController;
import org.flyve.mdm.agent.core.supervisor.SupervisorModel;
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
 * @date      31/7/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class PreviewSupervisorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_preview);

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

        SupervisorModel supervisor = new SupervisorController(PreviewSupervisorActivity.this).getCache();

        ImageView imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        if(supervisor.getPicture() != null && !supervisor.getPicture().equals("")) {
            imgPhoto.setImageBitmap(Helpers.StringToBitmap(supervisor.getPicture()));
        }

        TextView txtName = (TextView) findViewById(R.id.txtName);
        if(supervisor.getName() != null && !supervisor.getName().equals("")) {
            txtName.setText(supervisor.getName());
        }

        TextView txtEmail = (TextView) findViewById(R.id.txtEmail);
        if(supervisor.getEmail() != null && !supervisor.getEmail().equals("")) {
            txtEmail.setText(supervisor.getEmail());
        }

        TextView txtPhone = (TextView) findViewById(R.id.txtPhone);
        if(supervisor.getPhone() != null && !supervisor.getPhone().equals("")) {
            txtPhone.setText(supervisor.getPhone());
        }

        TextView txtWebsite = (TextView) findViewById(R.id.txtWebsite);
        if(supervisor.getWebsite() != null && !supervisor.getWebsite().equals("")) {
            txtWebsite.setText(supervisor.getWebsite());
        }

    }
}
