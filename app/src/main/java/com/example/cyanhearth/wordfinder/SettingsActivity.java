package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.List;

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

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case DICTIONARY_KEY:
                    String message = getResources().getText(R.string.dict_changed) + " "
                            + sharedPreferences.getString(key, "");
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    break;
                case MIN_LENGTH_KEY:
                    String minLength = sharedPreferences.getString(key, "");
                    if (minLength.equals("0")) {
                        minLength = "Use all letters";
                    }
                    message = getResources().getText(R.string.length_changed) + " " + minLength;
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    break;
                case WIFI_ONLY_KEY:
                    if (sharedPreferences.getBoolean(WIFI_ONLY_KEY, false)) {
                        message = "Wifi only";
                    }
                    else {
                        message = "Wifi or Data";
                    }
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
}
