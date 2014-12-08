package ru.ifmo.md.lesson8;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.Toast;

import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.service.ForecastService;
import ru.ifmo.md.lesson8.service.Receiver;
import ru.ifmo.md.lesson8.service.SupportReceiver;

public class MainActivity extends ActionBarActivity
        implements CitiesListFragment.Callbacks, Receiver, LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String currentCityId = null;

    public static final String CITY_ID = "city_id";
    public static final String WOEID = "woeid";

    private static final int LOADER_ID = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.abc_action_bar_home_description,
                R.string.abc_toolbar_collapse_description);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ((CitiesListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.cities_list))
                .setActivateOnItemClick(true);

        if (findViewById(R.id.forecast_frame) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateCurrentLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);

        if(provider != null) {

            Location curLoc = locationManager.getLastKnownLocation(provider);
            if (curLoc != null) {
                updateCurrentLocation(curLoc);
            } else {
                locationManager.requestSingleUpdate(provider, locationListener, null);
            }
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Callback method from {@link CitiesListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id, Bundle data) {
        if(id.equals(currentCityId)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if(data == null) {
            data = new Bundle();
        }

        data.putString(CITY_ID, id);

        CityDetailFragment cityFragment = new CityDetailFragment();
        cityFragment.setArguments(data);

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            ForecastListFragment forecastFragment = new ForecastListFragment();
            forecastFragment.setArguments(data);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.today_weather_frame, cityFragment)
                    .replace(R.id.forecast_frame, forecastFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.content_frame, cityFragment)
                    .commit();
        }

        currentCityId = id;

        mDrawerLayout.closeDrawers();
    }

    public void applyForecastFragment(Bundle data) {
        if(!mTwoPane) {
            ForecastListFragment forecastFragment = new ForecastListFragment();
            forecastFragment.setArguments(data);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.content_frame, forecastFragment)
                    .commit();
        }
    }

    public void applyTodayFragment(Bundle data) {
        if(!mTwoPane) {
            CityDetailFragment cityDetailFragment = new CityDetailFragment();
            cityDetailFragment.setArguments(data);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.content_frame, cityDetailFragment)
                    .commit();
        }
    }

    public void updateCurrentLocation(Location loc) {
        ForecastService.getCurrentLocation(this, loc, new SupportReceiver(new Handler(), this));
    }

    @Override
    public void onReceiveResult(int resCode, Bundle resData) {
        switch (resCode) {
            case ForecastService.STATUS_ERROR:
                Toast.makeText(this, resData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_SHORT).show();
                break;
            case ForecastService.STATUS_OK:
                getLoaderManager().restartLoader(LOADER_ID, null, this);
                break;
        }
    }

    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                WeatherContentProvider.CITIES_CONTENT_URL,
                new String[] {CitiesTable._ID, CitiesTable.COLUMN_NAME_WOEID},
                CitiesTable.COLUMN_NAME_IS_CURRENT + "=?",
                new String[] {"1"}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() > 0) {
            data.moveToFirst();
            int idCol = data.getColumnIndexOrThrow(CitiesTable._ID);
            int woeidCol = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_WOEID);
            Bundle bundle = new Bundle();
            bundle.putLong(WOEID, data.getLong(woeidCol));
            onItemSelected(data.getString(idCol), bundle);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
