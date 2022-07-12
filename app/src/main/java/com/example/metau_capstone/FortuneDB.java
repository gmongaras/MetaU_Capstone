package com.example.metau_capstone;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 ** Class used to store fortune data so it can be stored in the Room database
 */
@Entity
public class FortuneDB {
    // Used to store fortunes in a database
    @ColumnInfo
    @PrimaryKey(autoGenerate=true)
    public Long fortId;

    // Parts of the fortune we want to store so it can be
    // retrieved later
    @ColumnInfo
    public String date; // Date the fortune was created at
    @ColumnInfo
    public String dateDet; // Detailed date the fortune was created at
    @ColumnInfo
    public String message; // Fortune message
    @ColumnInfo
    public float Lat_; // Latitude of location of fortune
    @ColumnInfo
    public float Long_; // Longitude of location of fortune
    @ColumnInfo
    public boolean liked; // Has the user liked this fortune?
    @ColumnInfo
    public int likeCt; // Like count of this fortune
}
