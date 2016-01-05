package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.HashSet;

/**
 * Created by cyanhearth on 26/08/2015.
 */
public class LoadDictionaryFragment extends Fragment {

    interface TaskCallbacks {
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute();
    }

    private WeakReference<TaskCallbacks> callbacks;
    public static String currentDict;

    public static HashSet<String> words;

    public static LoadDictionaryFragment newInstance (String res) {
        currentDict = res;
        return new LoadDictionaryFragment();
    }

    public void startTask() {
        LoadDictionaryTask task = new LoadDictionaryTask();
        task.execute(currentDict);
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
        callbacks =  new WeakReference<>((TaskCallbacks)activity);
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

        // Create and execute the background task.
        startTask();
    }


    private class LoadDictionaryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... res) {
            words = new HashSet<>();
            int resourceId;
            // NullPointerException sometimes occurs here on startup!!
            switch (res[0]) {
                case "sowpods":
                    resourceId = R.raw.sowpods;
                    break;
                case "ospd":
                    resourceId = R.raw.ospd;
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
                    words.add(line);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void args) {
            if (callbacks != null)
                callbacks.get().onPostExecute();
        }
    }
}
