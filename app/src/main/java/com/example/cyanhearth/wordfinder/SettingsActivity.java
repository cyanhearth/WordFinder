package com.example.cyanhearth.wordfinder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by cyanhearth on 02/09/2015.
 */
public class SettingsActivity extends PreferenceActivity {

    public static final String DICTIONARY_KEY = "dictionary";
    public static final String MIN_LENGTH_KEY = "word_length";
    public static final String WIFI_ONLY_KEY = "wifi_only";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            initPreferences(getPreferenceManager());
        }

        @Override
        public void onResume() {
            super.onResume();

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        private void initPreferences(PreferenceManager manager) {
            Set<String> keys = PreferenceManager.getDefaultSharedPreferences(getActivity()).getAll().keySet();
            for (String key : keys) {
                updateSummary(manager.findPreference(key));
            }
        }

        private void updateSummary(Preference p) {
            if (p instanceof ListPreference) {
                ListPreference listPref = (ListPreference) p;
                listPref.setSummary(listPref.getEntry());
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference p = getPreferenceManager().findPreference(key);
            updateSummary(p);
        }
    }
}
