package com.example.metau_capstone;


import static java.util.Map.entry;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;


/**
 * This class is used to translate the English strings to
 * any other language if needed
 */
public class translationManager {
    public static final String TAG = "translationManager";

    /**
     * Public dictionary of all languages and their corresponding encoding
     */
    public static final Map<String, String> langEncodings = Map.<String, String>ofEntries(
            entry("Afrikaans", "af"),
            entry("Arabic", "ar"),
            entry("Belarusian", "be"),
            entry("Bulgarian", "bg"),
            entry("Bengali", "bn"),
            entry("Catalan", "ca"),
            entry("Czech", "cs"),
            entry("Welsh", "cy"),
            entry("Danish", "da"),
            entry("German", "de"),
            entry("Greek", "el"),
            entry("English", "en"),
            entry("Esperanto", "eo"),
            entry("Spanish", "es"),
            entry("Estonian", "et"),
            entry("Persian", "fa"),
            entry("Finnish", "fi"),
            entry("French", "fr"),
            entry("Irish", "ga"),
            entry("Galician", "gl"),
            entry("Gujarati", "gu"),
            entry("Hebrew", "he"),
            entry("Hindi", "hi"),
            entry("Croatian", "hr"),
            entry("Haitian", "ht"),
            entry("Hungarian", "hu"),
            entry("Indonesian", "id"),
            entry("Icelandic", "is"),
            entry("Italian", "it"),
            entry("Japanese", "ja"),
            entry("Georgian", "ka"),
            entry("Kannada", "kn"),
            entry("Korean", "ko"),
            entry("Lithuanian", "lt"),
            entry("Latvian", "lv"),
            entry("Macedonian", "mk"),
            entry("Marathi", "mr"),
            entry("Malay", "ms"),
            entry("Maltese", "mt"),
            entry("Dutch", "nl"),
            entry("Norwegian", "no"),
            entry("Polish", "pl"),
            entry("Portuguese", "pt"),
            entry("Romanian", "ro"),
            entry("Russian", "ru"),
            entry("Slovak", "sk"),
            entry("Slovenian", "sl"),
            entry("Albanian", "sq"),
            entry("Swedish", "sv"),
            entry("Swahili", "sw"),
            entry("Tamil", "ta"),
            entry("Telugu", "te"),
            entry("Thai", "th"),
            entry("Turkish", "tr"),
            entry("Ukrainian", "uk"),
            entry("Urdu", "ur"),
            entry("Vietnamese", "vi"),
            entry("Chinese (Traditional)", "zh")
    );


    /**
     * Public dictionary of all encodings and their corresponding languages
     */
    public static final Map<String, String> langEncodingsRev = Map.<String, String>ofEntries(
            entry("af", "Afrikaans"),
            entry("ar", "Arabic"),
            entry("be", "Belarusian"),
            entry("bg", "Bulgarian"),
            entry("bn", "Bengali"),
            entry("ca", "Catalan"),
            entry("cs", "Czech"),
            entry("cy", "Welsh"),
            entry("da", "Danish"),
            entry("de", "German"),
            entry("el", "Greek"),
            entry("en", "English"),
            entry("eo", "Esperanto"),
            entry("es", "Spanish"),
            entry("et", "Estonian"),
            entry("fa", "Persian"),
            entry("fi", "Finnish"),
            entry("fr", "French"),
            entry("ga", "Irish"),
            entry("gl", "Galician"),
            entry("gu", "Gujarati"),
            entry("he", "Hebrew"),
            entry("hi", "Hindi"),
            entry("hr", "Croatian"),
            entry("ht", "Haitian"),
            entry("hu", "Hungarian"),
            entry("id", "Indonesian"),
            entry("is", "Icelandic"),
            entry("it", "Italian"),
            entry("ja", "Japanese"),
            entry("ka", "Georgian"),
            entry("kn", "Kannada"),
            entry("ko", "Korean"),
            entry("lt", "Lithuanian"),
            entry("lv", "Latvian"),
            entry("mk", "Macedonian"),
            entry("ml", "Malayalam"),
            entry("mr", "Marathi"),
            entry("ms", "Malay"),
            entry("mt", "Maltese"),
            entry("nl", "Dutch"),
            entry("no", "Norwegian"),
            entry("pl", "Polish"),
            entry("pt", "Portuguese"),
            entry("ro", "Romanian"),
            entry("ru", "Russian"),
            entry("sk", "Slovak"),
            entry("sl", "Slovenian"),
            entry("sq", "Albanian"),
            entry("sv", "Swedish"),
            entry("sw", "Swahili"),
            entry("ta", "Tamil"),
            entry("te", "Telugu"),
            entry("th", "Thai"),
            entry("tr", "Turkish"),
            entry("uk", "Ukrainian"),
            entry("ur", "Urdu"),
            entry("vi", "Vietnamese"),
            entry("zh", "Chinese (Traditional)")
    );


