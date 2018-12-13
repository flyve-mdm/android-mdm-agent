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

import org.flyve.mdm.agent.data.database.entity.Policies;

import java.util.List;

@Dao
public interface PoliciesDao {

    @Insert
    void insert(Policies... policies);

    @Update
    void update(Policies... policies);

    @Delete
    void delete(Policies... policies);

    @Query("Select * FROM policies")
    List<Policies> loadAll();

    @Query("Select * FROM policies where policyName = :policyName order by priority desc limit 1")
    List<Policies> getPolicyByName(String policyName);

//    @Query("Select * FROM policies where policyName = :policyName and priority = :priority order by priority desc limit 1")
//    List<Policies> getPolicyBy(String policyName, int priority);

    @Query("Select * FROM policies where taskId = :taskId order by priority desc limit 1")
    List<Policies> getPolicyByTaskId(String taskId);

    @Query("DELETE FROM policies")
    void deleteAll();
}
