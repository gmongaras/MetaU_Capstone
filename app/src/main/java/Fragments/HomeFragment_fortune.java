package Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.metau_capstone.R;

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
import java.util.HashMap;
import java.util.Map;
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
    TextView tvText;
    Button btnInfer;

    // Pytorch model
    Module module;

    // Input tensor
    Tensor inputTensor;

    // Saved vocab
    Map<Integer, String> vocab;

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
        tvText = view.findViewById(R.id.tvText);
        btnInfer = view.findViewById(R.id.btnInfer);

        // Load the model
        try {
            //module = Module.load(assetFilePath(this, "model3.ptl"));
            module = LiteModuleLoader.load(assetFilePath(view.getContext(), "model5.ptl"));
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

        // Set on an onClick listener to the infer button
        btnInfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preparing trash input tensor
                int sequence_length = 64;
                int embedding_size = 10;
                long[] shape = new long[]{sequence_length, embedding_size};
                inputTensor = generateTensor(shape);

                // Get the output
                long[] scores = module.forward(IValue.from(inputTensor)).toTensor().getDataAsLongArray();

                // Get the output sequence
                StringBuilder text = new StringBuilder();
                for (long score : scores) {
                    text.append(vocab.get((int) score)).append(" ");
                }

                // Change the displayed string
                tvText.setText(text.toString());
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