    /**
     * Public dictionary of all languages and if they are reversed (right to left)
     */
    public static final Map<String, Boolean> langRev = Map.<String, Boolean>ofEntries(
            entry("Afrikaans", false),
            entry("Arabic", true),
            entry("Belarusian", false),
            entry("Bulgarian", false),
            entry("Bengali", false),
            entry("Catalan", false),
            entry("Czech", false),
            entry("Welsh", false),
            entry("Danish", false),
            entry("German", false),
            entry("Greek", false),
            entry("English", false),
            entry("Esperanto", false),
            entry("Spanish", false),
            entry("Estonian", false),
            entry("Persian", true),
            entry("Finnish", false),
            entry("French", false),
            entry("Irish", false),
            entry("Galician", false),
            entry("Gujarati", false),
            entry("Hebrew", true),
            entry("Hindi", false),
            entry("Croatian", false),
            entry("Haitian", false),
            entry("Hungarian", false),
            entry("Indonesian", false),
            entry("Icelandic", false),
            entry("Italian", false),
            entry("Japanese", false),
            entry("Georgian", false),
            entry("Kannada", false),
            entry("Korean", false),
            entry("Lithuanian", false),
            entry("Latvian", false),
            entry("Macedonian", false),
            entry("Marathi", false),
            entry("Malay", false),
            entry("Maltese", false),
            entry("Dutch", false),
            entry("Norwegian", false),
            entry("Polish", false),
            entry("Portuguese", false),
            entry("Romanian", false),
            entry("Russian", false),
            entry("Slovak", false),
            entry("Slovenian", false),
            entry("Albanian", false),
            entry("Swedish", false),
            entry("Swahili", false),
            entry("Tamil", false),
            entry("Telugu", false),
            entry("Thai", false),
            entry("Turkish", false),
            entry("Ukrainian", false),
            entry("Urdu", true),
            entry("Vietnamese", false),
            entry("Chinese (Traditional)", false)
    );


    /**
     * Public dictionary of all languages and their corresponding translation
     */
    public static final Map<String, String> langTrans = Map.<String, String>ofEntries(
            entry("Afrikaans", "Afrikaans"),
            entry("Arabic", "عربي"),
            entry("Belarusian", "беларускі"),
            entry("Bulgarian", "български"),
            entry("Bengali", "বাংলা"),
            entry("Catalan", "català"),
            entry("Czech", "čeština"),
            entry("Welsh", "Cymraeg"),
            entry("Danish", "dansk"),
            entry("German", "Deutsch"),
            entry("Greek", "Ελληνικά"),
            entry("English", "English"),
            entry("Esperanto", "Esperanto"),
            entry("Spanish", "Español"),
            entry("Estonian", "eesti keel"),
            entry("Persian", "فارسی"),
            entry("Finnish", "Suomalainen"),
            entry("French", "Français"),
            entry("Irish", "Gaeilge"),
            entry("Galician", "galego"),
            entry("Gujarati", "ગુજરાતી"),
            entry("Hebrew", "עִברִית"),
            entry("Hindi", "हिन्दी"),
            entry("Croatian", "Croatiang"),
            entry("Haitian", "ayisyen"),
            entry("Hungarian", "Magyar"),
            entry("Indonesian", "bahasa Indonesia"),
            entry("Icelandic", "íslenskur"),
            entry("Italian", "Italiano"),
            entry("Japanese", "日本"),
            entry("Georgian", "ქართული"),
            entry("Kannada", "ಕನ್ನಡ"),
            entry("Korean", "한국인"),
            entry("Lithuanian", "lietuvių"),
            entry("Latvian", "latviski"),
            entry("Macedonian", "македонски"),
            entry("Marathi", "मराठी"),
            entry("Malay", "Melayu"),
            entry("Maltese", "Malti"),
            entry("Dutch", "Nederlands"),
            entry("Norwegian", "norsk"),
            entry("Polish", "Polski"),
            entry("Portuguese", "Português"),
            entry("Romanian", "Română"),
            entry("Russian", "Русский"),
            entry("Slovak", "slovenský"),
            entry("Slovenian", "Slovenščina"),
            entry("Albanian", "shqiptare"),
            entry("Swedish", "svenska"),
            entry("Swahili", "kiswahili"),
            entry("Tamil", "தமிழ்"),
            entry("Telugu", "తెలుగు"),
            entry("Thai", "ไทย"),
            entry("Turkish", "Türk"),
            entry("Ukrainian", "українська"),
            entry("Urdu", "اردو"),
            entry("Vietnamese", "Tiếng Việt"),
            entry("Chinese (Traditional)", "中國人")
    );

