package ru.ifmo.md.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import ru.ifmo.md.weather.db.WeatherContentProvider;


public class MainActivity extends ActionBarActivity implements CitiesFragment.OnCitySelectedListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

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

        Toolbar toolbar = (Toolbar)findViewById(R.id.main_toolbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        /*toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return myOnMenuItemClick(item);
                    }
                });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.main_menu);*/
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
        super.onStart();
    }


    @Override
    public void onCitySelected(int index, long cityId) {
        Toast.makeText(this, "City selected: " + index, Toast.LENGTH_SHORT).show();
        chosenCityIndex = index;
        chosenCityId = cityId;
        if (isDualPane) {
            forecastFragment.display(cityId);
        }
        else {
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
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Toast.makeText(this, "Action refresh selected", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, LoadWeatherService.class);
                i.putExtra(LoadWeatherService.REQUEST_TYPE, LoadWeatherService.UPDATE_ALL_REQUEST);
                startService(i);
                break;
            case R.id.action_add:
                Toast.makeText(this, "Action Settings selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AddNewCityActivity.class);
                startActivity(intent);
                citiesFragment.onResume();
                /*intent = new Intent(this, LoadWeatherService.class);
                intent.putExtra(LoadWeatherService.REQUEST_TYPE, LoadWeatherService.UPDATE_ALL_REQUEST);
                startService(intent);*/
                break;

            case R.id.action_clear_weather:
                Toast.makeText(this, "Action Clear weather selected", Toast.LENGTH_SHORT).show();
                deleteAllWeather();
                break;

            default:
                break;
        }

        return true;
    }

    private void deleteAllWeather() {
        int r = getContentResolver().delete(WeatherContentProvider.CONTENT_URI_WEATHER, null, null);
        r = getContentResolver().delete(WeatherContentProvider.CONTENT_URI_CITIES, null, null);
    }

    public void startAlarmManager() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 8000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarmManager() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
    }

}
