package Fragments;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.MainActivity;
import com.example.metau_capstone.R;
import com.example.metau_capstone.WakefulReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment_fortune#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment_fortune extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "HomeFragment_fortune";

    // Elements in the view
    LottieAnimationView avCookie;
    TextView tvFortuneText;
    TextView tvTextPrompt;
    //ImageView ivFortune;

    // Pytorch model
    Module module;

    // Input tensor
    Tensor inputTensor;

    // Saved vocab
    Map<Integer, String> vocab;

    // Used for location
    FusedLocationProviderClient fusedLocationProviderClient;
    Location userLoc;

    // Animations
    protected AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;

    public HomeFragment_fortune() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment_fortune.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment_fortune newInstance(String param1, String param2) {
        HomeFragment_fortune fragment = new HomeFragment_fortune();
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
            // Get the parameters and save them
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_fortune, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the elements
        tvFortuneText = view.findViewById(R.id.tvFortuneText);
        tvTextPrompt = view.findViewById(R.id.tvTextPrompt);
        avCookie = view.findViewById(R.id.avCookie);
        //ivFortune = view.findViewById(R.id.ivFortune);

        // Create the animations
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        fadeOut.setDuration(1200);
        fadeOut.setFillAfter(true);
        fadeOut.setStartOffset(4200+fadeIn.getStartOffset());

        // Load the model
        try {
            //module = Module.load(assetFilePath(this, "model3.ptl"));
            module = LiteModuleLoader.load(assetFilePath(view.getContext(), "model7.ptl"));
        } catch (IOException e) {
            Log.e(TAG, "Unable to load model", e);
            return;
        }

        // Load in the dictionary
        try {
            vocab = loadVocab(assetFilePath(view.getContext(), "vocab.csv"));
        } catch (IOException e) {
            Log.e(TAG, "Unable to load file", e);
        }

        // Set on an onClick listener to the cookie
        avCookie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If we don't have location permission, ask for permission.
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // If we don't have permission, request permission and don't open the fortune
                    Toast.makeText(requireContext(), "Location required to get fortune", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                    return;
                }

                // Play the animation
                avCookie.playAnimation();
                avCookie.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // When the animation is over, get the fortune

                        // Preparing trash input tensor
                        int sequence_length = 64;
                        int embedding_size = 20;
                        long[] shape = new long[]{sequence_length, embedding_size};
                        inputTensor = generateTensor(shape);

                        // Get the output
                        long[] scores = module.forward(IValue.from(inputTensor)).toTensor().getDataAsLongArray();

                        // Get the output sequence
                        StringBuilder text = new StringBuilder();
                        for (long score : scores) {
                            text.append(vocab.get((int) score)).append(" ");
                        }

                        // Save the fortune to the database
                        saveFortune(text.toString());

                        // Make the fortune invisible and the text visible
                        avCookie.setAnimation(fadeOut);
                        tvFortuneText.setAnimation(fadeIn);
                        tvTextPrompt.setAnimation(fadeIn);
//                        avCookie.setVisibility(View.INVISIBLE);
//                        tvFortuneText.setVisibility(View.VISIBLE);

                        // Change the displayed text
                        tvTextPrompt.setText(R.string.promptAfter);
                        tvFortuneText.setText(text.toString());
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });


            }
        });
    }


    // Given a fortune, save the fortune to the database under this user
    public void saveFortune(String fortune) {
        // Setup the location manager to get the location
        LocationManager locationManager = (LocationManager) getContext().getApplicationContext().getSystemService(requireContext().LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have permission, request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            return;
        }

        // Get the location of the phone
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // The saved location
        ParseGeoPoint loc = null;

        // If the location is not null, save the location
        if (location != null) {
            loc = new ParseGeoPoint();

            loc.setLongitude(location.getLongitude());
            loc.setLatitude(location.getLatitude());
        }

        // Create a new fortune
        Fortune newFortune = new Fortune();

        // Add contents to the fortune
        if (loc != null) {
            newFortune.setLocation(loc);
        }
        newFortune.setMessage(fortune);
        newFortune.setUser(ParseUser.getCurrentUser());

        // Send the fortune to the database
        newFortune.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error saving fortune", e);
                }
                else {
                    Log.i(TAG, "Fortune saved!");

                    // Save the fortune to the User's fortune list
                    ParseRelation<Fortune> fortunes = ParseUser.getCurrentUser().getRelation("fortunes");
                    fortunes.add(newFortune);

                    newFortune.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error adding fortune to fortune list", e);
                            }
                            else {
                                Log.i(TAG, "Fortune successfully saved to fortune list");

                                // When the user opens the fortune, create a push notification timer
                                WakefulReceiver wr = new WakefulReceiver();
                                wr.setAlarm(requireContext());
                            }
                        }
                    });
                }
            }
        });
    }




    // Generate a random array given the 2-D size of the array
    public Tensor generateTensor(long[] Size) {
        // Create a random array of floats
        Random rand = new Random();
        float[] arr = new float[(int)(Size[0]*Size[1])];
        for (int i = 0; i < Size[0]; i++) {
            arr[i] = rand.nextFloat();
        }

        // Create the tensor
        Tensor tensor = Tensor.fromBlob(arr, Size);

        // Return the tensor
        return tensor;
    }

    // Load in the vocab given the filename
    public Map<Integer, String> loadVocab(String fileName) throws IOException {
        // Open the file
        FileInputStream fstream = new FileInputStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        // The vocab map
        Map<Integer, String> vocab = new HashMap<>();

        // Read the file line by line
        String strLine;
        while ((strLine = br.readLine()) != null) {
            String[] line = strLine.split(",");
            if (line.length >= 2) {
                vocab.put(Integer.parseInt(line[0]), line[1]);
            }
        }

        // Close the file stream
        fstream.close();

        // Return the map
        return vocab;
    }


    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}