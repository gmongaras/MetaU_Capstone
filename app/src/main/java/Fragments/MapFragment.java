package Fragments;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.metau_capstone.CustomWindowAdapter;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "MapFragment";

    // Map elements
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    // Error text element
    private TextView errorText;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the error text information
        errorText = view.findViewById(R.id.errorText);

        // Get the map information
        mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(requireContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;

        // If the map was loaded properly
        if (map != null) {
            // Once map is loaded
            // Supported types include: MAP_TYPE_NORMAL, MAP_TYPE_SATELLITE
            // MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID
            //map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            // Set the custom info window for each marker
            map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater()));


            // Load in all the fortunes as pins into the map
            loadPins();


            return;
        }
        // If the map was not loaded properly, display some text
        else {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText(R.string.mapError);
        }
    }



    private void loadPins() {
        // Specify which class to query
        ParseQuery<Fortune> query = ParseQuery.getQuery(Fortune.class);

        // Get only this user's fortunes
        query.whereEqualTo("user", ParseUser.getCurrentUser());

        // Find all the fortunes the user owns
        query.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> objects, ParseException e) {
                // If an error occurred, show an error message
                if (e != null) {
                    Toast.makeText(requireContext(), "Unable to load in fortunes", Toast.LENGTH_SHORT).show();
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
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });


    }
}