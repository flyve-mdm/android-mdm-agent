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

import org.flyve.mdm.agent.data.database.entity.Application;

@Dao
public interface ApplicationDao {

    @Insert
    void insert(Application... applications);

    @Update
    void update(Application... applications);

    @Delete
    void delete(Application... applications);

    @Query("DELETE FROM applications")
    void deleteAll();

    @Query("Select * FROM applications")
    Application[] loadAll();

    @Query("SELECT * FROM applications WHERE app_id = :id")
    Application[] getApplicationById(String id);

    @Query("SELECT * FROM applications WHERE app_package = :mPackage")
    Application[] getApplicationByPackageName(String mPackage);

    @Query("UPDATE applications SET app_status = :status WHERE app_id = :id")
    int updateStatus(String id, String status);

    @Query("UPDATE applications SET app_version_code = :versionCode WHERE app_id = :id")
    int updateVersionCode(String id, String versionCode);

    @Query("UPDATE applications SET task_id = :taskId WHERE app_package = :mPackage")
    void updateTaskId(String mPackage, String taskId);

    @Query("DELETE FROM applications WHERE app_package = :mPackage")
    void deleteByPackageName(String mPackage);

    @Query("DELETE FROM applications WHERE app_id = :id")
    void deleteById(String id);


}
