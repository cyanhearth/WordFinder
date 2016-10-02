package com.example.cyanhearth.wordfinder;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Craig on 27/09/2016.
 */

public class SetDefaultDictionaryFragment extends DialogFragment implements AdapterView.OnItemClickListener{

    ListView dictList;
    String[] dictionaries;

    private WeakReference<MainActivity> callbacks;

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            callbacks = new WeakReference<>((MainActivity) context);
        }
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dictionaries = getResources().getStringArray(R.array.dictionaries);

        CustomAdapter<String> adapter = new CustomAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, dictionaries);

        dictList.setAdapter(adapter);

        dictList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();

        String dictString = dictionaries[position];
        String dictValue = dictString.substring(0, dictString.indexOf(" "));
        if (position == dictionaries.length - 1) {
            dictValue = "WWF";
        }

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit()
                .putString(SettingsActivity.DICTIONARY_KEY, dictValue)
                .apply();

        if (isAdded() && callbacks.get() != null) {
            callbacks.get().loadDictionary(dictValue);
        }
    }

    public class CustomAdapter<t> extends ArrayAdapter {

        public CustomAdapter(Context context, int resource, Object[] objects) {
            super(context, resource, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = (TextView) super.getView(position, convertView, parent);

            tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

            return tv;
        }
    }
}
