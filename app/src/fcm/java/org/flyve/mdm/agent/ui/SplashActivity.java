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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.walkthrough.Walkthrough;
import org.flyve.mdm.agent.core.walkthrough.WalkthroughPresenter;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.core.walkthrough.WalkthroughSchema;
import org.flyve.mdm.agent.service.RunOnStartup;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

import java.util.ArrayList;

/**
 * This is the first screen of the app here you can get information about flyve-mdm-agent
 * if you are not register
 */
public class SplashActivity extends FragmentActivity implements Walkthrough.View {

    private static final int TIME = 3000;
    private static final int REQUEST_CODE_SCAN = 100;

    private IntentFilter mIntent;
    private Walkthrough.Presenter presenter;

    @Override
    public void onPause() {
        // unregister the broadcast
        if(mIntent != null) {
            unregisterReceiver(broadcastMessage);
            mIntent = null;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        // register the broadcast
        super.onResume();
        LocalBroadcastManager.getInstance(SplashActivity.this).registerReceiver(broadcastMessage, new IntentFilter("flyve.ACTION_CLOSE"));
    }

    /**
     * Called when the activity is starting
     * It shows the UI with the Splash screen
     * @param savedInstanceState if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //if create from RunOnStartup
        //move to background
        if(getIntent().getBooleanExtra(RunOnStartup.START_AFTER_BOOTING, false)) {
            FlyveLog.d("Start from RunOnStartUp move app to background");
            moveTaskToBack(true);
        }

        String topic = getIntent().getStringExtra("topic");
        if(topic!=null) {
            String message = getIntent().getStringExtra("message");

            Intent intent = new Intent(this, PushPoliciesActivity.class);
            intent.putExtra("topic", topic);
            intent.putExtra("message", message);
            SplashActivity.this.startActivity(intent);
        }

        MqttData cache = new MqttData( SplashActivity.this );

        // if broker is on cache open the main activity
        String agent = cache.getAgentId();
        if(!agent.isEmpty()) {

            // if user is enrolled show landing screen
            FlyveLog.d(cache.getSessionToken());

            setContentView(R.layout.activity_splash_enrolled);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openMain();
                }
            }, TIME);

            return;
       }

        // if user is not enrolled show help
        setContentView(R.layout.activity_splash);
        setupUI();

        FloatingActionButton btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SplashActivity.this.startActivityForResult(new Intent(SplashActivity.this, ScanActivity.class), REQUEST_CODE_SCAN);
            }
        });

        TextView txtVersion = findViewById(R.id.txtVersion);
        txtVersion.setText(MDMAgent.getCompleteVersion());
    }

    private void setupUI() {
        presenter = new WalkthroughPresenter(this);

        ArrayList<WalkthroughSchema> walkthrough = new ArrayList<>();
        walkthrough.add(new WalkthroughSchema(R.drawable.wt_text_1, getResources().getString(R.string.walkthrough_step_link_1), R.drawable.ic_walkthroug_1));
        walkthrough.add(new WalkthroughSchema(R.drawable.wt_text_2, getResources().getString(R.string.walkthrough_step_link_1), R.drawable.ic_walkthroug_2));
        walkthrough.add(new WalkthroughSchema(R.drawable.wt_text_3, getResources().getString(R.string.walkthrough_step_link_1), R.drawable.ic_walkthroug_3));

        presenter.createSlides(walkthrough, getSupportFragmentManager());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setupUI();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_OK) {
            String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);

            Intent miIntent = new Intent(SplashActivity.this, StartEnrollmentActivity.class);
            miIntent.putExtra("data", input);
            SplashActivity.this.startActivity(miIntent);
        } else {
            Helpers.snack(SplashActivity.this, getResources().getString(R.string.splash_error_scan));
        }
    }

    @Override
    public void addSlides(PagerAdapter mPagerAdapter) {
        ViewPager mPager = findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
    }

    /**
     * Open the main activity
     */
    private void openMain() {
        Intent miIntent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(miIntent);
        SplashActivity.this.finish();
    }

    private BroadcastReceiver broadcastMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Close this activity
            if("flyve.ACTION_CLOSE".equalsIgnoreCase(action)) {
                SplashActivity.this.finish();
            }
        }
    };


}
