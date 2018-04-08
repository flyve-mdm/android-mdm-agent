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

package org.flyve.mdm.agent.data;

import android.content.Context;

import org.flyve.mdm.agent.room.database.AppDataBase;
import org.flyve.mdm.agent.room.entity.MQTT;

public class MqttData {

    private AppDataBase dataBase;
    private MQTT mqtt;
    /**
     * Constructor
     *
     * @param context
     */
    public MqttData(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
        if(!dataBase.MQTTDao().loadAll().isEmpty()) {
            mqtt = dataBase.MQTTDao().loadAll().get(0);
        }
    }

    public boolean isEmpty() {
        return (mqtt==null);
    }

    private String getValue(String value) {
        if(value!=null) {
            return value;
        } else {
            return "";
        }
    }

    public void deleteAll() {
        dataBase.MQTTDao().deleteAll();
    }

    /**
     * Get the version of the Manifest
     * @return string the Manifest version
     */
    public String getManifestVersion() {
        return getValue(mqtt.manifestVersion);
    }

    /**
     * Get the URL
     * @return string the URL
     */
    public String getUrl() {
        return getValue(mqtt.url);
    }

    /**
     * Get the token of the user
     * @return string the user token
     */
    public String getUserToken() {
        return getValue(mqtt.userToken);
    }

    /**
     * Get the invitation token
     * @return string the invitation token
     */
    public String getInvitationToken() {
        return getValue(mqtt.invitationToken);
    }

    /**
     * Get the session token
     * @return string the session token
     */
    public String getSessionToken() {
        return getValue(mqtt.sessionToken);
    }

    /**
     * Get the ID of the profile
     * @return string the profile ID
     */
    public String getProfileId() {
        return getValue(mqtt.profileId);
    }

    /**
     * Get the ID of the agent
     * @return string the agent ID
     */
    public String getAgentId() {
        return getValue(mqtt.agentId);
    }

    /**
     * Get the broker
     * @return string the broker
     */
    public String getBroker() {
        return getValue(mqtt.broker);
    }

    /**
     * Get the port
     * @return string the port
     */
    public String getPort() {
        return getValue(mqtt.port);
    }

    /**
     * Get the Transport Layer Security (TLS)
     * @return string the TLS
     */
    public String getTls() {
        return getValue(mqtt.tls);
    }

    /**
     * Get the topic
     * @return string the topic
     */
    public String getTopic() {
        return getValue(mqtt.topic);
    }

    /**
     * Get the user of the Message Queue Telemetry Transport (MQTT)
     * @return string the MQTT user
     */
    public String getMqttuser() {
        return getValue(mqtt.mqttuser);
    }

    /**
     * Get the password of the Message Queue Telemetry Transport (MQTT)
     * @return the MQTT password
     */
    public String getMqttpasswd() {
        return getValue(mqtt.mqttpasswd);
    }

    /**
     * Get the Certificate
     * @return string the certificate
     */
    public String getCertificate() {
        return getValue(mqtt.certificate);
    }

    /**
     * Get the name
     * @return string the name
     */
    public String getName() {
        return getValue(mqtt.name);
    }

    /**
     * Get the ID of the computer
     * @return string the computer ID
     */
    public String getComputersId() {
        return getValue(mqtt.computersId);
    }

    /**
     * Get the ID of the entities
     * @return string the entities ID
     */
    public String getEntitiesId() {
        return getValue(mqtt.entitiesId);
    }

    /**
     * Get the ID of the Fleets of the Flyve MDM plugin
     * @return string the Fleets ID
     */
    public String getPluginFlyvemdmFleetsId() {
        return getValue(mqtt.pluginFlyvemdmFleetsId);
    }
}
