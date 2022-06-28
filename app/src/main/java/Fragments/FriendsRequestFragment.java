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
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.FriendsAdapter;
import com.example.metau_capstone.FriendsRequestAdapter;
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
 * Use the {@link FriendsRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsRequestFragment extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 10;

    private static final String TAG = "FriendsListFragment";

    // Elements in the view
    TextView tvNoRequests;
    RecyclerView rvFriendRequests;
    ProgressBar pbFriends;

    // Recycler view stuff
    LinearLayoutManager layoutManager;
    FriendsRequestAdapter adapter;

    // List of friends (users) for the recycler view
    List<ParseUser> Requests;

    // Current user logged in
    ParseUser user;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsRequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsRequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsRequestFragment newInstance(String param1, String param2) {
        FriendsRequestFragment fragment = new FriendsRequestFragment();
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
        return inflater.inflate(R.layout.fragment_friends_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Get the elements
        rvFriendRequests = view.findViewById(R.id.rvFriendRequests);
        tvNoRequests = view.findViewById(R.id.tvNoRequests);
        pbFriends = requireActivity().findViewById(R.id.pbFriends);



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
//                // Setup the fragment switch
//                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
//
//                // Go back to the Friends fragment
//                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
//                FriendsListFragment listFragment = FriendsListFragment.newInstance("a", "b");
//
//                // Add back the friends fragment
//                ft.replace(R.id.fragmentFriends, listFragment);
//                ft.commit();

                // Change the fragment to the list fragment
                try {
                    ((FriendsFragment) getParentFragmentManager().getFragments().get(0)).changeFrag(0);
                }
                catch (Exception e) {
                    ((FriendsFragment) getParentFragmentManager().getFragments().get(1)).changeFrag(0);
                }

                //((BottomNavigationView)getActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }


    private void getRequests() {
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

                // If the user has no friends, display a message and don't setup the
                // recycler view
                if (requests.size() == 0 && Requests.size() == 0) {
                    tvNoRequests.setVisibility(View.VISIBLE);
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
                    adapter.notifyDataSetChanged();
                }

                // Increase the skip value
                skipVal+=1;
            }
        });
    }
}