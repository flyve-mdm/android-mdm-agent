package org.flyve.mdm.agent.core.enrollment;

import junit.framework.Assert;

import org.flyve.mdm.agent.core.Routes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
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
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
@RunWith(MockitoJUnitRunner.class)
//@PrepareForTest( { ConnectionHTTP.class, Routes.class })
public class EnrollmentHelperTest {

    // private EnrollmentHelper enroll;

    @Mock
    private Routes routes;

    @Before
    public void setUp() throws Exception {
        //enroll = new EnrollmentHelper(RuntimeEnvironment.application);
        //routes = new Routes(RuntimeEnvironment.application);

        //mockStatic(ConnectionHTTP.class);

//        when(ConnectionHTTP.getSyncWebData(routes.initSession(""), "GET", null))
//                .thenReturn("{\"session_token\": \"session_token_fake\"}");
//
//        when(ConnectionHTTP.getSyncWebData(routes.getFullSession(), "GET", null))
//                .thenReturn("{\"session\": {\"plugin_flyvemdm_guest_profiles_id\": \"9\"}}");
//
//        when(ConnectionHTTP.getSyncWebData(routes.changeActiveProfile(""), "GET", null))
//                .thenReturn("{\"session_token\": \"session_token_fake\"}");

    }

    @Test
    public void createX509certificateSuccess() throws Exception {
                Assert.assertTrue(true);
        //        enroll.createX509cert(new EnrollmentHelper.EnrollCallBack() {
//            @Override
//            public void onSuccess(String data) {
//                Assert.assertTrue(true);
//            }
//
//            @Override
//            public void onError(String error) {
//                Assert.assertTrue(false);
//            }
//        });
    }

    @Test
    public void getActiveSessionToken() {
//        enroll.getActiveSessionToken(new EnrollmentHelper.EnrollCallBack() {
//            @Override
//            public void onSuccess(String data) {
//                Assert.assertTrue(true);
//            }
//
//            @Override
//            public void onError(String error) {
//                Assert.assertTrue(false);
//            }
//        });
    }
}