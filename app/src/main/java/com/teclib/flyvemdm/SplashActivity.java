/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
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

package com.teclib.flyvemdm;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.teclib.data.DataStorage;
import com.teclib.data.testData;
import com.teclib.security.FlyveAdminReceiver;

/**
 * This is the first screen of the app here you can get information about flyve-mdm-agent
 * if you are not register
 */
public class SplashActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    ComponentName mDeviceAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        DataStorage cache = new DataStorage( SplashActivity.this );
        mDeviceAdmin = new ComponentName(this, FlyveAdminReceiver.class);

        // if broker is on cache open the main activity
        String broker = cache.getBroker();
        if(broker != null) {
            openMain();
        }

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            testData data = new testData(SplashActivity.this);
            data.load();

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "EXPLANATION");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN && resultCode == Activity.RESULT_OK) {
            openMain();
        }
    }//onActivityResult

    /**
     * Open the main activity
     */
    private void openMain() {
        Intent miIntent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(miIntent);
        SplashActivity.this.finish();
    }
}
