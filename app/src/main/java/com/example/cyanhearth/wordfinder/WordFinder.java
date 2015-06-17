package com.example.cyanhearth.wordfinder;

/**
 * Created by cyanhearth on 12/03/2015.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;

public final class WordFinder {

    // holds all of the dictionary words
    private HashSet<String> words = new HashSet<>();

    public WordFinder(InputStream resource) {
        setWordList(resource);
    }

    public void setWordList(InputStream resource) {
        if (!words.isEmpty()) words.clear();
        //read in dictionary
        BufferedReader st = new BufferedReader(new InputStreamReader(resource));

        try {
            String line;
            while ((line = st.readLine()) != null) {
                words.add(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidWord(String word) {
        return words.contains(word);
    }

    public Iterable<String> possibleWords(String letters) {
        // hold results
        HashSet<String> results = new HashSet<>();

        // sort the letters we want to make words from
        char[] lettersToChar = letters.toCharArray();
        Arrays.sort(lettersToChar);

        for (String s : words) {
            // if the word contains too many letters move onto the next one
            if (s.length() > letters.length()) continue;

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
                    // if the letter is found in the word increment count
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

}
