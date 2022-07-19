package com.example.metau_capstone;


import android.content.AsyncQueryHandler;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.example.metau_capstone.offlineDB.FortuneDB;
import com.example.metau_capstone.offlineDB.FortuneDao;
import com.example.metau_capstone.offlineDB.databaseApp;
import com.example.metau_capstone.offlineDB.userSettingsDB;
import com.example.metau_capstone.offlineDB.userSettingsDao;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;
import java.util.Objects;

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


    /**
     * Create a new database and store all the current user's fortune in it. Then
     * Save this database to the user's phone for offline loading.
     */
    public void createDatabase(Context context) {
        // Get all the user's fortunes
        ParseRelation<Fortune> fortRel = ParseUser.getCurrentUser().getRelation("fortunes");
        ParseQuery<Fortune> fortuneQuery = fortRel.getQuery();
        fortuneQuery.include("location");
        fortuneQuery.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> fortunes, ParseException e) {
                // If an error occurred, log it
                if (e != null) {
                    Log.e("offlineHelpers", "Unable to retrieve fortunes", e);
                    return;
                }

                // Get all the users liked fortunes
                ParseRelation<Fortune> likedRel = ParseUser.getCurrentUser().getRelation("liked");
                ParseQuery<Fortune> likedQuery = likedRel.getQuery();
                likedQuery.include("location");
                likedQuery.findInBackground(new FindCallback<Fortune>() {
                    @Override
                    public void done(List<Fortune> liked, ParseException e) {
                        // If an error occurred, log it
                        if (e != null) {
                            Log.e("offlineHelpers", "Unable to retrieve liked fortunes", e);
                            return;
                        }

                        // Work with the database on a background thread
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                // Get the database DOA
                                final FortuneDao fortuneDoa = ((databaseApp) context.getApplicationContext()).getDatabase().fortuneDAO();

                                // Delete all fortune from the database
                                ((databaseApp) context.getApplicationContext()).getDatabase().clearAllTables();

                                // Set the user's dark mode state
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        userSettingsDB setting = new userSettingsDB();
                                        try {
                                            setting.state = ParseUser.getCurrentUser().fetch().getBoolean("darkMode");
                                        } catch (ParseException e) {
                                            setting.state = false;
                                        }
                                        setting.stateStr = "";
                                        setting.tag = "darkMode";

                                        // Get the database DOA
                                        final userSettingsDao settingsDao = ((databaseApp) context.getApplicationContext()).getDatabase().userSettingsDAO();

                                        // Add the new state to the database
                                        settingsDao.addSetting(setting);
                                    }
                                });

                                // Set the user's preferred language
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        userSettingsDB setting = new userSettingsDB();
                                        try {
                                            setting.stateStr = ParseUser.getCurrentUser().fetch().getString("lang");
                                        } catch (ParseException e) {
                                            setting.stateStr = "en";
                                        }
                                        setting.state = true;
                                        setting.tag = "lang";

                                        // Get the database DOA
                                        final userSettingsDao settingsDao = ((databaseApp) context.getApplicationContext()).getDatabase().userSettingsDAO();

                                        // Add the new state to the database
                                        settingsDao.addSetting(setting);
                                    }
                                });

                                // Iterate over each fortune
                                for (Fortune f : fortunes) {
                                    // Create a new fortune object for the database
                                    // and store the needed information
                                    FortuneDB fort = new FortuneDB();
                                    fort.date = new offlineHelpers().toTimestamp(f.getCreatedAt());
                                    fort.message = f.getMessage();
                                    try { // Checking if the location is null
                                        fort.Lat_ = f.getLocation().getLatitude();
                                        fort.Long_ = f.getLocation().getLongitude();
                                    }
                                    catch (NullPointerException e2) {
                                        fort.Lat_ = -99999999;
                                        fort.Long_ = -99999999;
                                    }
                                    fort.likeCt = f.getLikeCt();
                                    fort.likedFort = 0;

                                    // Does the user like this fortune
                                    for (Fortune l : liked) {
                                        if (Objects.equals(l.getObjectId(), f.getObjectId())) {
                                            fort.liked = true;
                                            break;
                                        }
                                    }

                                    // Insert the fortune into the database
                                    fortuneDoa.insertFortune(fort);
                                }



                                // Iterate over each liked fortune
                                for (Fortune f : liked) {
                                    // Create a new fortune object for the database
                                    // and store the needed information
                                    FortuneDB fort = new FortuneDB();
                                    fort.date = new offlineHelpers().toTimestamp(f.getCreatedAt());
                                    fort.message = f.getMessage();
                                    try { // Checking if the location is null
                                        fort.Lat_ = f.getLocation().getLatitude();
                                        fort.Long_ = f.getLocation().getLongitude();
                                    }
                                    catch (NullPointerException e2) {
                                        fort.Lat_ = -99999999;
                                        fort.Long_ = -99999999;
                                    }
                                    fort.likeCt = f.getLikeCt();
                                    fort.likedFort = 1;
                                    fort.liked = true;

                                    // Insert the fortune into the database
                                    fortuneDoa.insertFortune(fort);
                                }

                            }
                        });

                    }
                });
            }
        });
    }
}
