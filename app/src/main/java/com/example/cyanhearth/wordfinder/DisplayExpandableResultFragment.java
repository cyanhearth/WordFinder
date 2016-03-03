package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

/**
 * Created by cyanhearth on 16/02/2016.
 */
public class DisplayExpandableResultFragment extends Fragment {

    private View v;

    private ArrayList<String> rawResults;
    private ArrayList<Group> groups;
    private String include;
    private String letters;

    private ExpandableListView lv;

    private DisplayExpandableResultAdapter adapter;

    private WeakReference<MainActivity> callbacks;

    private String currentSortBy;

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

        if (currentSortBy  == null) {
            currentSortBy = sortBy;
        }
        else if (!sortBy.equals(currentSortBy)) {
            currentSortBy = sortBy;
            groups.clear();
            groups.addAll(sortResults(rawResults, currentSortBy));
            adapter.notifyDataSetChanged();
        }
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
                default:
                    score += 1;
                    break;
            }
        }
        return score;
    }

    public ArrayList<Group> sortResults(ArrayList<String> results, String sortBy) {
        if (results == null) {
            return null;
        }

        ArrayList<Group> groups = new ArrayList<>();

        int groupId;

        for (String word : results) {
            int score = getScrabbleScore(word);
            switch (sortBy) {
                case "0":
                    groupId = word.charAt(0);
                    break;
                case "1":
                    groupId = word.length();
                    break;
                case "2":
                    groupId = score;
                    break;
                default:
                    groupId = word.length();
                    Toast.makeText(callbacks.get(),
                            "Error retrieving order setting: using default setting (by length).",
                            Toast.LENGTH_SHORT).show();
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
                    }
                    else if (groupIdLeft < groupIdRight) {
                        return 1;
                    }
                    else {
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
                allLetters = allLetters + include;
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
                Collections.sort(results);
            }

            return results;
        }

        protected void onPostExecute(ArrayList<String> results) {
            if (callbacks != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(callbacks.get());
                String orderBy = prefs.getString("order", "1");

                rawResults = results;
                groups.addAll(sortResults(results, orderBy));
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
