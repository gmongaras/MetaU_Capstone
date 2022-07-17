package Fragments.Main;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.metau_capstone.Fortune;
import com.example.metau_capstone.R;
import com.example.metau_capstone.WakefulReceiver;
import com.example.metau_capstone.offlineHelpers;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is used to manage the Home Fortune Fragment
 */
public class HomeFragment_fortune extends Fragment {

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

    // Animations
    protected AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
    protected AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;

    public HomeFragment_fortune() {
        // Required empty public constructor
    }

    public static HomeFragment_fortune newInstance() {
        return new HomeFragment_fortune();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        avCookie.setClickable(true);
        //ivFortune = view.findViewById(R.id.ivFortune);

        // Create the animations
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        fadeOut.setDuration(1200);
        fadeOut.setFillAfter(true);
        fadeOut.setStartOffset(4200+fadeIn.getStartOffset());

        // Load the model
        try {
            module = LiteModuleLoader.load(assetFilePath(view.getContext(), "model-2.ptl"));
        } catch (IOException e) {
            Log.e(TAG, "Unable to load model", e);
            return;
        }

        // Load in the dictionary
        try {
            vocab = loadVocab(assetFilePath(view.getContext(), "vocab-2.csv"));
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

                // Get the mode in which the user wants a fortune
                boolean mode = ParseUser.getCurrentUser().getBoolean("useAI");

                // If the mode is false, load in the database of fortunes
                List<String> fortunes = null;
                if (mode == false) {
                    // Try to get the data
                    try {
                        fortunes = readByJavaClassic(assetFilePath(requireContext(), "real_fortunes.txt"));
                    }
                    // If an error occurred, default to using the AI
                    catch (IOException e) {
                        mode = true;
                    }
                }

                // Play the animation
                avCookie.setClickable(false);
                avCookie.playAnimation();
                boolean finalMode = mode;
                List<String> finalFortunes = fortunes;
                avCookie.addAnimatorListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // When the animation is over, get the fortune

                        String fortune;

                        // If the mode is true, get the fortune from the ai
                        if (finalMode == true) {

                            // Preparing trash input tensor
                            int sequence_length = 64;
                            long[] shape = new long[]{sequence_length};
                            inputTensor = generateTensor(shape);

                            // Get the output
                            long[] scores = module.forward(IValue.from(inputTensor)).toTensor().getDataAsLongArray();

                            // Get the output sequence
                            StringBuilder text = new StringBuilder();
                            for (long score : scores) {
                                // If a <START>, <PAD>, or <UNKNOWN> token is seen,
                                // skip it
                                if (score == 0 || score == 1 || score == 3) {
                                    continue;
                                }
                                // When an <END> token is reached, stop loading in the text
                                if (score == 2) {
                                    break;
                                }
                                text.append(vocab.get((int) score)).append(" ");
                            }

                            // Save the fortune
                            fortune = text.toString();

                        }

                        // If the mode is false, get a fortune from a
                        // list of fortunes
                        else {
                            // Get a random number in the range of the number
                            // of loaded fortunes
                            int i = ThreadLocalRandom.current().nextInt(0, finalFortunes.size() + 1);

                            // Get the fortune and save it
                            fortune = finalFortunes.get(i);
                        }

                        // Save the fortune to the database
                        saveFortune(fortune);

                        // Make the fortune invisible and the text visible
                        avCookie.setAnimation(fadeOut);
                        tvFortuneText.setAnimation(fadeIn);
                        tvTextPrompt.setAnimation(fadeIn);

                        // Change the displayed text
                        tvTextPrompt.setText(R.string.promptAfter);
                        tvFortuneText.setText(fortune);
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


    // Read in a given filename
    private static List<String> readByJavaClassic(String fileName) throws IOException {

        List<String> result = new ArrayList<>();
        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return result;
    }


    // Given a fortune, save the fortune to the database under this user
    public void saveFortune(String fortune) {
        // Save the context
        Context context = requireContext();

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
        newFortune.setLikeCt(0);

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
                                // if the user wants to have notifications
                                ParseUser user = null;
                                try {
                                    user = ParseUser.getCurrentUser().fetch();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                                if (user != null && user.getBoolean("pushNotifications") == true) {
                                    WakefulReceiver wr = new WakefulReceiver();
                                    wr.setAlarm(requireContext());
                                }

                                // When the fortune has been created and saved,
                                // recreate the database on the user's device
                                // and save all the fortunes to it
                                (new offlineHelpers()).createDatabase(context);
                            }
                        }
                    });
                }
            }
        });
    }



    /**
     * Generate a random array given the size of the array
     * @param Size A long array with the size of the wanted output array
     * @return A tensor of random floats with the same shape as the given size
     */
    public Tensor generateTensor(long[] Size) {
        // Create a random array of floats
        Random rand = new Random();
        double[] arr = new double[(int)(Size[0])];
        for (int i = 0; i < Size[0]; i++) {
            arr[i] = rand.nextGaussian();
        }

        // Create the tensor
        Tensor tensor = Tensor.fromBlob(arr, Size);

        // Return the tensor
        return tensor;
    }

    /**
     * Given a the filename of the vocab file, load it into memory
     * @param fileName The filename of the vocab file
     * @return A map mapping integer values (which will be outputted by the model)
     *          to Strings (which will be the word to model predicted)
     *          containing the entire vocab.
     * @throws IOException
     */
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