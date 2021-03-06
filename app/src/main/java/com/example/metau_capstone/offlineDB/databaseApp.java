package com.example.metau_capstone.offlineDB;

import androidx.room.Room;

import com.example.metau_capstone.ParseApplication;

/**
 * This class is used to initialize the application with a database
 * so that the database can be accessed from anywhere
 */
public class databaseApp extends ParseApplication {
    database db;

    @Override
    public void onCreate() {
        super.onCreate();

        // when upgrading versions, kill the original tables by using fallbackToDestructiveMigration()
        db = Room.databaseBuilder(this, database.class, database.NAME).fallbackToDestructiveMigration().build();
    }

    // Get the database
    public database getDatabase() {
        return db;
    }
}
