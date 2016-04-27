package com.example.cyanhearth.wordfinder;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity {

    ViewPager viewPager;
    MyPagerAdapter adapter;

    private static final String HELP_KEY = "help";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setTitle("Help");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // create floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse("mailto:cyanhearth@gmail.com?subject=WordFinder%20App");
                    intent.setData(uri);
                    startActivity(intent);

                }
            });
        }

        // create swipe views
        adapter = new MyPagerAdapter(getSupportFragmentManager());

        setFragments();

        viewPager = (ViewPager) findViewById(R.id.pager);
        if (viewPager != null) {
            viewPager.setAdapter(adapter);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }

    }

    private void setFragments() {
        Resources resources = getResources();

        HelpFragment searchHelpFragment = new HelpFragment();
        Bundle searchArgs = new Bundle();
        searchArgs.putString(HELP_KEY, resources.getString(R.string.help_search));
        searchHelpFragment.setArguments(searchArgs);

        HelpFragment defineHelpFragment = new HelpFragment();
        Bundle defineArgs = new Bundle();
        defineArgs.putString(HELP_KEY, resources.getString(R.string.help_define));
        defineHelpFragment.setArguments(defineArgs);

        HelpFragment includeHelpFragment = new HelpFragment();
        Bundle includeArgs = new Bundle();
        includeArgs.putString(HELP_KEY, resources.getString(R.string.help_include));
        includeHelpFragment.setArguments(includeArgs);

        HelpFragment settingsHelpFragment = new HelpFragment();
        Bundle settingsArgs = new Bundle();
        settingsArgs.putString(HELP_KEY, resources.getString(R.string.help_settings));
        settingsHelpFragment.setArguments(settingsArgs);

        adapter.addFragment(searchHelpFragment, resources.getString(R.string.button_search));
        adapter.addFragment(defineHelpFragment, resources.getString(R.string.button_define));
        adapter.addFragment(includeHelpFragment, resources.getString(R.string.button_include));
        adapter.addFragment(settingsHelpFragment, resources.getString(R.string.action_settings));
    }


    public static class HelpFragment extends Fragment {

        TextView textView;
        String text;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.help_fragment, container, false);
            textView = (TextView) v.findViewById(R.id.helpTextView);
            textView.setText(text);
            return v;

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            text = getArguments().getString(HELP_KEY);
        }

    }

    public class  MyPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> fragments = new ArrayList<>();
        private final ArrayList<String> fragmentTitles = new ArrayList<>();

        public MyPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment frag, String title) {
            fragments.add(frag);
            fragmentTitles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}
