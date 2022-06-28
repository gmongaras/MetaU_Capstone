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

import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.ProfileAdapter;
import com.example.metau_capstone.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileSearchText#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileSearchText extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 20;

    private static final String TAG = "ProfileSearchText";

    // Elements in the views
    SearchView svProfileSearchText;
    TextView tvProfileSearchText;
    RecyclerView rvProfileSearchText;
    TextView tvNoResultsText;
    TextView tvSearchTextPrompt;
    ProgressBar pbProfileSearchText;

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
    String queryText;

    public ProfileSearchText() {
        // Required empty public constructor
    }

    public static ProfileSearchText newInstance(ParseUser user) {
        ProfileSearchText fragment = new ProfileSearchText();
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
        return inflater.inflate(R.layout.fragment_profile_search_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        skipVal = 0;

        // Get the elements
        svProfileSearchText = view.findViewById(R.id.svProfileSearchText);
        rvProfileSearchText = view.findViewById(R.id.rvProfileSearchText);
        tvNoResultsText = view.findViewById(R.id.tvNoResultsText);
        tvSearchTextPrompt = view.findViewById(R.id.tvSearchTextPrompt);
        pbProfileSearchText = view.findViewById(R.id.pbProfileSearchText);
        int id = svProfileSearchText.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        tvProfileSearchText = ((TextView)svProfileSearchText.findViewById(id));
        tvProfileSearchText.setTextColor(getResources().getColor(R.color.black));
        tvProfileSearchText.setHintTextColor(getResources().getColor(R.color.black));

        // Initialize the fortunes
        Fortunes = new ArrayList<>();

        // When the fortunes have been loaded, setup the recycler view -->
        // Bind the adapter to the recycler view
        adapter = new ProfileAdapter(Fortunes, user, getContext(), requireActivity().getSupportFragmentManager());
        rvProfileSearchText.setAdapter(adapter);

        // Configure the Recycler View: Layout Manager
        layoutManager = new LinearLayoutManager(getContext());
        rvProfileSearchText.setLayoutManager(layoutManager);

        // Used for infinite scrolling
        rvProfileSearchText.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Set the querying flag to true
                querying = true;

                queryFortunes(queryText);
            }
        });





        // A a listener to look for a user entering a query into the search bar
        tvProfileSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If fortunes are already being queried, do nothing
                if (querying == true) {
                    return false;
                }

                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == 66) {
                    // Get the text from the text box
                    String text = tvProfileSearchText.getText().toString();

                    // If the text is blank, don't do anything
                    if (text.length() == 0) {
                        return false;
                    }

                    // If the prompt text is visible, make it invisible
                    tvSearchTextPrompt.setVisibility(View.INVISIBLE);

                    // Set the querying state to true
                    querying = true;

                    // Reset the skip value
                    skipVal = 0;

                    // Reset the list
                    Fortunes.clear();
                    adapter.notifyDataSetChanged();

                    // Query the text
                    queryFortunes(text);

                    return true;
                }

                return false;
            }
        });

    }





    // Query fortunes given text to query for
    private void queryFortunes(String queryText) {
        // If the query is null, do nothing
        if (queryText == null) {
            querying = false;
            return;
        }

        pbProfileSearchText.setVisibility(View.VISIBLE);


        // Create a new set of queries
        List<ParseQuery<Fortune>> queries = new ArrayList<>();

        // Break up the query by spaces
        String[] queryWords = queryText.split(" ");

        // Clean the words
        String[] queryWordsClean = queryWords.clone();
        for (int i = 0; i < queryWordsClean.length; i++) {
            queryWordsClean[i] = queryWordsClean[i].trim();
        }

        // Search for the given text using multiple queries
        queries.add(ParseQuery.getQuery(Fortune.class).whereContains("message", queryText));
        for (String s : queryWords) {
            if (s.length() > 0) {
                queries.add(ParseQuery.getQuery(Fortune.class).whereContains("message", s));
            }
        }
        for (String s : queryWordsClean) {
            if (s.length() > 0) {
                queries.add(ParseQuery.getQuery(Fortune.class).whereContains("message", s));
            }
        }

        // Combine the queries into a single query
        ParseQuery<Fortune> mainQuery = ParseQuery.or(queries);

        // Search for ids equal to this user
        mainQuery.whereEqualTo("user", user);

        // Skip some posts
        mainQuery.setSkip(skipVal*loadRate);

        // Set the limit to loadRate posts
        mainQuery.setLimit(loadRate);

        mainQuery.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> fortunes, ParseException e) {
                // Check if there was an exception
                if (e != null) {
                    Log.e(TAG, "Unable to load fortunes", e);
                    pbProfileSearchText.setVisibility(View.INVISIBLE);
                    querying = false;
                    return;
                }

                // If no fortunes were found, display an alert message
                if (fortunes.size() == 0 && Fortunes.size() == 0) {
                    tvNoResultsText.setVisibility(View.VISIBLE);
                }
                else {
                    tvNoResultsText.setVisibility(View.INVISIBLE);

                    // Get all fortunes from the list and load them in
                    Fortunes.addAll(fortunes);

                    // Increase the skip value
                    skipVal+=1;
                }


                // Notify the recycler view adapter of a change in data
                adapter.notifyDataSetChanged();

                // We are no longer querying
                querying = false;

                pbProfileSearchText.setVisibility(View.INVISIBLE);
            }
        });
    }
}