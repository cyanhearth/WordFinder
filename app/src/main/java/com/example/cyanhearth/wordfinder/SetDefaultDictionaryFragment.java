package com.example.cyanhearth.wordfinder;

import android.app.DialogFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Craig on 27/09/2016.
 */

public class SetDefaultDictionaryFragment extends DialogFragment implements AdapterView.OnItemClickListener{

    ListView dictList;
    String[] dictionaries;
    public static SetDefaultDictionaryFragment newInstance() {
        return new SetDefaultDictionaryFragment();
    }


    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        getDialog().setTitle("Welcome to Word Finder");
        View v = inflater.inflate(R.layout.set_default_dictionary_view, container, false);
        dictList = (ListView) v.findViewById(R.id.dictList);

        return v;

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dictionaries = getResources().getStringArray(R.array.dictionaries);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, dictionaries);

        dictList.setAdapter(adapter);

        dictList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();

        String dictString = dictionaries[position];
        String dictValue = dictString.substring(0, dictString.indexOf(" "));

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit()
                .putString(SettingsActivity.DICTIONARY_KEY, dictValue)
                .apply();

        Snackbar.make(getActivity().findViewById(R.id.layout),
                String.format(getResources().getString(R.string.dict_set),
                        dictionaries[position]),
                Snackbar.LENGTH_SHORT).show();
    }
}
