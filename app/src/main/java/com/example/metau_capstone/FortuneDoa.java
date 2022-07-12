package com.example.metau_capstone;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


/**
 ** Room class used to manage the FortuneDB class in the SQL database
 */
@Dao
public interface FortuneDoa {
    @Query("SELECT * FROM FortuneDB where fortId = :id")
    public FortuneDB getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public Long insertFortune(FortuneDB fort);

    @Delete
    public void deleteFortune(FortuneDB fort);
}