package org.flyve.mdm.agent.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity (tableName = "applications")
public class Application {

    @PrimaryKey (autoGenerate = true)
    public int id;

    @ColumnInfo (name = "app_id")
    public String appId;

    @ColumnInfo (name = "app_name")
    public String appName;

    @ColumnInfo (name = "app_package")
    public String appPackage;

    @ColumnInfo (name = "app_path")
    public String appPath;

    @ColumnInfo (name = "app_status")
    public String appStatus;

}