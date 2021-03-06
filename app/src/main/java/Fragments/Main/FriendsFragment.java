package Fragments.Main;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.metau_capstone.Friends.FriendCollectionAdapter;
import com.example.metau_capstone.R;
import com.example.metau_capstone.translationManager;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseUser;

/**
 * This class is used to manage the Friends List Fragment
 */
public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    // Elements in the fragment
    TabLayout tlFriends;
    ViewPager2 pagerFriends;
    FriendCollectionAdapter friendCollectionAdapter;

    // The current fragment in view
    int curFrag = -1;

    // Page to load when initialized
    private static final String ARG_PAGE = "page";
    private int page;

    translationManager manager;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * When initializing the friends fragment, A page is needed to
     * load in the correct page in the view pager
     * @param page A page values between 0 and 2 which 0 is friends
     *             list, 1 is friend requests, and 2 is friend search
     * @return The newly created friends fragment
     */
    public static FriendsFragment newInstance(int page) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page = getArguments().getInt(ARG_PAGE);
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

        manager = new translationManager(ParseUser.getCurrentUser().getString("lang"));

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
        manager.getText("Friends", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String text) {
                TabLayout.Tab tmp;
                tmp = tlFriends.newTab().setIcon(R.drawable.friends_list).setText(text);
                tlFriends.addTab(tmp);
                tmp = tlFriends.newTab().setIcon(R.drawable.friend_request);
                manager.addText(tmp, "Requests");
                tlFriends.addTab(tmp);
                tmp = tlFriends.newTab().setIcon(R.drawable.search);
                manager.addText(tmp, "Search");
                tlFriends.addTab(tmp);
                tlFriends.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        final TypedValue value = new TypedValue ();
                        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, value, true);
                        tab.getIcon().setTint(value.data);

                        pagerFriends.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        final TypedValue value = new TypedValue ();
                        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, value, true);
                        tab.getIcon().setTint(value.data);
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

                // Set the default page
                tlFriends.selectTab(tlFriends.getTabAt(page));
                pagerFriends.setCurrentItem(page, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final TypedValue value = new TypedValue ();
                    getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, value, true);
                    tlFriends.getTabAt(page).getIcon().setTint(value.data);
                }
            }
        });
    }


    /**
     * Given a fragment value, change the fragment in the page viewer to
     * the given fragment number
     * @param fragVal The fragment to change in the view pager
     */
    public void changeFrag(int fragVal) {
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