package com.example.metau_capstone.offlineDB;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.Date;

/**
 ** Class used to store fortune data so it can be stored in the Room database
 */
@Entity
public class FortuneDB implements Serializable {
    // Used to store fortunes in a database
    @ColumnInfo
    @PrimaryKey(autoGenerate=true)
    public Long fortId;

    // Parts of the fortune we want to store so it can be
    // retrieved later
    @ColumnInfo
    public Long date; // Date the fortune was created at
    @ColumnInfo
    public String message; // Fortune message
    @ColumnInfo
    public double Lat_; // Latitude of location of fortune
    @ColumnInfo
    public double Long_; // Longitude of location of fortune
    @ColumnInfo
    public boolean liked; // Has the user liked this fortune?
    @ColumnInfo
    public int likeCt; // Like count of this fortune
    @ColumnInfo
    public int likedFort; // Is this fortune in the database one that appears in the
                          // liked page (1) or the normal page (0)
}
