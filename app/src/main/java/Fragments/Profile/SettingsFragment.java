package Fragments.Profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.metau_capstone.BootReceiver;
import com.example.metau_capstone.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    // Elements in the layout
    ImageView ivChangePFP;
    SwitchCompat swFriendable;
    SwitchCompat swShowFortunesFriends;
    SwitchCompat swShowFortunesUsers;
    SwitchCompat swShowMapFriends;
    SwitchCompat swShowMapUsers;
    SwitchCompat swPushNotif;
    Button btnLocPerm;
    Button btnDeleteAccount;

    // The current parse user
    ParseUser user;

    // Is a switch being changed?
    boolean changing;

    // Map from element ID to Parse columns name
    Map<Integer, String> idToString = Map.of(
            R.id.swFriendable, "friendable",
            R.id.swShowFortunesFriends, "showFortunesFriends",
            R.id.swShowFortunesUsers, "showFortunesUsers",
            R.id.swShowMapFriends, "showMapFriends",
            R.id.swShowMapUsers, "showMapUsers",
            R.id.swPushNotif, "pushNotifications"
    );

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the elements
        ivChangePFP = view.findViewById(R.id.ivChangePFP);
        swFriendable = view.findViewById(R.id.swFriendable);
        swShowFortunesFriends = view.findViewById(R.id.swShowFortunesFriends);
        swShowFortunesUsers = view.findViewById(R.id.swShowFortunesUsers);
        swShowMapFriends = view.findViewById(R.id.swShowMapFriends);
        swShowMapUsers = view.findViewById(R.id.swShowMapUsers);
        swPushNotif = view.findViewById(R.id.swPushNotif);
        btnLocPerm = view.findViewById(R.id.btnLocPerm);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        changing = false;

        // Store the current user
        try {
            user = ParseUser.getCurrentUser().fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Store the user image
        ParseFile pic = user.getParseFile("profilePic");
        if (pic == null) {
            Glide.with(view.getContext())
                    .load(R.drawable.default_pfp)
                    .circleCrop()
                    .into(ivChangePFP);
        }
        else {
            Glide.with(view.getContext())
                    .load(pic.getUrl())
                    .error(R.drawable.default_pfp)
                    .circleCrop()
                    .into(ivChangePFP);
        }

        // Change all button states
        initializeState(swFriendable);
        initializeState(swShowFortunesFriends);
        initializeState(swShowFortunesUsers);
        initializeState(swShowMapFriends);
        initializeState(swShowMapUsers);
        initializeState(swPushNotif);

        // Put on click listeners on all switches
        swFriendable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(swFriendable);
            }
        });
        swShowFortunesFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(swShowFortunesFriends);
            }
        });
        swShowFortunesUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(swShowFortunesUsers);
            }
        });
        swShowMapFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(swShowMapFriends);
            }
        });
        swShowMapUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(swShowMapUsers);
            }
        });
        swPushNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(swPushNotif);
            }
        });

        // Put an onCLick listener onto the location access button
        btnLocPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the state is changing, do nothing
                if (changing) {
                    return;
                }

                // Make the button unclickable
                btnLocPerm.setClickable(false);
                changing = true;

                // If the user doesn't have permission, ask for permission
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                }
                else {
                    Toast.makeText(requireContext(), "Already granted permission!", Toast.LENGTH_SHORT).show();
                }
                
                // Make the button clickable again
                changing = false;
                btnLocPerm.setClickable(true);
            }
        });

        // When the user profile picture is clicked, allow the
        // user to upload a new profile picture.
        ivChangePFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivChangePFP.setClickable(false);

                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 21);
            }
        });
    }



    // Handle clicks on the switches
    private void handleClick(SwitchCompat Switch) {
        // If the switch is changing, do nothing
        if (changing) {
            return;
        }

        // The switch is now changing
        changing = true;
        Switch.setClickable(false);

        // Get the switch name
        String name = idToString.get(Switch.getId());

        // Get the boolean value
        boolean state = user.getBoolean(name);

        // Swap the boolean value
        boolean newState = Boolean.logicalXor(state, true);

        // Set the new boolean value
        user.put(name, newState);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Unable to change switch", e);
                    Toast.makeText(requireContext(), "Unable to change setting", Toast.LENGTH_SHORT).show();

                    // Change the switch
                    Switch.setChecked(state);
                }
                else {
                    Log.i(TAG, "Switch changed!");

                    // Change the switch
                    Switch.setChecked(newState);
                }

                // Make the switch clickable again
                Switch.setClickable(true);
                changing = false;
            }
        });
    }



    // Given a switch, initialize the state of the switch
    private void initializeState(SwitchCompat Switch) {
        // Get the boolean value
        boolean state = user.getBoolean(idToString.get(Switch.getId()));

        // Initialize the switch
        Switch.setChecked(state);
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
                                            .into(ivChangePFP);

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

        ivChangePFP.setClickable(true);


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