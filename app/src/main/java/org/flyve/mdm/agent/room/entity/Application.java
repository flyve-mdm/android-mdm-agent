package org.flyve.mdm.agent.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.location.Address;

@Entity (tableName = "person")
public class Person {

    @PrimaryKey (autoGenerate = true)
    public int id;

    @ColumnInfo (name = "first_name")
    public String firstName;

    @ColumnInfo (name = "last_name")
    public String lastName;

    @Ignore
    public Bitmap picture;

    @Embedded
    public Address address;
}