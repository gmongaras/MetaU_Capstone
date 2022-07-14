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
public interface FortuneDao {
    // Insert a fortune into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertFortune(FortuneDB fort);

    // Get all the fortunes in the database
    @Query("SELECT * FROM FortuneDB WHERE likedFort = 0 ORDER BY date DESC LIMIT :limit")
    public List<FortuneDB> getFortunes(int limit);

    // Get all the liked fortunes in the database
    @Query("SELECT * FROM FortuneDB WHERE likedFort = 1 ORDER BY date DESC LIMIT :limit")
    public List<FortuneDB> getLiked(int limit);
}