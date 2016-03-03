package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.ListFragment;
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

    private String include;
    private ArrayList<String> finalResults;

    public interface SelectionListener {
        void onItemSelected(int position);
        void enableButtons();
        void reset();
    }

    private WeakReference<SelectionListener> callbacks;

    public static DisplayResultFragment newInstance() {
        return new DisplayResultFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        String letters = getArguments().getString("letters");
        include = getArguments().getString("include");

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

    public ArrayList<String> possibleWords(String letters, Iterable<String> words, int minLength, String substring) {
        // hold results
        ArrayList<String> results = new ArrayList<>();

        // sort the letters we want to make words from
        char[] lettersToChar = letters.toLowerCase().toCharArray();
        Arrays.sort(lettersToChar);

        for (String s : words) {
            // if the word contains too many letters move onto the next one
            if (s.length() > letters.length() || s.length() < minLength) continue;
            // if the substring is set but is not contained in this word, move on
            if (substring != null && !s.contains(substring)) continue;

            // sort the characters in the word
            char[] sToChar = s.toCharArray();
            Arrays.sort(sToChar);

            // keeps track of where we last found a match
            int n = 0;
            // count the letter matches made, if this equals the number of letters
            // in the word then it will be added to the result
            int count = 0;
            int noOfBlanks = 0;
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

                    if (j == sToChar.length - 1 && c == '_') {
                        // account for wildcard
                        noOfBlanks++;
                    }
                }
            }
            // if the count equals the word length
            // and contains the substring (if it is set)
            // add it to the result
            if ((s.length() >= count && s.length() <= count + noOfBlanks))
                results.add(s);
        }

        return results;

    }

    private class FindWordsTask extends AsyncTask<String, Void, ArrayList<String>> {

        protected ArrayList<String> doInBackground(String... letters) {
            String allLetters = letters[0];
            if (include != null) {
                allLetters = allLetters + include;
            }
            int minLetters = Integer.parseInt(PreferenceManager.
                    getDefaultSharedPreferences((MainActivity) callbacks.get())
                    .getString("word_length", "3"));

            if (minLetters == 0) {
                minLetters = allLetters.length();
            }

            ArrayList<String> results = null;

            Iterable<String> words = ((MainActivity) getActivity()).words;

            if (words != null) {
                results = possibleWords(allLetters, ((MainActivity) getActivity()).words,
                        minLetters, include);

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

            }

            return results;
        }

        protected void onPostExecute(ArrayList<String> results) {
            finalResults = results;

            // if upon trying to restore the fragments previous state the wordlist is null, reset
            // the app to it's starting conditions
            if (finalResults == null) {
                (callbacks.get()).reset();
            }
            else {
                setListAdapter(new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_list_item_1, finalResults));

                if (callbacks != null)
                    callbacks.get().enableButtons();
            }
        }

    }
}
