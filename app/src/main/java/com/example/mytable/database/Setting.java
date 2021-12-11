package com.example.mytable.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "settings")
public class Setting {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "setting_id")
    public Integer id;

    @ColumnInfo(name = "setting_description")
    public String description;

    @ColumnInfo(name = "setting_value")
    public String value;

    public Setting(String name, String value){
        this.description = name;
        this.value = value;
    }

    public Setting(){ }
}
