package Fragments.Main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.Friends.Friend_queue;
import com.example.metau_capstone.LoginActivity;
import com.example.metau_capstone.Profile.ProfileCollectionAdapter;
import com.example.metau_capstone.R;
import com.example.metau_capstone.offlineHelpers;
import com.example.metau_capstone.translationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import Fragments.Profile.SettingsFragment;

/**
 * This class is used to manage the Profile Fragment
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // Elements in the view
    ImageView ivProfileImage;
    TextView tvUsername;
    ImageView ivOptions;

    // View Pager stuff
    TabLayout tlProfile;
    ViewPager2 pagerProfile;
    ProfileCollectionAdapter profileCollectionAdapter;

    translationManager manager;

    // The user to load data for
    private static final String ARG_USER = "user";
    private ParseUser user;

    // What mode should the profile be put in?
    // 0 - Current user
    // 1 - Friend
    // 2 - Other user
    // 3 - Other user blocked by logged in user
    // 4 - Logged in user blocked by other user
    private static final String ARG_MODE = "mode";
    private int mode;

    // Translated strings for Yes and No
    String YesTrans;
    String NoTrans;

    // Theme colors
    int textColor;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Create the fragment given information to load in
     * @param user The user to load into this fragment
     * @param mode The mode to load the user in
     * @return The newly created fragment
     */
    public static ProfileFragment newInstance(ParseUser user, int mode) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        args.putInt(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(ARG_USER);
            mode = getArguments().getInt(ARG_MODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the translation manager
        manager = new translationManager(ParseUser.getCurrentUser().getString("lang"));

        // Get translations for yes and no
        manager.getText("Yes", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String text) {
                YesTrans = text;
            }
        });
        manager.getText("No", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String text) {
                NoTrans = text;
            }
        });

        // Get the main theme text color
        int colorId = androidx.constraintlayout.widget.R.attr.textFillColor;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final TypedValue value = new TypedValue();
            requireActivity().getTheme().resolveAttribute(colorId, value, true);
            textColor = value.data;
        }

        // Get the elements
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvUsername = view.findViewById(R.id.tvUsername);
        ivOptions = view.findViewById(R.id.ivOptions);

        // If the user is null, default to the current user
        if (user == null) {
            user = ParseUser.getCurrentUser();
            mode = 0;
        }

        // Store the username
        tvUsername.setText(user.getUsername());

        // Store the user image
        ParseFile pic = user.getParseFile("profilePic");
        if (pic == null) {
            ivProfileImage.setImageResource(R.drawable.default_pfp);
        }
        else {
            Glide.with(view.getContext())
                    .load(pic.getUrl())
                    .error(R.drawable.default_pfp)
                    .circleCrop()
                    .into(ivProfileImage);
        }



        // Setup the user info
        setupInfo(view);



        // When the user profile picture is clicked, allow the
        // user to upload a new profile picture.
        // Do this only if the mode is 0 and the user is online
        if (mode == 0 && (new offlineHelpers().isNetworkAvailable(requireContext()))) {
            ivProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ivProfileImage.setClickable(false);

                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 21);
                }
            });
        }


        // Add an onClick listener to the options menu if the
        // user is online
        if (new offlineHelpers().isNetworkAvailable(requireContext())) {
            ivOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If the mode is 0, show the user menu
                    if (mode == 0) {
                        showUserMenu(v);
                    }
                    // If the mode is 1, show the friend menu
                    else if (mode == 1) {
                        showFriendMenu(v);
                    }
                    // If the mode is 2, show the other user menu
                    else {
                        showOtherUserMenu(v);
                    }

                }
            });
        }
        // If the user if offline, show a prompt stating settings
        // aren't available offline
        else {
            ivOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manager.createToast(requireContext(), "Settings unavailable offline");
                    //Toast.makeText(requireContext(), "Settings unavailable offline", Toast.LENGTH_SHORT).show();

                }
            });
        }


        // Handle back button presses
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If mode is 0, go back to the main page
                if (mode == 0) {
                    // Setup the fragment switch
                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                    // Go back to the Profile fragment
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    HomeFragment_countdown homeFragment = HomeFragment_countdown.newInstance();

                    // Add back the profile fragment
                    ft.replace(R.id.flContainer, homeFragment);
                    ft.commit();

                    ((BottomNavigationView)getActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
                    //((BottomNavigationView)getParentFragment().getView().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
                }
                // If mode is 1, go back to the friends page
                else if (mode == 1) {
                    // Setup the fragment switch
                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                    // Go back to the Friends fragment
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    FriendsFragment friendsFragment = FriendsFragment.newInstance(0);

                    // Add back the Friends fragment
                    ft.replace(R.id.flContainer, friendsFragment);
                    ft.commit();
                }
                // If mode is 2, go back to the friends search page
                else {
                    // Setup the fragment switch
                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                    // Go back to the friends fragment
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    FriendsFragment friendsFragment = FriendsFragment.newInstance(2);

                    // Add back the friends fragment
                    ft.replace(R.id.flContainer, friendsFragment);
                    ft.commit();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }



    // Setup the information in the user menu
    private void setupInfo(View view) {
        // Initialize the view pager
        profileCollectionAdapter = new ProfileCollectionAdapter(ProfileFragment.this, user, mode);
        pagerProfile = view.findViewById(R.id.pagerFriends);
        pagerProfile.setAdapter(profileCollectionAdapter);

        // Initialize the tab layout on top of the pager
        tlProfile = view.findViewById(R.id.tlProfile);
        TabLayout.Tab tmp;
        tmp = tlProfile.newTab();
        manager.addText(tmp, "Fortune List");
        tlProfile.addTab(tmp);
        tmp = tlProfile.newTab(); manager.addText(tmp, "Text Search");
        tlProfile.addTab(tmp);
        tmp = tlProfile.newTab(); manager.addText(tmp, "Location Search");
        tlProfile.addTab(tmp);
        tmp = tlProfile.newTab(); manager.addText(tmp, "Liked List");
        tlProfile.addTab(tmp);
        tlProfile.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pagerProfile.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        pagerProfile.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tlProfile.selectTab(tlProfile.getTabAt(position));
            }
        });
    }



    // Used for menu item with icons
    private CharSequence menuIconWithText(Drawable r, String title) {

        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
    }



    // Inflate and show the current user popup menu in the top right
    private void showUserMenu(View v) {
        // Translate the necessary text
        manager.getText("Settings", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String settings) {
                manager.getText("Logout", new translationManager.onCompleteListener() {
                    @Override
                    public void onComplete(String logout) {
                        // Show the popup menu
                        Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupStyle);
                        PopupMenu popup = new PopupMenu(wrapper, v);
                        popup.getMenu().add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.settings), settings));
                        popup.getMenu().add(0, 2, 1, menuIconWithText(getResources().getDrawable(R.drawable.logout), logout));
                        MenuInflater inflater = popup.getMenuInflater();
                        inflater.inflate(R.menu.menu_profile_options, popup.getMenu());
                        popup.show();

                        // Add an on click listener for the menu items
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                // If the settings item is clicked, go to the users settings
                                if (item.getItemId() == 1) {
                                    // Setup the fragment switch
                                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                                    // Go to the settings fragment
                                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                                    SettingsFragment settingsFragment = SettingsFragment.newInstance();

                                    ft.replace(R.id.flContainer, settingsFragment);
                                    ft.commit();

                                    return true;
                                }

                                // If the item is logout, log the user out
                                if (item.getItemId() == 2) {
                                    // Log the user out
                                    manager.createToast(requireContext(), "Logging out...");
                                    ParseUser.logOutInBackground();

                                    // Exit this fragment
                                    requireActivity().finishAffinity();

                                    // Go back to the main page
                                    Intent i = new Intent(v.getContext(), LoginActivity.class);
                                    startActivity(i);

                                    return true;
                                }

                                return false;
                            }
                        });
                    }
                });
            }
        });

    }




    // Inflate and show the friend popup menu in the top right
    private void showFriendMenu(View v) {
        // Translate the necessary text
        manager.getText("Remove Friend", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String unfriend) {
                manager.getText("Block", new translationManager.onCompleteListener() {
                    @Override
                    public void onComplete(String block) {
                        // Show the popup menu
                        Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupStyle);
                        PopupMenu popup = new PopupMenu(wrapper, v);
                        MenuInflater inflater = popup.getMenuInflater();
                        popup.getMenu().add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.unfriend), unfriend));
                        popup.getMenu().add(0, 2, 1, menuIconWithText(getResources().getDrawable(R.drawable.block), block));
                        inflater.inflate(R.menu.menu_friend_options, popup.getMenu());
                        popup.show();

                        // Get more translations
                        manager.getText("Are you sure you want to unfriend " + tvUsername.getText().toString() + "?", new translationManager.onCompleteListener() {
                            @Override
                            public void onComplete(String unfriendMsg) {
                                manager.getText("Are you sure you want to block " + tvUsername.getText().toString() + "? Blocking will also unfriend the other user and will remove all liked fortunes between both users", new translationManager.onCompleteListener() {
                                    @Override
                                    public void onComplete(String blockMsg) {
                                        // Add an on click listener for the menu items
                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                // If the item is unfriend, unfriend the user
                                                if (item.getItemId() == 1) {
                                                    // Display an alert dialog to make the user confirm they
                                                    // want to unfriend that user
                                                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                                            .setTitle(unfriend)
                                                            .setMessage(unfriendMsg)

                                                            // If the user clicks yes, unfriend this friend
                                                            .setPositiveButton(YesTrans, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    unfriend();

                                                                    // Go back to the friends fragment
                                                                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                                                                    ft.replace(R.id.flContainer, FriendsFragment.newInstance(0));
                                                                    ft.commit();

                                                                }
                                                            })

                                                            // If the user clicks no, do nothing
                                                            .setNegativeButton(NoTrans, null)
                                                            .show();

                                                    // Change the color of the buttons
                                                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
                                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);

                                                    return true;
                                                }

                                                // If the item is block, block the friend
                                                if (item.getItemId() == 2) {
                                                    // Display an alert dialog to make the user confirm they
                                                    // want to block the other user
                                                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                                            .setTitle(block)
                                                            .setMessage(blockMsg)

                                                            // If the user clicks yes, block this friend
                                                            .setPositiveButton(YesTrans, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    blockFriend();

                                                                    // Go back to the friends fragment
                                                                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                                                                    ft.replace(R.id.flContainer, FriendsFragment.newInstance(0));
                                                                    ft.commit();

                                                                }
                                                            })

                                                            // If the user clicks no, do nothing
                                                            .setNegativeButton(NoTrans, null)
                                                            .show();

                                                    // Change the color of the buttons
                                                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
                                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);

                                                    return true;
                                                }

                                                return false;
                                            }
                                        });
                                    }
                                });
                            }
                        });



                    }
                });
            }
        });


    }



    // Inflate and show the other user popup menu in the top right
    private void showOtherUserMenu(View v) {
        // Get the needed translations
        manager.getText("Blocked", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String block) {
                manager.getText("Unblock", new translationManager.onCompleteListener() {
                    @Override
                    public void onComplete(String unblock) {
                        // Is the user blocked?
                        ParseRelation<ParseUser> rel = ParseUser.getCurrentUser().getRelation("Blocked");
                        ParseQuery<ParseUser> query = rel.getQuery();
                        query.whereEqualTo("objectId", user.getObjectId());
                        query.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                // If the objects list has an item in it, the other user is blocked,
                                // otherwise the other user is not blocked
                                boolean blocked = false;
                                if (objects.size() != 0) {
                                    blocked = true;
                                }


                                // Show the popup menu
                                Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupStyle);
                                PopupMenu popup = new PopupMenu(wrapper, v);
                                MenuInflater inflater = popup.getMenuInflater();
                                if (blocked == false) {
                                    popup.getMenu().add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.block), block));
                                }
                                else {
                                    popup.getMenu().add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.block), unblock));
                                }
                                inflater.inflate(R.menu.menu_friend_options, popup.getMenu());
                                popup.show();


                                // Get more translations
                                boolean finalBlocked = blocked;
                                manager.getText("Are you sure you want to unblock " + tvUsername.getText().toString() + "?", new translationManager.onCompleteListener() {
                                    @Override
                                    public void onComplete(String unblockTxt) {
                                        manager.getText("Are you sure you want to block " + tvUsername.getText().toString() + "?\nBlocking will remove all liked fortunes between both users.", new translationManager.onCompleteListener() {
                                            @Override
                                            public void onComplete(String blockTxt) {

                                                // Add an on click listener for the menu items
                                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                    @Override
                                                    public boolean onMenuItemClick(MenuItem item) {
                                                        // Is the user blocked?
                                                        if (finalBlocked) {
                                                            // If the item is unblock, unblock the user
                                                            if (item.getItemId() == 1) {
                                                                // Show a prompt to confirm if the user wants to unblock the other user
                                                                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                                                        .setTitle(unblock)
                                                                        .setMessage(unblockTxt)

                                                                        // If the user clicks yes, unblock the user
                                                                        .setPositiveButton(YesTrans, new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {

                                                                                // unblock the user
                                                                                unblock();

                                                                            }
                                                                        })

                                                                        // If the user clicks no, do nothing
                                                                        .setNegativeButton(NoTrans, null)
                                                                        .show();

                                                                // Change the color of the buttons
                                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
                                                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);

                                                                return true;
                                                            }

                                                            return false;
                                                        }
                                                        else {
                                                            // If the item is block, block the user
                                                            if (item.getItemId() == 1) {
                                                                // Show a prompt to confirm if the user wants to block the other user
                                                                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                                                        .setTitle(block)
                                                                        .setMessage(blockTxt)

                                                                        // If the user clicks yes, block the user
                                                                        .setPositiveButton(YesTrans, new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {

                                                                                // Block the user
                                                                                block();

                                                                            }
                                                                        })

                                                                        // If the user clicks no, do nothing
                                                                        .setNegativeButton(NoTrans, null)
                                                                        .show();

                                                                // Change the color of the buttons
                                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
                                                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);

                                                                return true;
                                                            }

                                                            return false;
                                                        }
                                                    }


                                                });
                                            }
                                        });
                                    }
                                });


                            }
                        });
                    }
                });

            }
        });
    }



    // Unfriend a friend
    private void unfriend() {
        // Get the current user
        ParseUser curUser = ParseUser.getCurrentUser();

        // Remove the friend from the current user's friend list
        ParseRelation<ParseUser> friends = curUser.getRelation("friends");
        friends.remove(user);
        curUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Unable to unfriend user", e);
                    manager.createToast(requireContext(), "Unable to unfriend user");
                    return;
                }

                // Send a request to remove the friend from the
                // other user's friend's list
                Friend_queue queue = new Friend_queue();
                queue.setUser(user);
                queue.setFriend(curUser);
                queue.setMode("remove");
                queue.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Unable to send remove request to queue", e);
                        }
                        else {
                            try {
                                manager.createToast(requireContext(), "User unfriended");
                            } catch (Exception e2) {
                                Log.e(TAG, "Page unloaded before unfriending", e2);
                            }
                        }
                    }
                });
            }
        });
    }



    // Block a friend
    private void blockFriend() {
        // Get the needed translations
        manager.getText("Block this friend?", new translationManager.onCompleteListener() {
            @Override
            public void onComplete(String title) {
                manager.getText("Are you sure you want to block " + tvUsername.getText().toString() + "?\nBlocking will also unfriend them and remove all liked fortunes between you two.", new translationManager.onCompleteListener() {
                    @Override
                    public void onComplete(String text) {
                        // Show a prompt to confirm if the user wants to block a friend
                        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                .setTitle(title)
                                .setMessage(text)

                                // If the user clicks yes, block this friend
                                .setPositiveButton(YesTrans, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // Unfriend the user
                                        unfriend();

                                        // Block the user
                                        block();

                                    }
                                })

                                // If the user clicks no, do nothing
                                .setNegativeButton(NoTrans, null)
                                .show();

                        // Change the color of the buttons
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textColor);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textColor);
                    }
                });
            }
        });
    }



    // Block a user
    private void block() {
        // Get the relational blocked user data
        ParseRelation<ParseUser> rel = ParseUser.getCurrentUser().getRelation("Blocked");

        // Add the other to the blocked list
        rel.add(user);

        // Get the current user
        ParseUser curUser = ParseUser.getCurrentUser();

        // Save the new blocked user
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Remove all liked fortunes from the logged in user -->

                    // Query for all liked fortunes
                    ParseRelation<Fortune> rel = curUser.getRelation("liked");
                    ParseQuery<Fortune> query = rel.getQuery();
                    query.findInBackground(new FindCallback<Fortune>() {
                        @Override
                        public void done(List<Fortune> liked, ParseException e) {
                            // Iterate over all fortunes and remove the ones
                            // that belong to the other user
                            for (Fortune f : liked) {
                                if (Objects.equals(f.getUser().getObjectId(), user.getObjectId())) {
                                    // Remove the fortune
                                    rel.remove(f);

                                    // Update the like count
                                    f.setLikeCt(f.getLikeCt()-1);
                                    f.saveInBackground();
                                }
                            }
                            // Save the new relation
                            curUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    // When the logged in user's liked fortunes have been updated,
                                    // update the other user

                                    // Query for all liked fortunes
                                    ParseRelation<Fortune> rel = user.getRelation("liked");
                                    ParseQuery<Fortune> query = rel.getQuery();
                                    query.findInBackground(new FindCallback<Fortune>() {
                                        @Override
                                        public void done(List<Fortune> fortunes, ParseException e) {
                                            // Iterate over all fortunes and remove the ones
                                            // that belong to the logged in user
                                            for (Fortune f : liked) {
                                                if (Objects.equals(f.getUser().getObjectId(), curUser.getObjectId())) {
                                                    rel.remove(f);

                                                    // Update the like count
                                                    f.setLikeCt(f.getLikeCt()-1);
                                                    f.saveInBackground();
                                                }
                                            }

                                            // Save the updated relation
                                            user.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    // When the users have been updated,
                                                    // notify the user
                                                    manager.createToast(requireContext(), "User blocked");

                                                    // Setup the fragment switch
                                                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                                                    // Go back to the friends fragment
                                                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                                                    FriendsFragment friendsFragment = FriendsFragment.newInstance(0);

                                                    // Add back the friends fragment
                                                    ft.replace(R.id.flContainer, friendsFragment);
                                                    ft.commit();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
                else {
                    manager.createToast(requireContext(), "Unable to block user");
                }
            }
        });
    }



    // unblock a user
    private void unblock() {
        // Get the relational blocked user data
        ParseRelation<ParseUser> rel = ParseUser.getCurrentUser().getRelation("Blocked");

        // Remove the other user from the list
        rel.remove(user);

        // Save the new blocked list
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    manager.createToast(requireContext(), "User unblocked");

                    // Go back to the friend fragment

                    // Setup the fragment switch
                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                    // Go back to the friends fragment
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    FriendsFragment friendsFragment = FriendsFragment.newInstance(0);

                    // Add back the friends fragment
                    ft.replace(R.id.flContainer, friendsFragment);
                    ft.commit();
                }
                else {
                    manager.createToast(requireContext(), "Unable to unblock user");
                }
            }
        });
    }



    // When the get file intent is done, get the file information
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the user sent back an image
        if ((data != null) && requestCode == 21) {
            manager.createToast(requireContext(), "Uploading image...");

            // Get the URI of the image
            Uri photoUri = data.getData();

            // Load the image at the URI into a bitmap
            Bitmap selectedImage = loadFromUri(photoUri);
            selectedImage = Bitmap.createScaledBitmap(selectedImage, 200, 200, true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapBytes = stream.toByteArray();

            // Save the image so we can use it in Parse
            ParseFile imageFile = new ParseFile("profilePic", bitmapBytes);
            imageFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    // If the image was successfully saved
                    if (e == null) {
                        // Add the image to the profile
                        user.put("profilePic", imageFile);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                // If the image was saved,
                                if (e == null) {
                                    // When the image is saved, display it
                                    Object img = user.get("profilePic");
                                    Glide.with(getContext())
                                            .load(((ParseFile) img).getUrl())
                                            .error(R.drawable.default_pfp)
                                            .circleCrop()
                                            .into(ivProfileImage);

                                    manager.createToast(requireContext(), "Upload Success!");
                                }
                                else {
                                    Log.e(TAG, "File upload issue", e);
                                    manager.createToast(requireContext(), "Upload Failed :(");
                                }
                            }
                        });
                    }
                    else {
                        Log.e(TAG, "File Save Issue", e);
                        manager.createToast(requireContext(), "Upload Failed :(");
                    }
                }
            });
        }

        ivProfileImage.setClickable(true);


    }


    // Given the URI of a file on the device, load in the file into a bitmap
    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}





