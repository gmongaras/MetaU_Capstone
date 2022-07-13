package com.example.metau_capstone;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.metau_capstone.Friends.Friend_queue;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Objects;

import Fragments.Main.HomeFragment_fortune;


/**
 ** This class is used to manage the Login Activity (activity_login.xml)
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    // Objects in the activity
    EditText etUsername;
    EditText etPassword;
    Button btnLogin;
    Button btnRegisterGoTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_DarkMode); //when dark mode is enabled, we use the dark theme
        } else {
            setTheme(R.style.Theme_LightMode);  //default app theme
        }
        setContentView(R.layout.activity_login);

        try {
            getSupportActionBar().hide();
        }
        catch (Exception e) {
            Log.i(TAG, "No action bar to hide", e);
        }

        // If the user is already logged in, go straight to the main activity
        if (ParseUser.getCurrentUser() != null) {
            // Go to the main activity
            goMainActivity();
        }

        // Get the attributes
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterGoTo = findViewById(R.id.btnRegisterGoTo);

        // Create an onclick listener for the Login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick login button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        // Create an onClick listener for the Register Button
        btnRegisterGoTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick register button");

                // Go to the registration page
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        // Handle back button presses so the user doesn't go to the wrong
        // page after they logged out
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }




    // Login a user to the app given the username and password
    private void loginUser(String username, String password) {
        // Disable the login button
        btnLogin.setClickable(false);

        // Display a login message
        Log.i(TAG, "Attempting to login user " + username);
        Toast.makeText(LoginActivity.this, "Attempting to login user...", Toast.LENGTH_SHORT).show();

        // Log the user in using our Parse backend
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                // Enable the login button
                btnLogin.setClickable(true);

                // If the is a problem, then e will not be null.
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                // If e is null, then there is no error.
                // In this case, the user successfully logged in and
                // we should take them to the main page
                goMainActivity();
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     ** Upon login, handle any updates found in the Friend_queue class where
     ** this user is in "user". This ensures the user can update any
     ** friend requests when they log in.
     */
    public void addFriends() {
        ParseUser curUser = ParseUser.getCurrentUser();
        ParseQuery<Friend_queue> q = new ParseQuery<Friend_queue>(Friend_queue.class);
        q.whereEqualTo("user", curUser);
        q.orderByAscending("createdAt");
        q.findInBackground(new FindCallback<Friend_queue>() {
            @Override
            public void done(List<Friend_queue> new_friends, ParseException e) {
                // If there are no new friends, skip this function
                if (new_friends == null || new_friends.size() == 0) {
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
                    }
                });

            }
        });
    }




    // Go to the main activity when the user has logged in
    private void goMainActivity() {
        // Create a database when the user logs in and save it to
        // the user's phone for offline loading, if the user is online
        if (new offlineHelpers().isNetworkAvailable(this)) {
            (new offlineHelpers()).createDatabase(this);
        }

        // When the user logs in, check if they have any new friends and add them
        addFriends();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}