package com.example.alexey.wather_2;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, AppReceiver.Receiver {

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;
    public AppReceiver mReceiver;
    private CharSequence prevmTitle = "none";
    ProgressBar mProgress;
    static String RECEIVER = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        provider p = new provider();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        if (!prevmTitle.equals(mTitle)) {
            TabHost tabs = (TabHost) findViewById(R.id.tabHost);
            tabs.setup();
            TabHost.TabSpec spec = tabs.newTabSpec("tag1");

            spec.setContent(R.id.tab1);
            spec.setIndicator("Today");
            tabs.addTab(spec);

            spec = tabs.newTabSpec("tag2");
            spec.setContent(R.id.tab2);
            spec.setIndicator("Forecast");
            tabs.addTab(spec);
            tabs.setCurrentTab(0);
            if (!provider.isTableExists(provider.db, mTitle.toString())) {
                provider.add_table(mTitle.toString());
                refreshB(new View(this));
            } else {
                showData();
            }
        }
        prevmTitle = mTitle;
        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshB(new View(MainActivity.this));
            }
        });
    }

    void showData() {
        setData_today();
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(getData_forecast());
        mProgress = (ProgressBar) findViewById(R.id.progressBar2);
        mProgress.setVisibility(View.INVISIBLE);
    }

    SimpleCursorAdapter getData_forecast() {
        provider.match_name(mTitle.toString());
        Cursor cursor = getContentResolver().query(provider.CONTENT_URI, null, null, null, null);
        startManagingCursor(cursor);

        String[] from = new String[]{provider.DATE, provider.TEMPERATURE, provider.DAY};
        int[] to = new int[]{R.id.textView6, R.id.textView7, R.id.textView8};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.prog_bar, cursor, from, to);
        return adapter;
    }

    void setData_today() {
        provider.match_name(mTitle.toString());
        Cursor cursor = getContentResolver().query(provider.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        ImageView im = (ImageView) findViewById(R.id.imageView);
        im.setImageBitmap(ImageConverter.getImage(cursor.getBlob(cursor.getColumnIndex(provider.FIVE_PATH))));
        im = (ImageView) findViewById(R.id.imageView2);
        im.setImageBitmap(ImageConverter.getImage(cursor.getBlob(cursor.getColumnIndex(provider.SIX_PATH))));
        TextView tv = (TextView) findViewById(R.id.textView3);
        tv.setText("Today is " + cursor.getString(cursor.getColumnIndex(provider.DATE)));
        tv = (TextView) findViewById(R.id.description);
        tv.setText("Day forecast");
        tv = (TextView) findViewById(R.id.textView);
        tv.setText(cursor.getString(cursor.getColumnIndex(provider.DAY)));
        tv = (TextView) findViewById(R.id.textView1);
        tv.setText("Nigth forecast");
        tv = (TextView) findViewById(R.id.textView2);
        tv.setText(cursor.getString(cursor.getColumnIndex(provider.NIGHT)));


    }

    void refreshB(View view) {
        provider.match_name(mTitle.toString());
        getContentResolver().delete(provider.CONTENT_URI, null, null);
        final Intent intent = new Intent("SOME_COMMAND_ACTION", null, MainActivity.this, IServise.class);
        mReceiver = new AppReceiver(new Handler());
        mReceiver.setReceiver(this);
        intent.putExtra(RECEIVER, mReceiver).putExtra("task", mTitle);
        startService(intent);
        mProgress = (ProgressBar) findViewById(R.id.progressBar2);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {

        Log.i("Result", "got");
        switch (resultCode) {
            case 2:
                mProgress.setVisibility(View.VISIBLE);
                break;
            case 5:
                mProgress.setVisibility(View.INVISIBLE);
                showData();
                break;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
