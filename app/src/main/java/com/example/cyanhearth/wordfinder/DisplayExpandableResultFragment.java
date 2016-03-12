package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cyanhearth on 16/02/2016.
 */
public class DisplayExpandableResultFragment extends Fragment {

    private static final String STATE_HIGHLIGHT = "state_highlight";

    private View v;

    private ArrayList<String> rawResults;
    private ArrayList<Group> groups;
    private String include;
    private String letters;

    private ExpandableListView lv;

    private DisplayExpandableResultAdapter adapter;

    private WeakReference<MainActivity> callbacks;

    private String currentSortBy;

    private boolean currentHighlight;

    public static DisplayExpandableResultFragment newInstance() {
        return new DisplayExpandableResultFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        groups = new ArrayList<>();

        letters = getArguments().getString("letters");
        include = getArguments().getString("include");

        if (savedInstanceState == null) {
            currentHighlight = PreferenceManager.getDefaultSharedPreferences(callbacks.get())
                    .getBoolean("highlight", true);
        }
        else {
            currentHighlight = savedInstanceState.getBoolean(STATE_HIGHLIGHT);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_HIGHLIGHT, currentHighlight);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        callbacks = new WeakReference<>((MainActivity) activity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        v = inflater.inflate(R.layout.expandable_fragment, container, false);
        lv = (ExpandableListView)v.findViewById(R.id.expandableListView);

        adapter = new DisplayExpandableResultAdapter(getActivity().getApplicationContext(), groups);
        lv.setAdapter(adapter);

        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                callbacks.get().onExpandableItemSelected(((Child) (parent.getExpandableListAdapter()
                        .getChild(groupPosition, childPosition))).getChildName());
                return true;
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(rawResults == null)
            new FindWordsTask().execute(letters);

    }

    @Override
    public void onStart() {
        super.onStart();

        String sortBy = PreferenceManager.getDefaultSharedPreferences(callbacks.get()).getString("order", "1");
        boolean highlight = PreferenceManager.getDefaultSharedPreferences(callbacks.get()).getBoolean("highlight", true);

        if (currentSortBy  == null) {
            currentSortBy = sortBy;
        }
        else {
            if (!sortBy.equals(currentSortBy) || currentHighlight != highlight) {
                currentSortBy = sortBy;
                currentHighlight = highlight;
                groups.clear();
                groups.addAll(sortResults(rawResults, currentSortBy, currentHighlight));
                adapter.notifyDataSetChanged();
            }
        }
    }

    public ArrayList<String> possibleWords(String letters, Iterable<String> words, int minLength, String substring) {
        // hold results
        ArrayList<String> results = new ArrayList<>();
        Pattern p;
        Matcher m = null;

        // sort the letters we want to make words from
        char[] lettersToChar = letters.toLowerCase().toCharArray();
        Arrays.sort(lettersToChar);


        // if the substring is a pattern, substitute underscores for periods and initialize the matcher
        if (substring != null && substring.contains("_")) {
            String regex = substring.replace("_", ".");
            p = Pattern.compile(regex);
            m = p.matcher("");
        }

        for (String s : words) {
            // if the word contains too many letters move onto the next one
            if (s.length() > letters.length() || s.length() < minLength) continue;
            // if the substring is set but is not contained in this word, move on
            if (substring != null && !s.contains(substring)) {

                if (!substring.contains("_")) {
                    continue;
                }
            }

            // sort the characters in the word
            char[] sToChar = s.toCharArray();
            Arrays.sort(sToChar);

            // keeps track of where we last found a match
            int n = 0;
            // count the letter matches made, if this equals the number of letters
            // in the word then it will be added to the result
            int count = 0;
            int noOfBlanks = 0;
            char[] replacements = null;
            int replacementCount = 0;
            // look for each of the search letters
            for (int i = 0; i < lettersToChar.length; i++) {
                char c = lettersToChar[i];
                if (c == '_') {
                    // account for wildcard
                    noOfBlanks++;
                    continue;
                }
                if (replacements == null && noOfBlanks > 0) {
                    replacements = new char[noOfBlanks];
                }
                if (n == sToChar.length) {
                    if (replacements != null) {
                        for (int k = 0; k < replacements.length; k++) {
                            if (c != replacements[k]) {
                                replacementCount++;
                            }
                        }
                    }
                }
                for (int j = n; j < sToChar.length; j++) {
                    // if the letter is found in the word, increment count
                    // and set n to the index we start searching for the next letter
                    if (c == sToChar[j]) {
                        count++;
                        n = j + 1;

                        if (replacements != null) {
                            for (int k = 0; k < replacements.length; k++) {
                                if (c == replacements[k]) {
                                    if (replacementCount > 0)
                                        replacementCount--;
                                }
                            }
                        }

                        break;
                    }
                    // keep track of 'blank' letters to highlight
                    else if (noOfBlanks > 0 && replacementCount <  noOfBlanks) {
                        if (replacements != null) {
                            replacements[replacementCount] = sToChar[j];
                        }
                        replacementCount++;
                        n = j + 1;
                    }
                }

                // if there are any letters left in the word, add 'blanks' if required
                if (i == lettersToChar.length - 1) {
                    if (n < sToChar.length) {
                        for (int j = n; j < sToChar.length; j++) {
                            if (noOfBlanks > 0 && replacementCount < noOfBlanks) {
                                if (replacements != null) {
                                    replacements[replacementCount] = sToChar[j];
                                }
                                replacementCount++;
                            }
                        }

                    }
                }
            }
            // if the count equals the word length
            // and contains the substring (if it is set)
            // add it to the result
            if ((s.length() >= count && s.length() <= count + noOfBlanks)) {
                // if the pattern is set and the string does not contain it, continue
                if (substring != null && substring.contains("_") && m != null) {
                    m.reset(s);
                    if (!m.find()) {
                        continue;
                    }
                }

                // if there are letters to be highlighted, convert them to uppercase
                if (replacements != null && replacementCount > 0) {
                    for (char c : replacements) {
                        int foundAt = s.indexOf(c);
                        if (foundAt != -1) {
                            s = s.substring(0, foundAt)
                                    + Character.toUpperCase(s.charAt(foundAt))
                                    + s.substring(foundAt + 1);
                        }
                    }
                    // highlight the letters
                    for (int i = 0; i < s.length(); i++) {
                        if (Character.isUpperCase(s.charAt(i))) {
                            s = s.substring(0, i)
                                    + "<font color='red'>" + Character.toLowerCase(s.charAt(i)) + "</font>"
                                    + s.substring(i + 1);
                        }
                    }
                }
                results.add(s);
            }
        }
        return results;

    }

    public int getScrabbleScore(String word) {
        int score = 0;

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            switch (c) {
                case 'd':
                case 'g':
                    score += 2;
                    break;
                case 'b':
                case 'c':
                case 'm':
                case 'p':
                    score += 3;
                    break;
                case 'f':
                case 'h':
                case 'v':
                case 'w':
                case 'y':
                    score += 4;
                    break;
                case 'k':
                    score += 5;
                    break;
                case 'j':
                case 'x':
                    score+= 8;
                    break;
                case 'q':
                case 'z':
                    score += 10;
                    break;
                case 'a':
                case 'e':
                case 'i':
                case 'l':
                case 'n':
                case 'o':
                case 'r':
                case 's':
                case 't':
                case 'u':
                    score += 1;
                    break;
                default:
                    break;
            }
        }
        return score;
    }

