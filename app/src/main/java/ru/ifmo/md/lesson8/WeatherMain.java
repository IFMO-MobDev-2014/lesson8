package ru.ifmo.md.lesson8;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_main);

        Cursor cursor = getContentResolver().query(CITY_URI, null, null, null, null);
        cursor.moveToFirst();
        int n = cursor.getCount();
        for (int i = 0; i < n; i++) {
            Uri uri = ContentUris.withAppendedId(CITY_URI, cursor.getInt(0));
            getContentResolver().delete(uri, null, null);
            cursor = getContentResolver().query(CITY_URI, null, null, null, null);
            cursor.moveToFirst();
        }

        service = new Intent(this, WeatherService.class);
        service.putExtra("FLAG", "Moscow");
        startService(service);

		IntentFilter intentFilter = new IntentFilter(WeatherService.ACTION);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(broadcast, intentFilter);
    }


    public void setFragments() {
        getSupportActionBar().setTitle("Cities");
        if (getFragmentManager().findFragmentByTag("city") == null) {
            CityListFragment c = new CityListFragment();
            c.setList(items);
            getFragmentManager().beginTransaction().add(R.id.root_layout, c, "city").addToBackStack(null).commit();
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
        getSupportActionBar().setTitle(items.get(position).getName());
        WeatherDetails w = new WeatherDetails();
        w.setItem((items.get(position)));
        getFragmentManager().beginTransaction().replace(R.id.root_layout, w, "detail").addToBackStack(null).commit();
    }

    public void setItems(List<WeatherItem> items) {
        this.items = items;
    }
}
