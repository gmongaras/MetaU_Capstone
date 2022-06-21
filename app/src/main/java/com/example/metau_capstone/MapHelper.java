package com.example.metau_capstone;


import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

// Helper methods to work with a map
public class MapHelper {
    // The map we want to work with
    private GoogleMap map;

    // Map fragment to work with the map
    private SupportMapFragment mapFragment;

    // Context so we know where the call is coming from
    Context context;



    // Constructor taking in a map and the support fragment
    public MapHelper(GoogleMap map, SupportMapFragment mapFragment, Context context) {
        this.map = map;
        this.mapFragment = mapFragment;
        this.context = context;
    }



    public void loadMap(ParseUser user, TextView errorText) {
        // If the map was loaded properly
        if (map != null) {
            // Once map is loaded
            // Supported types include: MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE
            // MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
            //map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            // Set the custom info window for each marker
            LayoutInflater inflater = LayoutInflater.from(context);
            map.setInfoWindowAdapter(new CustomWindowAdapter(inflater));


            // Load in all the fortunes as pins into the map
            loadPins(user);
        }
        // If the map was not loaded properly, display some text
        else {
            if (errorText != null) {
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(R.string.mapError);
            }
        }
    }



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
                    Toast.makeText(context, "Unable to load in fortunes", Toast.LENGTH_SHORT).show();
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
                    options.title(fortune.getCreatedAt().toString());
                    String message = fortune.getMessage().toString();
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
    public void goToLatLng(LatLng latLng) {
        float zoom = 5;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}
