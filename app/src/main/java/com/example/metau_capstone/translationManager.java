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
            entry("Amharic", "am"), //
            entry("Arabic", "ar"),
            entry("Azerbaijani", "az"), //
            entry("Belarusian", "be"),
            entry("Bulgarian", "bg"),
            entry("Bengali", "bn"),
            entry("Bosnian", "bs"), //
            entry("Catalan", "ca"),
            entry("Cebuano", "ceb"), //
            entry("Corsican", "co"), //
            entry("Czech", "cs"),
            entry("Welsh", "cy"),
            entry("Danish", "da"),
            entry("German", "de"),
            entry("Greek", "el"),
            entry("English", "en"),
            entry("Esperanto", "eo"),
            entry("Spanish", "es"),
            entry("Estonian", "et"),
            entry("Basque", "eu"), //
            entry("Persian", "fa"),
            entry("Finnish", "fi"),
            entry("Filipino", "fil"), //
            entry("French", "fr"),
            entry("Western Frisian", "fy"), //
            entry("Irish", "ga"),
            entry("Scots Gaelic", "gd"), //
            entry("Galician", "gl"),
            entry("Gujarati", "gu"),
            entry("Hausa", "ha"), //
            entry("Hawaiian", "haw"), //
            entry("Hebrew", "he"),
            entry("Hindi", "hi"),
            entry("Hmong", "hmn"), //
            entry("Croatian", "hr"),
            entry("Haitian", "ht"),
            entry("Hungarian", "hu"),
            entry("Armenian", "hy"), //
            entry("Indonesian", "id"),
            entry("Igbo", "ig"), //
            entry("Icelandic", "is"), //
            entry("Italian", "it"),
            entry("Japanese", "ja"),
            entry("Javanese", "jv"), //
            entry("Georgian", "ka"),
            entry("Kazakh", "kk"), //
            entry("Khmer", "km"), //
            entry("Kannada", "kn"),
            entry("Korean", "ko"),
            entry("Kurdish", "ku"), //
            entry("Kyrgyz", "ky"), //
            entry("Latin", "la"), //
            entry("Luxembourgish", "lb"), //
            entry("Lao", "lo"), //
            entry("Lithuanian", "lt"),
            entry("Latvian", "lv"),
            entry("Malagasy", "mg"), //
            entry("Maori", "mi"), //
            entry("Macedonian", "mk"),
            entry("Malayalam", "ml"), //
            entry("Mongolian", "mn"), //
            entry("Marathi", "mr"),
            entry("Malay", "ms"),
            entry("Maltese", "mt"),
            entry("Burmese", "my"), //
            entry("Nepali", "ne"), //
            entry("Dutch", "nl"),
            entry("Norwegian", "no"),
            entry("Nyanja", "ny"), //
            entry("Punjabi", "pa"), //
            entry("Polish", "pl"),
            entry("Pashto", "ps"), //
            entry("Portuguese", "pt"),
            entry("Romanian", "ro"),
            entry("Russian", "ru"),
            entry("Sindhi", "sd"), //
            entry("Sinhala", "si"), //
            entry("Slovak", "sk"),
            entry("Slovenian", "sl"),
            entry("Samoan", "sm"), //
            entry("Shona", "sn"), //
            entry("Somali", "so"), //
            entry("Albanian", "sq"),
            entry("Serbian", "sr"), //
            entry("Sesotho", "st"), //
            entry("Sundanese", "su"), //
            entry("Swedish", "sv"),
            entry("Swahili", "sw"),
            entry("Tamil", "ta"),
            entry("Telugu", "te"),
            entry("Tajik", "tg"), //
            entry("Thai", "th"),
            entry("Turkish", "tr"),
            entry("Ukrainian", "uk"),
            entry("Urdu", "ur"),
            entry("Uzbek", "uz"), //
            entry("Vietnamese", "vi"),
            entry("Xhosa", "xh"), //
            entry("Yiddish", "yi"), //
            entry("Yoruba", "yo"), //
            entry("Chinese (Traditional)", "zh"),
            entry("Zulu", "zu") //
    );


    /**
     * Public dictionary of all encodings and their corresponding languages
     */
    public static final Map<String, String> langEncodingsRev = Map.<String, String>ofEntries(
            entry("af", "Afrikaans"),
            entry("am", "Amharic"),
            entry("ar", "Arabic"),
            entry("az", "Azerbaijani"),
            entry("be", "Belarusian"),
            entry("bg", "Bulgarian"),
            entry("bn", "Bengali"),
            entry("bs", "Bosnian"),
            entry("ca", "Catalan"),
            entry("ceb", "Cebuano"),
            entry("co", "Corsican"),
            entry("cs", "Czech"),
            entry("cy", "Welsh"),
            entry("da", "Danish"),
            entry("de", "German"),
            entry("el", "Greek"),
            entry("en", "English"),
            entry("eo", "Esperanto"),
            entry("es", "Spanish"),
            entry("et", "Estonian"),
            entry("eu", "Basque"),
            entry("fa", "Persian"),
            entry("fi", "Finnish"),
            entry("fil", "Filipino"),
            entry("fr", "French"),
            entry("fy", "Western Frisian"),
            entry("ga", "Irish"),
            entry("gd", "Scots Gaelic"),
            entry("gl", "Galician"),
            entry("gu", "Gujarati"),
            entry("ha", "Hausa"),
            entry("haw", "Hawaiian"),
            entry("he", "Hebrew"),
            entry("hi", "Hindi"),
            entry("hmn", "Hmong"),
            entry("hr", "Croatian"),
            entry("ht", "Haitian"),
            entry("hu", "Hungarian"),
            entry("hy", "Armenian"),
            entry("id", "Indonesian"),
            entry("ig", "Igbo"),
            entry("is", "Icelandic"),
            entry("it", "Italian"),
            entry("ja", "Japanese"),
            entry("jv", "Javanese"),
            entry("ka", "Georgian"),
            entry("kk", "Kazakh"),
            entry("km", "Khmer"),
            entry("kn", "Kannada"),
            entry("ko", "Korean"),
            entry("ku", "Kurdish"),
            entry("ky", "Kyrgyz"),
            entry("la", "Latin"),
            entry("lb", "Luxembourgish"),
            entry("lo", "Lao"),
            entry("lt", "Lithuanian"),
            entry("lv", "Latvian"),
            entry("mg", "Malagasy"),
            entry("mi", "Maori"),
            entry("mk", "Macedonian"),
            entry("ml", "Malayalam"),
            entry("mn", "Mongolian"),
            entry("mr", "Marathi"),
            entry("ms", "Malay"),
            entry("mt", "Maltese"),
            entry("my", "Burmese"),
            entry("ne", "Nepali"),
            entry("nl", "Dutch"),
            entry("no", "Norwegian"),
            entry("ny", "Nyanja"),
            entry("pa", "Punjabi"),
            entry("pl", "Polish"),
            entry("ps", "Pashto"),
            entry("pt", "Portuguese"),
            entry("ro", "Romanian"),
            entry("ru", "Russian"),
            entry("sd", "Sindhi"),
            entry("si", "Sinhala"),
            entry("sk", "Slovak"),
            entry("sl", "Slovenian"),
            entry("sm", "Samoan"),
            entry("sn", "Shona"),
            entry("so", "Somali"),
            entry("sq", "Albanian"),
            entry("sr", "Serbian"),
            entry("st", "Sesotho"),
            entry("su", "Sundanese"),
            entry("sv", "Swedish"),
            entry("sw", "Swahili"),
            entry("ta", "Tamil"),
            entry("te", "Telugu"),
            entry("tg", "Tajik"),
            entry("th", "Thai"),
            entry("tr", "Turkish"),
            entry("uk", "Ukrainian"),
            entry("ur", "Urdu"),
            entry("uz", "Uzbek"),
            entry("vi", "Vietnamese"),
            entry("xh", "Xhosa"),
            entry("yi", "Yiddish"),
            entry("yo", "Yoruba"),
            entry("zh", "Chinese (Traditional)"),
            entry("zu", "Zulu")
    );


    /**
     * Public dictionary of all languages and if they are reversed (right to left)
     */
    public static final Map<String, Boolean> langRev = Map.<String, Boolean>ofEntries(
            entry("Afrikaans", false),
            entry("Amharic", false),
            entry("Arabic", true),
            entry("Azerbaijani", false),
            entry("Belarusian", false),
            entry("Bulgarian", false),
            entry("Bengali", false),
            entry("Bosnian", false),
            entry("Catalan", false),
            entry("Cebuano", false),
            entry("Corsican", false),
            entry("Czech", false),
            entry("Welsh", false),
            entry("Danish", false),
            entry("German", false),
            entry("Greek", false),
            entry("English", false),
            entry("Esperanto", false),
            entry("Spanish", false),
            entry("Estonian", false),
            entry("Basque", false),
            entry("Persian", true),
            entry("Finnish", false),
            entry("Filipino", false),
            entry("French", false),
            entry("Western Frisian", false),
            entry("Irish", false),
            entry("Scots Gaelic", false),
            entry("Galician", false),
            entry("Gujarati", false),
            entry("Hausa", false),
            entry("Hawaiian", false),
            entry("Hebrew", true),
            entry("Hindi", false),
            entry("Hmong", false),
            entry("Croatian", false),
            entry("Haitian", false),
            entry("Hungarian", false),
            entry("Armenian", false),
            entry("Indonesian", false),
            entry("Igbo", false),
            entry("Icelandic", false),
            entry("Italian", false),
            entry("Japanese", false),
            entry("Javanese", false),
            entry("Georgian", false),
            entry("Kazakh", false),
            entry("Khmer", false),
            entry("Kannada", false),
            entry("Korean", false),
            entry("Kurdish", false),
            entry("Kyrgyz", false),
            entry("Latin", false),
            entry("Luxembourgish", false),
            entry("Lao", false),
            entry("Lithuanian", false),
            entry("Latvian", false),
            entry("Malagasy", false),
            entry("Maori", false),
            entry("Macedonian", false),
            entry("Malayalam", false),
            entry("Mongolian", false),
            entry("Marathi", false),
            entry("Malay", false),
            entry("Maltese", false),
            entry("Burmese", false),
            entry("Nepali", false),
            entry("Dutch", false),
            entry("Norwegian", false),
            entry("Nyanja", false),
            entry("Punjabi", false),
            entry("Polish", false),
            entry("Pashto", true),
            entry("Portuguese", false),
            entry("Romanian", false),
            entry("Russian", false),
            entry("Sindhi", true),
            entry("Sinhala", false),
            entry("Slovak", false),
            entry("Slovenian", false),
            entry("Samoan", false),
            entry("Shona", false),
            entry("Somali", false),
            entry("Albanian", false),
            entry("Serbian", false),
            entry("Sesotho", false),
            entry("Sundanese", false),
            entry("Swedish", false),
            entry("Swahili", false),
            entry("Tamil", false),
            entry("Telugu", false),
            entry("Tajik", false),
            entry("Thai", false),
            entry("Turkish", false),
            entry("Ukrainian", false),
            entry("Urdu", true),
            entry("Uzbek", false),
            entry("Vietnamese", false),
            entry("Xhosa", false),
            entry("Yiddish", true),
            entry("Yoruba", false),
            entry("Chinese (Traditional)", false),
            entry("Zulu", false)
    );


    /**
     * Public dictionary of all languages and their corresponding translation
     */
    public static final Map<String, String> langTrans = Map.<String, String>ofEntries(
            entry("Afrikaans", "Afrikaans"),
            entry("Amharic", "አማርኛ"),
            entry("Arabic", "عربي"),
            entry("Azerbaijani", "Azərbaycan"),
            entry("Belarusian", "беларускі"),
            entry("Bulgarian", "български"),
            entry("Bengali", "বাংলা"),
            entry("Bosnian", "bosanski"),
            entry("Catalan", "català"),
            entry("Cebuano", "Cebuano"),
            entry("Corsican", "Corsu"),
            entry("Czech", "čeština"),
            entry("Welsh", "Cymraeg"),
            entry("Danish", "dansk"),
            entry("German", "Deutsch"),
            entry("Greek", "Ελληνικά"),
            entry("English", "English"),
            entry("Esperanto", "Esperanto"),
            entry("Spanish", "Español"),
            entry("Estonian", "eesti keel"),
            entry("Basque", "euskara"),
            entry("Persian", "فارسی"),
            entry("Finnish", "Suomalainen"),
            entry("Filipino", "Filipino"),
            entry("French", "Français"),
            entry("Western Frisian", "Westerfrysk"),
            entry("Irish", "Gaeilge"),
            entry("Scots Gaelic", "Gàidhlig na h-Alba"),
            entry("Galician", "galego"),
            entry("Gujarati", "ગુજરાતી"),
            entry("Hausa", "Hausa"),
            entry("Hawaiian", "ʻŌlelo Hawaiʻi"),
            entry("Hebrew", "עִברִית"),
            entry("Hindi", "हिन्दी"),
            entry("Hmong", "Hmoob"),
            entry("Croatian", "Croatiang"),
            entry("Haitian", "ayisyen"),
            entry("Hungarian", "Magyar"),
            entry("Armenian", "հայերեն"),
            entry("Indonesian", "bahasa Indonesia"),
            entry("Igbo", "Igbo"),
            entry("Icelandic", "íslenskur"),
            entry("Italian", "Italiano"),
            entry("Japanese", "日本"),
            entry("Javanese", "basa jawa"),
            entry("Georgian", "ქართული"),
            entry("Kazakh", "қазақ"),
            entry("Khmer", "ខ្មែរ"),
            entry("Kannada", "ಕನ್ನಡ"),
            entry("Korean", "한국인"),
            entry("Kurdish", "Kurdî"),
            entry("Kyrgyz", "Кыргызча"),
            entry("Latin", "Latinus"),
            entry("Luxembourgish", "lëtzebuergesch"),
            entry("Lao", "ພາສາລາວ"),
            entry("Lithuanian", "lietuvių"),
            entry("Latvian", "latviski"),
            entry("Malagasy", "Malagasy"),
            entry("Maori", "Maori"),
            entry("Macedonian", "македонски"),
            entry("Malayalam", "മലയാളം"),
            entry("Mongolian", "Монгол"),
            entry("Marathi", "मराठी"),
            entry("Malay", "Melayu"),
            entry("Maltese", "Malti"),
            entry("Burmese", "မြန်မာ"),
            entry("Nepali", "नेपाली"),
            entry("Dutch", "Nederlands"),
            entry("Norwegian", "norsk"),
            entry("Nyanja", "Nyanja"),
            entry("Punjabi", "ਪੰਜਾਬੀ"),
            entry("Polish", "Polski"),
            entry("Pashto", "پښتو"),
            entry("Portuguese", "Português"),
            entry("Romanian", "Română"),
            entry("Russian", "Русский"),
            entry("Sindhi", "سنڌي"),
            entry("Sinhala", "සිංහල"),
            entry("Slovak", "slovenský"),
            entry("Slovenian", "Slovenščina"),
            entry("Samoan", "Samoa"),
            entry("Shona", "Shona"),
            entry("Somali", "Soomaali"),
            entry("Albanian", "shqiptare"),
            entry("Serbian", "Српски"),
            entry("Sesotho", "Sesotho"),
            entry("Sundanese", "basa Sunda"),
            entry("Swedish", "svenska"),
            entry("Swahili", "kiswahili"),
            entry("Tamil", "தமிழ்"),
            entry("Telugu", "తెలుగు"),
            entry("Tajik", "тоҷикӣ"),
            entry("Thai", "ไทย"),
            entry("Turkish", "Türk"),
            entry("Ukrainian", "українська"),
            entry("Urdu", "اردو"),
            entry("Uzbek", "o'zbek"),
            entry("Vietnamese", "Tiếng Việt"),
            entry("Xhosa", "isiXhosa"),
            entry("Yiddish", "יידיש"),
            entry("Yoruba", "Yoruba"),
            entry("Chinese (Traditional)", "中國人"),
            entry("Zulu", "Zulu")
    );

    // English translation string
    public String english;

    // Current language to translate to
    public String lang;

    // The actual translator used to translate strings
    private Translator translator;

    // Manages downloaded models
    private RemoteModelManager modelManager;


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
        // Create the new translator
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(english)
                .setTargetLanguage(language)
                .build();
        translator = Translation.getClient(options);

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

                            // Download the new model
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
                                                    if (listener != null) {
                                                        listener.onLanguageSet();
                                                    }
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
