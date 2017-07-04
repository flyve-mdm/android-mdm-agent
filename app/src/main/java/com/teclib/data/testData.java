package com.teclib.data;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
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
 * @author    Rafael Hernández
 * @date      4/7/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class testData {

    private DataStorage cache;
    private Context mContext;

    public testData(Context context) {
        mContext = context;
        cache = new DataStorage(context);
    }

    public boolean load() {
        Properties prop = new Properties();

        try {
            //load a properties file
            InputStream inputStream = mContext.getAssets().open("app.properties");
            prop.load(inputStream);

            cache.setProfileId(prop.getProperty("profileId"));
            cache.setSessionToken(prop.getProperty("sessionToken"));
            cache.setTls(prop.getProperty("tls"));
            cache.setName(prop.getProperty("name"));
            cache.setMqttpasswd(prop.getProperty("mqttPassword"));
            cache.setAgentId(prop.getProperty("agentId"));
            cache.setBroker(prop.getProperty("broker"));
            cache.setComputersId(prop.getProperty("computerId"));
            cache.setEntitiesId(prop.getProperty("entitiesId"));
            cache.setInvitationToken(prop.getProperty("invitationToken"));
            cache.setMqttuser(prop.getProperty("userMqtt"));
            cache.setPluginFlyvemdmFleetsId(prop.getProperty("fleetsId"));
            cache.setPort(prop.getProperty("port"));
            cache.setUserToken(prop.getProperty("userToken"));
            cache.setUrl(prop.getProperty("url"));
            cache.setTopic(prop.getProperty("topic"));

            return true;
        } catch (IOException ex) {
            return false;
        }

    }
}
