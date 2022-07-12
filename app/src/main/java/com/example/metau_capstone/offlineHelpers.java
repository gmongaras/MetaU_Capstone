package com.example.metau_capstone;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * This class has a couple of functions to help with offline loading
 */
public class offlineHelpers {

    // Is the network available?
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Convert a long value to a date
    public Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    // Convert a date to a long value
    public Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
