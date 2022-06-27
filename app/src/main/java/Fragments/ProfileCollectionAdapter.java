package Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.parse.ParseUser;

public class ProfileCollectionAdapter extends FragmentStateAdapter {
    // User to load data for
    ParseUser user;

    public ProfileCollectionAdapter(Fragment fragment, ParseUser user) {
        super(fragment);
        this.user = user;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // If position is 0, load the list of fortunes
        Fragment fragment;
        if (position == 0) {
            fragment = ProfileList.newInstance(user);
        }
        // If position is 1, load the search fragment
        else {
            fragment = ProfileList.newInstance(user);
        }

        return fragment;
    }

    // Number of fragments in the page viewer
    @Override
    public int getItemCount() {
        return 2;
    }
}
