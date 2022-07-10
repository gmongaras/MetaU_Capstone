package com.example.metau_capstone;

import com.example.metau_capstone.Friends.Friend_queue;
import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;

/**
 * Class used to initialize Parse when the application is loaded
 */
public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register custom classes
        ParseObject.registerSubclass(Fortune.class);
        ParseObject.registerSubclass(Friend_queue.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        );
    }
}