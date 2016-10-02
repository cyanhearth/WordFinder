package com.example.cyanhearth.wordfinder;

import android.app.DialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity{
    private static final String BASE_URI = "https://en.wiktionary.org/wiki/";

    private static final String STATE_LETTERS = "state_letters";
    private static final String STATE_INCLUDE = "state_include";
    private static final String STATE_INCLUDE_WORD = "state_include_word";

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String TAG_RESULTS_FRAGMENT = "results_fragment";
    private static final String TAG_DIALOG = "dialog";

    public static final String DICTIONARY_KEY = "dict";
    public static final String LETTERS_KEY = "letters";
    public static final String INCLUDE_KEY = "include";

    public static final boolean WIFI_DEFAULT = false;
    private static final String DEFAULT_DICTIONARY_STRING = "sowpods";

    private static final int MAX_LETTERS = 16;
    private static final int MAX_BLANKS_SEARCH = 2;

    private static final String SHOWCASE_ID = "startup_showcase";

    // Current network preference
    public static boolean wifiOnly;

    public ArrayList<String> words;

    private String includeWord;

    private TextView includeTextView;
    private TextView lettersInput;

    private KeyboardView keyboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        includeWord = null;

        Button search = (Button) findViewById(R.id.button);
        Button define = (Button) findViewById(R.id.button2);
        Button clear = (Button) findViewById(R.id.button3);
        Button include = (Button) findViewById(R.id.button4);
        lettersInput = (TextView) findViewById(R.id.editText);
        includeTextView = (TextView) findViewById(R.id.textView);

        final FragmentManager manager = getSupportFragmentManager();

        // set up keyboard
        // Create the Keyboard
        final Keyboard keyboard = new Keyboard(this, R.xml.keyboard);

        // Lookup the KeyboardView
        keyboardView = (KeyboardView) findViewById(R.id.keyboardview);
        // Attach the keyboard to the view
        if (keyboardView != null) {
            keyboardView.setKeyboard(keyboard);

            // Do not show the preview balloons
            keyboardView.setPreviewEnabled(false);

            // Install the key handler
            keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
                @Override
                public void onKey(int primaryCode, int[] keyCodes) {
                    //Here find the primaryCode to see which key is pressed
                    //based on the android:codes property
                    String inputText = lettersInput.getText().toString();
                    if (primaryCode == -1 && !inputText.equals("")) {
                        inputText = inputText.substring(0, inputText.length() - 1);
                        lettersInput.setText(inputText);
                    }
                    if (primaryCode != -1) {
                        if (inputText.length() < MAX_LETTERS) {
                            inputText += String.valueOf((char) primaryCode);
                            lettersInput.setText(inputText);
                        }
                    }
                }

                @Override
                public void onPress(int arg0) {
                }

                @Override
                public void onRelease(int primaryCode) {
                }

                @Override
                public void onText(CharSequence text) {
                }

                @Override
                public void swipeDown() {
                }

                @Override
                public void swipeLeft() {
                }

                @Override
                public void swipeRight() {
                }

                @Override
                public void swipeUp() {
                }
            });
        }


        // restore state
        if (savedInstanceState != null) {
            lettersInput.setText(savedInstanceState.getString(STATE_LETTERS));
            includeTextView.setText(savedInstanceState.getString(STATE_INCLUDE));
            includeWord = savedInstanceState.getString(STATE_INCLUDE_WORD);

            if (manager.findFragmentByTag(TAG_RESULTS_FRAGMENT) != null) {
                keyboardView.setVisibility(View.GONE);
            }
        }

        // set up fragments
        LoadDictionaryFragment dictFragment = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
        FragmentTransaction transaction = manager.beginTransaction();

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (dictFragment == null) {
            dictFragment = LoadDictionaryFragment
                    .newInstance();
            Bundle args = new Bundle();
            args.putString(DICTIONARY_KEY, DEFAULT_DICTIONARY_STRING);
            dictFragment.setArguments(args);
            transaction.add(dictFragment, TAG_TASK_FRAGMENT);
        }
        else {
            words = dictFragment.getWords();
        }

        transaction.commit();


        // set "Search" button function
        if (search != null) {
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String letters = lettersInput.getText().toString();
                    if (letters.equals("") && (includeWord == null || includeWord.equals(""))) {
                        Snackbar.make(v,
                                String.format(getResources()
                                                .getString(R.string.search_empty),
                                        MAX_LETTERS),
                                Snackbar.LENGTH_SHORT).show();
                    } else if (letters.matches("^_+$")) {
                        Snackbar.make(v,
                                getResources().getString(R.string.search_no_letters),
                                Snackbar.LENGTH_SHORT).show();
                    } else if (letters.length() - letters.replace("_", "").length() > MAX_BLANKS_SEARCH) {
                        Snackbar.make(v,
                                String.format(getResources()
                                                .getString(R.string.search_exceeds_wild),
                                        MAX_BLANKS_SEARCH),
                                Snackbar.LENGTH_SHORT).show();
                    } else if (letters.length() > MAX_LETTERS) {
                        Snackbar.make(v,
                                String.format(getResources()
                                                .getString(R.string.search_exceeds_max),
                                        MAX_LETTERS),
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        LoadDictionaryFragment frag = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);

                        if (frag != null && ((AsyncTask) frag.task).getStatus() == AsyncTask.Status.FINISHED) {
                            DisplayExpandableResultFragment resFrag = (DisplayExpandableResultFragment) manager.findFragmentByTag(TAG_RESULTS_FRAGMENT);
                            boolean isReady = true;
                            if (resFrag != null && ((AsyncTask) resFrag.findWordsTask).getStatus() != AsyncTask.Status.FINISHED) {
                                isReady = false;
                            }
                            if (isReady) {
                                DisplayExpandableResultFragment fragment = DisplayExpandableResultFragment.newInstance();
                                Bundle args = new Bundle();
                                if (letters.equals("")) {
                                    for (int i = 0; i < includeWord.length(); i++) {
                                        if (includeWord.charAt(i) == '_') {
                                            letters += "_";
                                        }
                                    }
                                }

                                args.putString(LETTERS_KEY, letters);
                                args.putString(INCLUDE_KEY, includeWord);
                                fragment.setArguments(args);
                                manager.beginTransaction()
                                        .replace(R.id.fragment_container,
                                                fragment,
                                                TAG_RESULTS_FRAGMENT).commit();

                                keyboardView.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }

        // set "Define" button function
        if (define != null) {
            define.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String currentWord = lettersInput.getText().toString();
                    if (isValidWord(currentWord)) {
                        ConnectivityManager conn = (ConnectivityManager)
                                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

                        if (wifiOnly && networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                                || !wifiOnly && networkInfo != null) {
                            DialogFragment frag = WordDialogFragment.newInstance();
                            Bundle args = new Bundle();
                            args.putString(LETTERS_KEY, currentWord);
                            frag.setArguments(args);
                            frag.show(getFragmentManager(), TAG_DIALOG);
                        } else {
                            Snackbar.make(v,
                                    getResources().getString(R.string.network_error),
                                    Snackbar.LENGTH_SHORT).show();
                        }

                    } else if (!currentWord.equals("")) {
                        Snackbar.make(v,
                                String.format(getResources()
                                                .getString(R.string.word_not_found),
                                        currentWord),
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(v,
                                getResources().getString(R.string.word_empty),
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // reset the activity
        if (clear != null) {
            clear.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    reset();
                }
            });
        }

        // set the "Include" button function
        if (include != null) {
            include.setOnClickListener(new View.OnClickListener() {
                ;

                @Override
                public void onClick(View v) {

                    includeWord = lettersInput.getText().toString();

                    if (includeWord.length() > 0) {
                        if (includeWord.length() > MAX_LETTERS) {
                            Snackbar.make(v,
                                    String.format(getResources()
                                                    .getString(R.string.search_exceeds_max),
                                            MAX_LETTERS),
                                    Snackbar.LENGTH_SHORT).show();
                        } else if (includeWord.matches("^_+$")) {
                            Snackbar.make(v,
                                    getResources().getString(R.string.include_no_letters),
                                    Snackbar.LENGTH_SHORT).show();
                        } else {
                            String text = String.format(
                                    getResources().getString(R.string.include_message), includeWord);
                            includeTextView.setText(text);
                            lettersInput.setText("");
                            DisplayExpandableResultFragment fragment =
                                    (DisplayExpandableResultFragment) getSupportFragmentManager()
                                            .findFragmentByTag(TAG_RESULTS_FRAGMENT);
                            if (fragment != null) {
                                if (fragment.findWordsTask.getStatus() != AsyncTask.Status.FINISHED) {
                                    fragment.findWordsTask.cancel(true);
                                }
                                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                            }
                            keyboardView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Snackbar.make(v,
                                getResources().getString(R.string.include_empty),
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // on first run, show tutorial and allow user to choose
        // their preferred dictionary
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setConfig(config);

        String nextString = getResources().getString(R.string.intro_next);
        String dismissString = getResources().getString(R.string.intro_dismiss);
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(search)
                .setDismissText(nextString)
                .setContentText(getResources().getString(R.string.intro_search))
                .withRectangleShape()
                .build());
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(define)
                .setDismissText(nextString)
                .setContentText(getResources().getString(R.string.intro_define))
                .withRectangleShape()
                .build());
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(include)
                .setDismissText(nextString)
                .setContentText(getResources().getString(R.string.intro_include))
                .withRectangleShape()
                .build());
        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(clear)
                .setDismissText(dismissString)
                .setContentText(getResources().getString(R.string.intro_clear))
                .withRectangleShape()
                .build());

        sequence.start();


        Boolean firstRun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_run", true);
        if (firstRun) {
            DialogFragment setDefaultDictionaryFragment = SetDefaultDictionaryFragment.newInstance();
            setDefaultDictionaryFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
            setDefaultDictionaryFragment.show(getFragmentManager(), TAG_DIALOG);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean("first_run", false)
                    .apply();
        }
    }

    public void sendIntentForDefinition(String word) {
        String uriString = BASE_URI + word.toLowerCase();
        Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));

        if (baseIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(baseIntent,
                    getResources().getString(R.string.chooser_text)));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save the letters previously entered by the user
        savedInstanceState.putString(STATE_LETTERS, lettersInput.getText().toString());
        savedInstanceState.putString(STATE_INCLUDE, includeTextView.getText().toString());
        savedInstanceState.putString(STATE_INCLUDE_WORD, includeWord);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_help) {
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        wifiOnly = preferences.getBoolean(SettingsActivity.WIFI_ONLY_KEY, WIFI_DEFAULT);

        String dict = preferences.getString(SettingsActivity.DICTIONARY_KEY, DEFAULT_DICTIONARY_STRING);

        String currentDictString = ((LoadDictionaryFragment)
                getSupportFragmentManager().findFragmentByTag(TAG_TASK_FRAGMENT)).getCurrentDict();

        if (!dict.equals(currentDictString)) {

            loadDictionary(dict);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public boolean isValidWord(String word) {
        return words.contains(word.toLowerCase());
    }

    // LoadDictionaryFragment onPostExecute callback
    public void setDictionary(ArrayList<String> words) {
        this.words = words;
    }

    public void onExpandableItemSelected(String word) {
        ConnectivityManager conn =  (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (wifiOnly && networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                || !wifiOnly && networkInfo != null) {
            sendIntentForDefinition(word);
        }
        else {
            View v = findViewById(R.id.expandableListView);
            if (v != null) {
                Snackbar.make(v, getResources().getString(R.string.network_error),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void reset() {
        DisplayExpandableResultFragment fragment =
                (DisplayExpandableResultFragment) getSupportFragmentManager()
                        .findFragmentByTag(TAG_RESULTS_FRAGMENT);
        if (fragment != null) {
            if (fragment.findWordsTask.getStatus() != AsyncTask.Status.FINISHED) {
                fragment.findWordsTask.cancel(true);
            }
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        keyboardView.setVisibility(View.VISIBLE);

        includeWord = null;

        includeTextView.setText("");
        lettersInput.setText("");
    }

    public void loadDictionary(String dict) {
        FragmentManager manager = this.getSupportFragmentManager();

        LoadDictionaryFragment dictFragment = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
        dictFragment.setCurrentDict(dict);
        dictFragment.startTask();
    }
}
