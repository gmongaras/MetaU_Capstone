package Fragments.Profile;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.Profile.ProfileAdapter;
import com.example.metau_capstone.Profile.ProfileAdapterOffline;
import com.example.metau_capstone.R;
import com.example.metau_capstone.offlineDB.FortuneDB;
import com.example.metau_capstone.offlineDB.FortuneDoa;
import com.example.metau_capstone.offlineDB.databaseApp;
import com.example.metau_capstone.offlineHelpers;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage the Profile List Fragment
 */
public class ProfileList extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 20;

    private static final String TAG = "ProfileList";

    // Elements in the views
    RecyclerView rvProfileList;
    TextView tvNoAccess;
    TextView tvBlocked1;
    TextView tvBlocked2;
    TextView tvNoFortunes;

    // Recycler view stuff
    LinearLayoutManager layoutManager;
    ProfileAdapter adapter;
    ProfileAdapterOffline adapterOffline;

    // Mode in which the profile is in
    // 0 - Current user
    // 1 - Friend
    // 2 - Other user
    // 3 - Other user blocked by logged in user
    // 4 - Logged in user blocked by other user
    private static final String ARG_INT = "mode";
    private int mode;

    // List of fortunes for the recycler view
    List<Fortune> Fortunes;
    List<FortuneDB> FortunesDB;

    // The user to load data for
    private static final String ARG_USER = "user";
    private ParseUser user;

    public ProfileList() {
        // Required empty public constructor
    }

    /**
     * Create the fragment given information to load in
     * @param user The user to load into this fragment
     * @param mode The mode to load the user in
     * @return The newly created fragment
     */
    public static ProfileList newInstance(ParseUser user, int mode) {
        ProfileList fragment = new ProfileList();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        args.putInt(ARG_INT, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(ARG_USER);
            mode = getArguments().getInt(ARG_INT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the user is offline, handle offline fortune loading
        if (!new offlineHelpers().isNetworkAvailable(requireContext())) {
            loadOffline(view);
            return;
        }

        tvNoFortunes = view.findViewById(R.id.tvNoFortunes);

        // Should the fortunes be loaded
        boolean load = true;

        // If the mode is 1 (friend), check if the user has access
        if (mode == 1) {
            // If the user doesn't allow friends to see fortunes, set
            // load to false
            if (user.getBoolean("showFortunesFriends") == false) {
                load = false;
            }
        }
        
        // If the mode is 2 (other user), check if the user has access
        else if (mode == 2) {
            // If the user doesn't allow other users to see fortunes, set
            // load to false
            if (user.getBoolean("showFortunesUsers") == false) {
                load = false;
            }
        }

        // If the mode is 3 (this user blocked by logged in user), the
        // logged in user cannot access the other users info
        else if (mode == 3) {
            load = false;
            tvBlocked1 = view.findViewById(R.id.tvBlocked1);
            tvBlocked1.setVisibility(View.VISIBLE);
        }

        // If the mode is 4 (logged in user block by other user), the
        // logged in user cannot access the other users info
        else if (mode == 4) {
            load = false;
            tvBlocked2 = view.findViewById(R.id.tvBlocked2);
            tvBlocked2.setVisibility(View.VISIBLE);
        }

        // Load the fortunes if the user has access to do so
        if (load == true) {
            skipVal = 0;

            // Get the elements
            rvProfileList = view.findViewById(R.id.rvProfileLiked);

            // Initialize the fortunes
            Fortunes = new ArrayList<>();

            // Load in the fortunes
            queryFortunes();
        }
        // If the user doesn't have access to the fortunes, display a message
        else {
            if (mode != 3 && mode != 4) {
                tvNoAccess = view.findViewById(R.id.tvNoAccess);
                tvNoAccess.setVisibility(View.VISIBLE);
            }
        }
    }



    // Load the user profile in offline mode
    private void loadOffline(View view) {
        // The skip value is initially 0
        skipVal = 1;

        // Get the elements
        rvProfileList = view.findViewById(R.id.rvProfileLiked);

        // Initialize the list
        FortunesDB = new ArrayList<>();

        // Initialize the adapter
        try {
            adapterOffline = new ProfileAdapterOffline(FortunesDB, user, getContext(), requireActivity().getSupportFragmentManager(), mode);
        }
        catch (Exception e2) {
            return;
        }
        rvProfileList.setAdapter(adapterOffline);

        // Configure the Recycler View Layout Manager
        layoutManager = new LinearLayoutManager(getContext());
        rvProfileList.setLayoutManager(layoutManager);

        // Used for infinite scrolling
        rvProfileList.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadFortunesOffline();
            }
        });

        // Load in the fortunes
        loadFortunesOffline();
    }


    // Get some fortunes from the database
    private void loadFortunesOffline() {
        // Use a background thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Get the database DOA
                final FortuneDoa fortuneDoa = ((databaseApp) requireContext().getApplicationContext()).getDatabase().fortuneDOA();

                // Get the fortunes from the database and load
                // them in
                FortunesDB = fortuneDoa.getFortunes(skipVal*loadRate);

                // Notify the adapter
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterOffline.fortunes = FortunesDB;
                        adapterOffline.notifyDataSetChanged();
                    }
                });

                // Increase the skip count
                skipVal++;
            }
        });
    }



    // Get fortunes the user owns
    private void queryFortunes() {
        // Specify which class to query
        ParseQuery<Fortune> query = ParseQuery.getQuery(Fortune.class);

        // Include data from the user table
        query.include(Fortune.KEY_USER);

        // Get only this user's fortunes
        query.whereEqualTo("user", user);

        // Have the newest fortunes on top
        query.orderByDescending(Fortune.KEY_TIME_CREATED);

        // Skip some fortunes that have already been loaded
        query.setSkip(skipVal*loadRate);

        // Set the limit to loadRate
        query.setLimit(loadRate);

        // Find all the fortunes the user owns
        query.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> objects, ParseException e) {
                // If an error occurred, log an error
                if (e != null) {
                    Log.e(TAG, "Issue retrieving all fortunes", e);
                    return;
                }

                // Store all new fortunes in the Fortunes list
                Fortunes.addAll(objects);

                // Setup the recycler view if it isn't setup
                if (rvProfileList.getAdapter() == null) {

                    // If the list is empty, show some text stating that
                    if (objects.size() == 0) {
                        tvNoFortunes.setVisibility(View.VISIBLE);
                        return;
                    }
                    else {
                        tvNoFortunes.setVisibility(View.INVISIBLE);
                    }

                    // When the fortunes have been loaded, setup the recycler view -->
                    // Bind the adapter to the recycler view
                    try {
                        adapter = new ProfileAdapter(Fortunes, user, getContext(), requireActivity().getSupportFragmentManager(), mode);
                    }
                    catch (Exception e2) {
                        return;
                    }
                    rvProfileList.setAdapter(adapter);

                    // Configure the Recycler View: Layout Manager
                    layoutManager = new LinearLayoutManager(getContext());
                    rvProfileList.setLayoutManager(layoutManager);

                    // Used for infinite scrolling
                    rvProfileList.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                        @Override
                        public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                            queryFortunes();
                        }
                    });
                }
                else {
                    // Notify the recycler view adapter of a change in data
                    adapter.notifyDataSetChanged();
                }

                // Increase the skip value
                skipVal+=1;
            }
        });
    }
}