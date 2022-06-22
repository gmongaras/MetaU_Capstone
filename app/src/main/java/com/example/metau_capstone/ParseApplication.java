package com.example.metau_capstone;

import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register classes
        ParseObject.registerSubclass(Fortune.class);
        ParseObject.registerSubclass(Friend_queue.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("Dwsfp3rsCpvhUvRpB8dtfiA97kOHeORreiuIAu4o")
                .clientKey("Uk8DpECE78TqC1r4OUr8jYMUksag8lBdvC5TTnzh")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}