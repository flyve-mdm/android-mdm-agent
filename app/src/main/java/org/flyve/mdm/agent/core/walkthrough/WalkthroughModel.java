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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import org.flyve.mdm.agent.data.WalkthroughData;
import org.flyve.mdm.agent.ui.FragmentSlideWalkthrough;

import java.util.ArrayList;

public class WalkthroughModel implements Walkthrough.Model {

    private Walkthrough.Presenter Presenter;
    private ArrayList<WalkthroughData> data;

    public WalkthroughModel(Walkthrough.Presenter Presenter) {
        this.Presenter = Presenter;
    }

    @Override
    public void createSlides(ArrayList<WalkthroughData> data, FragmentManager fm) {
        this.data = data;

        PagerAdapter mPagerAdapter = new SimpleSlidePagerAdapter(fm);
        Presenter.addSlides(mPagerAdapter);
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
