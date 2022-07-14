package com.example.metau_capstone.offlineDB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 ** Class used to store user settings to handle when the user is offline
 */
@Entity
public class userSettingsDB {
    // Id of each entity in this class
    @ColumnInfo
    @PrimaryKey(autoGenerate=true)
    public Long setId;

    // Boolean value and tag for each setting to easily retrieve the setting state
    @ColumnInfo
    public boolean state;
    @ColumnInfo
    public String tag;
}
