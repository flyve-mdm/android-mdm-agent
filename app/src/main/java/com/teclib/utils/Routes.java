/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   com.teclib.data is part of flyve-mdm-android
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
 * @date      02/06/2017
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package com.teclib.utils;

import android.content.Context;

import com.teclib.data.DataStorage;

/**
 * Created by rafaelhernandez on 6/5/17.
 */

public class Routes {

    String url;

    public Routes(Context context) {
        DataStorage cache = new DataStorage(context);
        url = cache.getVariablePermanente("url");
    }

    public String initSession(String user_token) {
        return url + "/initSession?user_token=" + user_token;
    }

    public String getFullSession() {
        return url + "/getFullSession";
    }

    public String changeActiveProfile(String profile_id) {
        return url + "/changeActiveProfile?profile_id=" + profile_id;
    }

    public String PluginFlyvemdmAgent() {
        return url + "/PluginFlyvemdmAgent";
    }

    public String PluginFlyvemdmAgent(String agent_id) {
        return url + "/PluginFlyvemdmAgent/" + agent_id;
    }


}
