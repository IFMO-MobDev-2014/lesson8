package ru.ifmo.md.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

import ru.ifmo.md.weather.db.WeatherContentProvider;
import ru.ifmo.md.weather.db.model.CityTable;


public class MainActivity extends ActionBarActivity implements
        CitiesFragment.OnCitySelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    boolean isDualPane = false;

    CitiesFragment citiesFragment;
    ForecastFragment forecastFragment;

    int chosenCityIndex = 0;

    long chosenCityId = 0;

    private PendingIntent pendingIntent;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(LoadWeatherService.RESULT);
                String error = bundle.getString(LoadWeatherService.ERROR_MSG);
                if (resultCode == 1) {
                    Toast.makeText(MainActivity.this,
                            "Download complete.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        citiesFragment = (CitiesFragment) getFragmentManager().findFragmentById(
                R.id.cities);
        forecastFragment = (ForecastFragment) getFragmentManager().findFragmentById(
                R.id.forecast);

        //getFragmentManager().beginTransaction().add(R.id.cities, citiesFragment).commit();

        View forecastView = findViewById(R.id.forecast);
        isDualPane = forecastView != null && forecastView.getVisibility() == View.VISIBLE;

        if (isDualPane)
            getFragmentManager().beginTransaction().add(R.id.forecast, forecastFragment).commit();

        citiesFragment.setOnCitySelectedListener(this);

        getSupportActionBar().setTitle("Places");
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoUpdate = settingsPref.getBoolean(getResources().getString(R.string.settings_set_auto_update), false);
        String intervalStr = settingsPref.getString(getResources().getString(R.string.settings_update_interval), "0");
        int interval = 0;
        if (autoUpdate) {
            try {
                interval = Integer.parseInt(intervalStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startAlarmManager(interval);
    }

    void restoreSelection(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (isDualPane) {
                int cityIndex = savedInstanceState.getInt("cityItemIndex", 0);
                long cityId = savedInstanceState.getLong("cityId", 0);
                onCitySelected(cityIndex, cityId);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        restoreSelection(savedInstanceState);
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAlarmManager();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i("onConnected", "location");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        mLocationRequest.setNumUpdates(1);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("", "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("", "Location received: " + location.toString());

        try {
            Geocoder gcd = new Geocoder(this, Locale.ENGLISH);
            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                String city = addresses.get(0).getLocality();
                if (city == null)
                    city = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryCode();
                ContentValues values = new ContentValues();
                values.put(CityTable.NAME_COLUMN, city.trim());
                values.put(CityTable.COUNTRY_COLUMN, country.trim());
                Util.addCityWithNameToBase(this, values);
                startLoadWeatherService(true);
                //Toast.makeText(this, "city: " + city + " add", Toast.LENGTH_SHORT).show();
                citiesFragment.onResume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCitySelected(int index, long cityId) {
        chosenCityIndex = index;
        chosenCityId = cityId;
        if (isDualPane) {
            forecastFragment.display(cityId);
        } else {
            Intent i = new Intent(this, ForecastActivity.class);
            i.putExtra("cityItemIndex", index);
            i.putExtra("cityId", cityId);
            startActivity(i);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("cityItemIndex", chosenCityIndex);
        outState.putLong("cityId", chosenCityId);
        super.onSaveInstanceState(outState);
    }

    //NEW
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu);
        Log.i("onCreate", "menu");
        return true;
    }

    //NEW
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_refresh:
                intent = new Intent(this, LoadWeatherService.class);
                intent.putExtra(LoadWeatherService.REQUEST_TYPE, LoadWeatherService.UPDATE_ALL_REQUEST);
                startService(intent);
                break;
            case R.id.action_add:
                intent = new Intent(this, AddNewCityActivity.class);
                startActivityForResult(intent, 1);
                citiesFragment.onResume();
                break;
            case R.id.action_clear_weather:
                deleteAllWeather();
                break;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            startLoadWeatherService(true);
        }
    }

    private void startLoadWeatherService(boolean forceUpdate) {
        Intent intent = new Intent(this, LoadWeatherService.class);
        intent.putExtra(LoadWeatherService.REQUEST_TYPE, LoadWeatherService.UPDATE_ALL_REQUEST);
        intent.putExtra(LoadWeatherService.FORCE_UPDATE, forceUpdate);
        startService(intent);
    }

    private void deleteAllWeather() {
        int r = getContentResolver().delete(WeatherContentProvider.CONTENT_URI_WEATHER, null, null);
        r = getContentResolver().delete(WeatherContentProvider.CONTENT_URI_CITIES, null, null);
    }

    private void startAlarmManager(int interval) {
        interval *= 1000;
        if (interval == 0) {
            cancelAlarmManager();
            return;
        }
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Log.i("", "Alarm Set");
        //Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarmManager() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Log.i("", "Alarm Canceled");
        //Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
    }

}
