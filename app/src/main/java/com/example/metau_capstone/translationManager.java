package com.example.metau_capstone;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Set;

/**
 * This class is used to translate the English strings to
 * any other language if needed
 */
public class translationManager {
    public static final String TAG = "translationManager";

    // English translation string
    String english;

    // Current language to translate to
    String lang;

    // The actual translator used to translate strings
    Translator translator;

    // Manages downloaded models
    RemoteModelManager modelManager;


    /**
     * Given an initial language to translate to, setup the class to
     * easily translate any string
     * @param language A string which is the language to translate from English to
     */
    public translationManager(String language) {
        // Store the English language string for later use
        english = TranslateLanguage.ENGLISH;

        // Model manager to manage downloaded models
        modelManager = RemoteModelManager.getInstance();

        // Set the current language as null and setup the new
        // language
        lang = null;
        setLanguage(language);
    }

    /**
     * Given a new language, delete the old model and download a new model
     * for later use
     * @param language A string which is the language to translate from English to
     */
    public void setLanguage(String language) {
        // If the language is not null, delete the old model
        if (lang != null) {
            // Delete the old model
            TranslateRemoteModel modelToDel =
                    new TranslateRemoteModel.Builder(lang).build();
            modelManager.deleteDownloadedModel(modelToDel)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            // Log that the model has been deleted
                            Log.i(TAG, "Model deleted");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Log that the model has not been deleted
                            Log.e(TAG, "MIssue deleteing model", e);
                        }
                    });
        }

        // Create the new translator
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(english)
                .setTargetLanguage(language)
                .build();
        translator = Translation.getClient(options);

        // Download the new model if needed
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // When the model has been downloaded, log it and save the
                        // new language
                        Log.i(TAG, "Model downloaded");
                        lang = language;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the model had an issue downloading, log the error
                        Log.e(TAG, "Issue downloading new model", e);
                    }
                });
    }
}
