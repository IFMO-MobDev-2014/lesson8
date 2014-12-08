package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;

public class WeatherActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String APP = "ru.ifmo.md.lesson8";
    public static final String CURRENT_CITY = APP + ".current_city";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        CityNameGetService.setHandler(new Handler(new Handler.Callback() {
            private AlertDialog alertDialog;
            @Override
            public boolean handleMessage(Message message) {
                final City city = (City) message.obj;
                SharedPreferences prefs = getSharedPreferences(APP, Context.MODE_PRIVATE);
                String previousCity = prefs.getString(CURRENT_CITY, "");
                if (!previousCity.equals(city.getName())) {
                    Cursor cur = getContentResolver().query(WeatherContentProvider.CITY_CONTENT_URI, null,
                            WeatherDatabaseHelper.CITY_NAME + " = '" + city.getName() + "'", null, null);
                    cur.moveToNext();
                    if (cur.isAfterLast()) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WeatherActivity.this);
                        alertDialogBuilder.setTitle("Auto-geolocation");
                        alertDialogBuilder.setMessage("Your current location is " + city.getName() + ". " +
                                "Do you want to add " + city.getName() +
                                " in list of your cities?");
                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getContentResolver().insert(WeatherContentProvider.CITY_CONTENT_URI, city.getContentValues());
                            }
                        });

                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
                return true;
            }
        }));
        Location lastKnown = ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastKnown != null) {
            startService(new Intent(getApplicationContext(), CityNameGetService.class).putExtra(CityNameGetService.LAT_EXTRA, lastKnown.getLatitude()).putExtra(CityNameGetService.LON_EXTRA, lastKnown.getLongitude()));
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onSectionAttached(String name) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(name);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, City city) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, WeatherFragment.newInstance(city)).commit();
    }

    @Override
    public void onNavigationDrawerItemLongSelected(int position, City city) {
        getContentResolver().delete(WeatherContentProvider.CITY_CONTENT_URI, WeatherDatabaseHelper.CITY_ID + " = " + city.getId(), null);
    }
}
