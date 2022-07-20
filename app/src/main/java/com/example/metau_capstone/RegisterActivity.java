package com.example.metau_capstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.metau_capstone.offlineDB.databaseApp;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 ** This class is used to manage the Register Activity (activity_register.xml)
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // Elements in the view
    EditText etUsername_reg;
    EditText etPassword_reg;
    EditText etPassword2_reg;
    Spinner spLanguages;
    Button btnRegister;

    // Used to keep track of the indices
    Integer[] idx;
    Integer[] idx_rev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_LightMode);  //default app theme
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
        spLanguages = findViewById(R.id.spLanguages);
        btnRegister = findViewById(R.id.btnRegister);

        // Add options to the language dropdown

        // Get all the items in the dictionary
        String[] languages = new String[translationManager.langEncodings.size()];
        Object[] keys = translationManager.langEncodings.keySet().toArray();
        idx = new Integer[keys.length];
        for (int i = 0; i < idx.length; i++) {
            idx[i] = i;
        }

        // Sort the languages in alphabetical order and get the indices
        // for that order
        Arrays.sort(idx, new Comparator<Integer>() {
            @Override public int compare(final Integer o1, final Integer o2) {
                return ((String)keys[o1]).compareTo((String)keys[o2]);
            }
        });
        idx_rev = new Integer[idx.length];
        for (int i = 0; i < idx.length; i++) {
            idx_rev[idx[i]] = i;
        }

        // Add each item to the spinner while finding the index of English
        int engLoc = 0;
        for (int i = 0; i < languages.length; i++) {
            String s = (String)keys[idx[i]];
            if (Objects.equals(s, "English")) {
                engLoc = i;
            }
            languages[i] = s + " (" + translationManager.langTrans.get(s) + ")";
        }

        // Create the adapter for the spinner and add english as the starting language
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguages.setAdapter(adapter);
        spLanguages.setSelection(engLoc, false);

        // Set the spinner text color
        View v = spLanguages.getSelectedView();
        ((TextView)v).setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.black));
        spLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                View v = spLanguages.getSelectedView();
                ((TextView)view).setTextColor(ContextCompat.getColor(RegisterActivity.this, R.color.black));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

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
                    user.put("darkMode", false);
                    user.put("lang", translationManager.langEncodings.get(translationManager.langEncodings.keySet().toArray()[idx[(int)spLanguages.getSelectedItemId()]]));

                    // Hide all views
                    etUsername_reg.setVisibility(View.INVISIBLE); etUsername_reg.setClickable(false);
                    etPassword_reg.setVisibility(View.INVISIBLE); etPassword_reg.setClickable(false);
                    etPassword2_reg.setVisibility(View.INVISIBLE); etPassword2_reg.setClickable(false);
                    ConstraintLayout clRegister = findViewById(R.id.clRegister);
                    clRegister.setVisibility(View.INVISIBLE); clRegister.setClickable(false);
                    btnRegister.setVisibility(View.INVISIBLE); btnRegister.setClickable(false);
                    findViewById(R.id.pbRegister).setVisibility(View.VISIBLE);

                    // Sign the user up
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(RegisterActivity.this, "User Registered!", Toast.LENGTH_SHORT).show();

                                // Create a database when the user logs in and save it to
                                // the user's phone for offline loading, if the user is online
                                if (new offlineHelpers().isNetworkAvailable(RegisterActivity.this)) {
                                    (new offlineHelpers()).createDatabase(RegisterActivity.this);
                                }

                                // Create a new translation manager object
                                translationManager manager = new translationManager(ParseUser.getCurrentUser().getString("lang"), new translationManager.onLanguageSetListener() {
                                    @Override
                                    public void onLanguageSet() {
                                        // When the language is set, go to the main activity
                                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
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