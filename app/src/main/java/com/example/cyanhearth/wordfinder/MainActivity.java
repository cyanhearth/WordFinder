package com.example.cyanhearth.wordfinder;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
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
        implements LoadDictionaryFragment.TaskCallbacks, DisplayResultFragment.SelectionListener{
    private static final String BASE_URI = "https://en.wiktionary.org/wiki/";
    private static final String CHOOSER_TEXT = "Open with...";
    private static final String STATE_LETTERS = "state_letters";

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String TAG_RESULTS_FRAGMENT = "results_fragment";
    private static final String TAG_KEYBOARD_FRAGMENT = "keyboard_fragment";
    private static final String TAG_DEBUG = "Debug";
    private static final String DEFAULT_DICTIONARY_STRING = "sowpods";

    private TextView lettersInput;
    private Button find;
    private Button check;
    private Button clear;

    // holds all of the dictionary words
    public HashSet<String> words;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Entering onCreate", TAG_DEBUG);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        find = (Button)findViewById(R.id.button);
        check = (Button)findViewById(R.id.button2);
        clear = (Button)findViewById(R.id.button3);
        lettersInput = (TextView)findViewById(R.id.editText);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        // restore state
        if (savedInstanceState != null) {
            lettersInput.setText(savedInstanceState.getString(STATE_LETTERS));
        }

        // set up fragments
        final FragmentManager manager = getFragmentManager();
        LoadDictionaryFragment dictFragment =
                (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance();
        FragmentTransaction transaction = manager.beginTransaction();

        if (manager.findFragmentByTag(TAG_RESULTS_FRAGMENT) == null) {
            clear.setEnabled(false);
            transaction.replace(R.id.fragment_container, keyboardFragment, TAG_KEYBOARD_FRAGMENT);
        }
        else {
            find.setEnabled(false);
        }

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (dictFragment == null) {
            find.setEnabled(false);
            check.setEnabled(false);

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


        // set "How many words" button function
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String letters = lettersInput.getText().toString();
                if (letters.equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter some letters!",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    find.setEnabled(false);
                    DisplayResultFragment fragment = DisplayResultFragment.newInstance();
                    Bundle args = new Bundle();
                    args.putString("letters", letters);
                    fragment.setArguments(args);
                    manager.beginTransaction()
                            .replace(R.id.fragment_container,
                                    fragment,
                                    TAG_RESULTS_FRAGMENT).commit();
                }
            }
        });

        // set "Is it there?" button function
        check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String currentWord = lettersInput.getText().toString();
                if (isValidWord(currentWord)) {
                    DialogFragment frag = WordDialogFragment.newInstance();
                    Bundle args = new Bundle();
                    args.putString("word", currentWord);
                    frag.setArguments(args);
                    frag.show(getFragmentManager(), "dialog");
                }
                else if (!currentWord.equals("")){
                    Toast.makeText(getApplicationContext(), currentWord +
                            " is not in the current dictionary", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter a word first!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset();
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
        if (id == R.id.action_overflow) {
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

    }

    @Override
    protected void onResume() {
        Log.d("Entering onResume", TAG_DEBUG);
        super.onResume();

        String dict = preferences.getString(SettingsActivity.DICTIONARY_KEY, "");

        String currentDictString = ((LoadDictionaryFragment)
                getFragmentManager().findFragmentByTag(TAG_TASK_FRAGMENT)).getCurrentDict();

        if (!dict.equals(currentDictString)) {

            FragmentManager manager = this.getFragmentManager();

            LoadDictionaryFragment dictFragment = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
            dictFragment.setCurrentDict(dict);
            dictFragment.startTask();

            find.setEnabled(false);
            check.setEnabled(false);
        }
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

    // LoadDictionaryFragment.TaskCallbacks methods
    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute() {

        words = ((LoadDictionaryFragment) getFragmentManager().findFragmentByTag(TAG_TASK_FRAGMENT)).getWords();
        find.setEnabled(true);
        check.setEnabled(true);
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

    @Override
    public void enableButtons() {
        check.setEnabled(true);
        clear.setEnabled(true);
    }

    @Override
    public void reset() {
        if (getFragmentManager().findFragmentByTag(TAG_KEYBOARD_FRAGMENT) == null) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    KeyboardFragment.newInstance(), TAG_KEYBOARD_FRAGMENT).commit();
        }

        find.setEnabled(true);
        check.setEnabled(true);
        clear.setEnabled(false);

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
    }
}
