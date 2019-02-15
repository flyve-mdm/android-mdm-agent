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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.flyve.mdm.agent.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentActivity extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    private int selectTab = 0;

    /**
     * Create the Fragment view
     * @param inflater the object that can be used to inflate any views in the fragment
     * @param container the parent View the fragment's UI should be attached to
     * @param savedInstanceState this fragment is being re-constructed from a previous saved state
     * @return View the View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activity, null);

        viewPager = v.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = v.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(selectTab);

        return v;
    }

    /**
     * Indicates the number of the Tab
     * @param extra
     */
    public void setup(String extra) {
        if(extra.equalsIgnoreCase("DeployApp")) {
            selectTab = 2;
        }
    }

    /**
     * Include fragments, FragmentLog, FragmentPolicies, FragmentApplications, FragmentFiles, FargmentTopics
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        // ----------------
        // LOG
        // ----------------
        adapter.addFragment(new FragmentLog(), getString(R.string.log));

        // ----------------
        // Policies
        // ----------------
        adapter.addFragment(new FragmentPolicies(), getString(R.string.policies));

        // ----------------
        // App
        // ----------------
        adapter.addFragment(new FragmentAppList(), getString(R.string.applications));

        // ----------------
        // File
        // ----------------
        adapter.addFragment(new FragmentFileList(), getString(R.string.files));

        // ----------------
        // Topics
        // ----------------
        adapter.addFragment(new FragmentTopics(), getString(R.string.topics));

        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
