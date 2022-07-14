package com.example.metau_capstone.offlineDB;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities={FortuneDB.class, userSettingsDB.class}, version=7)
public abstract class database extends RoomDatabase {
    // Data access objects
    public abstract FortuneDao fortuneDAO();
    public abstract userSettingsDao userSettingsDAO();

    // Database name to be used
    public static final String NAME = "database";
}