    // English translation string
    public String english;

    // Current language to translate to
    public String lang;

    // The actual translator used to translate strings from english -> language
    private Translator translator;

    // The actual translator used to translate strings from language -> english
    private Translator translatorRev;

    // Manages downloaded models
    private final RemoteModelManager modelManager;


    /**
     * Given an initial language to translate to, setup the class to
     * easily translate any string
     * @param language A string which is the language to translate from English to
     * @param listener A callback listener to know when the manager has been setup
     */
    public translationManager(String language, onLanguageSetListener... listener) {
        // Store the English language string for later use
        english = TranslateLanguage.ENGLISH;

        // Model manager to manage downloaded models
        modelManager = RemoteModelManager.getInstance();

        // Set the current language as null and setup the new
        // language
        lang = null;
        if (listener.length != 0) {
            setLanguage(language, listener[0]);
        }
        else {
            setLanguage(language, null);
        }
    }


    /**
     * Class used to handle callbacks to know when the language has been changed
     */
    public interface onLanguageSetListener {
        void onLanguageSet();
    }
    /**
     * Given a new language, delete the old model and download a new model
     * for later use
     * @param language A string which is the language to translate from English to
     * @param listener (Optional) A callback listener to know when the language has been changed
     */
    public void setLanguage(String language, @Nullable onLanguageSetListener listener) {

        // Create the new translator from english -> language
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(english)
                .setTargetLanguage(language)
                .build();
        translator = Translation.getClient(options);

        // Create the new translator from language -> english
        TranslatorOptions optionsRev = new TranslatorOptions.Builder()
                .setSourceLanguage(language)
                .setTargetLanguage(english)
                .build();
        translatorRev = Translation.getClient(optionsRev);

        // Download the new model if needed and have a callback
        // when the model is done downloading
        modelManager.isModelDownloaded(new TranslateRemoteModel.Builder(language).build())
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        // If the mode is downloaded, send back the callback
                        if (aBoolean) {
                            if (listener != null) {
                                lang = language;
                                listener.onLanguageSet();
                            }
                        }
                        // If the model is not downloaded, download it, then send
                        // the callback
                        else {

                            // If a new model needs to be downloaded, delete any old models
                            // in the background
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    for (String l : TranslateLanguage.getAllLanguages()) {
                                        // If the string is the current model, don't delete it
                                        if (Objects.equals(l, lang)) {
                                            continue;
                                        }

                                        TranslateRemoteModel modelToDel =
                                                new TranslateRemoteModel.Builder(l).build();
                                        modelManager.deleteDownloadedModel(modelToDel)
                                                .addOnSuccessListener(new OnSuccessListener() {
                                                    @Override
                                                    public void onSuccess(Object o) {
                                                        // Log that the model has been deleted
                                                        Log.i(TAG, "Model deleted");
                                                    }
                                                });
                                    }
                                }
                            });

                            // Download the new model from english -> language
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
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
                                                    if (listener != null) {
                                                        listener.onLanguageSet();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // If the model had an issue downloading, log the error
                                                    Log.e(TAG, "Issue downloading new model", e);
                                                    lang = english;
                                                    if (listener != null) {
                                                        listener.onLanguageSet();
                                                    }
                                                }
                                            });
                                }
                            }, 1);


                            // Download the new model from language -> english
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DownloadConditions conditions = new DownloadConditions.Builder()
                                            .requireWifi()
                                            .build();
                                    translatorRev.downloadModelIfNeeded(conditions)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    // When the model has been downloaded, log it and save the
                                                    // new language
                                                    Log.i(TAG, "Reversed Model downloaded");
                                                    lang = language;
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // If the model had an issue downloading, log the error
                                                    Log.e(TAG, "Issue downloading new reversed model", e);
                                                    lang = english;
                                                }
                                            });
                                }
                            }, 1);

                        }
                    }
                });

    }



    /**
     * Given a text view and the id of a string resource to translate, translate the
     * text and put it into the text view
     * @param view A text view to add text to
     * @param textId The string id of the text to translate
     * @param context Context used to get the string
     */
    public void addText(TextView view, int textId, Context context) {
        addText(view, context.getString(textId));
    }


    /**
     * Given a text view and text to translate, translate the text and put it into
     * The text view
     * @param view A text view to add text to
     * @param text The text to translate and put into the view
     */
    public void addText(TextView view, String text) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            view.setText(text);
            return;
        }

        // Remove all text currently in the view
        //view.setText("");

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, put the text in the view
                        view.setText(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // put the untranslated text in the view
                        Log.e(TAG, "Error translating text", e);
                        view.setText(text);
                    }
                });
    }


    /**
     * Given a Tab and text to translate, translate the text and put it into
     * The text view in the tab
     * @param tab A tab to add text to
     * @param text The text to translate and put into the view
     */
    public void addText(TabLayout.Tab tab, String text) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            tab.setText(text);
            return;
        }

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, put the text in the view
                        tab.setText(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // put the untranslated text in the view
                        Log.e(TAG, "Error translating text", e);
                        tab.setText(text);
                    }
                });
    }


    /**
     * Given a Switch and text to translate, translate the text and put it into
     * The text view in the switch
     * @param switchCompat A switch to add text to
     * @param text The text to translate and put into the view
     */
    public void addText(SwitchCompat switchCompat, String text) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            switchCompat.setText(text);
            return;
        }

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, put the text in the view
                        switchCompat.setText(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // put the untranslated text in the view
                        Log.e(TAG, "Error translating text", e);
                        switchCompat.setText(text);
                    }
                });
    }


    /**
     * Given a Button and text to translate, translate the text and put it into
     * The text view in the button
     * @param button A button to add text to
     * @param text The text to translate and put into the view
     */
    public void addText(Button button, String text) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            button.setText(text);
            return;
        }

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, put the text in the view
                        button.setText(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // put the untranslated text in the view
                        Log.e(TAG, "Error translating text", e);
                        button.setText(text);
                    }
                });
    }


    /**
     * Given a Menu Item and text to translate, translate the text and put it into
     * The text view in the menu item
     * @param item A menu item to add text to
     * @param text The text to translate and put into the view
     */
    public void addText(MenuItem item, String text) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            item.setTitle(text);
            return;
        }

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, put the text in the view
                        item.setTitle(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // put the untranslated text in the view
                        Log.e(TAG, "Error translating text", e);
                        item.setTitle(text);
                    }
                });
    }


    /**
     * Given a text view and text to translate, translate the text and set it
     * as a hint in the text view
     * @param view A text view to add text to
     * @param text The text to translate and put into the view
     */
    public void addHint(TextView view, String text) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            view.setHint(text);
            return;
        }

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, put the text in the view
                        view.setHint(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // put the untranslated text in the view
                        Log.e(TAG, "Error translating text", e);
                        view.setHint(text);
                    }
                });
    }


    /**
     * Given context and text to translate, translate the text and display a toast
     * @param context The context to display the toast with
     * @param text The text to translate and put in the toast
     */
    public void createToast(Context context, String text) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return;
        }

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, show the translated toast
                        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // show the toast in english
                        Log.e(TAG, "Error translating text", e);
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Class used to handle callbacks to know when text has been translated
     */
    public interface onCompleteListener {
        void onComplete(String text);
    }
    /**
     * Given text to translate, translate the text and return the translated text
     * @param text The text to translate
     */
    public void getText(String text, onCompleteListener listener) {
        // If the language is english, don't worry about
        // translating
        if (Objects.equals(lang, "en")) {
            listener.onComplete(text);
            return;
        }

        // Translate the text
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        // If the translation succeeds, return the translated text
                        listener.onComplete(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If the translation does not succeed, log it and
                        // return the english text
                        Log.e(TAG, "Error translating text", e);
                        listener.onComplete(text);
                    }
                });
    }

}
