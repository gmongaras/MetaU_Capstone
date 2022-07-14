package Fragments.Main;

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

import com.example.metau_capstone.MapHelper;
import com.example.metau_capstone.R;
import com.example.metau_capstone.offlineDB.FortuneDB;
import com.example.metau_capstone.offlineHelpers;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.List;

import Fragments.Profile.ProfileList;

/**
 * This class is used to manage the Map Fragment
 */
public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";

    private static final String ARG_FORTUNES = "fortunes";
    private List<FortuneDB> fortunes;

    // Map helper object
    MapHelper mapHelper;

    // Error text element
    private TextView errorText;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Create the fragment given information to load in
     * @param fortunes A list of FortuneDB objects. This should be null if the
     *                 user is online, and not null if the user is offline.
     * @return The newly created fragment
     */
    public static MapFragment newInstance(@Nullable List<FortuneDB> fortunes) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        if (fortunes != null) {
            args.putSerializable(ARG_FORTUNES, (Serializable) fortunes);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_FORTUNES)) {
                fortunes = (List<FortuneDB>)getArguments().getSerializable(ARG_FORTUNES);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the error text information
        errorText = view.findViewById(R.id.errorText);

        // Get the map information
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.profileMap));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap map) {
                    // Create the map helper object
                    mapHelper = new MapHelper(map, mapFragment, getContext());

                    // Load the map using the helper
                    mapHelper.loadMap(ParseUser.getCurrentUser(), errorText, true);

                    // If the user is offline, also load the pins
                    if (!((new offlineHelpers()).isNetworkAvailable(requireContext()))) {
                        mapHelper.loadPinsOffline(fortunes);
                    }
                }
            });
        } else {
            Log.e(TAG, "Error - Map Fragment was null!!");
        }

        // Handle back button presses by going to the home fragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Setup the fragment switch
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                // Go back to the Profile fragment
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                HomeFragment_countdown homeFragment = HomeFragment_countdown.newInstance();

                // Add back the profile fragment
                ft.replace(R.id.flContainer, homeFragment);
                ft.commit();

                ((BottomNavigationView)getActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

}