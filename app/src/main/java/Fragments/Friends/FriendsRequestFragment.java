package Fragments.Friends;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.Friends.Friend_queue;
import com.example.metau_capstone.Friends.FriendsRequestAdapter;
import com.example.metau_capstone.R;
import com.example.metau_capstone.offlineHelpers;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Fragments.Main.FriendsFragment;

/**
 * This class is used to manage the Friends Request Fragment
 */
public class FriendsRequestFragment extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 10;

    private static final String TAG = "FriendsRequestFragment";

    // Elements in the view
    TextView tvNoRequests;
    RecyclerView rvFriendRequests;
    ProgressBar pbFriends;

    // Recycler view stuff
    private SwipeRefreshLayout swipeContainer;
    LinearLayoutManager layoutManager;
    FriendsRequestAdapter adapter;

    // List of friends (users) for the recycler view
    List<ParseUser> Requests;

    // Current user logged in
    ParseUser user;


    public FriendsRequestFragment() {
        // Required empty public constructor
    }

    public static FriendsRequestFragment newInstance() {
        return new FriendsRequestFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If the user is offline, show a message
        if (!(new offlineHelpers()).isNetworkAvailable(requireContext())) {
            view.findViewById(R.id.tv_notOnine_request).setVisibility(View.VISIBLE);
            return;
        }


        // Get the elements
        rvFriendRequests = view.findViewById(R.id.rvFriendRequests);
        tvNoRequests = view.findViewById(R.id.tvNoRequests);
        pbFriends = requireActivity().findViewById(R.id.pbFriends);
        swipeContainer = view.findViewById(R.id.srlRequests);



        // Get the current user
        user = ParseUser.getCurrentUser();

        // Initialize the requests list
        Requests = new ArrayList<>();

        // Get the requests
        getRequests();

        // Handle back button presses by going to the home fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                pbFriends.setVisibility(View.INVISIBLE);

                // Change the fragment to the list fragment
                ((FriendsFragment)getParentFragment()).changeFrag(0);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        // Handle swipe reloads
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Clear out all old requests
                Requests = new ArrayList<>();
                skipVal = 0;
                adapter.requests = new ArrayList<>();

                // Get all new requests
                updateRequests();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    /**
     ** Upon refresh, handle any updates found in the Friend_queue class where
     ** this user is in "user". This ensures the user can update any
     ** friend requests when they log in.
     */
    public void updateRequests() {
        ParseUser curUser = ParseUser.getCurrentUser();
        ParseQuery<Friend_queue> q = new ParseQuery<Friend_queue>(Friend_queue.class);
        q.whereEqualTo("user", curUser);
        q.orderByAscending("createdAt");
        q.findInBackground(new FindCallback<Friend_queue>() {
            @Override
            public void done(List<Friend_queue> new_friends, ParseException e) {
                // If there are no new friends, skip this function
                if (new_friends == null || new_friends.size() == 0) {
                    // When the new users are saved, load them in
                    getRequests();
                    return;
                }

                // Get the relation and add or remove all friends to it
                ParseRelation<ParseUser> friends = curUser.getRelation("friends");
                ParseRelation<ParseUser> requests = curUser.getRelation("friend_requests");
                ParseRelation<ParseUser> sent_requests = curUser.getRelation("sent_requests");
                for (Friend_queue f : new_friends) {
                    if (Objects.equals(f.getMode(), "add")) {
                        friends.add(f.getFriend());
                    }
                    else if (Objects.equals(f.getMode(), "request")) {
                        requests.add(f.getFriend());
                    }
                    else if (Objects.equals(f.getMode(), "accept")) {
                        sent_requests.remove(f.getFriend());
                    }
                    else if (Objects.equals(f.getMode(), "rejected")) {
                        sent_requests.remove(f.getFriend());
                    }
                    else if (Objects.equals(f.getMode(), "remove")) {
                        friends.remove(f.getFriend());
                    }
                    else if (Objects.equals(f.getMode(), "remove_request")) {
                        requests.remove(f.getFriend());
                    }
                    f.deleteInBackground();
                }

                // If any users were updated, update the friends list
                // as well.
                for (Fragment frag : getParentFragmentManager().getFragments()) {
                    if (frag.getClass() == FriendsListFragment.class) {
                        getParentFragmentManager().beginTransaction().remove(frag).commit();
                        break;
                    }
                }

                // Save the new friends to the user
                curUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Could not save new friends", e);
                        }
                        else {
                            Log.i(TAG, "Saved new friends");
                        }

                        // When the new users are saved, load them in
                        getRequests();
                    }
                });

            }
        });
    }


    // Load in more friend requests
    private void getRequests() {
        swipeContainer.setRefreshing(false);

        // Get the query to query the requests
        ParseRelation<ParseUser> requests = user.getRelation("friend_requests");
        ParseQuery<ParseUser> query = requests.getQuery();


        // Skip some fortunes that have already been loaded
        query.setSkip(skipVal*loadRate);

        // Set the limit to loadRate
        query.setLimit(loadRate);


        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> requests, ParseException e) {
                // Check if there was an exception
                if (e != null) {
                    Log.e(TAG, "Unable to load requests", e);
                    return;
                }

                // If the user has friends, setup the recycler view

                // Get all friends from the list and load them in
                Requests.addAll(requests);

                // Setup the recycler view if it hasn't been setup already
                if (rvFriendRequests.getAdapter() == null) {

                    // When the fortunes have been loaded, setup the recycler view -->
                    // Bind the adapter to the recycler view
                    adapter = new FriendsRequestAdapter(Requests, getContext(), requireActivity().getSupportFragmentManager());
                    rvFriendRequests.setAdapter(adapter);

                    // Configure the Recycler View: Layout Manager
                    layoutManager = new LinearLayoutManager(getContext());
                    rvFriendRequests.setLayoutManager(layoutManager);

                    // Used for infinite scrolling
                    rvFriendRequests.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                        @Override
                        public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                            getRequests();
                        }
                    });
                }
                else {
                    // Notify the recycler view adapter of a change in data
                    adapter.requests = Requests;
                    adapter.notifyDataSetChanged();
                }

                // If the user has no friends, display a message
                if (requests.size() == 0 && Requests.size() == 0) {
                    tvNoRequests.setVisibility(View.VISIBLE);
                    return;
                }
                else {
                    tvNoRequests.setVisibility(View.INVISIBLE);
                }

                // Increase the skip value
                skipVal+=1;
            }
        });
    }
}