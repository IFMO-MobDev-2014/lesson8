package com.pinguinson.lesson10.activities;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.pinguinson.lesson10.R;
import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.db.WeatherContentProvider;
import com.pinguinson.lesson10.fragments.CitiesListFragment;
import com.pinguinson.lesson10.fragments.CityDetailFragment;
import com.pinguinson.lesson10.fragments.ForecastListFragment;
import com.pinguinson.lesson10.services.AlarmService;
import com.pinguinson.lesson10.services.ForecastService;

public class MainActivity extends ActionBarActivity
        implements CitiesListFragment.Callbacks, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final String CITY_ID = "city_id";
    public static final String WOEID = "woeid";
    private static final int LOADER_ID = 4;
    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String currentCityId = null;
    private ProgressDialog dialog;
    private CityDetailFragment cityFragment;
    private ForecastListFragment forecastFragment;
    private MainActivity self = this;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (dialog != null) {
                dialog.dismiss();
            }
            int resCode = intent.getIntExtra(ForecastService.STATUS, -1);
            switch (intent.getAction()) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_detail);

        ((CitiesListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.cities_list))
                .setActivateOnItemClick(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.abc_action_bar_home_description,
                R.string.abc_toolbar_collapse_description);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        Intent alarm = new Intent(this, AlarmService.class);
        startService(alarm);
    }

    private void startCurrentLocationUpdate(boolean force) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);

        if (provider != null) {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CITY_ID, currentCityId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        //Simple crutch to prevent app from loading forecast for last city in db on screen rotate
        onItemSelected(savedInstanceState.getString(CITY_ID), null);
    }

    @Override
    public void onItemSelected(String id, Bundle data) {
        if (id.equals(currentCityId)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (data == null) {
            data = new Bundle();
        }

        data.putString(CITY_ID, id);

        cityFragment = new CityDetailFragment();
        forecastFragment = new ForecastListFragment();
        forecastFragment.setArguments(data);
        cityFragment.setArguments(data);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.city_frame, cityFragment)
                .replace(R.id.forecast_frame, forecastFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
        mDrawerLayout.closeDrawers();
        currentCityId = id;
    }

    public void addNewCity() {
        Intent intent = new Intent(this, AddCityActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                WeatherContentProvider.CITIES_CONTENT_URL,
                new String[]{CitiesTable._ID, CitiesTable.COLUMN_NAME_WOEID},
                CitiesTable.COLUMN_NAME_IS_CURRENT + "=?",
                new String[]{"1"}, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                forecastFragment.onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            data.moveToFirst();
            int idCol = data.getColumnIndexOrThrow(CitiesTable._ID);
            int woeidCol = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_WOEID);
            Bundle bundle = new Bundle();
            bundle.putLong(WOEID, data.getLong(woeidCol));
            onItemSelected(data.getString(idCol), bundle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.today_weather_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_location_button:
                startCurrentLocationUpdate(true);
                break;
            case R.id.add_new_city_button:
                addNewCity();
                break;
        }
    }
}
