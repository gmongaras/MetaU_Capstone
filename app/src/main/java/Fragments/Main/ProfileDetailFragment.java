package Fragments.Main;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.MapHelper;
import com.example.metau_capstone.R;
import com.example.metau_capstone.dateFormatter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileDetailFragment extends Fragment {

    private static final String TAG = "ProfileDetailFragment";

    // The fortune to display in detail
    private static final String ARG_FORTUNE = "fortune";
    private Fortune fortune;

    // Mode in which the profile is in
    private static final String ARG_INT = "mode";
    private int mode;

    // Elements in the view
    TextView tvDate_detail;
    TextView tvFortune_detail;
    TextView tvNoAccessMap;
    Fragment profileMap;
    ImageView ivLike;
    ImageView ivShare;

    // Used to work with the map
    MapHelper mapHelper;

    // The user to load the map for
    private static final String ARG_USER = "user";
    private ParseUser user;

    // Used to load date information
    dateFormatter df = new dateFormatter();

    public ProfileDetailFragment() {
        // Required empty public constructor
    }

    // When the fragment is created, get the fortune that was passed in
    public static ProfileDetailFragment newInstance(Fortune fortune, ParseUser user, int mode) {
        ProfileDetailFragment fragment = new ProfileDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FORTUNE, fortune);
        args.putParcelable(ARG_USER, user);
        args.putInt(ARG_INT, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fortune = (Fortune) getArguments().get(ARG_FORTUNE);
            user = getArguments().getParcelable(ARG_USER);
            mode = getArguments().getInt(ARG_INT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Should the user have map access?
        boolean access = true;

        // If the mode is 1 (friend), check if the user has access
        if (mode == 1) {
            // If the user doesn't allow friends to see their map, set
            // access to false
            if (user.getBoolean("showMapFriends") == false) {
                access = false;
            }
        }

        // If the mode is 2 (other user), check if the user has access
        else if (mode == 2) {
            // If the user doesn't allow other users to see their map, set
            // access to false
            if (user.getBoolean("showMapUsers") == false) {
                access = false;
            }
        }

        // Get the elements in the view
        tvDate_detail = view.findViewById(R.id.tvDate_detail);
        tvFortune_detail = view.findViewById(R.id.tvFortune_detail);
        profileMap = getChildFragmentManager().findFragmentById(R.id.profileMap);
        ivLike = view.findViewById(R.id.ivLike);
        ivShare = view.findViewById(R.id.ivShare);

        // Get the fortune information and store it
        tvDate_detail.setText(df.toMonthDayTime(fortune.getCreatedAt()));
        tvFortune_detail.setText(fortune.getMessage());

        // If the user doesn't have access to the fortunes, display a message
        // and hide the map
        if (access == false) {
            tvNoAccessMap = view.findViewById(R.id.tvNoAccessMap);
            tvNoAccessMap.setVisibility(View.VISIBLE);
            profileMap.getView().setVisibility(View.INVISIBLE);
            ivLike.setVisibility(View.INVISIBLE);
            ivShare.setVisibility(View.INVISIBLE);
        }

        // If the user has access, load the map
        else {
            // Get the map information and load it
            SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.profileMap));
            if (mapFragment != null) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap map) {
                        // Create the map helper object
                        mapHelper = new MapHelper(map, mapFragment, getContext());

                        // Load the map using the helper
                        mapHelper.loadMap(user, null, false);

                        // Go to the spot on the map
                        ParseGeoPoint loc = fortune.getLocation();
                        if (loc != null) {
                            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                            mapHelper.goToLatLng(latLng, 10);
                        }
                    }
                });
            } else {
                Log.e(TAG, "Error - Map Fragment was null!!");
            }
        }

        // Add an onClick listener to the

        // If the mode is not 0, remove the like button
        if (mode != 0) {
            ivLike.setVisibility(View.INVISIBLE);
        }
        // If the mode is 0, set an on click listener to the like button
        else {
            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ;
                }
            });
        }

        // Handle clicks on the share button
        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String whoTxt = "";
                // If the mode is 0, use the text "I got"
                if (mode == 0) {
                    whoTxt = "I got";
                }
                // If the mode is 1, use the text "a friend got"
                else if (mode == 1) {
                    whoTxt = "a friend got";
                }
                // If the mode is 2, use the text "someone got"
                else if (mode == 2) {
                    whoTxt = "someone got";
                }

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check ou this fortune " + whoTxt + " from a Fortune Cookie app:\n" + tvFortune_detail.getText());

                sendIntent.putExtra(Intent.EXTRA_TITLE, "Check out this fortune " + whoTxt);

                // Show the Sharesheet
                startActivity(Intent.createChooser(sendIntent, null));
            }
        });

        // Handle back button presses so the user doesn't go to the wrong
        // page after they logged in and pressed the back button.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Setup the fragment switch
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                // Go back to the Profile fragment
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                ProfileFragment profileFragment = ProfileFragment.newInstance(user, mode);

                // Add back the profile fragment
                ft.replace(R.id.flContainer, profileFragment);
                ft.commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }
}