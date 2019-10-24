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

import org.flyve.mdm.agent.data.database.entity.File;

@Dao
public interface FileDao {

    @Insert
    void insert(File... files);

    @Update
    void update(File... files);

    @Delete
    void delete(File... files);

    @Query("DELETE FROM files")
    void deleteAll();

    @Query("Select * FROM files")
    File[] loadAll();

    @Query("SELECT * FROM files WHERE file_id = :id")
    File[] getFileById(String id);

    @Query("UPDATE files SET file_status = :status WHERE file_id = :id")
    int updateStatus(String id, String status);

    @Query("DELETE FROM files WHERE file_name = :name")
    void deleteByName(String name);

    @Query("DELETE FROM files WHERE task_id = :taskId")
    void deteleByTaskId(String taskId);

    @Query("DELETE FROM files WHERE file_path = :filePath")
    void deleteByFilePath(String filePath);
}
