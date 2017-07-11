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

package org.flyve.mdm.agent.utils;

import android.content.Context;

import org.flyve.mdm.agent.data.DataStorage;

/**
 * Content all the routes of the app
 */
public class Routes {

    private String url;
    DataStorage cache;

    /**
     * Constructor
     * @param context
     */
    public Routes(Context context) {
        cache = new DataStorage(context);
        url = cache.getUrl();
    }

    /**
     * initSession url
     * @param userToken String user token
     * @return String with the url
     */
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

    /**
     * changeActiveProfile url
     * @param profileId String profile id to activate
     * @return String with the url
     */
    public String changeActiveProfile(String profileId) {
        return url + "/changeActiveProfile?profile_id=" + profileId;
    }

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
    }

    /**
     * Download files
     * @param fileId String file id
     * @return String url
     */
    public String PluginFlyvemdmFile(String fileId, String sessionToken) {
        return url + "/PluginFlyvemdmFile/" + fileId + "?session_token=" + sessionToken;
    }

    /**
     * Download apk
     * @param fileId String file id
     * @return String url
     */
    public String PluginFlyvemdmPackage(String fileId, String sessionToken) {
        return url + "/PluginFlyvemdmPackage/" + fileId + "?session_token=" + sessionToken;
    }


}
