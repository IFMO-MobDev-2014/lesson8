package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

import ru.ifmo.md.lesson8.data.BackgroundReceiver;
import ru.ifmo.md.lesson8.data.WeatherBroadcast;
import ru.ifmo.md.lesson8.data.WeatherItem;
import ru.ifmo.md.lesson8.data.WeatherListAdapter;
import ru.ifmo.md.lesson8.data.WeatherService;



public class WeatherMain extends ActionBarActivity implements ListListener {

    public Intent service;
    WeatherBroadcast broadcast = new WeatherBroadcast();
    private List<WeatherItem> items;
    private FragmentTransaction transaction;
    private WeatherListAdapter adapter;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private WeatherItem currentWeatherItem;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private boolean resumed = false;
    private WeatherItem resumedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);
        setEverything();

        if (resumed) {
            Log.d("resuming", resumedItem.getName());
            WeatherDetails w = new WeatherDetails();
            w.setItem(resumedItem);
            transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.root_layout, w, "detail");
            transaction.commit();
            resumed = false;
        }
        service = new Intent(getApplicationContext(), WeatherService.class);
        service.putExtra("FLAG", "all");
        startService(service);
        IntentFilter intentFilter = new IntentFilter(WeatherService.ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcast, intentFilter);
    }

    @Override
    public void onPause(){

        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (resumed) {
            Log.d("resuming", resumedItem.getName());
            WeatherDetails w = new WeatherDetails();
            w.setItem(resumedItem);
            transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.root_layout, w, "detail");
            transaction.commit();
            resumed = false;
        }
    }


    public void setEverything() {

        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(0xff686868));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, BackgroundReceiver.class);
        intent.setAction("upload");
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
         //       SystemClock.elapsedRealtime() + 3000, 14400000, pendingIntent);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setSelector(R.drawable.selector);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();
                Log.d("old", "lat : " + latitude);
                Log.d("old", "long : " + longitude);

                service = new Intent(getApplicationContext(), WeatherService.class);
                service.putExtra("FLAG", "current");
                service.putExtra("latitude", latitude);
                service.putExtra("longitude", longitude);
                startService(service);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100000, locationListener);
    }

    public void setFragments() {

        ListView l = (ListView) findViewById(R.id.left_drawer);
        adapter = new WeatherListAdapter(items);
        l.setAdapter(adapter);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawer(drawerList);
                solveSelection(position);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        WeatherDetails details = (WeatherDetails) getFragmentManager().findFragmentByTag("detail");
        if (details != null) {
            resumedItem = details.getItem();
            WeatherDetails w = new WeatherDetails();
            w.setItem(resumedItem);
            transaction = getFragmentManager().beginTransaction();
            transaction.replace(getFragmentManager().findFragmentByTag("detail").getId(), w, "detail");
            transaction.commit();
            resumed = false;
        }
        super.onConfigurationChanged(newConfig);
    }


    public List<WeatherItem> getItems() {
        return items;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weather_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.default_activity_button) {
            service = new Intent(this, WeatherService.class);
            service.putExtra("FLAG", "all");
            startService(service);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcast);
	}

    @Override
    public void solveSelection(int position) {
        WeatherDetails w = new WeatherDetails();
        w.setItem((items.get(position)));
        transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.root_layout, w, "detail");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setItems(List<WeatherItem> items) {
        this.items = items;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void listAppear(MenuItem item) {
        if (drawerLayout.isDrawerOpen(drawerList))
            drawerLayout.closeDrawer(drawerList);
        else
            drawerLayout.openDrawer(drawerList);
    }

    public void addCityButton(MenuItem item) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Add city");
        alert.setMessage("Enter city name in English, please");

        final EditText input = new EditText(this);
        input.setTextColor(0xff000000);
        alert.setView(input);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = String.valueOf(input.getText());
                sendAddRequest(value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();

    }

    void sendAddRequest(String city) {
        service = new Intent(this, WeatherService.class);
        service.putExtra("FLAG", city);
        startService(service);

		IntentFilter intentFilter = new IntentFilter(WeatherService.ACTION);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(broadcast, intentFilter);
    }

    public WeatherItem getCurrentWeatherItem() {
        return currentWeatherItem;
    }

    public void setCurrentWeatherItem(WeatherItem currentWeatherItem) {
        this.currentWeatherItem = currentWeatherItem;
    }

    public void deleteCities(MenuItem item) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Add city");
        alert.setMessage("Enter city name in English, please");

        final EditText input = new EditText(this);
        input.setTextColor(0xff000000);
        alert.setView(input);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = String.valueOf(input.getText());
                sendAddRequest(value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();

    }
}
