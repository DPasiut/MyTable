package com.example.mytable.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SettingsDao {
    @Insert
    void insert(Setting setting);

    @Update
    void update(Setting setting);


    @Query("DELETE FROM settings WHERE setting_id =:id")
    void delete(Integer id);

    @Query("SELECT setting_value FROM settings WHERE setting_id =:id" )
    public String getValueBySettingId(Integer id);

    @Query("SELECT * FROM settings ORDER BY setting_id")
    List<Setting> getAllSettings();

    @Query("DELETE FROM settings")
    void deleteAllSettings();
}
