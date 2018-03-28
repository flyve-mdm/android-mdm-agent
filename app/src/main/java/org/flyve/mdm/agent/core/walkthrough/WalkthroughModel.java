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

package org.flyve.mdm.agent.core.walkthrough;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.MqttData;
import org.flyve.mdm.agent.data.WalkthroughData;
import org.flyve.mdm.agent.ui.FragmentSlideWalkthrough;
import org.flyve.mdm.agent.ui.MainActivity;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

import java.util.ArrayList;

public class WalkthroughModel implements Walkthrough.Model {

    private Walkthrough.Presenter Presenter;
    private ArrayList<WalkthroughData> data;

    public WalkthroughModel(Walkthrough.Presenter Presenter) {
        this.Presenter = Presenter;
    }

    public boolean checkIfLogged(Context context) {
        MqttData cache = new MqttData(context);

        // if broker is on cache open the main activity
        String broker = cache.getBroker();
        if(broker != null) {
            // if user is enrolled show landing screen
            FlyveLog.d(cache.getSessionToken());
            return true;
        } else {
            return false;
        }
    }

    public void goToMainWithDelay(final Activity activity, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Helpers.openActivity(activity, MainActivity.class, true);
            }
        }, delay);
    }

    public void setupSlides(Context context, FragmentManager fm, ViewPager viewPager) {
        data = new ArrayList<>();
        data.add(new WalkthroughData(R.drawable.wt_text_1, context.getResources().getString(R.string.walkthrough_step_link_1), R.drawable.ic_walkthroug_1));
        data.add(new WalkthroughData(R.drawable.wt_text_2, context.getResources().getString(R.string.walkthrough_step_link_1), R.drawable.ic_walkthroug_2));
        data.add(new WalkthroughData(R.drawable.wt_text_3, context.getResources().getString(R.string.walkthrough_step_link_1), R.drawable.ic_walkthroug_3));

        PagerAdapter mPagerAdapter = new SimpleSlidePagerAdapter(fm);
        viewPager.setAdapter(mPagerAdapter);
    }

    /**
     * A simple pager adapter
     */
    private class SimpleSlidePagerAdapter extends FragmentStatePagerAdapter {
        public SimpleSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            FragmentSlideWalkthrough f = new FragmentSlideWalkthrough();
            f.config(data.get(position), data.size(), position);
            return f;
        }

        @Override
        public int getCount() {
            return data.size();
        }
    }

}
