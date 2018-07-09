package org.flyve.mdm.agent.core.deeplink;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Base64;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

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
 * @author    @rafaelje
 * @date      25/6/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { Base64.class })
public class DeeplinkModelTest {

    private Deeplink.Presenter presenter;
    private DeeplinkModel model;

    @Mock
    private Context mockContext;

    @Mock
    private Resources mockResources;

    @Before
    public void setUp() {
        presenter = mock(Deeplink.Presenter.class);
        model = new DeeplinkModel(presenter);

        when(mockContext.getResources())
                .thenReturn(mockResources);

        when(mockResources.getString(anyInt())).thenReturn("mocked string");
        when(mockResources.getStringArray(anyInt())).thenReturn(new String[]{"mocked string 1", "mocked string 2"});
        when(mockResources.getColor(anyInt())).thenReturn(Color.BLACK);
        when(mockResources.getBoolean(anyInt())).thenReturn(false);
        when(mockResources.getDimension(anyInt())).thenReturn(100f);
        when(mockResources.getIntArray(anyInt())).thenReturn(new int[]{1,2,3});

        mockStatic(Base64.class);
        when(Base64.encode(any(byte[].class), anyInt())).thenAnswer(invocation -> java.util.Base64.getEncoder().encode((byte[]) invocation.getArguments()[0]));
        when(Base64.decode(anyString(), anyInt())).thenAnswer(invocation -> java.util.Base64.getMimeDecoder().decode((String) invocation.getArguments()[0]));
    }


    @Test
    public void lint() {
        String hash = "aHR0cHM6Ly9kZXYuZmx5dmUub3JnL2dscGkvYXBpcmVzdC5waHBcOzQxUHRUbWVsVkNUM2oxMTExMWZhcGlVbmFxWk9HdFo0b0lGYWh2MjlcO2Q1ODdlMTExMTFlMzE1N2Y4NzZiMDBiM2ViODRhMjBjMzQ4NjVmNDRlMjUxYmVkZDAxMmE4NjM1NWRkMTJjNTFcO0dyZWF0IHN1cHBvcnRcOyszMyAxMjM0NTY3ODlcO2h0dHBzOi8vbXlncmVhdHN1cHBvcnQuY29tXDtncmVhdHN1cHBvcnRAZXhhbXBsZS5jb20=";
        model.lint(mockContext, hash);
        verify(presenter).lintSuccess(any(DeeplinkSchema.class));
    }

    @Test
    public void saveSupervisor() {
        String name = "Name";
        String phone = "Phone";
        String webSite = "http://flyve.org";
        String email = "info@flyve.org";

        model.saveSupervisor(mockContext, name, phone, webSite, email);
        Assert.assertTrue(true);
    }
}