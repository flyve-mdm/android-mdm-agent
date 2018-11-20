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

package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.util.Properties;

public class EnvInfoSetup {
    private Properties properties = new Properties();

    private Boolean isLoaded = false;

    public EnvInfoSetup(Context context) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open("setup.properties");
            properties.load(inputStream);
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", EnvInfoSetup", ex.getMessage());
            isLoaded = false;
        }
        isLoaded = true;
    }

    public Boolean getIsLoaded() {
        return isLoaded;
    }

    public String getAdminWebConsole() {
        return properties.getProperty("setup.admin_web_console");
    }

    public String getThestralbot() {
        return properties.getProperty("setup.thestralbot");
    }

}
