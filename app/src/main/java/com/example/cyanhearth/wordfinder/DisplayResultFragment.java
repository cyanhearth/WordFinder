package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by cyanhearth on 28/08/2015.
 */
public class DisplayResultFragment extends ListFragment {

    private static String letters;
    private ArrayList<String> finalResults;

    public interface SelectionListener {
        void onItemSelected(int position);
        void enableButtons();
    }

    private WeakReference<SelectionListener> callbacks;

    public static DisplayResultFragment newInstance(String lettersInput) {
        letters = lettersInput;
        return new DisplayResultFragment();
    }

    @Override
    public void onCreate(Bundle savedInsanceState) {
        super.onCreate(savedInsanceState);

        setRetainInstance(true);

        if (finalResults == null) {
            new FindWordsTask().execute(letters);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the hosting Activity has implemented
        // the SelectionListener callback interface. We need this
        // because when an item in this ListFragment is selected,
        // the hosting Activity's onItemSelected() method will be called.

        try {

            callbacks = new WeakReference<>((SelectionListener) activity);

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectionListener");
        }

    }


    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {

        // Notify the hosting Activity that a selection has been made.

        callbacks.get().onItemSelected(position);

    }

    public ArrayList<String> possibleWords(String letters, Iterable<String> words, int minLength) {
        // hold results
        ArrayList<String> results = new ArrayList<>();

        // sort the letters we want to make words from
        char[] lettersToChar = letters.toLowerCase().toCharArray();
        Arrays.sort(lettersToChar);

        for (String s : words) {
            // if the word contains too many letters move onto the next one
            if (s.length() > letters.length() || s.length() < minLength) continue;

            // sort the characters in the word
            char[] sToChar = s.toCharArray();
            Arrays.sort(sToChar);

            // keeps track of where we last found a match
            int n = 0;
            // count the letter matches made, if this equals the number of letters
            // in the word then it will be added to the result
            int count = 0;

            // look for each of the search letters
            for (char c : lettersToChar) {
                for (int j = n; j < sToChar.length; j++) {
                    // if the letter is found in the word, increment count
                    // and set n to the index we start searching for the next letter
                    if (c == sToChar[j]) {
                        count++;
                        n = j + 1;
                        break;
                    }
                }
            }
            // if the count equals the word length
            // add it to the result
            if (count == s.length()) results.add(s);
        }

        return results;

    }

    private class FindWordsTask extends AsyncTask<String, Void, ArrayList<String>> {

        protected ArrayList<String> doInBackground(String... letters) {
            int minLetters = Integer.parseInt(PreferenceManager.
                    getDefaultSharedPreferences((MainActivity) callbacks.get())
                    .getString("word_length", "3"));

            if (minLetters == 0) {
                minLetters = letters[0].length();
            }
            ArrayList<String> results = possibleWords(letters[0], MainActivity.words, minLetters);
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
            finalResults = results;

            setListAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, finalResults));
            if (callbacks != null)
                callbacks.get().enableButtons();
        }

    }
}
