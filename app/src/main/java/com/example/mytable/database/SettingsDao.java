package com.example.mytable.database;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SettingsDao {
    @Query("SELECT setting_value FROM settings WHERE setting_id =:id" )
    public String getValueBySettingId(Integer id);

    @Query("UPDATE settings  SET setting_value =:value WHERE setting_id =:id ")
    public void updateSettingValueByName(Integer id, String value);

    @Query("INSERT INTO settings VALUES(:id, :name, :value)")
    public void insert(Integer id, String name, String value);

    @Query("SELECT * FROM settings ORDER BY setting_id")
    public List<Setting> getAllSettings();
}
