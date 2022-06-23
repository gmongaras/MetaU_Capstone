package Fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.metau_capstone.FriendsAdapter;
import com.example.metau_capstone.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
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
    }




    // Handle fragment changes
    private void changeFrag(int fragVal) {
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

                // Create the fragment with paramters
                FriendsListFragment fragmentFriends = FriendsListFragment.newInstance("a", "b");

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
                FriendsRequestFragment fragmentRequests = FriendsRequestFragment.newInstance("a", "b");

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
}