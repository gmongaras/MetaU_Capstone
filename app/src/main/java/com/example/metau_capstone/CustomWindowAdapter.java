package com.example.metau_capstone;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 ** This class handles the creation of popups above each of the
 ** markers in any of the Google Maps views.
 */
public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater mInflater;

    public CustomWindowAdapter(LayoutInflater i){
        mInflater = i;
    }

    // This defines the contents within the info window based on the marker
    @Override
    public View getInfoContents(Marker marker) {
        // Getting view from the layout file
        View v = mInflater.inflate(R.layout.custom_info_window, null);
        // Populate fields
        TextView tvDate_map = (TextView) v.findViewById(R.id.tvDate_map);
        tvDate_map.setText(marker.getTitle());

        TextView tvFortuneText_map = (TextView) v.findViewById(R.id.tvFortuneText_map);
        tvFortuneText_map.setText(marker.getSnippet());
        // Return info window contents
        return v;
    }

    // This changes the frame of the info window; returning null uses the default frame.
    // This is just the border and arrow surrounding the contents specified above
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}