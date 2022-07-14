package Fragments.Profile;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.Profile.ProfileAdapter;
import com.example.metau_capstone.Profile.ProfileAdapterOffline;
import com.example.metau_capstone.R;
import com.example.metau_capstone.offlineDB.FortuneDB;
import com.example.metau_capstone.offlineDB.FortuneDao;
import com.example.metau_capstone.offlineDB.databaseApp;
import com.example.metau_capstone.offlineHelpers;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage the Profile Liked Fragment
 */
public class ProfileLikedFragment extends Fragment {

    private static final String TAG = "ProfileLikedFragment";

    // Elements in the views
    RecyclerView rvProfileLiked;
    TextView tvNoAccess_liked;
    TextView tvBlockedLiked1;
    TextView tvBlockedLiked2;
    TextView tvNoLiked;

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

    // In offline mode, have the first batch of fortunes been loaded?
    boolean loadedSome;

    public ProfileLikedFragment() {
        // Required empty public constructor
    }

    /**
     * Create the fragment given information to load in
     * @param user The user to load into this fragment
     * @param mode The mode to load the user in
     * @return The newly created fragment
     */
    public static ProfileLikedFragment newInstance(ParseUser user, int mode) {
        ProfileLikedFragment fragment = new ProfileLikedFragment();
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
        return inflater.inflate(R.layout.fragment_profile_liked, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the user is offline, handle offline fortune loading
        if (!new offlineHelpers().isNetworkAvailable(requireContext())) {
            loadOffline(view);
            return;
        }


        tvNoLiked = view.findViewById(R.id.tvNoLiked);

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
            tvBlockedLiked1 = view.findViewById(R.id.tvBlockedLiked2);
            tvBlockedLiked1.setVisibility(View.VISIBLE);
        }

        // If the mode is 4 (logged in user block by other user), the
        // logged in user cannot access the other users info
        else if (mode == 4) {
            load = false;
            tvBlockedLiked2 = view.findViewById(R.id.tvBlockedLiked2);
            tvBlockedLiked2.setVisibility(View.VISIBLE);
        }

        // Load the fortunes if the user has access to do so
        if (load == true) {

            // Get the elements
            rvProfileLiked = view.findViewById(R.id.rvProfileLiked);

            // Initialize the fortunes
            Fortunes = new ArrayList<>();

            // Load in the liked fortunes
            queryLiked();
        }
        // If the user doesn't have access to the fortunes, display a message
        else {
            if (mode != 3 && mode != 4) {
                tvNoAccess_liked = view.findViewById(R.id.tvNoAccess_liked);
                tvNoAccess_liked.setVisibility(View.VISIBLE);
            }
        }
    }


    // Load the liked fortunes in offline mode
    private void loadOffline(View view) {
        loadedSome = false;

        // Get the elements
        rvProfileLiked = view.findViewById(R.id.rvProfileLiked);

        // Initialize the list
        FortunesDB = new ArrayList<>();

        // Initialize the adapter
        try {
            adapterOffline = new ProfileAdapterOffline(FortunesDB, user, getContext(), requireActivity().getSupportFragmentManager(), mode);
        }
        catch (Exception e2) {
            return;
        }
        rvProfileLiked.setAdapter(adapterOffline);

        // Configure the Recycler View Layout Manager
        layoutManager = new LinearLayoutManager(getContext());
        rvProfileLiked.setLayoutManager(layoutManager);

        // Load in the fortunes
        loadLikedOffline();
    }


    // Get some fortunes from the database
    private void loadLikedOffline() {
        // Use a background thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Get the database DOA
                final FortuneDao fortuneDoa = ((databaseApp) requireContext().getApplicationContext()).getDatabase().fortuneDAO();

                // Get the fortunes from the database and load
                // them in
                FortunesDB = fortuneDoa.getLiked(Integer.MAX_VALUE);

                // Notify the adapter
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterOffline.fortunes = FortunesDB;
                        adapterOffline.notifyDataSetChanged();

                        // If the fortune count is 0, show a text prompt
                        if (!loadedSome && FortunesDB.size() == 0) {
                            getView().findViewById(R.id.tvNoLiked).setVisibility(View.VISIBLE);
                        }

                        loadedSome = true;
                    }
                });
            }
        });
    }


    // Get fortunes the user has liked
    private void queryLiked() {
        // Get the liked relation and the query for it
        ParseRelation<Fortune> rel = user.getRelation("liked");
        ParseQuery<Fortune> query = rel.getQuery();
        query.orderByDescending("createdAt");

        // Query for all liked fortunes
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
                if (rvProfileLiked.getAdapter() == null) {

                    // If the list is empty, show some text stating that
                    if (objects.size() == 0) {
                        tvNoLiked.setVisibility(View.VISIBLE);
                    }
                    else {
                        tvNoLiked.setVisibility(View.INVISIBLE);
                    }

                    // When the fortunes have been loaded, setup the recycler view -->
                    // Bind the adapter to the recycler view
                    adapter = new ProfileAdapter(Fortunes, user, getContext(), requireActivity().getSupportFragmentManager(), mode);
                    rvProfileLiked.setAdapter(adapter);

                    // Configure the Recycler View: Layout Manager
                    layoutManager = new LinearLayoutManager(getContext());
                    rvProfileLiked.setLayoutManager(layoutManager);
                }
                else {
                    // Notify the recycler view adapter of a change in data
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }
}