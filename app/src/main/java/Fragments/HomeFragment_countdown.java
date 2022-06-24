package Fragments;

import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import android.location.Location;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment_countdown#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment_countdown extends Fragment {

    // Elements in the view
    TextView tvCountdown;

    // Current timer values
    long hours;
    long minutes;
    long seconds;

    private static final String TAG = "HomeFragment_countdown";

    // Timer to track the time left until a fortune can be opened
    CountDownTimer timer;

    // Time until next fortune in miliseconds (23 hours)
    public static final double timeLeft = 8.28e+7;
    //public static final double timeLeft = 5000;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment_countdown() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment_countdown.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment_countdown newInstance(String param1, String param2) {
        HomeFragment_countdown fragment = new HomeFragment_countdown();
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
        return inflater.inflate(R.layout.fragment_home_countdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the elements in the view
        tvCountdown = view.findViewById(R.id.tvCountdown);

        // Get the last fortune and set the timer
        setTimer();
    }



    // Query the database and set the timer
    private void setTimer() {
        // Specify which class to query
        ParseQuery<Fortune> query = ParseQuery.getQuery(Fortune.class);

        // Get only this user's fortunes
        query.whereEqualTo("user", ParseUser.getCurrentUser());

        // Have the newest fortunes on top
        query.orderByDescending(Fortune.KEY_TIME_CREATED);

        // We only want one fortune, the newest one
        query.setLimit(1);

        // Find all the fortunes the user owns
        query.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> fortunes, ParseException e) {
                // Check if there was an error
                if (e != null) {
                    Log.e(TAG, "Unable to retrieve fortunes", e);
                    return;
                }

                // If the number of fortunes retreived is 0, go to the
                // fortune opening page.
                if (fortunes.size() == 0) {
                    goToFortune();
                    return;
                }

                // If the user has fortunes, get the time of the latest one
                Date latestCreatedAt = fortunes.get(0).getCreatedAt();

                // Get the current date
                Date currentTime = Calendar.getInstance().getTime();

                // Get the difference between the date times. If the difference
                // is more than 23 hours, go to the fortune page
                long diff = currentTime.getTime() - latestCreatedAt.getTime();
                if (diff >= timeLeft) {
                    goToFortune();
                }
                // If the difference is less than 23 hours, start the countdown timer
                // with a tick every second
                else {
                    timer =  new CountDownTimer((long) (timeLeft-diff), 1000) {

                        // Every second, update the on screen timer
                        public void onTick(long millisUntilFinished) {
                            // Get the hours, seconds, and minutes until the
                            // timer is done
                            hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                            minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)%60;
                            seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)%60;

                            // Get the values as a string and make sure
                            // there are two characters.
                            String h = String.valueOf(hours);
                            if (h.length() == 1) {
                                h = "0" + h;
                            }
                            String m = String.valueOf(minutes);
                            if (m.length() == 1) {
                                m = "0" + m;
                            }
                            String s = String.valueOf(seconds);
                            if (s.length() == 1) {
                                s = "0" + s;
                            }

                            // Display the new time left
                            tvCountdown.setText(h + ":" + m + ":" + s);
                        }

                        // When the timer is finished, switch to the fortune page
                        public void onFinish() {
                            // If the user is on the main page, swap to the
                            // fortune view when the timer is up
                            ArrayList<Fragment> stack = (ArrayList<Fragment>) getActivity().getSupportFragmentManager().getFragments();
                            if (stack.get(stack.size()-1).getClass() == HomeFragment_countdown.class) {
                                goToFortune();
                            }
                        }
                    }.start();
                }
            }
        });
    }


    // Go to the home fortune fragment so the user can open a new fortune.
    private void goToFortune() {
        // Start the fragment transition
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

        // Create the fragment with paramters
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        HomeFragment_fortune fragmentHome = HomeFragment_fortune.newInstance("a", "b");

        // Change the fragment
        ft.replace(R.id.flContainer, fragmentHome);
        ft.commit();
    }
}