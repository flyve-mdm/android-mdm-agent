package org.flyve.mdm.agent.core.deeplink;

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
 * @author    Rafael Hernandez
 * @date      9/7/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import org.flyve.mdm.agent.BuildConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class DeepLinkModelRoboTest {

    private Deeplink.Presenter presenter;
    private DeeplinkModel model;

    @Before
    public void setUp() {
        presenter = mock(Deeplink.Presenter.class);
        model = new DeeplinkModel(presenter);
    }

    @Test
    public void saveMQTTConfig() {
        String url = "http://flyve.org";
        String userToken = "41PtTmelVCT3jUb834fapiUnaq11111Z4oIFahv29";
        String invitationToken = "d587e80887e3157f876b00b3eb84a20c11111f44e251bedd012a86355dd12c51";

        model.saveMQTTConfig(RuntimeEnvironment.application, url, userToken, invitationToken);
        Assert.assertTrue(true);
    }
}
