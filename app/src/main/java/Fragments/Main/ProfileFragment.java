package Fragments.Main;

import android.app.AlertDialog;
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
import com.example.metau_capstone.Friends.Friend_queue;
import com.example.metau_capstone.LoginActivity;
import com.example.metau_capstone.Profile.ProfileCollectionAdapter;
import com.example.metau_capstone.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import Fragments.Profile.SettingsFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
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

    // The user to load data for
    private static final String ARG_USER = "user";
    private ParseUser user;

    // What mode should the profile be put in?
    // 0 - Current user
    // 1 - Friend
    private static final String ARG_MODE = "mode";
    private int mode;

    public ProfileFragment() {
        // Required empty public constructor
    }

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
            Glide.with(view.getContext())
                    .load(R.drawable.default_pfp)
                    .circleCrop()
                    .into(ivProfileImage);
        }
        else {
            Glide.with(view.getContext())
                    .load(pic.getUrl())
                    .error(R.drawable.default_pfp)
                    .circleCrop()
                    .into(ivProfileImage);
        }



        // Initialize the view pager
        profileCollectionAdapter = new ProfileCollectionAdapter(ProfileFragment.this, user);
        pagerProfile = view.findViewById(R.id.pagerProfile);
        pagerProfile.setAdapter(profileCollectionAdapter);

        // Initialize the tab layout on top of the pager
        tlProfile = view.findViewById(R.id.tlProfile);
        tlProfile.addTab(tlProfile.newTab().setText("Fortune List"));
        tlProfile.addTab(tlProfile.newTab().setText("Text Search"));
        tlProfile.addTab(tlProfile.newTab().setText("Location Search"));
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



        // When the user profile picture is clicked, allow the
        // user to upload a new profile picture.
        // Do this only if the mode is 0
        if (mode == 0) {
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


        // Add an onClick listener to the options menu
        ivOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the mode is 0, show the user menu
                if (mode == 0) {
                    showUserMenu(v);
                }
                // If the mode is 1, show the friend menu
                else {
                    showFriendMenu(v);
                }

            }
        });


        // Handle back button presses
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If mode is 0, go back to the main page
                if (mode == 0) {
                    // Setup the fragment switch
                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                    // Go back to the Profile fragment
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                    HomeFragment_countdown homeFragment = HomeFragment_countdown.newInstance();

                    // Add back the profile fragment
                    ft.replace(R.id.flContainer, homeFragment);
                    ft.commit();

                    ((BottomNavigationView)getActivity().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
                    //((BottomNavigationView)getParentFragment().getView().findViewById(R.id.bottomNav)).setSelectedItemId(R.id.action_home);
                }
                // If mode is 1, go back to the friends page
                else {
                    // Setup the fragment switch
                    FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();

                    // Go back to the Profile fragment
                    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    FriendsFragment friendsFragment = FriendsFragment.newInstance();

                    // Add back the profile fragment
                    ft.replace(R.id.flContainer, friendsFragment);
                    ft.commit();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }



    // Used for menu item with icons
    private CharSequence menuIconWithText(Drawable r, String title) {

        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
    }



    private void showUserMenu(View v) {
        // Show the popup menu
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.settings), "Settings"));
        popup.getMenu().add(0, 2, 1, menuIconWithText(getResources().getDrawable(R.drawable.logout), "Logout"));
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
                    Toast.makeText(v.getContext(), "Logging out..", Toast.LENGTH_SHORT).show();
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




    private void showFriendMenu(View v) {
        // Show the popup menu
        PopupMenu popup = new PopupMenu(requireContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.getMenu().add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.unfriend), "Unfriend"));
        popup.getMenu().add(0, 2, 1, menuIconWithText(getResources().getDrawable(R.drawable.block), "Block"));
        inflater.inflate(R.menu.menu_friend_options, popup.getMenu());
        popup.show();

        // Add an on click listener for the menu items
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // If the item is unfriend, unfriend the user
                if (item.getItemId() == 1) {
                    // Display an alert dialog to make the user confirm they
                    // want to unfriend that user
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Unfriend")
                            .setMessage("Are you sure you want to unfriend " + tvUsername.getText().toString() + "?")

                            // If the user clicks yes, unfriend this friend
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                                                Toast.makeText(requireContext(), "Unable to unfriend user", Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(requireActivity(), "User unfriended", Toast.LENGTH_SHORT).show();
                                                    }

                                                    // Go back to the friends fragment
                                                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                                                    ft.replace(R.id.flContainer, new FriendsFragment());
                                                    ft.commit();
                                                }
                                            });
                                        }
                                    });
                                }
                            })

                            // If the user clicks no, do nothing
                            .setNegativeButton("No", null)
                            .show();

                    return true;
                }

                return false;
            }
        });
    }



    // When the get file intent is done, get the file information
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the user sent back an image
        if ((data != null) && requestCode == 21) {
            Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();

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

                                    Toast.makeText(getContext(), "Upload Success!", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Log.e(TAG, "File upload issue", e);
                                    Toast.makeText(getContext(), "Upload Failed :(", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        Log.e(TAG, "File Save Issue", e);
                        Toast.makeText(getContext(), "Upload Failed :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        ivProfileImage.setClickable(true);


    }


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





