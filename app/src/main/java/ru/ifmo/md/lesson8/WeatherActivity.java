package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;

import java.util.ArrayList;


public class WeatherActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, ActionBar.OnNavigationListener {
    public static final String CITY_ID_EXTRA = "city_id";
    public static final String LOG_TAG = WeatherActivity.class.getSimpleName();

    private int cityId = -1;
    private ArrayList<City> cities = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayList<String> citiesName = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_weather);
        if (savedInstanceState != null) {
            cityId = savedInstanceState.getInt(CITY_ID_EXTRA);
        }
        getLoaderManager().restartLoader(345738, null, this);
        Log.d(LOG_TAG, "Loaded");
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        Log.d(LOG_TAG, "Created");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CITY_ID_EXTRA, cityId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(LOG_TAG, "onCreateLoader()");
        return new CursorLoader(this, WeatherContentProvider.CITY_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(LOG_TAG, "onLoadFinished()");
        int id = -1;
        cities.clear();
        citiesName.clear();
        while (cursor.moveToNext()) {
            City c = WeatherDatabaseHelper.CityCursor.getCity(cursor);
            Log.d(LOG_TAG,
                    "City loaded: " + c.getName() + " " + c.getId() + " " + c.getUpdateDate());
            cities.add(c);
            citiesName.add(c.getName());
        }
        for (int i = 0; i < cities.size(); ++i) {
            if (cities.get(i).getId() == cityId) {
                id = i;
            }
        }

        if (id == -1) {
            id = 0;
        }
        adapter = new ArrayAdapter<>(getActionBar().getThemedContext(), android.R.layout.simple_list_item_1, android.R.id.text1, citiesName);
        getActionBar().setListNavigationCallbacks(adapter, this);
        getActionBar().setSelectedNavigationItem(id);
        //onNavigationItemSelected(id, -1);
        Log.d(LOG_TAG, "onLoadFinished() exit");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(LOG_TAG, "onLoaderReset()");
        adapter = null;
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        Log.d(LOG_TAG, "onNavigationItemSelected()");
        android.app.Fragment f = getFragmentManager().findFragmentById(R.id.container);
        if (f == null ||
                f.getArguments() == null ||
                f.getArguments().getInt(WeatherFragment.CITY_ID_EXTRA) != cities.get(i).getId()) {
            cityId = cities.get(i).getId();
            Log.d(LOG_TAG, "City: " + cities.get(i).getName());

            android.app.Fragment fragment = new WeatherFragment();
            Bundle args = new Bundle();
            args.putInt(WeatherFragment.CITY_ID_EXTRA, cities.get(i).getId());
            args.putString(WeatherFragment.CITY_NAME_EXTRA, cities.get(i).getName());
            fragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            Log.d(LOG_TAG, "Transaction success");
        }
        return true;
    }
}
