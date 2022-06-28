package Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.ProfileAdapter;
import com.example.metau_capstone.R;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileSearchLoc#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileSearchLoc extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 20;

    private static final String TAG = "ProfileSearchLoc";

    // Elements in the views
    TextView svProfileSearchLat;
    TextView svProfileSearchLng;
    TextView svProfileSearchDist;
    RecyclerView rvProfileSearchLoc;
    TextView tvNoResultsLoc;
    TextView tvSearchLocPrompt;
    ProgressBar pbProfileSearchLoc;

    // Recycler view stuff
    LinearLayoutManager layoutManager;
    ProfileAdapter adapter;

    // List of fortunes for the recycler view
    List<Fortune> Fortunes;

    // The user to load data for
    private static final String ARG_USER = "user";
    private ParseUser user;

    // Is querying happening?
    boolean querying;

    // Text from the query
    LatLng queryLoc;
    int queryDist;

    public ProfileSearchLoc() {
        // Required empty public constructor
    }

    public static ProfileSearchLoc newInstance(ParseUser user) {
        ProfileSearchLoc fragment = new ProfileSearchLoc();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_search_loc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        skipVal = 0;

        // Get the elements
        svProfileSearchLat = view.findViewById(R.id.svProfileSearchLat);
        svProfileSearchLng = view.findViewById(R.id.svProfileSearchLng);
        svProfileSearchDist = view.findViewById(R.id.svProfileSearchDist);
        rvProfileSearchLoc = view.findViewById(R.id.rvProfileSearchLoc);
        tvNoResultsLoc = view.findViewById(R.id.tvNoResultsLoc);
        tvSearchLocPrompt = view.findViewById(R.id.tvSearchLocPrompt);
        pbProfileSearchLoc = view.findViewById(R.id.pbProfileSearchLoc);

        // Initialize the fortunes
        Fortunes = new ArrayList<>();

        // When the fortunes have been loaded, setup the recycler view -->
        // Bind the adapter to the recycler view
        adapter = new ProfileAdapter(Fortunes, user, getContext(), requireActivity().getSupportFragmentManager());
        rvProfileSearchLoc.setAdapter(adapter);

        // Configure the Recycler View: Layout Manager
        layoutManager = new LinearLayoutManager(getContext());
        rvProfileSearchLoc.setLayoutManager(layoutManager);

        // Used for infinite scrolling
        rvProfileSearchLoc.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Set the querying flag to true
                querying = true;

                queryFortunes(queryLoc, queryDist);
            }
        });



        // Add a listener to both text views
        svProfileSearchLat.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if the key is ENTER, take action
                if (keyCode == 66) {
                    submit();
                    return true;
                }

                return false;
            }
        });
        svProfileSearchLng.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if the key is ENTER, take action
                if (keyCode == 66) {
                    submit();
                    return true;
                }

                return false;
            }
        });
        svProfileSearchDist.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if the key is ENTER, take action
                if (keyCode == 66) {
                    submit();
                    return true;
                }

                return false;
            }
        });


    }




    // When a submission occurs, handle the event
    private void submit() {
        // Get the text from both inputs
        String Lat_str = svProfileSearchLat.getText().toString();
        String Lng_str = svProfileSearchLng.getText().toString();
        String Dist_str = svProfileSearchDist.getText().toString();

        // If the fields are empty, handle the issue
        if (Lat_str.isEmpty()) {
            Toast.makeText(requireContext(), "Latitude input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Lng_str.isEmpty()) {
            Toast.makeText(requireContext(), "Longitude input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Dist_str.isEmpty()) {
            Toast.makeText(requireContext(), "Distance input cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the input is not empty, get the text as numbers
        int Lat = Integer.parseInt(Lat_str);
        int Lng = Integer.parseInt(Lng_str);
        int Dist = Integer.parseInt(Dist_str);

        // If the input is too large or too small, handle the issue
        if (Lat < -90 || Lat > 90) {
            Toast.makeText(requireContext(), "Latitude must be between -90 and 90", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Lng < -180 || Lng > 180) {
            Toast.makeText(requireContext(), "Longitude must be between -180 and 180", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Dist <= 0) {
            Toast.makeText(requireContext(), "Distance must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the input is valid, start the query
        LatLng loc = new LatLng(Lat, Lng);
        queryLoc = loc;
        queryDist = Dist;


        // If fortunes are already being queried, do nothing
        if (querying == true) {
            return;
        }

        // If the prompt text is visible, make it invisible
        tvSearchLocPrompt.setVisibility(View.INVISIBLE);

        // Set the querying state to true
        querying = true;

        // Reset the skip value
        skipVal = 0;

        // Reset the list
        Fortunes.clear();
        adapter.notifyDataSetChanged();

        // Query the text
        queryFortunes(loc, Dist);
    }




    // Query fortunes given a location to query for
    private void queryFortunes(LatLng loc, int dist) {
        pbProfileSearchLoc.setVisibility(View.VISIBLE);


        // Create the query
        ParseQuery<Fortune> query = ParseQuery.getQuery(Fortune.class);

        // Query nearby the given location
        query.whereWithinMiles("location", new ParseGeoPoint(loc.latitude, loc.longitude), dist);

        // Search for ids equal to this user
        query.whereEqualTo("user", user);

        // Skip some posts
        query.setSkip(skipVal*loadRate);

        // Set the limit to loadRate posts
        query.setLimit(loadRate);

        query.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> fortunes, ParseException e) {
                // Check if there was an exception
                if (e != null) {
                    Log.e(TAG, "Unable to load fortunes", e);
                    pbProfileSearchLoc.setVisibility(View.INVISIBLE);
                    querying = false;
                    return;
                }

                // If no fortunes were found, display an alert message
                if (fortunes.size() == 0 && Fortunes.size() == 0) {
                    tvNoResultsLoc.setVisibility(View.VISIBLE);
                }
                else {
                    tvNoResultsLoc.setVisibility(View.INVISIBLE);

                    // Get all fortunes from the list and load them in
                    Fortunes.addAll(fortunes);

                    // Increase the skip value
                    skipVal+=1;
                }


                // Notify the recycler view adapter of a change in data
                adapter.notifyDataSetChanged();

                // We are no longer querying
                querying = false;

                pbProfileSearchLoc.setVisibility(View.INVISIBLE);
            }
        });
    }

}