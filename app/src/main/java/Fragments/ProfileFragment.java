package Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.metau_capstone.EndlessRecyclerViewScrollListener;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.LoginActivity;
import com.example.metau_capstone.ProfileAdapter;
import com.example.metau_capstone.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // Number to skip when loading more posts
    private int skipVal;

    // Constant number to load each time we want to load more posts
    private static final int loadRate = 20;

    private static final String TAG = "ProfileFragment";

    // Elements in the view
    ImageView ivProfileImage;
    TextView tvUsername;
    RecyclerView rvProfile;
    Button btnLogout;

    // Recycler view stuff
    LinearLayoutManager layoutManager;
    ProfileAdapter adapter;

    // List of fortunes for the recycler view
    List<Fortune> Fortunes;

    // Current user logged in
    ParseUser user;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the elements
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvUsername = view.findViewById(R.id.tvUsername);
        rvProfile = view.findViewById(R.id.rvProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Get the current user
        user = ParseUser.getCurrentUser();

        // Store the username
        tvUsername.setText(user.getUsername());

        // Store the user image
        ParseQuery<ParseUser> q = new ParseQuery<ParseUser>(ParseUser.class);
        q.whereEqualTo("objectId", user.getObjectId());
        q.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> user, ParseException e) {
                ParseFile pic = user.get(0).getParseFile("profilePic");
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
            }
        });

        // Initialize the fortunes
        Fortunes = new ArrayList<>();

        // Load in the fortunes
        queryFortunes();

        // When the user profile picture is clicked, allow the
        // user to upload a new profile picture.
        // Do this only if the mode is main
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivProfileImage.setClickable(false);

                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 21);
            }
        });

        // Put an onClick listener onto the logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log the user out
                Toast.makeText(view.getContext(), "Logging out..", Toast.LENGTH_SHORT).show();
                ParseUser.logOutInBackground();

                // Exit this fragment
                requireActivity().onBackPressed();

                // Go back to the main page
                Intent i = new Intent(view.getContext(), LoginActivity.class);
                startActivity(i);
            }
        });
    }




    // Get fortunes the user owns
    private void queryFortunes() {
        // Specify which class to query
        ParseQuery<Fortune> query = ParseQuery.getQuery(Fortune.class);

        // Include data from the user table
        query.include(Fortune.KEY_USER);

        // Get only this user's fortunes
        query.whereEqualTo("user", user);

        // Have the newest fortunes on top
        query.orderByDescending(Fortune.KEY_TIME_CREATED);

        // Skip some fortunes that have already been loaded
        query.setSkip(skipVal*loadRate);

        // Set the limit to loadRate
        query.setLimit(loadRate);

        // Find all the fortunes the user owns
        query.findInBackground(new FindCallback<Fortune>() {
            @Override
            public void done(List<Fortune> objects, ParseException e) {
                // If an error occurred, log an error
                if (e != null) {
                    Log.e(TAG, "Issue retrieving all posts", e);
                    return;
                }

                // Store all new fortunes in the Fortunes list
                Fortunes.addAll(objects);

                // Setup the recycler view if it isn't setup
                if (rvProfile.getAdapter() == null) {

                    // When the fortunes have been loaded, setup the recycler view -->
                    // Bind the adapter to the recycler view
                    adapter = new ProfileAdapter(Fortunes, getContext(), requireActivity().getSupportFragmentManager());
                    rvProfile.setAdapter(adapter);

                    // Configure the Recycler View: Layout Manager
                    layoutManager = new LinearLayoutManager(getContext());
                    rvProfile.setLayoutManager(layoutManager);

                    // Used for infinite scrolling
                    rvProfile.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                        @Override
                        public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                            queryFortunes();
                        }
                    });
                }
                else {
                    // Notify the recycler view adapter of a change in data
                    adapter.notifyDataSetChanged();
                }

                // Increase the skip value
                skipVal+=1;
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