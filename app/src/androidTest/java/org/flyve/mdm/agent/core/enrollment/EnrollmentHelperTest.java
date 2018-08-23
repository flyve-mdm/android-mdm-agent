package org.flyve.mdm.agent.core.enrollment;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
 * @date      7/1/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
@RunWith(AndroidJUnit4.class)
public class EnrollmentHelperTest {

    private Context context;
    private EnrollmentHelper enroll;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        enroll = new EnrollmentHelper(context);
    }

    @Test
    public void createX509certificateSuccess() throws Exception {
        enroll.createX509cert(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String data) {
                Assert.assertTrue(true);
            }

            @Override
            public void onError(int type, String error) {
                Assert.assertTrue(false);
            }
        });
    }
}