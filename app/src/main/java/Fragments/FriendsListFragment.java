package Fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.FriendsAdapter;
import com.example.metau_capstone.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsListFragment extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 10;

    private static final String TAG = "FriendsListFragment";

    // Elements in the view
    TextView tvNoFriends;
    RecyclerView rvFriends;
    ProgressBar pbFriends;

    // Recycler view stuff
    LinearLayoutManager layoutManager;
    FriendsAdapter adapter;

    // List of friends (users) for the recycler view
    List<ParseUser> Friends;

    // Current user logged in
    ParseUser user;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsListFragment newInstance(String param1, String param2) {
        FriendsListFragment fragment = new FriendsListFragment();
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
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the elements
        rvFriends = view.findViewById(R.id.rvFriendRequests);
        tvNoFriends = view.findViewById(R.id.tvNoFriends);
        pbFriends = requireActivity().findViewById(R.id.pbFriends);



        // Get the current user
        user = ParseUser.getCurrentUser();

        // Initialize the friends list
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
                HomeFragment_countdown homeFragment = HomeFragment_countdown.newInstance("a", "b");

                // Add back the profile fragment
                ft.replace(R.id.flContainer, homeFragment);
                ft.commit();

                ((BottomNavigationView)getActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }



    private void getFriends() {
        pbFriends.setVisibility(View.VISIBLE);

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
                    return;
                }

                // If the user has no friends, display a message and don't setup the
                // recycler view
                if (friends.size() == 0 && Friends.size() == 0) {
                    tvNoFriends.setVisibility(View.VISIBLE);
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

                    // Used for infinite scrolling
                    rvFriends.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                        @Override
                        public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                            getFriends();
                        }
                    });
                }
                else {
                    // Notify the recycler view adapter of a change in data
                    adapter.notifyDataSetChanged();
                }

                pbFriends.setVisibility(View.INVISIBLE);

                // Increase the skip value
                skipVal+=1;
            }
        });
    }
}