package com.example.metau_capstone.offlineDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;


/**
 ** Room class used to manage the FortuneDB class in the SQL database
 */
@Dao
public interface userSettingsDao {
    // Add a setting to the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void addSetting(userSettingsDB setting);

    // Get the dark mode setting from the database
    @Query("SELECT * FROM userSettingsDB WHERE tag = 'darkMode'")
    public List<userSettingsDB> getDarkMode();
}