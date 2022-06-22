package com.example.metau_capstone;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        setContentView(R.layout.activity_login);

        // Get permission to get the user's location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request permission
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        // When the user logs in, check if they have any new friends and add them
        addFriends();

        // If the user is already logged in, go straight to the main activity
        if (ParseUser.getCurrentUser() != null) {
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




    private void addFriends() {
        ParseUser curUser = ParseUser.getCurrentUser();
        ParseQuery<Friend_queue> q = new ParseQuery<Friend_queue>(Friend_queue.class);
        q.whereEqualTo("user", curUser);
        q.findInBackground(new FindCallback<Friend_queue>() {
            @Override
            public void done(List<Friend_queue> new_friends, ParseException e) {
                // If there are no new friends, skip this function
                if (new_friends.size() == 0) {
                    return;
                }

                // Get the relation and add all friends to it
                //curUser.add("friends", new_friends.get(0).getFriend());
                ParseRelation<ParseUser> friends = curUser.getRelation("friends");
                for (Friend_queue f : new_friends) {
                    friends.add(f.getFriend());
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

                // Save the new friends
//                curUser.add("friends", c);
//                curUser.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e != null) {
//                            Log.e(TAG, "Error saving new friends", e);
//                        }
//                    }
//                });
            }
        });
    }




    // Go to the main activity when the user has logged in
    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}