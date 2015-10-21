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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

public class MainActivity extends ActionBarActivity
        implements LoadDictionaryFragment.TaskCallbacks, DisplayResultFragment.SelectionListener{
    static private final String BASE_URI = "https://en.wiktionary.org/wiki/";
    static private final String CHOOSER_TEXT = "Open with...";
    static private final String STATE_LETTERS = "state_letters";

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String TAG_RESULTS_FRAGMENT = "results_fragment";
    private static final String TAG_KEYBOARD_FRAGMENT = "keyboard_fragment";
    private TextView lettersInput;
    private TextView currentDict;
    private TextView minLength;
    private Button find;
    private Button check;
    private Button clear;

    // holds all of the dictionary words
    public static HashSet<String> words;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        find = (Button)findViewById(R.id.button);
        check = (Button)findViewById(R.id.button2);
        clear = (Button)findViewById(R.id.button3);
        lettersInput = (TextView)findViewById(R.id.editText);

        currentDict = (TextView) findViewById(R.id.currentDict);
        minLength = (TextView) findViewById(R.id.minLength);

        currentDict.setText(currentDict.getText().toString() + " "
                + preferences.getString(SettingsActivity.DICTIONARY_KEY, ""));
        minLength.setText(minLength.getText().toString() + " "
                + preferences.getString(SettingsActivity.MIN_LENGTH_KEY, ""));

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
                    .newInstance(preferences.getString("dictionary", "sowpods"));
            transaction.add(dictFragment, TAG_TASK_FRAGMENT);
        }
        else {
            words = LoadDictionaryFragment.words;
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
                    manager.beginTransaction()
                            .replace(R.id.fragment_container,
                                    DisplayResultFragment.newInstance(letters),
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
                    DialogFragment frag = WordDialogFragment.newInstance(currentWord);
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
                clear.setEnabled(false);
                lettersInput.setText("");
                if (manager.findFragmentByTag(TAG_RESULTS_FRAGMENT) != null) {
                    manager.beginTransaction().replace(R.id.fragment_container,
                            KeyboardFragment.newInstance(),
                            TAG_KEYBOARD_FRAGMENT).commit();
                }
                find.setEnabled(true);
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
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String dict = preferences.getString(SettingsActivity.DICTIONARY_KEY, "");

        String wordLength = preferences.getString(SettingsActivity.MIN_LENGTH_KEY, "");

        if (wordLength.equals("0")) {
            wordLength = "All letters";
        }

        minLength.setText(getResources().getText(R.string.min_length) + " " + wordLength);

        if (!dict.equals(LoadDictionaryFragment.currentDict)) {


            FragmentManager manager = this.getFragmentManager();

            LoadDictionaryFragment dictFragment = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
            dictFragment.currentDict = dict;
            dictFragment.startTask();

            find.setEnabled(false);
            check.setEnabled(false);

            currentDict.setText(getResources().getText(R.string.current_dict) + " " + dict);
        }
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

    // LoadDictionaryFragment.TaskCallbacks methods
    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute() {
        words = LoadDictionaryFragment.words;
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
            lettersInput.setText(lettersInput.getText() + letter);
        }
    }
}
