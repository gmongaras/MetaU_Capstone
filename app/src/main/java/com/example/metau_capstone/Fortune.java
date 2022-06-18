package com.example.metau_capstone;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Fortune")
public class Fortune extends ParseObject {

    // Keys (columns names) in the database for each post
    public static final String KEY_USER = "user";
    public static final String KEY_TIME_CREATED = "createdAt";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_LOCATION = "location";

    // Getter and setter methods for the user who owns this fortune
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }
    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    // Getter and setter methods for the message of the fortune
    public String getMessage() { return getString(KEY_MESSAGE); }
    public void setMessage(String message) { put(KEY_MESSAGE, message); }

    // Getter and setter methods for the location of this fortune
    public ParseGeoPoint getLocation() { return getParseGeoPoint(KEY_LOCATION); }
    public void setLocation(ParseGeoPoint location) { put(KEY_LOCATION, location); }
}
