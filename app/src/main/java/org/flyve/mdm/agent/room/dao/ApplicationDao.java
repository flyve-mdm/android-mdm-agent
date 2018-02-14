package org.flyve.mdm.agent.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.flyve.mdm.agent.room.entity.Application;


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
}
