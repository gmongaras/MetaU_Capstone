package com.example.metau_capstone.Friends;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 ** Parse class used to manage the Friend_queue class in the database
 */
@ParseClassName("Friend_queue")
public class Friend_queue extends ParseObject {

    // Keys (columns names) in the database for each post
    public static final String KEY_USER = "user";
    public static final String KEY_FRIEND = "friend";
    public static final String KEY_MODE = "mode";

    // Getter and setter methods for the user
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }
    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    // Getter and setter methods for the friend to add to the user's friends
    public ParseUser getFriend() {
        return getParseUser(KEY_FRIEND);
    }
    public void setFriend(ParseUser friend) {
        put(KEY_FRIEND, friend);
    }

    // Getter and setter methods for the friend to add to the user's friends
    public String getMode() {
        return getString(KEY_MODE);
    }
    public void setMode(String mode) {
        put(KEY_MODE, mode);
    }
}
