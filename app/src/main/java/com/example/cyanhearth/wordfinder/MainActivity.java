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
    private Button find;
    private Button check;
    private Button clear;

    // holds all of the dictionary words
    public static HashSet<String> words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        find = (Button)findViewById(R.id.button);
        check = (Button)findViewById(R.id.button2);
        clear = (Button)findViewById(R.id.button3);
        lettersInput = (TextView)findViewById(R.id.editText);

        // restore state
        if (savedInstanceState != null) {
            lettersInput.setText(savedInstanceState.getString(STATE_LETTERS));
        }

        // set up fragments
        final FragmentManager manager = getFragmentManager();
        LoadDictionaryFragment dictFragment = (LoadDictionaryFragment) manager.findFragmentByTag(TAG_TASK_FRAGMENT);
        KeyboardFragment keyboardFragment = KeyboardFragment.newInstance();
        FragmentTransaction transaction = manager.beginTransaction();

        if (manager.findFragmentByTag(TAG_RESULTS_FRAGMENT) == null) {
            clear.setEnabled(false);
            transaction.replace(R.id.fragment_container, keyboardFragment, TAG_KEYBOARD_FRAGMENT);
        }

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (dictFragment == null) {
            find.setEnabled(false);
            check.setEnabled(false);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            dictFragment = LoadDictionaryFragment.newInstance(preferences.getString("dictionary", "sowpods"));
            transaction.add(dictFragment, TAG_TASK_FRAGMENT);
        }
        else {
            this.words = LoadDictionaryFragment.words;
        }

        transaction.commit();


        // set "How many words" button function
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find.setEnabled(false);
                manager.beginTransaction()
                        .replace(R.id.fragment_container, DisplayResultFragment.newInstance(lettersInput.getText().toString()),
                                TAG_RESULTS_FRAGMENT).commit();
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
                    Toast.makeText(getApplicationContext(), "Please enter a word first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clear.setEnabled(false);
                lettersInput.setText("");
                if (manager.findFragmentByTag(TAG_RESULTS_FRAGMENT) != null) {
                    manager.beginTransaction().replace(R.id.fragment_container, KeyboardFragment.newInstance(),
                            TAG_KEYBOARD_FRAGMENT).commit();
                }
            }
        });
    }

    @Override
    public void enableButtons() {
        find.setEnabled(true);
        check.setEnabled(true);
        clear.setEnabled(true);
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

        String dict = PreferenceManager.getDefaultSharedPreferences(this).getString("dictionary", "sowpods");

        if (!dict.equals(LoadDictionaryFragment.currentDict)) {
            FragmentManager manager = this.getFragmentManager();
            manager.beginTransaction().remove(manager.findFragmentByTag(TAG_TASK_FRAGMENT)).commit();
            find.setEnabled(false);
            check.setEnabled(false);
            manager.beginTransaction().add(LoadDictionaryFragment.newInstance(dict), TAG_TASK_FRAGMENT).commit();

            Toast.makeText(this, "Dictionary changed - " + dict, Toast.LENGTH_LONG).show();
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

    // dispatch intent to retrieve word definition when a word is selected
    // from the list
    // DisplayResultFragment.SelectionListener method
    @Override
    public void onItemSelected(int position) {

        DisplayResultFragment frag = (DisplayResultFragment)(getFragmentManager()
                .findFragmentByTag(TAG_RESULTS_FRAGMENT));

        if (frag != null)
            sendIntentForDefinition(frag.getListAdapter().getItem(position).toString());

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

    /*private class FindWordsTask extends AsyncTask<String, Void, ArrayList<String>> {

        protected ArrayList<String> doInBackground(String... letters) {
            ArrayList<String> results = possibleWords(letters[0]);
            // sort alphabetically
            Collections.sort(results);
            // sort by length, longest to shortest
            Collections.sort(results, new Comparator<String>() {

                @Override
                public int compare(String s1, String s2) {
                    if (s1.length() > s2.length())
                        return -1;
                    else if (s1.length() < s2.length())
                        return 1;
                    else {
                        return 0;
                    }
                }
            });

            return results;
        }

        protected void onPostExecute(ArrayList<String> results) {
            find.setEnabled(true);
            clear.setEnabled(true);

            MainActivity.this.getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, DisplayResultFragment.newInstance(results), TAG_RESULTS_FRAGMENT).commit();
        }


    }*/
}
