/*
 *   Copyright  Teclib. All rights reserved.
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
 * @author    @rafaelje
 * @copyright Copyright  Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.data.database;

import android.content.Context;

import org.flyve.mdm.agent.data.database.entity.Topics;
import org.flyve.mdm.agent.data.database.setup.AppDataBase;

import java.util.List;

public class TopicsData {

    private AppDataBase dataBase;

    public TopicsData(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
    }

    public List<Topics> getAllTopics() {
        return dataBase.TopicsDao().loadAll();
    }

    public void deleteAll() {
        dataBase.TopicsDao().deleteAll();
    }

    public List<Topics> setValue(String topic, int qos) {
        if(dataBase.TopicsDao().getByTopic(topic).isEmpty()) {
            // remove previous fleet to keep just one
            if(topic.contains("fleet")) {
                dataBase.TopicsDao().deleteFleets();
            }

            Topics topics = new Topics();
            topics.topic = topic;
            topics.qos = qos;
            topics.status = 0;
            dataBase.TopicsDao().insert(topics);
        } else {
            Topics topics = dataBase.TopicsDao().getByTopic(topic).get(0);
            topics.qos = qos;
            dataBase.TopicsDao().update(topics);
        }

        return dataBase.TopicsDao().getByTopic(topic);
    }

    public void setStatusTopic(String topic, int status) {
        Topics topics = dataBase.TopicsDao().getByTopic(topic).get(0);
        topics.status = status;
        dataBase.TopicsDao().update(topics);
    }

    public void clearTopics() {
        dataBase.TopicsDao().clearTopics();
    }
}
