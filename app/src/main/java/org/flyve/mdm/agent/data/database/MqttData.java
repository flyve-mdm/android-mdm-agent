package org.flyve.mdm.agent.data.database;

/*
 *   Copyright  2018 Teclib. All rights reserved.
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
 * @author    rafael hernandez
 * @date      9/4/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import org.flyve.mdm.agent.data.database.entity.MQTT;
import org.flyve.mdm.agent.data.database.setup.AppDataBase;

import java.util.List;

public class MqttData {

    private static final String MANIFEST_VERSION = "manifestVersion";
    private static final String URL = "url";
    private static final String USER_TOKEN = "userToken";
    private static final String INVITATION_TOKEN = "invitationToken";
    private static final String SESSION_TOKEN = "sessionToken";
    private static final String PROFILE_ID = "profileId";
    private static final String AGENT_ID = "agentId";
    private static final String BROKER = "broker";
    private static final String TLS = "tls";
    private static final String TOPIC = "topic";
    private static final String MQTT_USER = "mqttuser";
    private static final String MQTT_PASSWD = "mqttpasswd";
    private static final String CERTIFICATE = "certificate";
    private static final String NAME = "name";
    private static final String COMPUTERS_ID = "computersId";
    private static final String ENTITIES_ID = "entitiesId";
    private static final String PLUGIN_FLYVE_MDM_FLEETS_ID = "pluginFlyvemdmFleetsId";
    private static final String PORT = "port";
    private static final String API_TOKEN = "api_token";

    private AppDataBase dataBase;

    private static Handler uiHandler;

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }
    private static void runOnUI(Runnable runnable) {
        uiHandler.post(runnable);
    }

    public MqttData(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
    }

    public void deleteAll() {
        dataBase.MQTTDao().deleteAll();
    }

    public String getManifestVersion() {
        return getStringValue(MANIFEST_VERSION);
    }

    public void setManifestVersion(String value) {
        setStringValue(MANIFEST_VERSION, value);
    }

    public String getUrl() {
        return getStringValue(URL);
    }

    public void setUrl(String value) {
        setStringValue(URL, value);
    }

    public String getUserToken() {
        return getStringValue(USER_TOKEN);
    }

    public void setApiToken(String value) {
        setStringValue(API_TOKEN, value);
    }

    public String getApiToken() {
        return getStringValue(API_TOKEN);
    }

    public void setUserToken(String value) {
        setStringValue(USER_TOKEN, value);
    }

    public String getInvitationToken() {
        return getStringValue(INVITATION_TOKEN);
    }

    public void setInvitationToken(String value) {
        setStringValue(INVITATION_TOKEN, value);
    }

    public String getSessionToken() {
        return getStringValue(SESSION_TOKEN);
    }

    public void setSessionToken(String value) {
        setStringValue(SESSION_TOKEN, value);
    }

    public String getProfileId() {
        return getStringValue(PROFILE_ID);
    }

    public void setProfileId(String value) {
        setStringValue(PROFILE_ID, value);
    }

    public String getAgentId() {
        return getStringValue(AGENT_ID);
    }

    public void setAgentId(String value) {
        setStringValue(AGENT_ID, value);
    }

    public String getBroker() {
        return getStringValue(BROKER);
    }

    public void setBroker(String value) {
        setStringValue(BROKER, value);
    }

    public String getTls() {
        return getStringValue(TLS);
    }

    public void setTls(String value) {
        setStringValue(TLS, value);
    }

    public String getTopic() {
        return getStringValue(TOPIC);
    }

    public void setTopic(String value) {
        setStringValue(TOPIC, value);
    }

    public String getMqttUser() {
        return getStringValue(MQTT_USER);
    }

    public void setMqttUser(String value) {
        setStringValue(MQTT_USER, value);
    }

    public String getMqttPasswd() {
        return getStringValue(MQTT_PASSWD);
    }

    public void setMqttPasswd(String value) {
        setStringValue(MQTT_PASSWD, value);
    }

    public String getCertificate() {
        return getStringValue(CERTIFICATE);
    }

    public void setCertificate(String value) {
        setStringValue(CERTIFICATE, value);
    }

    public String getName() {
        return getStringValue(NAME);
    }

    public void setName(String value) {
        setStringValue(NAME, value);
    }

    public String getComputersId() {
        return getStringValue(COMPUTERS_ID);
    }

    public void setComputersId(String value) {
        setStringValue(COMPUTERS_ID, value);
    }

    public String getEntitiesId() {
        return getStringValue(ENTITIES_ID);
    }

    public void setEntitiesId(String value) {
        setStringValue(ENTITIES_ID, value);
    }

    public String getPluginFlyvemdmFleetsId() {
        return getStringValue(PLUGIN_FLYVE_MDM_FLEETS_ID);
    }

    public void setPluginFlyvemdmFleetsId(String value) {
        setStringValue(PLUGIN_FLYVE_MDM_FLEETS_ID, value);
    }

    public String getPort() {
        return getStringValue(PORT);
    }

    public void setPort(String value) {
        setStringValue(PORT, value);
    }

    private String getStringValue(String name) {
        List<MQTT> arrMQTT = dataBase.MQTTDao().getByName(name);
        if(!arrMQTT.isEmpty()) {
            return arrMQTT.get(0).value;
        } else {
            return "";
        }
    }

    private void setStringValue(final String name, final String value) {
        Thread t = new HandlerThread("UIHandler") {
            @Override
            public void run() {
                if (dataBase.MQTTDao().getByName(name).isEmpty()) {
                    MQTT mqtt = new MQTT();
                    mqtt.name = name;
                    mqtt.value = value;
                    dataBase.MQTTDao().insert(mqtt);
                } else {
                    MQTT mqtt = dataBase.MQTTDao().getByName(name).get(0);
                    mqtt.value = value;
                    dataBase.MQTTDao().update(mqtt);
                }
            }
        };
        t.start();
    }
}
