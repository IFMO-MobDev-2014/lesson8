package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.Intent;
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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public final String API_KEY = "f59a47febebdfe9a";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

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
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        String name = null, zmw = null;
        if (mNavigationDrawerFragment == null || mNavigationDrawerFragment.adapter == null) {
        } else {
            name = (String) mNavigationDrawerFragment.adapter.getItem(position + 1);
            zmw = (String) mNavigationDrawerFragment.adapter.getItemZMW(position + 1);
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(name, zmw))
                .commit();
        onSectionAttached(position + 1);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor> {
        private ForecastAdapter adapter;

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
            adapter.mDataset.clear();
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
                String zwm = getArguments().getString(WeatherProvider.ZMW);

                adapter = new ForecastAdapter(getActivity(), this, city, zwm);
                mRecyclerView.setAdapter(adapter);

                Intent loadForecast = new Intent(getActivity(), WeatherService.class);
                loadForecast.putExtra(WeatherProvider.NAME, adapter.name);
                loadForecast.putExtra(WeatherProvider.ZMW, adapter.zmw);
                loadForecast.putExtra("force", false);
                getActivity().startService(loadForecast);
            }

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            reloadData();
            getActivity().getContentResolver().registerContentObserver(WeatherProvider.WEATHER_URI,
                    true, new WeatherObserver(new Handler(), this));
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
            while (data.moveToNext()) {
                adapter.mDataset.add(new ForecastAdapter.WeatherEntry(data.getString(data.getColumnIndex(WeatherProvider.ICON)),
                        data.getString(data.getColumnIndex(WeatherProvider.TXT)),
                        data.getString(data.getColumnIndex(WeatherProvider.WDAY)),
                        data.getInt(data.getColumnIndex(WeatherProvider.YEAR)),
                        data.getInt(data.getColumnIndex(WeatherProvider.YDAY))));
            }
            data.close();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            loader.stopLoading();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }

}
