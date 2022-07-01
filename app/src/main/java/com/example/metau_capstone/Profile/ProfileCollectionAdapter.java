package com.example.metau_capstone.Profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.parse.ParseUser;

import Fragments.Profile.ProfileLikedFragment;
import Fragments.Profile.ProfileList;
import Fragments.Profile.ProfileSearchLoc;
import Fragments.Profile.ProfileSearchText;

public class ProfileCollectionAdapter extends FragmentStateAdapter {
    // User to load data for
    ParseUser user;

    // Mode to load user
    int mode;

    public ProfileCollectionAdapter(Fragment fragment, ParseUser user, int mode) {
        super(fragment);
        this.user = user;
        this.mode = mode;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // If position is 0, load the list of fortunes
        Fragment fragment;
        if (position == 0) {
            fragment = ProfileList.newInstance(user, mode);
        }
        // If position is 1, load the search by text fragment
        else if (position == 1) {
            fragment = ProfileSearchText.newInstance(user, mode);
        }
        // If the position is 2, load the search by location fragment
        else if (position == 2) {
            fragment = ProfileSearchLoc.newInstance(user, mode);
        }
        // If the position is 3, load the Liked list fragment
        else {
            fragment = ProfileLikedFragment.newInstance(user, mode);
        }

        return fragment;
    }

    // Number of fragments in the page viewer
    @Override
    public int getItemCount() {
        return 4;
    }
}
