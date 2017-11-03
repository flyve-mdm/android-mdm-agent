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
    private DataStorage cache;

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
        String str = url + "/initSession?user_token=" + userToken;
        FlyveLog.d("initSession URL: ",  str);
        return str;
    }

    /**
     * getFullSession url
     * @return String with the url
     */
    public String getFullSession() {
        String str = url + "/getFullSession";
        FlyveLog.d("getFullSession: ",  str);
        return str;

    }

    /**
     * changeActiveProfile url
     * @param profileId String profile id to activate
     * @return String with the url
     */
    public String changeActiveProfile(String profileId) {
        String str = url + "/changeActiveProfile?profiles_id=" + profileId;
        FlyveLog.d("changeActiveProfile: ",  str);
        return str;
    }

    /**
     * PluginFlyvemdmAgent url
     * @return String with the url
     */
    public String pluginFlyvemdmAgent() {
        String str = url + "/PluginFlyvemdmAgent";
        FlyveLog.d("pluginFlyvemdmAgent: ",  str);
        return str;
    }

    /**
     * PluginFlyvemdmAgent url
     * @param agentId String Agent Id
     * @return String with the url
     */
    public String pluginFlyvemdmAgent(String agentId) {
        String str = url + "/PluginFlyvemdmAgent/" + agentId;
        FlyveLog.d("pluginFlyvemdmAgent: ",  str);
        return  str;
    }

    /**
     * Download files
     * @param fileId String file id
     * @return String url
     */
    public String pluginFlyvemdmFile(String fileId, String sessionToken) {
        String str = url + "/PluginFlyvemdmFile/" + fileId + "?session_token=" + sessionToken;
        FlyveLog.d("PluginFlyvemdmFile: ",  str);
        return str;
    }

    /**
     * Download apk
     * @param fileId String file id
     * @return String url
     */
    public String pluginFlyvemdmPackage(String fileId, String sessionToken) {
        String str = url + "/PluginFlyvemdmPackage/" + fileId + "?session_token=" + sessionToken;
        FlyveLog.d("PluginFlyvemdmPackage: ",  str);
        return str;
    }


}
