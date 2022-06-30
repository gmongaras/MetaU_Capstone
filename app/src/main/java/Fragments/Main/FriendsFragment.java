package Fragments.Main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.metau_capstone.R;

import Fragments.Friends.FriendsListFragment;
import Fragments.Friends.FriendsRequestFragment;
import Fragments.Friends.FriendsSearchFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    // Elements in the fragment
    Button btnFriends_F;
    Button btnRequests_F;
    Button btnSearch_F;
    FrameLayout fragmentFriends;

    // The current fragment in view
    int curFrag = -1;

    // Position states for the touch gestures
    int posX = 0;
    int curPosX = 0;
    boolean hasMoved = false;

    public FriendsFragment() {
        // Required empty public constructor
    }

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Get the elements in the view
        btnFriends_F = view.findViewById(R.id.btnFriends_F);
        btnRequests_F = view.findViewById(R.id.btnRequests_F);
        btnSearch_F = view.findViewById(R.id.btnSearch_F);
        fragmentFriends = view.findViewById(R.id.fragmentFriends);



        // When the friends button is pressed, go to the friends fragment
        btnFriends_F.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrag(0);
            }
        });

        // When the requests button is pressed, go to the requests tab
        btnRequests_F.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrag(1);
            }
        });

        // When the search button is pressed, go to the search tab
        btnSearch_F.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrag(2);
            }
        });

        // Load in the initial fragment
        changeFrag(0);



        // Handle left and right swipes
        fragmentFriends.setClickable(true);
        fragmentFriends.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleSwipe(v, event);
            }
        });
    }




    // Handle fragment changes
    public void changeFrag(int fragVal) {
        // Start the fragment transition
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

        switch (fragVal) {
            // If the menu item clicked is Friends
            case 0:
                // If the same fragment was clicked, do nothing
                if (curFrag == 0) {
                    break;
                }

                // Animation
                if (curFrag < 1) {
                    ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                }
                else {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                }

                // Create the fragment with parameters
                FriendsListFragment fragmentFriends = FriendsListFragment.newInstance();

                curFrag = 0;

                // Change the fragment
                ft.replace(R.id.fragmentFriends, fragmentFriends);
                ft.commit();

                break;

            // If the menu item clicked is Requests
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
                FriendsRequestFragment fragmentRequests = FriendsRequestFragment.newInstance();

                curFrag = 1;

                // Change the fragment
                ft.replace(R.id.fragmentFriends, fragmentRequests);
                ft.commit();

                break;

            // If the menu item clicked is Search
            case 2:
                // If the same fragment was clicked, do nothing
                if (curFrag == 2) {
                    break;
                }

                // Create the fragment with paramters
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
                FriendsSearchFragment fragmentSearch = FriendsSearchFragment.newInstance();

                curFrag = 2;

                // Change the fragment
                ft.replace(R.id.fragmentFriends, fragmentSearch);
                ft.commit();

                break;
        }
    }



    // Handle swipes on the fragment view
    public boolean handleSwipe(View v, MotionEvent event) {
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

                //bottomNav.setSelectedItemId(fragVal_to_Id.get(curFrag));

                hasMoved = true;
            }

            return true;
        }
        return false;
    }
}