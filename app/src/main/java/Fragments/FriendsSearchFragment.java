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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.FriendsSearchAdapter;
import com.example.metau_capstone.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsSearchFragment extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 10;

    private static final String TAG = "FriendsSearchFragment";

    // Elements in the view
    TextView tvAlert;
    SearchView svFriends_search;
    TextView tvSearchFriends_search;
    RecyclerView rvFriends_search;
    TextView tvPrompt;
    ProgressBar pbFriends;

    // Recycler view stuff
    LinearLayoutManager layoutManager;
    FriendsSearchAdapter adapter;

    // List of users for the recycler view
    List<ParseUser> Users;

    // Current user logged in
    ParseUser user;

    // True if users are being queried, false otherwise
    boolean querying = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_QUERY_TEXT = "queryText";

    // TODO: Rename and change types of parameters
    private String queryText;

    public FriendsSearchFragment() {
        // Required empty public constructor
    }

    public static FriendsSearchFragment newInstance() {
        FriendsSearchFragment fragment = new FriendsSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //queryText = getArguments().getString(ARG_QUERY_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        skipVal = 0;

        // Get the elements in the view
        tvAlert = view.findViewById(R.id.tvAlert);
        svFriends_search = view.findViewById(R.id.svProfileSearch);
        int id = svFriends_search.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        tvSearchFriends_search = ((TextView)svFriends_search.findViewById(id));
        tvSearchFriends_search.setTextColor(getResources().getColor(R.color.black));
        tvSearchFriends_search.setHintTextColor(getResources().getColor(R.color.black));
        rvFriends_search = view.findViewById(R.id.rvFriends_search);
        tvPrompt = view.findViewById(R.id.tvPrompt);
        pbFriends = requireActivity().findViewById(R.id.pbFriends);

        // Get the current user
        user = ParseUser.getCurrentUser();

        // Initialize the Users list
        Users = new ArrayList<>();

        // Get the Users using the query text
        //queryUsers(" ");




        // When the users have been loaded, setup the recycler view -->
        // Bind the adapter to the recycler view
        adapter = new FriendsSearchAdapter(Users, getContext(), requireActivity().getSupportFragmentManager());
        rvFriends_search.setAdapter(adapter);

        // Configure the Recycler View: Layout Manager
        layoutManager = new LinearLayoutManager(getContext());
        rvFriends_search.setLayoutManager(layoutManager);

        // Used for infinite scrolling
        rvFriends_search.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Set the querying flag to true
                querying = true;

                queryUsers(queryText);
            }
        });






        // A a listener to look for a user entering a query into the search bar
        tvSearchFriends_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If friends are already being queried, do nothing
                if (querying == true) {
                    return false;
                }

                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == 66) {
                    // Get the text from the text box
                    String text = tvSearchFriends_search.getText().toString();

                    // If the text is blank, don't do anything
                    if (text.length() == 0) {
                        return false;
                    }

                    // If the prompt text is visible, make it invisible
                    tvPrompt.setVisibility(View.INVISIBLE);

                    // Set the querying state to true
                    querying = true;

                    // Reset the skip value
                    skipVal = 0;

                    // Reset the list
                    Users.clear();
                    adapter.notifyDataSetChanged();

                    // Query for the username
                    queryUsers(text);

                    return true;
                }

                return false;
            }
        });

        // Handle back button presses by going to the friends fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                pbFriends.setVisibility(View.INVISIBLE);

                // Setup the fragment switch
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                // Go back to the Friends fragment
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                FriendsListFragment listFragment = FriendsListFragment.newInstance("a", "b");

                // Add back the friends fragment
                ft.replace(R.id.fragmentFriends, listFragment);
                ft.commit();

                //((BottomNavigationView)getActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }


    // Query users given a username to query for
    private void queryUsers(String queryText) {
        // If the query is null, do nothing
        if (queryText == null) {
            querying = false;
            return;
        }

        pbFriends.setVisibility(View.VISIBLE);


        // Create a new query for users
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);

        // Search for the given username
        query.whereEqualTo("username", queryText);
        query.whereStartsWith("username", queryText.substring(0, 1));
        query.whereContains("username", queryText);
        query.whereContains("username", queryText.trim());
        if (queryText.trim().length() > 0) {
            query.whereStartsWith("username", queryText.trim().substring(0, 1));
        }

        //Collection<String> c = new ArrayList<>();
        //c.add(queryText);
        //c.add(queryText.substring(0, 1));
        //query.
        //query.whereContainedIn("username", c);

        // Search for ids not equal to this user
        query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());

        // Skip some posts
        query.setSkip(skipVal*loadRate);

        // Set the limit to loadRate posts
        query.setLimit(loadRate);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                // Check if there was an exception
                if (e != null) {
                    Log.e(TAG, "Unable to load users", e);
                    pbFriends.setVisibility(View.INVISIBLE);
                    return;
                }

                // If no users were found, display an alert message
                if (users.size() == 0 && Users.size() == 0) {
                    tvAlert.setVisibility(View.VISIBLE);
                }
                else {
                    tvAlert.setVisibility(View.INVISIBLE);

                    // Get all friends from the list and load them in
                    Users.addAll(users);

                    // Increase the skip value
                    skipVal+=1;
                }


                // Notify the recycler view adapter of a change in data
                adapter.notifyDataSetChanged();

                // We are no longer querying
                querying = false;

                pbFriends.setVisibility(View.INVISIBLE);
            }
        });
    }
}