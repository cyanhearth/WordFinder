package com.example.cyanhearth.wordfinder;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class LoadDictionaryFragment extends Fragment {

    private WeakReference<MainActivity> callbacks;
    private String currentDict;

    public ArrayList<String> words;

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

    public ArrayList<String> getWords() {
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
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            callbacks = new WeakReference<>((MainActivity) context);
        }
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

        currentDict = getArguments().getString(MainActivity.DICTIONARY_KEY);

        // Create and execute the background task.
        startTask();
    }


    private class LoadDictionaryTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... res) {
            words = new ArrayList<>();
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

            if (!isAdded()) {
                cancel(true);
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

        protected void onPostExecute(ArrayList<String> args) {
            if (callbacks.get() != null) {
                callbacks.get().setDictionary(args);
            }
        }
    }
}
