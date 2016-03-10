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
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

public class MainActivity extends ActionBarActivity
        implements DisplayResultFragment.SelectionListener{
    private static final String BASE_URI = "https://en.wiktionary.org/wiki/";
    private static final String CHOOSER_TEXT = "Open with...";
    private static final String STATE_LETTERS = "state_letters";
    private static final String STATE_INCLUDE = "state_include";

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String TAG_RESULTS_FRAGMENT = "results_fragment";
    private static final String TAG_KEYBOARD_FRAGMENT = "keyboard_fragment";
    private static final String TAG_DEBUG = "Debug";
    private static final String DEFAULT_DICTIONARY_STRING = "sowpods";

    // Current network preference
    public static boolean wifiOnly;

    private String includeWord;

    private TextView includeTextView;
    private TextView lettersInput;
    private Button search;
    private Button define;
    private Button clear;
    private Button include;

    private Keyboard keyboard;
    private KeyboardView keyboardView;

    // holds all of the dictionary words
    public HashSet<String> words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Entering onCreate", TAG_DEBUG);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        includeWord = null;

        search = (Button)findViewById(R.id.button);
        define = (Button)findViewById(R.id.button2);
        clear = (Button)findViewById(R.id.button3);
        include = (Button)findViewById(R.id.button4);
        lettersInput = (TextView)findViewById(R.id.editText);
        includeTextView = (TextView)findViewById(R.id.textView);

        final FragmentManager manager = getFragmentManager();

        // set up keyboard
        // Create the Keyboard
        keyboard = new Keyboard(this,R.xml.keyboard);

        // Lookup the KeyboardView
        keyboardView = (KeyboardView)findViewById(R.id.keyboardview);
        // Attach the keyboard to the view
        keyboardView.setKeyboard(keyboard) ;

        // Do not show the preview balloons
        keyboardView.setPreviewEnabled(false);

        // Install the key handler
        keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override public void onKey(int primaryCode, int[] keyCodes) {
                //Here find the primaryCode to see which key is pressed
                //based on the android:codes property
                String inputText = lettersInput.getText().toString();
                if (primaryCode == -1 && !inputText.equals("")) {
                    inputText = inputText.substring(0, inputText.length() - 1);
                    lettersInput.setText(inputText);
                }
                if (primaryCode != -1) {
                    inputText += String.valueOf((char)primaryCode);
                    lettersInput.setText(inputText);
                }

                clear.setEnabled(!inputText.equals(""));
                include.setEnabled(!inputText.equals(""));
            }

            @Override public void onPress(int arg0) {
            }

            @Override public void onRelease(int primaryCode) {
            }

            @Override public void onText(CharSequence text) {
            }

            @Override public void swipeDown() {
            }

            @Override public void swipeLeft() {
            }

            @Override public void swipeRight() {
            }

            @Override public void swipeUp() {
            }
        });


        // restore state
        if (savedInstanceState != null) {
            lettersInput.setText(savedInstanceState.getString(STATE_LETTERS));
            includeTextView.setText(savedInstanceState.getString(STATE_INCLUDE));

            if (manager.findFragmentByTag(TAG_RESULTS_FRAGMENT) != null) {
                keyboardView.setVisibility(View.GONE);
            }
        }

        if (lettersInput.getText().toString().equals("")) {
            clear.setEnabled(false);
            include.setEnabled(false);
        }

        // set up fragments
        LoadDictionaryFragment dictFragment =
                (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
        FragmentTransaction transaction = manager.beginTransaction();

        if (manager.findFragmentByTag(TAG_RESULTS_FRAGMENT) == null) {
            //transaction.replace(R.id.fragment_container, KeyboardFragment.newInstance(), TAG_KEYBOARD_FRAGMENT);
        }
        else {
            include.setEnabled(false);
            search.setEnabled(false);
        }

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (dictFragment == null) {
            search.setEnabled(false);
            define.setEnabled(false);

            dictFragment = LoadDictionaryFragment
                    .newInstance();
            Bundle args = new Bundle();
            args.putString("dict", DEFAULT_DICTIONARY_STRING);
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
                if (letters.equals("") && (includeWord == null || !includeWord.contains("_"))) {
                    Toast.makeText(MainActivity.this, "Please enter some letters!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    search.setEnabled(false);
                    include.setEnabled(false);
                    DisplayExpandableResultFragment fragment = DisplayExpandableResultFragment.newInstance();
                    Bundle args = new Bundle();
                    if (letters.equals("")) {
                        for (int i = 0; i < includeWord.length(); i++) {
                            if (includeWord.charAt(i) == '_') {
                                letters += "_";
                            }
                        }
                    }
                    args.putString("letters", letters);
                    args.putString("include", includeWord);
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
                        args.putString("word", currentWord);
                        frag.setArguments(args);
                        frag.show(getFragmentManager(), "dialog");
                    } else {
                        Toast.makeText(getApplicationContext(), "Cannot retrieve definitions: no network connection.",
                                Toast.LENGTH_LONG).show();
                    }

                } else if (!currentWord.equals("")) {
                    Toast.makeText(getApplicationContext(), currentWord +
                            " is not in the current dictionary", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter a word first!",
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
                String currentWord = lettersInput.getText().toString();

                includeWord = currentWord;

                if (includeWord.length() > 0) {
                    String text = String.format(
                            getResources().getString(R.string.include_message), includeWord);
                    includeTextView.setText(text);
                    lettersInput.setText("");
                    include.setEnabled(false);
                }

                else {
                    Toast.makeText(getApplicationContext(), "Please provide a word/pattern to include in your search.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void sendIntentForDefinition(String word) {
        String uriString = BASE_URI + word.toLowerCase();
        Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));

        if (baseIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(baseIntent, CHOOSER_TEXT));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save the letters previously entered by the user
        savedInstanceState.putString(STATE_LETTERS, lettersInput.getText().toString());
        savedInstanceState.putString(STATE_INCLUDE, includeTextView.getText().toString());

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
        Log.d("Entering onStart", TAG_DEBUG);
        super.onStart();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        wifiOnly = preferences.getBoolean("wifi_only", false);

        String dict = preferences.getString(SettingsActivity.DICTIONARY_KEY, "");

        String currentDictString = ((LoadDictionaryFragment)
                getFragmentManager().findFragmentByTag(TAG_TASK_FRAGMENT)).getCurrentDict();

        if (!dict.equals(currentDictString)) {

            FragmentManager manager = this.getFragmentManager();

            LoadDictionaryFragment dictFragment = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
            dictFragment.setCurrentDict(dict);
            dictFragment.startTask();

            search.setEnabled(false);
            define.setEnabled(false);
        }

    }

    @Override
    protected void onResume() {
        Log.d("Entering onResume", TAG_DEBUG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("Entering onPause", TAG_DEBUG);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("Entering onStop", TAG_DEBUG);
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        Log.d("Entering onDestroy", TAG_DEBUG);
        super.onDestroy();
    }


    public boolean isValidWord(String word) {
        return words.contains(word.toLowerCase());
    }

    // LoadDictionaryFragment onPostExecute callback

    public void onPostExecute(HashSet<String> words) {

        this.words = words;
        search.setEnabled(true);
        define.setEnabled(true);
    }

    // DisplayResultFragment.SelectionListener methods
    // dispatch intent to retrieve word definition when a word is selected
    // from the list
    @Override
    public void onItemSelected(int position) {

        DisplayResultFragment frag = (DisplayResultFragment)(getFragmentManager()
                .findFragmentByTag(TAG_RESULTS_FRAGMENT));

        if (frag != null)
            sendIntentForDefinition(frag.getListAdapter().getItem(position).toString());

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
            Toast.makeText(this, "Cannot retrieve definition: no network connection",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void enableButtons() {
        define.setEnabled(true);
        clear.setEnabled(true);
    }

    @Override
    public void reset() {
        //if (getFragmentManager().findFragmentByTag(TAG_KEYBOARD_FRAGMENT) == null) {
        //    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
        //            KeyboardFragment.newInstance(), TAG_KEYBOARD_FRAGMENT).commit();
        //}
        DisplayExpandableResultFragment fragment = (DisplayExpandableResultFragment) getFragmentManager()
                .findFragmentByTag(TAG_RESULTS_FRAGMENT);
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }

        search.setEnabled(true);
        define.setEnabled(true);
        clear.setEnabled(false);
        include.setEnabled(false);

        keyboardView.setVisibility(View.VISIBLE);

        includeWord = null;

        includeTextView.setText("");
        lettersInput.setText("");
    }

    // onClick used by keyboard buttons
    public void onButtonPressed(View view) {
        Button buttonPressed = (Button) view;
        String letter = buttonPressed.getText().toString();
        String inputText = lettersInput.getText().toString();

        if(buttonPressed.getId() == R.id.bksp && !inputText.equals("")) {
            inputText = inputText.substring(0, inputText.length() - 1);
            lettersInput.setText(inputText);
        }
        else {
            inputText += letter;
            lettersInput.setText(inputText);
        }

        clear.setEnabled(!inputText.equals(""));
        include.setEnabled(!inputText.equals(""));
    }
}
