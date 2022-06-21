package Fragments;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.FriendsAdapter;
import com.example.metau_capstone.ProfileAdapter;
import com.example.metau_capstone.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 10;

    private static final String TAG = "FriendsFragment";

    // Elements in the view
    TextView tvNoFriends;
    SearchView svFriends;
    TextView tvSearchFriends;
    RecyclerView rvFriends;

    // Recycler view stuff
    LinearLayoutManager layoutManager;
    FriendsAdapter adapter;

    // List of friends (users) for the recycler view
    List<ParseUser> Friends;

    // Current user logged in
    ParseUser user;

    // True if friends are being queried, false otherwise
    boolean querying = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
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
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        skipVal = 0;

        // Get the elements in the view
        tvNoFriends = view.findViewById(R.id.tvNoFriends);
        svFriends = view.findViewById(R.id.svFriends);
        int id = svFriends.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        tvSearchFriends = ((TextView)svFriends.findViewById(id));
        tvSearchFriends.setTextColor(getResources().getColor(R.color.black));
        tvSearchFriends.setHintTextColor(getResources().getColor(R.color.light_grey));
        rvFriends = view.findViewById(R.id.rvFriends);

        // Get the current user
        user = ParseUser.getCurrentUser();

        // Initialize the friends list
        Friends = new ArrayList<>();

        // Get the friends
        getFriends();

        // A a listener to look for a user entering a query into the search bar
        tvSearchFriends.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If friends are already being queried, do nothing
                if (querying == true) {
                    return false;
                }

                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == 66) {

                    // Get the text from the text box
                    String text = tvSearchFriends.getText().toString();

                    // If the text is blank, don't do anything
                    if (text.length() == 0) {
                        return false;
                    }

                    // Set the querying state to true
                    querying = true;

                    // Query for the username
                    queryUsernames(text);

                    return true;
                }

                return false;
            }
        });
    }



    // When querying, go to a new fragment
    private void queryUsernames(String username) {
        // Begin the querying process
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();

        // Get the search fragment
        SearchFriendsFragment fragment = SearchFriendsFragment.newInstance(username);

        // Change the fragment
        querying = false;
        ft.replace(R.id.flContainer, fragment);
        ft.commit();
    }



    private void getFriends() {
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

                // Increase the skip value
                skipVal+=1;
            }
        });
    }
}