package com.example.metau_capstone;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

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




    // Go to the main activity when the user has logged in
    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}