    public ArrayList<Group> sortResults(ArrayList<String> results, String sortBy, boolean highlight) {
        if (results == null) {
            return null;
        }

        ArrayList<Group> groups = new ArrayList<>();

        int groupId;

        for (String word : results) {
            String strippedWord = word.replace("<font color='red'>", "");
            strippedWord = strippedWord.replace("</font>", "");
            int score = getScrabbleScore(strippedWord);
            switch (sortBy) {
                case "0":
                    groupId = strippedWord.charAt(0);
                    break;
                case "1":
                    groupId = strippedWord.length();
                    break;
                case "2":
                    groupId = score;
                    break;
                default:
                    groupId = strippedWord.length();
                    Toast.makeText(callbacks.get(),
                            "Error retrieving order setting: using default setting (by length).",
                            Toast.LENGTH_SHORT).show();
            }

            if (!highlight) {
                word = strippedWord;
            }

            boolean groupExists = false;

            for (Group group : groups) {
                if (group.getGroupId() == groupId) {
                    group.addChild(new Child(word, score));
                    groupExists = true;
                    break;
                }
            }

            if (!groupExists) {
                String groupName;

                switch (sortBy) {
                    case "0":
                        groupName = String.valueOf((char) groupId).toUpperCase();
                        break;
                    case "1":
                        groupName = String.valueOf(groupId) +  " letters";
                        break;
                    case "2":
                        groupName = String.valueOf(groupId) + " points";
                        break;
                    default:
                        groupName = String.valueOf(groupId) +  " letters";
                }

                Group group = new Group(groupId);
                group.setGroupName(groupName);
                group.addChild(new Child(word, score));
                groups.add(group);
            }
        }

        if (!sortBy.equals("0")) {
            Collections.sort(groups, new Comparator<Group>() {

                @Override
                public int compare(Group lhs, Group rhs) {
                    int groupIdLeft = lhs.getGroupId();
                    int groupIdRight = rhs.getGroupId();

                    if (groupIdLeft > groupIdRight) {
                        return -1;
                    } else if (groupIdLeft < groupIdRight) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        }

        return groups;
    }

    private class FindWordsTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {

        }

        protected ArrayList<String> doInBackground(String... letters) {
            String allLetters = letters[0];
            if (include != null) {
                String lettersToAdd = include.replace("_", "");
                Log.d("ADD", lettersToAdd);
                Log.d("INCLUDE", include);
                allLetters = allLetters + lettersToAdd;
            }
            int minLetters = Integer.parseInt(PreferenceManager.
                    getDefaultSharedPreferences(callbacks.get())
                    .getString("word_length", "3"));

            if (minLetters == 0) {
                minLetters = allLetters.length();
            }

            ArrayList<String> results = null;

            Iterable<String> words = callbacks.get().words;

            if (words != null) {
                results = possibleWords(allLetters, callbacks.get().words,
                        minLetters, include);

                // sort alphabetically
                Collections.sort(results, new Comparator<String>() {

                    @Override
                    public int compare(String lhs, String rhs) {
                        String strippedLeft = lhs.replace("<font color='red'>", "");
                        strippedLeft = strippedLeft.replace("</font>", "");

                        String strippedRight = rhs.replace("<font color='red'>", "");
                        strippedRight = strippedRight.replace("</font>", "");

                        int compare = strippedLeft.compareTo(strippedRight);

                        if (compare < 0) {
                            return -1;
                        }
                        else if (compare > 0) {
                            return 1;
                        }
                        else {
                            return 0;
                        }
                    }
                });
            }

            return results;
        }

        protected void onPostExecute(ArrayList<String> results) {
            if (callbacks != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(callbacks.get());
                String orderBy = prefs.getString("order", "1");

                rawResults = results;
                groups.addAll(sortResults(results, orderBy, currentHighlight));
            }

            // if upon trying to restore the fragments previous state the wordlist is null, reset
            // the app to it's starting conditions
            if (groups == null && callbacks != null) {
                callbacks.get().reset();
            }
            else {
                adapter.notifyDataSetChanged();
                if (results.isEmpty()) {
                    TextView emptyView = (TextView) v.findViewById(R.id.emptyView);
                    lv.setEmptyView(emptyView);
                }
                if (callbacks != null)
                    //container.removeView(pb);
                    callbacks.get().enableButtons();
            }
        }

    }

}
