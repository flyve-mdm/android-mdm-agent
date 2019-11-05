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

package org.flyve.mdm.agent.data.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.flyve.mdm.agent.data.database.entity.Topics;

import java.util.List;

@Dao
public interface TopicsDao {

    @Insert
    void insert(Topics... topics);

    @Update
    void update(Topics... topics);

    @Delete
    void delete(Topics... topics);

    @Query("Select * FROM topics")
    List<Topics> loadAll();

    @Query("DELETE FROM topics")
    void deleteAll();

    @Query("select * FROM topics where topic = :topic")
    List<Topics> getByTopic(String topic);

    @Query("delete FROM topics where topic like '%fleet%'")
    void deleteFleets();

    @Query("select * FROM topics where topic like '%fleet%'")
    List<Topics> getFleets();

    @Query("update topics set status = 0")
    void clearTopics();
}