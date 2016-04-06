package com.example.cyanhearth.wordfinder;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

public class MainActivity extends ActionBarActivity {
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

    // Current network preference
    public static boolean wifiOnly;

    public HashSet<String> words;

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

        final FragmentManager manager = getFragmentManager();

        // set up keyboard
        // Create the Keyboard
        Keyboard keyboard = new Keyboard(this, R.xml.keyboard);

        // Lookup the KeyboardView
        keyboardView = (KeyboardView) findViewById(R.id.keyboardview);
        // Attach the keyboard to the view
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
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String letters = lettersInput.getText().toString();
                if (letters.equals("") && (includeWord == null || includeWord.equals(""))) {
                    Toast.makeText(MainActivity.this,
                            String.format(getResources()
                                    .getString(R.string.search_empty),
                                    MAX_LETTERS),
                            Toast.LENGTH_SHORT).show();
                }
                else if (letters.matches("^_+$")) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.search_no_letters),
                            Toast.LENGTH_SHORT).show();
                }
                else if (letters.length() - letters.replace("_", "").length() > MAX_BLANKS_SEARCH){
                    Toast.makeText(MainActivity.this,
                            String.format(getResources()
                                    .getString(R.string.search_exceeds_wild),
                                    MAX_BLANKS_SEARCH),
                            Toast.LENGTH_SHORT).show();
                }
                else if (letters.length() > MAX_LETTERS) {
                    Toast.makeText(MainActivity.this,
                            String.format(getResources()
                                    .getString(R.string.search_exceeds_max),
                                    MAX_LETTERS),
                            Toast.LENGTH_SHORT).show();
                }
                else {
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

                    includeWord = null;
                    keyboardView.setVisibility(View.GONE);
                }
            }
        });

        // set "Define" button function
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
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.network_error),
                                Toast.LENGTH_LONG).show();
                    }

                } else if (!currentWord.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            String.format(getResources()
                                    .getString(R.string.word_not_found),
                                    currentWord),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.word_empty),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // reset the activity
        clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset();
            }
        });

        // set the "Include" button function
        include.setOnClickListener(new View.OnClickListener() {
;
            @Override
            public void onClick(View v) {

                includeWord = lettersInput.getText().toString();

                if (includeWord.length() > 0) {
                    if (includeWord.length() > MAX_LETTERS) {
                        Toast.makeText(getApplicationContext(),
                                String.format(getResources()
                                        .getString(R.string.search_exceeds_max),
                                        MAX_LETTERS),
                                Toast.LENGTH_SHORT).show();
                    }
                    else if (includeWord.matches("^_+$")) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.include_no_letters),
                                Toast.LENGTH_SHORT).show();
                    }
                    /*else if (includeWord.length() - includeWord.replace("_", "")
                            .length() > MAX_BLANKS_INCLUDE) {
                        Toast.makeText(getApplicationContext(),
                                String.format(getResources()
                                        .getString(R.string.search_exceeds_wild),
                                        MAX_BLANKS_INCLUDE),
                                Toast.LENGTH_SHORT).show();
                    }*/
                    else {
                        String text = String.format(
                                getResources().getString(R.string.include_message), includeWord);
                        includeTextView.setText(text);
                        lettersInput.setText("");
                    }
                }

                else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.include_empty),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        wifiOnly = preferences.getBoolean(SettingsActivity.WIFI_ONLY_KEY, WIFI_DEFAULT);

        String dict = preferences.getString(SettingsActivity.DICTIONARY_KEY, DEFAULT_DICTIONARY_STRING);

        String currentDictString = ((LoadDictionaryFragment)
                getFragmentManager().findFragmentByTag(TAG_TASK_FRAGMENT)).getCurrentDict();

        if (!dict.equals(currentDictString)) {

            FragmentManager manager = this.getFragmentManager();

            LoadDictionaryFragment dictFragment = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
            dictFragment.setCurrentDict(dict);
            dictFragment.startTask();
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
    public void setDictionary(HashSet<String> words) {

        this.words = words;
    }

    public void onExpandableItemSelected(String word) {
        ConnectivityManager conn =  (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        if (wifiOnly && networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                || !wifiOnly && networkInfo != null) {
            word = word.replace("<font color='" + getResources().getColor(R.color.accent) + "'>", "");
            word = word.replace("</font>", "");
            sendIntentForDefinition(word);
        }
        else {
            Toast.makeText(this, getResources().getString(R.string.network_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void reset() {
        DisplayExpandableResultFragment fragment = (DisplayExpandableResultFragment) getFragmentManager()
                .findFragmentByTag(TAG_RESULTS_FRAGMENT);
        if (fragment != null) {
            if (fragment.findWordsTask.getStatus() != AsyncTask.Status.FINISHED) {
                fragment.findWordsTask.cancel(true);
            }
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }

        keyboardView.setVisibility(View.VISIBLE);

        includeWord = null;

        includeTextView.setText("");
        lettersInput.setText("");
    }
}
