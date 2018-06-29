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

import org.flyve.mdm.agent.ui.FragmentSlideWalkthrough;

import java.util.ArrayList;
import java.util.List;

public class WalkthroughModel implements Walkthrough.Model {

    private Walkthrough.Presenter Presenter;
    public WalkthroughModel(Walkthrough.Presenter Presenter) {
        this.Presenter = Presenter;
    }

    @Override
    public void createSlides(ArrayList<WalkthroughSchema> data, FragmentManager fm) {
        PagerAdapter mPagerAdapter = new SimpleSlidePagerAdapter(fm, data);
        Presenter.addSlides(mPagerAdapter);
    }

    /**
     * A simple pager adapter
     */
    private class SimpleSlidePagerAdapter extends FragmentStatePagerAdapter {
        List<WalkthroughSchema> data;

        public SimpleSlidePagerAdapter(FragmentManager fm, List<WalkthroughSchema> data) {
            super(fm);
            this.data = data;
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
