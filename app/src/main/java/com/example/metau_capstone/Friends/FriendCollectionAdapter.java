package com.example.metau_capstone.Friends;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.parse.ParseUser;

import Fragments.Friends.FriendsListFragment;
import Fragments.Friends.FriendsRequestFragment;
import Fragments.Friends.FriendsSearchFragment;
import Fragments.Main.FriendsFragment;
import Fragments.Profile.ProfileLikedFragment;
import Fragments.Profile.ProfileList;
import Fragments.Profile.ProfileSearchLoc;
import Fragments.Profile.ProfileSearchText;

public class FriendCollectionAdapter extends FragmentStateAdapter {

    public FriendCollectionAdapter(Fragment fragment) {
        super(fragment);
    }

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
