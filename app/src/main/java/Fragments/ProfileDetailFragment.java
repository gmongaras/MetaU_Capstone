package Fragments;

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
import android.widget.TextView;

import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.MapHelper;
import com.example.metau_capstone.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

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

    // Elements in the view
    TextView tvDate_detail;
    TextView tvFortune_detail;

    // Used to work with the map
    MapHelper mapHelper;

    public ProfileDetailFragment() {
        // Required empty public constructor
    }

    // When the fragment is created, get the fortune that was passed in
    public static ProfileDetailFragment newInstance(Fortune fortune) {
        ProfileDetailFragment fragment = new ProfileDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_FORTUNE, fortune);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fortune = (Fortune) getArguments().get(ARG_FORTUNE);
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

        // Get the fortune information and store it
        tvDate_detail.setText(fortune.getCreatedAt().toString());
        tvFortune_detail.setText(fortune.getMessage());

        // Get the map information and load it
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    // Create the map helper object
                    mapHelper = new MapHelper(map, mapFragment, getContext());

                    // Load the map using the helper
                    mapHelper.loadMap(null);

                    // Go to the spot on the map
                    ParseGeoPoint loc = fortune.getLocation();
                    if (loc != null) {
                        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                        mapHelper.goToLatLng(latLng);
                    }
                }
            });
        } else {
            Log.e(TAG, "Error - Map Fragment was null!!");
        }

        // Handle back button presses so the user doesn't go to the wrong
        // page after they logged in and pressed the back button.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Setup the fragment switch
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                // Go back to the Profile fragment
                ProfileFragment profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), 0);

                // Add back the profile fragment
                ft.replace(R.id.flContainer, profileFragment);
                ft.commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }
}