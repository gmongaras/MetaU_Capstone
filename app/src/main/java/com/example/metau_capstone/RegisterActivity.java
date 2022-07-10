package com.example.metau_capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 ** This class is used to manage the Register Activity (activity_register.xml)
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Elements in the view
    EditText etUsername_reg;
    EditText etPassword_reg;
    EditText etPassword2_reg;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        try {
            getSupportActionBar().hide();
        }
        catch (Exception e) {
            Log.i(TAG, "No action bar to hide", e);
        }

        // Get the elements
        etUsername_reg = findViewById(R.id.etUsername_reg);
        etPassword_reg = findViewById(R.id.etPassword_reg);
        etPassword2_reg = findViewById(R.id.etPassword2_reg);
        btnRegister = findViewById(R.id.btnRegister);

        // Put an onClick listener to the button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the button as unclickable
                btnRegister.setClickable(false);

                // Check if the input is valid
                boolean check = checkInput();

                // If the check failed, don't register the user
                if (!check) {
                    // Set the button as unclickable
                    btnRegister.setClickable(true);
                    return;
                }
                // If the check succeeded, register the user
                else {
                    Toast.makeText(RegisterActivity.this, "Registering User...", Toast.LENGTH_SHORT).show();

                    // Create a new user
                    ParseUser user = new ParseUser();

                    // Fill in the user details
                    user.setUsername(etUsername_reg.getText().toString());
                    user.setPassword(etPassword_reg.getText().toString());
                    user.put("showFortunesFriends", true);
                    user.put("showFortunesUsers", true);
                    user.put("showMapFriends", true);
                    user.put("showMapUsers", false);
                    user.put("pushNotifications", true);
                    user.put("useAI", true);
                    user.put("friendable", true);

                    // Sign the user up
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(RegisterActivity.this, "User Registered!", Toast.LENGTH_SHORT).show();

                                // Go to the main page
                                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                            else {
                                Log.e(TAG, "Unable to register user", e);
                                Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }



    // Checks if the input is valid and returns True if it is, False otherwise
    private boolean checkInput() {
        // Check if the username is blank
        if (etUsername_reg.getText().toString().equals("")) {
            Toast.makeText(this, "Username cannot be blank", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if the password is blank
        if (etPassword_reg.getText().toString().equals("")) {
            Toast.makeText(this, "Password cannot be blank", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if the passwords match
        if (!etPassword_reg.getText().toString().equals(etPassword2_reg.getText().toString())) {
            Toast.makeText(this, "Passwords must match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}