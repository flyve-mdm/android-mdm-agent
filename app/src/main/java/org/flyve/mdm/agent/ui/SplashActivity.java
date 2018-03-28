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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.walkthrough.Walkthrough;
import org.flyve.mdm.agent.core.walkthrough.WalkthroughPresenter;

/**
 * This is the first screen of the app here you can get information about flyve-mdm-agent
 * if you are not register
 */
public class SplashActivity extends FragmentActivity implements Walkthrough.View {

    private static final int SPLASH_DELAY = 3000;

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
        presenter = new WalkthroughPresenter(this);

        if(presenter.checkIfLogged(SplashActivity.this)) {
            setContentView(R.layout.activity_splash_enrolled);
            presenter.goToMainWithDelay(this, SPLASH_DELAY);
            return;
        }

        // if user is not enrolled show help
        setContentView(R.layout.activity_splash);
        ViewPager viewPager = findViewById(R.id.viewpager);

        presenter.setupSlides(this, getSupportFragmentManager(), viewPager);
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
