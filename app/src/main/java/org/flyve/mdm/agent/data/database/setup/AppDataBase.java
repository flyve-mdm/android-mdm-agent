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

package org.flyve.mdm.agent.data.database.setup;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import org.flyve.mdm.agent.data.database.dao.ApplicationDao;
import org.flyve.mdm.agent.data.database.dao.FileDao;
import org.flyve.mdm.agent.data.database.dao.MDMLogDao;
import org.flyve.mdm.agent.data.database.dao.MQTTDao;
import org.flyve.mdm.agent.data.database.dao.PoliciesDao;
import org.flyve.mdm.agent.data.database.dao.TopicsDao;
import org.flyve.mdm.agent.data.database.entity.Application;
import org.flyve.mdm.agent.data.database.entity.File;
import org.flyve.mdm.agent.data.database.entity.MDMLog;
import org.flyve.mdm.agent.data.database.entity.MQTT;
import org.flyve.mdm.agent.data.database.entity.Policies;
import org.flyve.mdm.agent.data.database.entity.Topics;


@Database(entities = {Application.class, MQTT.class, Policies.class, File.class, MDMLog.class, Topics.class}, version = 14, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    private static AppDataBase instance;

    public abstract ApplicationDao applicationDao();
    public abstract MQTTDao MQTTDao();
    public abstract PoliciesDao PoliciesDao();
    public abstract FileDao FileDao();
    public abstract MDMLogDao MDMLogDao();
    public abstract TopicsDao TopicsDao();

    public static AppDataBase getAppDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDataBase.class,
                    "flyve-mdm-db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
