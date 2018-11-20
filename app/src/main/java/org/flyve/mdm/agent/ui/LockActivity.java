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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.localstorage.SupervisorData;
import org.flyve.mdm.agent.utils.FlyveLog;

public class LockActivity extends AppCompatActivity {

    private TextView txtNameSupervisor;
    private TextView txtDescriptionSupervisor;
    private ViewGroup mTopView;
    private WindowManager wm;

    private BroadcastReceiver broadcastLock = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("org.flyvemdm.finishlock")) {
                if (mTopView != null) wm.removeView(mTopView);
                Intent miIntent = new Intent(LockActivity.this, MainActivity.class);
                LockActivity.this.startActivity(miIntent);
                LockActivity.this.finish();
            }
        }
    };

    @Override
    protected void onPause() {
        //unregister our receiver
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(broadcastLock, new IntentFilter("org.flyvemdm.finishlock"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.registerReceiver(broadcastLock, new IntentFilter("org.flyvemdm.finishlock"));

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);

        wm = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        mTopView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_lock, null);
        getWindow().setAttributes(params);
        wm.addView(mTopView, params);

        txtNameSupervisor = mTopView.findViewById(R.id.txtNameSupervisor);
        txtDescriptionSupervisor = mTopView.findViewById(R.id.txtDescriptionSupervisor);

        loadSupervisor();

        Button btnUnlock = mTopView.findViewById(R.id.btnUnlock);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockActivity.this.finish();
            }
        });
    }

    /**
     * Load Supervisor information
     */
    private void loadSupervisor() {

        try {
            SupervisorData supervisor = new SupervisorData(LockActivity.this);

            if (supervisor.getName() != null && !supervisor.getName().equals("")) {
                txtNameSupervisor.setText(supervisor.getName());
            }
            if (supervisor.getEmail() != null && !supervisor.getEmail().equals("")) {
                txtDescriptionSupervisor.setText(supervisor.getEmail());
            }
        } catch(Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", loadSupervisor", ex.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }

    public void unlockScreen(View view) {
        if (mTopView != null) wm.removeView(mTopView);
        Intent miIntent = new Intent(LockActivity.this, MainActivity.class);
        LockActivity.this.startActivity(miIntent);
        LockActivity.this.finish();
    }

    // Handle button clicks
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)
                || (keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }

        return false;
    }

    // handle the key press events here itself
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

            return true;
        }
        return false;
    }
}