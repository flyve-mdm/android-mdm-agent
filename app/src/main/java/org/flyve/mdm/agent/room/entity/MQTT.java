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

package org.flyve.mdm.agent.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity (tableName = "mqtt")
public class MQTT {

    @PrimaryKey (autoGenerate = true)
    public int id;

    @ColumnInfo (name = "agentId")
    public String agentId;

    @ColumnInfo (name = "broker")
    public String broker;

    @ColumnInfo (name = "port")
    public String port;

    @ColumnInfo (name = "tls")
    public String tls;

    @ColumnInfo (name = "topic")
    public String topic;

    @ColumnInfo (name = "mqttuser")
    public String mqttuser;

    @ColumnInfo (name = "mqttpasswd")
    public String mqttpasswd;

    @ColumnInfo (name = "certificate")
    public String certificate;

    @ColumnInfo (name = "name")
    public String name;

    @ColumnInfo (name = "computersId")
    public String computersId;

    @ColumnInfo (name = "entitiesId")
    public String entitiesId;

    @ColumnInfo (name = "pluginFlyvemdmFleetsId")
    public String pluginFlyvemdmFleetsId;

}