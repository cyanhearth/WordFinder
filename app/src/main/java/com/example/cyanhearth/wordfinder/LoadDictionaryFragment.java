package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by cyanhearth on 26/08/2015.
 */
public class LoadDictionaryFragment extends Fragment {

    private WeakReference<MainActivity> callbacks;
    private String currentDict;

    public HashSet<String> words;

    public static LoadDictionaryFragment newInstance () {
        return new LoadDictionaryFragment();
    }

    public void startTask() {
        LoadDictionaryTask task = new LoadDictionaryTask();
        task.execute(currentDict);
    }

    public String getCurrentDict() {
        return currentDict;
    }

    public HashSet<String> getWords() {
        return words;
    }

    public void setCurrentDict(String dict) {
        this.currentDict = dict;
    }

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks =  new WeakReference<>((MainActivity)activity);
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        currentDict = getArguments().getString("dict");

        // Create and execute the background task.
        startTask();
    }


    private class LoadDictionaryTask extends AsyncTask<String, Void, HashSet<String>> {

        @Override
        protected HashSet<String> doInBackground(String... res) {
            words = new HashSet<>();
            int resourceId;
            switch (res[0]) {
                case "SOWPODS":
                    resourceId = R.raw.sowpods;
                    break;
                case "OSPD":
                    resourceId = R.raw.ospd;
                    break;
                case "CSW15":
                    resourceId = R.raw.csw15;
                    break;
                case "ENABLE1":
                    resourceId = R.raw.enable1;
                    break;
                default:
                    resourceId = R.raw.sowpods;
                    break;
            }

            //read in dictionary
            BufferedReader st = new BufferedReader(new InputStreamReader(getResources()
                    .openRawResource(resourceId)));

            try {
                String line;
                while ((line = st.readLine()) != null) {
                    words.add(line.toLowerCase());
                }
                st.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return words;
        }

        protected void onPostExecute(HashSet<String> args) {
            //if (callbacks != null)
                //callbacks.get().onPostExecute(args);
        }
    }
}
