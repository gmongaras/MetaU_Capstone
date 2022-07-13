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
public interface FortuneDoa {
    @Query("SELECT * FROM FortuneDB where fortId = :id")
    public FortuneDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Long insertFortune(FortuneDB fort);

    @Query("SELECT * FROM FortuneDB WHERE likedFort = 0 ORDER BY date DESC LIMIT :limit")
    public List<FortuneDB> getFortunes(int limit);

    @Query("SELECT * FROM FortuneDB WHERE likedFort = 1 ORDER BY date DESC LIMIT :limit")
    public List<FortuneDB> getLiked(int limit);

    @Delete
    public void deleteFortune(FortuneDB fort);
}