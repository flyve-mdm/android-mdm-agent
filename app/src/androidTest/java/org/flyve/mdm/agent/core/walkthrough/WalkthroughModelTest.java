package org.flyve.mdm.agent.core.walkthrough;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.ui.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;

/*
 *   Copyright  2018 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @author    @rafaelje
 * @date      25/6/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

public class WalkthroughModelTest {

    private Context context;
    private WalkthroughModel model;

    @Rule
    public ActivityTestRule<MainActivity> rule  = new ActivityTestRule<>(MainActivity.class);


    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        Walkthrough.Presenter presenter = mock(Walkthrough.Presenter.class);
        model = new WalkthroughModel(presenter);
    }

    @Test
    public void createSlides() {
        ArrayList<WalkthroughSchema> walkthrough = new ArrayList<>();
        walkthrough.add(new WalkthroughSchema(R.drawable.wt_text_1, context.getResources().getString(R.string.walkthrough_step_link_1), R.drawable.ic_walkthroug_1));
        model.createSlides(walkthrough, rule.getActivity().getSupportFragmentManager());
    }
}