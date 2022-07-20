package com.example.metau_capstone;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.example.metau_capstone.offlineDB.FortuneDB;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;


/**
 * Class with helper methods to easily work with any Google Maps map
 */
public class MapHelper {
    // The map we want to work with
    private GoogleMap map;

    // Map fragment to work with the map
    private SupportMapFragment mapFragment;

    // Context so we know where the call is coming from
    Context context;

    // Used to convert times
    dateFormatter df;

    // Used to work with offline data
    offlineHelpers h;



    // Constructor taking in a map and the support fragment
    public MapHelper(GoogleMap map, SupportMapFragment mapFragment, Context context) {
        this.map = map;
        this.mapFragment = mapFragment;
        this.context = context;
        this.df = new dateFormatter();
        this.h = new offlineHelpers();
    }


    /**
     * Load this map given information to load the map with
     * @param user The user to load the map for (who's fortunes should be loaded?)
     * @param errorText Text to display when the map can't be loaded
     * @param goToLoc True to go to the current user's location, False otherwise
     */
    public void loadMap(ParseUser user, TextView errorText, boolean goToLoc) {
        // If the map was loaded properly
        if (map != null) {
            // Once map is loaded
            // Supported types include: MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE
            // MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
            //map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            map.setIndoorEnabled(true);
            map.setBuildingsEnabled(true);

            // Set the custom info window for each marker
            LayoutInflater inflater = LayoutInflater.from(context);
            map.setInfoWindowAdapter(new CustomWindowAdapter(inflater));

            // Put the map in dark mode
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(mapFragment.requireContext(), R.raw.map_dark));
            }


            // Load in all the fortunes as pins into the map
            // if th user is online
            if (user != null && ((new offlineHelpers()).isNetworkAvailable(context))) {
                loadPins(user);
            }

            // Go to the location of the user for a better map experience
            // if request to do so
            if (goToLoc) {
                goToLocation();
            }
        }
        // If the map was not loaded properly, display some text
        else {
            if (errorText != null) {
                errorText.setVisibility(View.VISIBLE);
                (new translationManager(ParseUser.getCurrentUser().getString("lang"))).addText(errorText, R.string.mapError, context);
            }
        }
    }



    // go to the current location of the user
    private void goToLocation() {
        // Setup the location manager to get the location
        LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            return;
        }

        // Get the location of the phone
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // The saved location
        ParseGeoPoint loc = null;

        // If the location is not null, goto that location
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            goToLatLng(latLng, 5);
        }
    }



    // Given a user, load all the fortunes as pins on the map for that user
    private void loadPins(ParseUser user) {
        // Specify which class to query
        ParseQuery<Fortune> query = ParseQuery.getQuery(Fortune.class);

        // Get only this user's fortunes
        query.whereEqualTo("user", user);

        // Find all the fortunes the user owns
        query.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> objects, ParseException e) {
                // If an error occurred, show an error message
                if (e != null) {
                    (new translationManager(ParseUser.getCurrentUser().getString("lang"))).createToast(context, "Unable to load map");
                    Log.e("MapHelper", "Unable to load in fortunes", e);
                    return;
                }

                // If no error occurred, load in the fortunes
                for (Fortune fortune : objects) {
                    // Create the marker options
                    MarkerOptions options = new MarkerOptions();

                    // Get the location from the database
                    ParseGeoPoint point = fortune.getLocation();

                    // If the point is null, skip this fortune
                    if (point == null) {
                        continue;
                    }

                    // Set the position of the marker
                    LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                    options.position(latLng);

                    // Set the title and snippet from the given view
                    options.title(df.toMonthDayTime(fortune.getCreatedAt()));
                    String message = fortune.getMessage();
                    if (message.length() > 50) {
                        message = message.substring(0, 50) + "...";
                    }
                    options.snippet(message);

                    // Define custom marker
                    //options.icon(bitmapDescriptorFromVector(MapDemoActivity.this, R.drawable.marker));

                    // Add the marker with an animation
                    dropPinEffect(map.addMarker(options));
                }
            }
        });
    }


    // Given a list of FortuneDB object, load the pins on the map
    public void loadPinsOffline(List<FortuneDB> fortunes) {
        // If no error occurred, load in the fortunes
        for (FortuneDB fortune : fortunes) {
            // Create the marker options
            MarkerOptions options = new MarkerOptions();

            // If the longitude is -1, there is no location for this fortune
            if (fortune.Long_ == -99999999) {
                continue;
            }

            // Set the position of the marker
            LatLng latLng = new LatLng(fortune.Lat_, fortune.Long_);
            options.position(latLng);

            // Set the title and snippet from the given view
            options.title(df.toMonthDayTime(h.toDate(fortune.date)));
            String message = fortune.message;
            if (message.length() > 50) {
                message = message.substring(0, 50) + "...";
            }
            options.snippet(message);

            // Define custom marker
            //options.icon(bitmapDescriptorFromVector(MapDemoActivity.this, R.drawable.marker));

            // Add the marker with an animation
            dropPinEffect(map.addMarker(options));
        }
    }


    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.1f, 1.0f + 7 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }



    // Given a latitude and longitude location, go to that spot on the map
    public void goToLatLng(LatLng latLng, float zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}
