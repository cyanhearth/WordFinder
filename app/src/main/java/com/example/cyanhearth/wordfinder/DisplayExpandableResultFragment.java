package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private static final String HIGHLIGHT_KEY = "highlight";
    private static final String ORDER_KEY = "order";
    private static final String LENGTH_KEY = "word_length";
    private static final String ORDER_DEFAULT = "1";
    private static final String LENGTH_DEFAULT = "3";
    private static final boolean HIGHLIGHT_DEFAULT = true;

    private View v;

    private ProgressBar pb;

    private ArrayList<String> rawResults;
    private ArrayList<Group> groups;
    private String include;
    private String letters;

    public AsyncTask<String, Void, ArrayList<Group>> findWordsTask;

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

        letters = getArguments().getString(MainActivity.LETTERS_KEY);
        include = getArguments().getString(MainActivity.INCLUDE_KEY);

        if (savedInstanceState == null) {
            currentHighlight = PreferenceManager.getDefaultSharedPreferences(callbacks.get())
                    .getBoolean(HIGHLIGHT_KEY, true);
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

        if(rawResults == null) {
            findWordsTask = new FindWordsTask();
            findWordsTask.execute(letters);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        String sortBy = PreferenceManager.getDefaultSharedPreferences(callbacks.get())
                .getString(ORDER_KEY, ORDER_DEFAULT);
        boolean highlight = PreferenceManager.getDefaultSharedPreferences(callbacks.get())
                .getBoolean(HIGHLIGHT_KEY, HIGHLIGHT_DEFAULT);

        if (currentSortBy  == null) {
            currentSortBy = sortBy;
        }
        else {
            if ((!sortBy.equals(currentSortBy) || currentHighlight != highlight)
                    && findWordsTask.getStatus() != AsyncTask.Status.RUNNING) {
                currentSortBy = sortBy;
                currentHighlight = highlight;

                final Handler handler = new Handler();

                Runnable sortRunnable = new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Group> temp = sortResults(rawResults, currentSortBy, currentHighlight);
                        groups.clear();
                        groups.addAll(temp);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                };

                new Thread(sortRunnable).start();
            }
        }
    }

    public ArrayList<String> possibleWords(String letters, Iterable<String> words, int minLength, String include) {
        // hold results
        ArrayList<String> results = new ArrayList<>();
        Pattern p;
        Matcher m = null;
        int noOfBlanks = 0;
        char[] blanks = null;

        // sort the letters we want to make words from
        char[] lettersToChar = letters.toLowerCase().toCharArray();
        Arrays.sort(lettersToChar);

        for (char c : lettersToChar) {
            if (c == '_') {
                noOfBlanks++;
                continue;
            }
            break;
        }

        if (noOfBlanks > 0) {
            blanks = new char[noOfBlanks];
        }

        // if the include contains a pattern, build the regular expression
        if (include != null && include.contains("_")) {
            String regex = setPattern(include, letters);
            p = Pattern.compile(regex);
            m = p.matcher("");
        }

        for (String s : words) {
            if (findWordsTask.isCancelled()) {
                break;
            }
            // if the word contains too many (or too few) letters move onto the next one
            if (s.length() > letters.length() || s.length() < minLength) continue;
            // if the include is set but is not contained in this word, move on
            if (include != null && !s.contains(include)) {
                if (!include.contains("_")) {
                    continue;
                }
            }

            // sort the characters in the word
            char[] sToChar = s.toCharArray();
            Arrays.sort(sToChar);

            // keeps track of where we last found a match
            int n = noOfBlanks;
            // count the letter matches made, if this equals the number of letters
            // in the word then it will be added to the result
            int count = 0;
            int filledBlanks = 0;
            // look for each of the search letters
            for (char c : sToChar) {
                boolean found = false;
                for (int j = n; j < lettersToChar.length; j++) {
                    // if the letter is found in the word, increment count
                    // and set n to the index we start searching for the next letter
                    if (c == lettersToChar[j]) {
                        count++;
                        n = j + 1;
                        found = true;
                        break;
                    }
                }
                if (!found && filledBlanks < noOfBlanks) {
                    if (blanks != null) {
                        blanks[filledBlanks] = c;
                        filledBlanks++;
                    }
                }
            }

            // if the count equals the word length
            // and contains the substring (if it is set)
            // add it to the result
            if ((s.length() >= count && s.length() <= count + noOfBlanks)) {
                // if the pattern is set and the string does not contain it, continue
                int end = -1;
                int start = -1;
                if (include != null && include.contains("_") && m != null) {
                    m.reset(s);
                    if (!m.find()) {
                        continue;
                    }
                    else {
                        // end index (inclusive) of the pattern match in s
                        end = m.end() - 1;
                        // start position of the pattern match in s
                        start = m.start();
                    }
                }

                if (noOfBlanks > 0) {
                    s = setHighlights(s, include, blanks, start, end);
                }
                results.add(s);
            }
        }
        return results;
    }

    private String setPattern(String include, String letters) {
        String regex = include;
        // if the leading and trailing underscores were not redundant set the regex
        if (include.charAt(0) == '_' && include.charAt(include.length() - 1) == '_') {
            if (letters.length() != include.length()) {
                regex = "\\w*" + include.replaceAll("^_+|_+$", "") + "\\w*";
            }
        }
        // if the expression starts with an underscore, want to find words that end with the
        // letters that come after the underscore
        else if (include.charAt(0) == '_') {
            regex = "\\w+" + include.replaceAll("^_+|_+$", "") + "$";
        }
        // if only the last character is an underscore, look for words that start with the
        // letters that come before the underscore
        else if (include.charAt(include.length() - 1) == '_') {
            regex = "^" + include.replaceAll("^_+|_+$", "") + "\\w*";
        }
        return regex.replace("_", ".");
    }

    private String setHighlights(String s, String include, char[] blanks, int start, int end) {
        int blanksFound = 0;
        int charToCompare = include == null ? -1 : include.length() - 1;
        for (int i = s.length() - 1; i >= 0; i--) {
            // do not highlight letters included in a pattern
            if (i >= start  && i <= end) {
                if (include != null && charToCompare >= 0) {
                    if (s.charAt(i) == include.charAt(charToCompare)) {
                        charToCompare--;
                        continue;
                    }
                }
            }
            for (int j = 0; j < blanks.length; j++) {
                if (s.charAt(i) == blanks[j]) {
                    s = s.substring(0, i) + "<font color='" + getResources().getColor(R.color.accent) + "'>" +
                            s.charAt(i) + "</font>" + s.substring(i + 1);
                    blanks[j] = 0;
                    blanksFound++;
                    charToCompare--;
                }
            }
            if (blanksFound == blanks.length) {
                break;
            }
        }
        return s;
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
        ArrayList<Group> groups = new ArrayList<>();
        int groupId;

        for (String word : results) {
            String strippedWord = word.replace("<font color='" + getResources().getColor(R.color.accent) + "'>", "");
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
                            getResources().getString(R.string.orderby_error),
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
                        groupName = String.valueOf(groupId) +  " " +
                                getResources().getString(R.string.orderby_letters);
                        break;
                    case "2":
                        groupName = String.valueOf(groupId) + " " +
                                getResources().getString(R.string.orderby_points);
                        break;
                    default:
                        groupName = String.valueOf(groupId) +  " " +
                                getResources().getString(R.string.orderby_letters);
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

    private class FindWordsTask extends AsyncTask<String, Void, ArrayList<Group>> {

        @Override
        protected void onPreExecute() {
            if (callbacks != null) {
                pb = new ProgressBar(callbacks.get().getApplicationContext(), null,
                        android.R.attr.progressBarStyleHorizontal);
                pb.setIndeterminate(true);
                ((LinearLayout) v).addView(pb);
            }
        }

        protected ArrayList<Group> doInBackground(String... letters) {
            String allLetters = letters[0];
            if (include != null) {
                String lettersToAdd = include.replace("_", "");
                allLetters = allLetters + lettersToAdd;
            }
            int minLetters = Integer.parseInt(PreferenceManager.
                    getDefaultSharedPreferences(callbacks.get())
                    .getString(LENGTH_KEY, LENGTH_DEFAULT));

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
                        String strippedLeft = lhs.replace("<font color='" + getResources().getColor(R.color.accent) + "'>", "");
                        strippedLeft = strippedLeft.replace("</font>", "");

                        String strippedRight = rhs.replace("<font color='" + getResources().getColor(R.color.accent) + "'>", "");
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

            rawResults = results;
            if (callbacks != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(callbacks.get());
                String orderBy = prefs.getString(ORDER_KEY, ORDER_DEFAULT);

                groups.addAll(sortResults(results, orderBy, currentHighlight));
            }
            return groups;
        }

        protected void onPostExecute(ArrayList<Group> groups) {
            // if upon trying to restore the fragments previous state the wordlist is null, reset
            // the app to it's starting conditions
            if (callbacks != null) {
                if (groups == null) {
                    callbacks.get().reset();
                } else {
                    if (getActivity() != null) {
                        adapter = new DisplayExpandableResultAdapter(
                                getActivity().getApplicationContext(), groups);
                        lv.setAdapter(adapter);
                        //adapter.notifyDataSetChanged();
                        if (groups.isEmpty()) {
                            TextView emptyView = (TextView) v.findViewById(R.id.emptyView);
                            lv.setEmptyView(emptyView);
                        }
                    }
                }
                ((LinearLayout) v).removeView(pb);
            }
        }
    }
}
