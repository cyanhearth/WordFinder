package com.example.cyanhearth.wordfinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by cyanhearth on 26/08/2015.
 */
public class WordDialogFragment extends DialogFragment {

    private static String word;

    public static WordDialogFragment newInstance(String currentWord) {
        word = currentWord;
        return new WordDialogFragment();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(word + " is in the current dictionary.\n\nWould you like to find the definition?\n" +
                        "(Note: requires internet access)")
                .setCancelable(false)
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: open browser and get word definition
                                ((MainActivity) getActivity()).sendIntentForDefinition(word);
                            }
                        }).create();
    }
}
