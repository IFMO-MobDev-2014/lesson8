package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LoaderManager.LoaderCallbacks {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (mNavigationDrawerFragment.adapter != null && mNavigationDrawerFragment.adapter.getCount() > 0)
            onNavigationDrawerItemSelected(mNavigationDrawerFragment.mCurrentSelectedPosition);

        if (getSharedPreferences("FirstRun", MODE_PRIVATE).getBoolean("FirstRun", true)) {
            Toast toast = Toast.makeText(this, "Getting current city...", Toast.LENGTH_SHORT);
            toast.show();

            getSupportLoaderManager().initLoader(1, null, this);
            getSharedPreferences("FirstRun", MODE_PRIVATE).edit().putBoolean("FirstRun", false).commit();
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        String name = null, zmw = null;
        if (mNavigationDrawerFragment == null || mNavigationDrawerFragment.adapter == null) {
        } else if (position >= 0) {
            name = (String) mNavigationDrawerFragment.adapter.getItem(position);
            zmw = (String) mNavigationDrawerFragment.adapter.getItemZMW(position);
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(name, zmw))
                .commit();
        onSectionAttached(Math.max(0, position));
        restoreActionBar();
    }

    public void onSectionAttached(int number) {
        if (mNavigationDrawerFragment == null || mNavigationDrawerFragment.adapter == null ||
                number >= mNavigationDrawerFragment.adapter.getCount()) {
            mTitle = getString(R.string.app_name);
            return;
        }

        mTitle = (String)mNavigationDrawerFragment.adapter.getItem(number);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new GeolookupLoader(this, mNavigationDrawerFragment.adapter);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if ((Boolean)data) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mNavigationDrawerFragment.selectItem(mNavigationDrawerFragment.adapter.getCount() - 1);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor> {
        private ForecastAdapter adapter;
        private SmoothProgressBar progressBar;

        static class WeatherObserver extends ContentObserver {
            PlaceholderFragment fragment;
            Handler handler;

            public WeatherObserver(Handler handler, PlaceholderFragment placeholderFragment) {
                super(handler);
                this.handler = handler;
                fragment = placeholderFragment;
            }

            @Override
            public void onChange(boolean selfChange) {
                this.onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (fragment.isAdded())
                    fragment.reloadData();
            }
        }

        private void reloadData() {
            getLoaderManager().restartLoader(1, null, this);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String name, String zwm) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(WeatherProvider.NAME, name);
            args.putString(WeatherProvider.ZMW, zwm);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            mRecyclerView.setHasFixedSize(true);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);

            if (getArguments() != null) {
                String city = getArguments().getString(WeatherProvider.NAME);
                String zmw = getArguments().getString(WeatherProvider.ZMW);

                progressBar = (SmoothProgressBar) rootView.findViewById(R.id.materialProgress);
                if (zmw != null)
                    LocalBroadcastManager.getInstance(getActivity()).registerReceiver(loadFinishedReceiver,
                            new IntentFilter(zmw));

                adapter = new ForecastAdapter(getActivity(), this, city, zmw);
                mRecyclerView.setAdapter(adapter);

                Intent loadForecast = new Intent(getActivity(), WeatherService.class);
                loadForecast.putExtra(WeatherProvider.NAME, adapter.name);
                loadForecast.putExtra(WeatherProvider.ZMW, adapter.zmw);
                loadForecast.putExtra("force", false);
                getActivity().startService(loadForecast);
            }

            return rootView;
        }

        private BroadcastReceiver loadFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (intent.getExtras().getBoolean("isLoadActive"))
                        progressBar.setVisibility(View.VISIBLE);
                    else
                        progressBar.setVisibility(View.GONE);

                    if (intent.getExtras().getBoolean("error")) {
                        Toast toast = Toast.makeText(getActivity(),
                                getString(R.string.load_error), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (Exception ignored) {}
            }
        };

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            reloadData();
            getActivity().getContentResolver().registerContentObserver(WeatherProvider.WEATHER_URI,
                    true, new WeatherObserver(new Handler(), this));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(loadFinishedReceiver);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] projection = {WeatherProvider.ICON, WeatherProvider.TXT, WeatherProvider.WDAY,
                    WeatherProvider.YEAR, WeatherProvider.YDAY};
            if (adapter.zmw != null)
                return new CursorLoader(getActivity(), WeatherProvider.WEATHER_URI, projection,
                    WeatherProvider.ZMW + "=?", new String[] {adapter.zmw}, WeatherProvider._ID);
            else
                return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ArrayList<ForecastAdapter.WeatherEntry> tmp = new ArrayList<>();
            while (data.moveToNext()) {
                tmp.add(new ForecastAdapter.WeatherEntry(data.getBlob(data.getColumnIndex(WeatherProvider.ICON)),
                        data.getString(data.getColumnIndex(WeatherProvider.TXT)),
                        data.getString(data.getColumnIndex(WeatherProvider.WDAY)),
                        data.getInt(data.getColumnIndex(WeatherProvider.YEAR)),
                        data.getInt(data.getColumnIndex(WeatherProvider.YDAY))));
            }
            data.close();
            adapter.mDataset = tmp;

            adapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            loader.stopLoading();
        }
    }

}
