package com.example.metau_capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.Map;

import Fragments.Main.FriendsFragment;
import Fragments.Main.HomeFragment_countdown;
import Fragments.Main.MapFragment;
import Fragments.Main.ProfileFragment;


/**
 ** This class is used to manage the Main Activity (activity_main.xml)
 */
public class MainActivity extends AppCompatActivity {

    // Elements in the application
    private BottomNavigationView bottomNav;
    private FrameLayout flContainer;

    private static final String TAG = "MainActivity";

    // Convert the id of a fragment to a pre-defined integer value
    private static final Map<Integer, Integer> fragId_to_val = Map.of(
            R.id.action_map, 0,
            R.id.action_home, 1,
            R.id.action_profile, 2,
            R.id.action_friends, 3
    );
    private static final Map<Integer, Integer> fragVal_to_Id = Map.of(
            0, R.id.action_map,
            1, R.id.action_home,
            2, R.id.action_profile,
            3, R.id.action_friends
    );

    // Stores the current fragment for animation purposes
    int curFrag = -1;

    // Position states for the touch gestures
    int posX = 0;
    int curPosX = 0;
    boolean hasMoved = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the elements
        bottomNav = findViewById(R.id.bottomNav);
        flContainer = findViewById(R.id.flContainer);

        try {
            getSupportActionBar().hide();
        }
        catch (Exception e) {
            Log.i(TAG, "No action bar to hide", e);
        }


        // Allow clicks on the Bottom Navigation View
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            // Note: a MenuItem will be one of the items in the menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Get the id of the item as an integer
                int val = fragId_to_val.get(item.getItemId());

                // Change the fragment
                changeFrag(val);

                return true;
            }
        });

        // Set the default fragment to load
        bottomNav.setSelectedItemId(R.id.action_home);




        // Handle left and right swipes
        flContainer.setClickable(true);
        flContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // If the touch event is one finger or more
                if (event.getPointerCount() >= 1) {
                    int action = event.getActionMasked();
                    int actionIndex = event.getActionIndex();

                    // If the action is a down action, save the X position
                    if (action == MotionEvent.ACTION_DOWN) {
                        posX = (int) event.getX(0);
                        hasMoved = false;
                    }

                    // If an action has already happened, don't allow another one
                    // to happen until there is no more touch
                    if (hasMoved == true) {
                        return true;
                    }

                    // If the action is a move action
                    else if (action == MotionEvent.ACTION_MOVE) {
                        // Get the current x position
                        curPosX = (int) event.getX(0);

                        // The difference shouldn't be too small
                        if (Math.abs(curPosX-posX) < 20) {
                            return true;
                        }

                        // If the current position is less than the past position,
                        // swipe right
                        if (curPosX < posX) {
                            changeFrag(Math.min(3, curFrag+1));
                        }
                        // If the current position is greater than the past position,
                        // swipe left
                        else {
                            changeFrag(Math.max(0, curFrag-1));
                        }

                        bottomNav.setSelectedItemId(fragVal_to_Id.get(curFrag));

                        hasMoved = true;
                    }

                    return true;
                }
                return false;
            }
        });
    }


    /**
     * Given a numerical fragment value, change the currently displayed fragment
     * with the given fragment
     * @param fragVal A numerical value representing which fragment to load
     */
    private void changeFrag(int fragVal) {
        // Start the fragment transition
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        switch (fragVal) {
            // If the menu item clicked is map
            case 0:
                // If the same fragment was clicked, do nothing
                if (curFrag == 0) {
                    break;
                }

                // Create the fragment with paramters
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                MapFragment fragmentMap = MapFragment.newInstance();

                curFrag = 0;

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentMap);
                ft.commit();

                break;

            // If the menu item clicked is home
            case 1:
                // If the same fragment was clicked, do nothing
                if (curFrag == 1) {
                    break;
                }

                // Animation
                if (curFrag < 1) {
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                }
                else {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                }

                // Create the fragment with paramters
                HomeFragment_countdown fragmentHome = HomeFragment_countdown.newInstance();

                curFrag = 1;

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentHome);
                ft.commit();

                break;


            // If the menu item clicked is profile
            case 2:
                // If the same fragment was clicked, do nothing
                if (curFrag == 2) {
                    break;
                }

                // Animation
                if (curFrag < 2) {
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                }
                else {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                }

                // Create the fragment with paramters
                ProfileFragment fragmentProfile = ProfileFragment.newInstance(ParseUser.getCurrentUser(), 0);

                curFrag = 2;

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentProfile);
                ft.commit();

                break;

            // If the menu item clicked is friends
            case 3:
                // If the same fragment was clicked, do nothing
                if (curFrag == 3) {
                    break;
                }

                // Create the fragment with paramters
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                FriendsFragment fragmentFriends = FriendsFragment.newInstance(0);

                curFrag = 3;

                // Change the fragment
                ft.replace(R.id.flContainer, fragmentFriends);
                ft.commit();

                break;
        }
    }
}