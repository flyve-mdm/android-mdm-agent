package org.flyve.mdm.agent.core.permission;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.flyve.mdm.agent.ui.EnrollmentActivity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
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
 * @author    rafaelhernandez
 * @date      25/6/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

public class PermissionModelTest {

    private Context context;
    private PermissionModel model;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        Permission.Presenter presenter = mock(Permission.Presenter.class);
        model = new PermissionModel(presenter);
    }

    @Rule
    public ActivityTestRule<EnrollmentActivity> rule  = new ActivityTestRule<>(EnrollmentActivity.class);


    @Test
    public void showDialogShare() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                model.showDialogShare(rule.getActivity());
            }
        });

        Assert.assertTrue(true);
    }
}