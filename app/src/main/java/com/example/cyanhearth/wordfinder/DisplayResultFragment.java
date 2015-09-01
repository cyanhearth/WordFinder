package com.example.cyanhearth.wordfinder;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by cyanhearth on 28/08/2015.
 */
public class DisplayResultFragment extends ListFragment {

    private static ArrayList<String> results;

    public interface SelectionListener {
        void onItemSelected(int position);
    }

    private SelectionListener mCallback;

    public static DisplayResultFragment newInstance(ArrayList<String> results) {
        DisplayResultFragment.results = results;

        return new DisplayResultFragment();
    }

    @Override
    public void onCreate(Bundle savedInsanceState) {
        super.onCreate(savedInsanceState);

        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, results));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the hosting Activity has implemented
        // the SelectionListener callback interface. We need this
        // because when an item in this ListFragment is selected,
        // the hosting Activity's onItemSelected() method will be called.

        try {

            mCallback = (SelectionListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectionListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {

        // Notify the hosting Activity that a selection has been made.

        mCallback.onItemSelected(position);

    }
}
