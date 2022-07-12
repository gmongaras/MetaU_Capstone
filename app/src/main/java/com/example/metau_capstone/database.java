package com.example.metau_capstone;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities={FortuneDB.class}, version=1)
public abstract class database extends RoomDatabase {
    // Declare your data access objects as abstract
    public abstract FortuneDoa fortune();

    // Database name to be used
    public static final String NAME = "database";
}