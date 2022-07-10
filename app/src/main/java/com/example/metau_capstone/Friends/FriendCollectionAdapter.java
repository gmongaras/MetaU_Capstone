package com.example.metau_capstone.Friends;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import Fragments.Friends.FriendsListFragment;
import Fragments.Friends.FriendsRequestFragment;
import Fragments.Friends.FriendsSearchFragment;

/**
 ** Adapter used to manage the View Pager in the Friends Fragment
 */
public class FriendCollectionAdapter extends FragmentStateAdapter {

    // Initialize the view pager with a given fragment
    public FriendCollectionAdapter(Fragment fragment) {
        super(fragment);
    }

    // Given a position, create a new fragment for that position
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // If the position is 0, load the friend list
        Fragment fragment;
        if (position == 0) {
            fragment = FriendsListFragment.newInstance();
        }
        // If the position is 1, load the requests fragment
        else if (position == 1) {
            fragment = FriendsRequestFragment.newInstance();
        }
        // If the position is 2, load the search fragment
        else {
            fragment = FriendsSearchFragment.newInstance();
        }

        return fragment;
    }

    // Number of fragments in the page viewer
    @Override
    public int getItemCount() {
        return 3;
    }
}
