package com.example.mytable.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "settings")
public class Setting {

    @PrimaryKey
    @ColumnInfo(name = "setting_id")
    public Integer id;

    @ColumnInfo(name = "setting_name")
    public String name;

    @ColumnInfo(name = "setting_value")
    public String value;

    public Setting(Integer id, String name, String value){
        this.id = id;
        this.name = name;
        this.value = value;
    }

}
