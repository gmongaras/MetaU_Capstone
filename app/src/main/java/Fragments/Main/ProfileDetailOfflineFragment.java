package Fragments.Main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.MapHelper;
import com.example.metau_capstone.R;
import com.example.metau_capstone.dateFormatter;
import com.example.metau_capstone.offlineDB.FortuneDB;
import com.example.metau_capstone.offlineHelpers;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage the Profile Detail Fragment when offline
 */
public class ProfileDetailOfflineFragment extends Fragment {

    private static final String TAG = "ProfileDetailOffFrag";

    // The parts of the fortune to display in detail
    private static final String ARG_DATE = "date";
    private long date;
    private static final String ARG_MESSAGE = "message";
    private String message;
    private static final String ARG_LAT = "Lat_";
    private double Lat_;
    private static final String ARG_LONG = "Long_";
    private double Long_;
    private static final String ARG_LIKED = "liked";
    private boolean liked;
    private static final String ARG_LIKECT = "likeCt";
    private int likeCt;

    // List of other fortunes
    private static final String ARG_FORTUNES = "fortunes";
    private List<FortuneDB> fortunes;

    // Elements in the view
    TextView tvDate_detail;
    TextView tvFortune_detail;
    Fragment profileMap;
    ImageView ivLike;
    ImageView ivShare;
    TextView tvLikeCt;
    ImageView ivBack;

    // Used to work with the map
    MapHelper mapHelper;

    // The user to load the map for
    private static final String ARG_USER = "user";
    private ParseUser user;

    // Used to load date information
    dateFormatter df;

    // Used to work with offline data
    offlineHelpers h;

    public ProfileDetailOfflineFragment() {
        // Required empty public constructor
        df = new dateFormatter();
        h = new offlineHelpers();
    }

    /**
     * When the fragment is created, get the fortune to load
     * @param fortune The fortune to load into this fragment
     * @param user The user who owns this fortune
     * @param fortunes The list of fortunes to load into the map
     * @return The newly created Profile Detail Fragment
     */
    public static ProfileDetailOfflineFragment newInstance(FortuneDB fortune, ParseUser user, List<FortuneDB> fortunes) {
        ProfileDetailOfflineFragment fragment = new ProfileDetailOfflineFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, fortune.date);
        args.putString(ARG_MESSAGE, fortune.message);
        args.putDouble(ARG_LAT, fortune.Lat_);
        args.putDouble(ARG_LONG, fortune.Long_);
        args.putBoolean(ARG_LIKED, fortune.liked);
        args.putInt(ARG_LIKECT, fortune.likeCt);
        args.putParcelable(ARG_USER, user);
        args.putSerializable(ARG_FORTUNES, new ArrayList<FortuneDB>(fortunes));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = (long) getArguments().get(ARG_DATE);
            message = (String) getArguments().get(ARG_MESSAGE);
            Lat_ = (double) getArguments().get(ARG_LAT);
            Long_ = (double) getArguments().get(ARG_LONG);
            liked = (boolean) getArguments().get(ARG_LIKED);
            likeCt = (int) getArguments().get(ARG_LIKECT);
            user = getArguments().getParcelable(ARG_USER);
            fortunes = (List<FortuneDB>)getArguments().getSerializable(ARG_FORTUNES);
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

        // Get the elements in the view
        tvDate_detail = view.findViewById(R.id.tvDate_detail);
        tvFortune_detail = view.findViewById(R.id.tvFortune_detail);
        profileMap = getChildFragmentManager().findFragmentById(R.id.profileMap);
        ivLike = view.findViewById(R.id.ivLike);
        ivShare = view.findViewById(R.id.ivShare);
        tvLikeCt = view.findViewById(R.id.tvLikeCt);
        ivBack = view.findViewById(R.id.ivBack);

        // Get the fortune information and store it
        tvDate_detail.setText(df.toMonthDayTime(h.toDate(date)));
        tvFortune_detail.setText(message);

        // Get the map information and load it
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.profileMap));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    // Create the map helper object
                    mapHelper = new MapHelper(map, mapFragment, getContext());

                    // Load the map using the helper
                    mapHelper.loadMap(null, null, false);
                    mapHelper.loadPinsOffline(fortunes);

                    // Go to the spot on the map
                    if (Long_ != -99999999) {
                        LatLng latLng = new LatLng(Lat_, Long_);
                        mapHelper.goToLatLng(latLng, 10);
                    }
                }
            });
        } else {
            Log.e(TAG, "Error - Map Fragment was null!!");
        }

        // Change the drawable based on the liked state
        if (liked) {
            ivLike.setImageResource(R.drawable.like_filled);
        }
        else {
            ivLike.setImageResource(R.drawable.like);
        }

        // Set the number of likes for the fortune
        tvLikeCt.setText(String.valueOf(likeCt));

        // Handle clicks on the share button
        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check ou this fortune I got from a Fortune Cookie app:\n" + tvFortune_detail.getText());

                sendIntent.putExtra(Intent.EXTRA_TITLE, "Check out this fortune I got");

                // Show the Sharesheet
                startActivity(Intent.createChooser(sendIntent, null));
            }
        });

        // Handle back button presses
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackPress();
                v.setClickable(false);
            }
        });

        // Handle back button presses so the user doesn't go to the wrong
        // page after they logged in and pressed the back button.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }



    // Handle back button presses
    private void handleBackPress() {
        // Setup the fragment switch
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

        // Go back to the Profile fragment
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ProfileFragment profileFragment = ProfileFragment.newInstance(user, 1);

        // Add back the profile fragment
        ft.replace(R.id.flContainer, profileFragment);
        ft.commit();
    }
}