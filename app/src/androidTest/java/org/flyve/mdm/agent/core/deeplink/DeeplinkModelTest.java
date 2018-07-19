package org.flyve.mdm.agent.core.deeplink;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.flyve.mdm.agent.ui.StartEnrollmentActivity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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

public class DeeplinkModelTest {

    private Context context;
    private DeeplinkModel model;

    @Rule
    public ActivityTestRule<StartEnrollmentActivity> rule  = new ActivityTestRule<>(StartEnrollmentActivity.class);

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        Deeplink.Presenter presenter = mock(Deeplink.Presenter.class);
        model = new DeeplinkModel(presenter);
    }


    @Test
    public void lint() {
        String hash = "aHR0cHM6Ly9kZXYuZmx5dmUub3JnL2dscGkvYXBpcmVzdC5waHBcOzQxUHRUbWVsVkNUM2oxMTExMWZhcGlVbmFxWk9HdFo0b0lGYWh2MjlcO2Q1ODdlMTExMTFlMzE1N2Y4NzZiMDBiM2ViODRhMjBjMzQ4NjVmNDRlMjUxYmVkZDAxMmE4NjM1NWRkMTJjNTFcO0dyZWF0IHN1cHBvcnRcOyszMyAxMjM0NTY3ODlcO2h0dHBzOi8vbXlncmVhdHN1cHBvcnQuY29tXDtncmVhdHN1cHBvcnRAZXhhbXBsZS5jb20=";
        model.lint(context, hash);
        Assert.assertTrue(true);
    }

    @Test
    public void saveSupervisor() {
        String name = "Name";
        String phone = "Phone";
        String webSite = "http://flyve.org";
        String email = "info@flyve.org";

        model.saveSupervisor(context, name, phone, webSite, email);
        Assert.assertTrue(true);
    }

    @Test
    public void saveMQTTConfig() {
        String url = "http://flyve.org";
        String userToken = "41PtTmelVCT3jUb834fapiUnaq11111Z4oIFahv29";
        String invitationToken = "d587e80887e3157f876b00b3eb84a20c11111f44e251bedd012a86355dd12c51";

        model.saveMQTTConfig(context, url, userToken, invitationToken);
        Assert.assertTrue(true);
    }

    @Test
    public void openEnrollment() {
        model.openEnrollment(rule.getActivity(), 1);
        Assert.assertTrue(true);
    }
}