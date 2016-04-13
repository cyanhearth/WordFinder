package com.example.cyanhearth.wordfinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class WordDialogFragment extends DialogFragment {

    private String word;

    public static WordDialogFragment newInstance() {
        return new WordDialogFragment();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        word = getArguments().getString(MainActivity.LETTERS_KEY);

        return new AlertDialog.Builder(getActivity())
                .setMessage(String.format(getResources().getString(R.string.word_dialog_string), word))
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                .setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) getActivity()).sendIntentForDefinition(word);
                            }
                        }).create();
    }
}
