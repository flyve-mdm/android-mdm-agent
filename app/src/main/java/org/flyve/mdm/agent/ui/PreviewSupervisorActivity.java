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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.localstorage.SupervisorData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class PreviewSupervisorActivity extends AppCompatActivity {

    /**
     * Called when the activity is starting
     * It shows the UI with the Supervisor information
     * @param savedInstanceState if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_preview);

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

        try {
            SupervisorData supervisor = new SupervisorData(PreviewSupervisorActivity.this);

            ImageView imgPhoto = findViewById(R.id.imgPhoto);
            if (supervisor.getPicture() != null && !supervisor.getPicture().equals("")) {
                imgPhoto.setImageBitmap(Helpers.stringToBitmap(supervisor.getPicture()));
            }

            TextView txtName = findViewById(R.id.txtName);
            if (supervisor.getName() != null && !supervisor.getName().equals("")) {
                txtName.setText(supervisor.getName());
            }

            TextView txtEmail = findViewById(R.id.txtEmail);
            if (supervisor.getEmail() != null && !supervisor.getEmail().equals("")) {
                txtEmail.setText(supervisor.getEmail());
            }

            TextView txtPhone = findViewById(R.id.txtPhone);
            if (supervisor.getPhone() != null && !supervisor.getPhone().equals("")) {
                txtPhone.setText(supervisor.getPhone());
            }

            TextView txtWebsite = findViewById(R.id.txtWebsite);
            if (supervisor.getWebsite() != null && !supervisor.getWebsite().equals("")) {
                txtWebsite.setText(supervisor.getWebsite());
            }
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", onCreate", ex.getMessage());
        }
    }
}
