package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.service.AlarmStartService;
import ru.ifmo.md.lesson8.service.ForecastService;

public class MainActivity extends ActionBarActivity
        implements CitiesListFragment.Callbacks, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String currentCityId = null;
    private ProgressDialog dialog;

    private MainActivity self = this;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(dialog != null) {
                dialog.dismiss();
            }
            int resCode = intent.getIntExtra(ForecastService.STATUS, -1);
            switch(intent.getAction()) {
                case ForecastService.ACTION_GET_LOCATION_WOEID:
                    switch (resCode) {
                        case ForecastService.STATUS_ERROR:
                            Toast.makeText(self, intent.getStringExtra(Intent.EXTRA_TEXT), Toast.LENGTH_SHORT).show();
                            break;
                        case ForecastService.STATUS_OK:
                            getLoaderManager().restartLoader(LOADER_ID, null, self);
                            break;
                    }
                    break;
            }
        }
    };

    public static final String CITY_ID = "city_id";
    public static final String WOEID = "woeid";

    private static final int LOADER_ID = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((CitiesListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.cities_list))
                .setActivateOnItemClick(true);

        if (findViewById(R.id.forecast_frame) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        } else {
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerToggle = new ActionBarDrawerToggle(this,
                    mDrawerLayout,
                    R.string.abc_action_bar_home_description,
                    R.string.abc_toolbar_collapse_description);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
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

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(ForecastService.ACTION_GET_LOCATION_WOEID));

        startCurrentLocationUpdate(false);
        getLoaderManager().initLoader(LOADER_ID, null, this);

        Intent alarm = new Intent(this, AlarmStartService.class);
        startService(alarm);
    }

    /**
     * Callback method from {@link CitiesListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id, Bundle data) {
        if(id.equals(currentCityId)) {
            if(!mTwoPane) {
                mDrawerLayout.closeDrawers();
            }
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
            mDrawerLayout.closeDrawers();
        }

        currentCityId = id;

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

    private void startCurrentLocationUpdate(boolean force) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);

        if(provider != null) {

            Location curLoc = locationManager.getLastKnownLocation(provider);
            if (!force && curLoc != null) {
                updateCurrentLocation(curLoc);
            } else {
                locationManager.requestSingleUpdate(provider, locationListener, null);
            }
        }
    }

    public void updateCurrentLocation(Location loc) {
        dialog = ProgressDialog.show(this, getString(R.string.current_loc_retrieving), getString(R.string.loading_wait));
        ForecastService.getCurrentLocation(this, loc);
    }

    public void addNewCity() {
        Intent intent = new Intent(this, AddCityActivity.class);
        startActivity(intent);
    }

    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(!mTwoPane) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(!mTwoPane) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (!mTwoPane && mDrawerToggle.onOptionsItemSelected(item)) {
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.current_location_btn:
                startCurrentLocationUpdate(true);
                break;
            case R.id.add_new_city_btn:
                addNewCity();
                break;
        }
    }
}
