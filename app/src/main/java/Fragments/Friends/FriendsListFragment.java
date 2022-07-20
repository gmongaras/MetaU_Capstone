package Fragments.Friends;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import com.example.metau_capstone.Friends.FriendsAdapter;
import com.example.metau_capstone.R;
import com.example.metau_capstone.offlineHelpers;
import com.example.metau_capstone.translationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import Fragments.Main.HomeFragment_countdown;


/**
 * This class is used to manage the Friends List Fragment
 */
public class FriendsListFragment extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 10;

    private static final String TAG = "FriendsListFragment";

    // Elements in the view
    TextView tvNoFriends;
    TextView tv_notOnine_list;
    RecyclerView rvFriends;
    ProgressBar pbFriends;

    // Recycler view stuff
    private SwipeRefreshLayout swipeContainer;
    LinearLayoutManager layoutManager;
    FriendsAdapter adapter;

    // List of friends (users) for the recycler view
    List<ParseUser> Friends;

    // Current user logged in
    ParseUser user;

    translationManager manager;

    public FriendsListFragment() {
        // Required empty public constructor
    }

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the manager for translation
        manager = new translationManager(ParseUser.getCurrentUser().getString("lang"));

        // Get the elements
        rvFriends = view.findViewById(R.id.rvFriendRequests);
        tvNoFriends = view.findViewById(R.id.tvNoFriends);
        tv_notOnine_list = view.findViewById(R.id.tv_notOnine_list);
        pbFriends = requireActivity().findViewById(R.id.pbFriends);
        swipeContainer = view.findViewById(R.id.srlList);

        // Translate any text in the views
        manager.addText(tvNoFriends, R.string.noFriends, requireContext());
        manager.addText(tv_notOnine_list, R.string.offlineFriends, requireContext());

        // If the user is offline, show a message
        if (!(new offlineHelpers()).isNetworkAvailable(requireContext())) {
            view.findViewById(R.id.tv_notOnine_list).setVisibility(View.VISIBLE);
            return;
        }



        // Get the current user
        user = ParseUser.getCurrentUser();

        // Initialize the friends list
        skipVal = 0;
        Friends = new ArrayList<>();

        // Get the friends
        getFriends();

        // Handle back button presses by going to the home fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                pbFriends.setVisibility(View.INVISIBLE);

                // Setup the fragment switch
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                // Go back to the Profile fragment
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                HomeFragment_countdown homeFragment = HomeFragment_countdown.newInstance();

                // Add back the profile fragment
                ft.replace(R.id.flContainer, homeFragment);
                ft.commit();

                ((BottomNavigationView)getActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        // Handle swipe reloads
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Clear out all old requests
                Friends = new ArrayList<>();
                skipVal = 0;
                adapter.friends = new ArrayList<>();

                // Get all new friends
                updateFriends();
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
    public void updateFriends() {
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
                    getFriends();
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
                        getFriends();
                    }
                });

            }
        });
    }



    // Load in more friends
    private void getFriends() {
        pbFriends.setVisibility(View.VISIBLE);
        swipeContainer.setRefreshing(false);

        // Get the query to query the friends
        ParseRelation<ParseUser> friends = user.getRelation("friends");
        ParseQuery<ParseUser> query = friends.getQuery();


        // Skip some fortunes that have already been loaded
        query.setSkip(skipVal*loadRate);

        // Set the limit to loadRate
        query.setLimit(loadRate);


        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                // Check if there was an exception
                if (e != null) {
                    Log.e(TAG, "Unable to load friends", e);
                    pbFriends.setVisibility(View.INVISIBLE);
                    return;
                }

                // If the user has friends, setup the recycler view

                // Get all friends from the list and load them in
                Friends.addAll(friends);

                // Setup the recycler view if it hasn't been setup already
                if (rvFriends.getAdapter() == null) {

                    // When the fortunes have been loaded, setup the recycler view -->
                    // Bind the adapter to the recycler view
                    adapter = new FriendsAdapter(Friends, getContext(), requireActivity().getSupportFragmentManager());
                    rvFriends.setAdapter(adapter);

                    // Configure the Recycler View: Layout Manager
                    layoutManager = new LinearLayoutManager(getContext());
                    rvFriends.setLayoutManager(layoutManager);

                    // If there are no friends, don't load more
                    if (friends.size() != 0) {
                        // Used for infinite scrolling
                        rvFriends.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                            @Override
                            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                                getFriends();
                            }
                        });
                    }
                }
                else {
                    // Notify the recycler view adapter of a change in data
                    adapter.friends = Friends;
                    adapter.notifyDataSetChanged();
                }

                // If the user has no friends, display a message
                if (friends.size() == 0 && Friends.size() == 0) {
                    tvNoFriends.setVisibility(View.VISIBLE);
                    pbFriends.setVisibility(View.INVISIBLE);
                    return;
                }
                else {
                    tvNoFriends.setVisibility(View.INVISIBLE);
                    pbFriends.setVisibility(View.INVISIBLE);
                }

                // Increase the skip value
                skipVal+=1;
            }
        });
    }
}