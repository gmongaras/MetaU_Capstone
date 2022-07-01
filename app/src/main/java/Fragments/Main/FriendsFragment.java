package Fragments.Main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.metau_capstone.Friends.FriendCollectionAdapter;
import com.example.metau_capstone.R;
import com.google.android.material.tabs.TabLayout;

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
    TabLayout tlFriends;
    ViewPager2 pagerFriends;
    FriendCollectionAdapter friendCollectionAdapter;

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
        tlFriends = view.findViewById(R.id.tlFriends);
        pagerFriends = view.findViewById(R.id.pagerFriends);

        // Setup the page viewer
        setupViewer(view);
    }



    // Setup the information in the user menu
    private void setupViewer(View view) {
        // Initialize the view pager
        friendCollectionAdapter = new FriendCollectionAdapter(FriendsFragment.this);
        pagerFriends = view.findViewById(R.id.pagerFriends);
        pagerFriends.setAdapter(friendCollectionAdapter);

        // Initialize the tab layout on top of the pager
        tlFriends.addTab(tlFriends.newTab().setText("Friends").setIcon(R.drawable.friends_list));
        tlFriends.addTab(tlFriends.newTab().setText("Requests").setIcon(R.drawable.friend_request));
        tlFriends.addTab(tlFriends.newTab().setText("Search").setIcon(R.drawable.search));
        tlFriends.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pagerFriends.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        pagerFriends.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tlFriends.selectTab(tlFriends.getTabAt(position));
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

                // Change the fragment
                pagerFriends.setCurrentItem(0);

                break;

            // If the menu item clicked is Requests
            case 1:
                // If the same fragment was clicked, do nothing
                if (curFrag == 1) {
                    break;
                }

                // Change the fragment
                pagerFriends.setCurrentItem(1);

                break;

            // If the menu item clicked is Search
            case 2:
                // If the same fragment was clicked, do nothing
                if (curFrag == 2) {
                    break;
                }

                // Change the fragment
                pagerFriends.setCurrentItem(2);

                break;
        }
    }
}