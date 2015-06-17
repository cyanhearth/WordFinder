package com.example.cyanhearth.wordfinder;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;


public class MainActivity extends ActionBarActivity {

    private EditText lettersInput;
    private TextView textView;
    private WordFinder wordFinder;
    private String currentDict = "sowpods";
    private int resourceId = R.raw.sowpods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Thread init = new Thread(new Runnable() {

            @Override
            public void run() {
                wordFinder = new WordFinder(getResources().openRawResource(R.raw.sowpods));
            }
        });
        init.start();

        try {
            init.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        Button find = (Button)findViewById(R.id.button);
        lettersInput = (EditText)findViewById(R.id.editText);
        textView = (TextView)findViewById(R.id.textView);

        Spinner spinner = (Spinner)findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.dictionaries,
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String res = parent.getItemAtPosition(position).toString();

                switch (res) {
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

                if (!res.equals(currentDict))  {
                    currentDict = res;
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            wordFinder.setWordList(getResources().openRawResource(resourceId));
                        }
                    });
                    t.start();
                    try {
                        t.join();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Toast dictSetToast = Toast.makeText(getApplicationContext(), "Dictionary changed!", Toast.LENGTH_SHORT);
                    dictSetToast.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button check = (Button)findViewById(R.id.button2);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        ArrayList<String> results = new ArrayList<>(
                                (HashSet<String>)wordFinder.possibleWords(lettersInput.getText().toString()));
                        Collections.sort(results);
                        Collections.sort(results, new Comparator<String>() {

                            @Override
                            public int compare(String s1, String s2) {
                                if (s1.length() > s2.length())
                                    return -1;
                                else if (s1.length() < s2.length())
                                    return 1;
                                else {
                                    return 0;
                                }
                            }
                        });

                        Intent intent = new Intent(MainActivity.this, DisplayResultActivity.class);
                        intent.putStringArrayListExtra("results", results);

                        startActivity(intent);
                        results.clear();

                    }
                });
                t.start();

                try {
                    t.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (wordFinder.isValidWord(lettersInput.getText().toString())) {
                    textView.setText("Yes!");
                }
                else {
                    textView.setText("No :(");
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
