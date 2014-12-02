package ru.ifmo.md.lesson8;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import ru.ifmo.md.lesson8.data.WeatherBroadcast;
import ru.ifmo.md.lesson8.data.WeatherContentProvider;
import ru.ifmo.md.lesson8.data.WeatherItem;
import ru.ifmo.md.lesson8.data.WeatherService;

import static ru.ifmo.md.lesson8.data.WeatherContentProvider.*;


public class WeatherMain extends ActionBarActivity implements ListListener {

    public Intent service;
    WeatherBroadcast broadcast = new WeatherBroadcast();
    private List<WeatherItem> items;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);

        service = new Intent(this, WeatherService.class);
        service.putExtra("FLAG", "all");
        startService(service);

		IntentFilter intentFilter = new IntentFilter(WeatherService.ACTION);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(broadcast, intentFilter);
    }


    public void setFragments() {
        getSupportActionBar().setTitle("Weather forecast");
        if (getFragmentManager().findFragmentByTag("city") == null) {
            CityListFragment c = new CityListFragment();
            c.setList(items);
            transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.root_layout, c, "city");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            CityListFragment c = (CityListFragment) getFragmentManager().findFragmentByTag("city");
            c.setList(items);
            c.notifyData();
        }
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
        transaction.replace(R.id.root_layout, w, "detail");
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
}
