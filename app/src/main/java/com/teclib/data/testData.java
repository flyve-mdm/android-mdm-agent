package com.teclib.data;

import android.content.Context;

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

    public testData(Context context) {
        cache = new DataStorage(context);
    }

    public void load() {
        String broker = "mqdev.flyve.org";
        String agentId = "9";
        String computerId = "13";
        String entitiesId = "2";
        String invitationToken = "9cf949fff1aeb857badde36c1a08b3a0bbd00db31b886c97f618fec6911ab5a4";
        String url = "https://dev.flyve.org/glpi/apirest.php";
        String mqttPassword = "h0WWfquQkGlXcC5wT7kbc9c8NvWOsJaJ";
        String userMqtt = "ABCDEFGHIJ1234";
        String name = "dyceca@hi2.in";
        String fleetsId = "3";
        String port = "8883";
        String profileId = "9";
        String sessionToken = "329g0b8aabatp185nvpu68mrm4";
        String tls = "1";
        String topic = "/2/agent/ABCDEFGHIJ1234";
        String userToken = "tvspbbvfe3mmwj18shu619q01u9nqw5omo5j0phn";

        cache.setProfileId(profileId);
        cache.setSessionToken(sessionToken);
        cache.setTls(tls);
        cache.setName(name);
        cache.setMqttpasswd(mqttPassword);
        cache.setAgentId(agentId);
        cache.setBroker(broker);
        cache.setComputersId(computerId);
        cache.setEntitiesId(entitiesId);
        cache.setInvitationToken(invitationToken);
        cache.setMqttuser(userMqtt);
        cache.setPluginFlyvemdmFleetsId(fleetsId);
        cache.setPort(port);
        cache.setUserToken(userToken);
        cache.setUrl(url);
        cache.setTopic(topic);
    }
}
