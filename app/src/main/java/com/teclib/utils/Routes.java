/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package com.teclib.utils;

import android.content.Context;

import com.teclib.data.DataStorage;

/**
 * Content all the routes of the app
 */
public class Routes {

    private String url;

    /**
     * Constructor
     * @param context
     */
    public Routes(Context context) {
        DataStorage cache = new DataStorage(context);
        url = cache.getUrl();
    }

<<<<<<< HEAD
    /**
     * initSession url
     * @param userToken String user token
     * @return String with the url
     */
=======
>>>>>>> 4959d8279a378008c355841710a6040797b112a2
    public String initSession(String userToken) {
        return url + "/initSession?user_token=" + userToken;
    }

    /**
     * getFullSession url
     * @return String with the url
     */
    public String getFullSession() {
        return url + "/getFullSession";
    }

<<<<<<< HEAD
    /**
     * changeActiveProfile url
     * @param profileId String profile id to activate
     * @return String with the url
     */
=======
>>>>>>> 4959d8279a378008c355841710a6040797b112a2
    public String changeActiveProfile(String profileId) {
        return url + "/changeActiveProfile?profile_id=" + profileId;
    }

<<<<<<< HEAD
    /**
     * PluginFlyvemdmAgent url
     * @return String with the url
     */
    public String pluginFlyvemdmAgent() {
        return url + "/PluginFlyvemdmAgent";
    }

    /**
     * PluginFlyvemdmAgent url
     * @param agentId String Agent Id
     * @return String with the url
     */
    public String pluginFlyvemdmAgent(String agentId) {
        return url + "/PluginFlyvemdmAgent/" + agentId;
=======
    public String pluginFlyvemdmAgent() {
        return url + "/pluginFlyvemdmAgent";
    }

    public String pluginFlyvemdmAgent(String agentId) {
        return url + "/pluginFlyvemdmAgent/" + agentId;
>>>>>>> 4959d8279a378008c355841710a6040797b112a2
    }
}
