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

public class MqttData extends LocalStorage {

    /**
     * Constructor
     *
     * @param context
     */
    public MqttData(Context context) {
        super(context);
    }

    /**
     * Get the version of the Manifest
     * @return string the Manifest version
     */
    public String getManifestVersion() {
        return getData("manifestVersion");
    }

    /**
     * Set the version of the Manifest
     * @param version number of the version
     */
    public void setManifestVersion(String version) {
        setData("manifestVersion", version);
    }

    /**
     * Get the URL
     * @return string the URL
     */
    public String getUrl() {
        return getData("url");
    }

    /**
     * Set the URL
     * @param url
     */
    public void setUrl(String url) {
        setData("url", url);
    }

    /**
     * Get the token of the user
     * @return string the user token
     */
    public String getUserToken() {
        return getData("user_token");
    }

    /**
     * Set the token of the user
     * @param userToken
     */
    public void setUserToken(String userToken) {
        setData("user_token", userToken);
    }

    /**
     * Get the invitation token
     * @return string the invitation token
     */
    public String getInvitationToken() {
        return getData("invitation_token");
    }

    /**
     * Set the invitation token
     * @param invitationToken
     */
    public void setInvitationToken(String invitationToken) {
        setData("invitation_token", invitationToken);
    }

    /**
     * Get the session token
     * @return string the session token
     */
    public String getSessionToken() {
        return getData("session_token");
    }

    /**
     * Set the session token
     * @param sessionToken
     */
    public void setSessionToken(String sessionToken) {
        setData("session_token", sessionToken);
    }

    /**
     * Get the ID of the profile
     * @return string the profile ID
     */
    public String getProfileId() {
        return getData("profile_id");
    }

    /**
     * Set the ID of the profile
     * @param profileId
     */
    public void setProfileId(String profileId) {
        setData("profile_id", profileId);
    }

    /**
     * Get the ID of the agent
     * @return string the agent ID
     */
    public String getAgentId() {
        return getData("agent_id");
    }

    /**
     * Set the ID of the agent
     * @param agentId
     */
    public void setAgentId(String agentId) {
        setData("agent_id", agentId);
    }

    /**
     * Get the broker
     * @return string the broker
     */
    public String getBroker() {
        return getData("broker");
    }

    /**
     * Set the broker
     * @param broker
     */
    public void setBroker(String broker) {
        setData("broker", broker);
    }

    /**
     * Get the port
     * @return string the port
     */
    public String getPort() {
        return getData("port");
    }

    /**
     * Set the port
     * @param port
     */
    public void setPort(String port) {
        setData("port", port);
    }

    /**
     * Get the Transport Layer Security (TLS)
     * @return string the TLS
     */
    public String getTls() {
        return getData("tls");
    }

    /**
     * Set the Transport Layer Security (TLS)
     * @param tls
     */
    public void setTls(String tls) {
        setData("tls", tls);
    }

    /**
     * Get the topic
     * @return string the topic
     */
    public String getTopic() {
        return getData("topic");
    }

    /**
     * Set the topic
     * @param topic
     */
    public void setTopic(String topic) {
        setData("topic", topic);
    }

    /**
     * Get the user of the Message Queue Telemetry Transport (MQTT)
     * @return string the MQTT user
     */
    public String getMqttuser() {
        return getData("mqttuser");
    }

    /**
     * Set the user of the Message Queue Telemetry Transport (MQTT)
     * @param mqttuser
     */
    public void setMqttuser(String mqttuser) {
        setData("mqttuser", mqttuser);
    }

    /**
     * Get the password of the Message Queue Telemetry Transport (MQTT)
     * @return the MQTT password
     */
    public String getMqttpasswd() {
        return getData("mqttpasswd");
    }

    /**
     * Set the password of the Message Queue Telemetry Transport (MQTT)
     * @param mqttpasswd
     */
    public void setMqttpasswd(String mqttpasswd) {
        setData("mqttpasswd", mqttpasswd);
    }

    /**
     * Get the Certificate
     * @return string the certificate
     */
    public String getCertificate() {
        return getData("certificate");
    }

    /**
     * Set the Certificate
     * @param certificate
     */
    public void setCertificate(String certificate) {
        setData("certificate", certificate);
    }

    /**
     * Get the name
     * @return string the name
     */
    public String getName() {
        return getData("name");
    }

    /**
     * Set the name
     * @param name
     */
    public void setName(String name) {
        setData("name", name);
    }

    /**
     * Get the ID of the computer
     * @return string the computer ID
     */
    public String getComputersId() {
        return getData("computers_id");
    }

    /**
     * Set the ID of the computer
     * @param computersId
     */
    public void setComputersId(String computersId) {
        setData("computers_id", computersId);
    }

    /**
     * Get the ID of the entities
     * @return string the entities ID
     */
    public String getEntitiesId() {
        return getData("entities_id");
    }

    /**
     * Set the ID of the entities
     * @param entitiesId
     */
    public void setEntitiesId(String entitiesId) {
        setData("entities_id", entitiesId);
    }

    /**
     * Get the ID of the Fleets of the Flyve MDM plugin
     * @return string the Fleets ID
     */
    public String getPluginFlyvemdmFleetsId() {
        return getData("plugin_flyvemdm_fleets_id");
    }

    /**
     * Set the ID of the Fleets of the Flyve MDM plugin
     * @param pluginFlyvemdmFleetsId
     */
    public void setPluginFlyvemdmFleetsId(String pluginFlyvemdmFleetsId) {
        setData("plugin_flyvemdm_fleets_id", pluginFlyvemdmFleetsId);
    }
}
