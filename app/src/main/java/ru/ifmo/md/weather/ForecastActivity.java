package ru.ifmo.md.weather;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import ru.ifmo.md.weather.db.WeatherContentProvider;
import ru.ifmo.md.weather.db.model.CityTable;


/**
 * Created by Kirill on 08.12.2014.
 */
public class ForecastActivity extends ActionBarActivity {
    int chosenCityIndex;
    long chosenCityId;
    String cityName;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ForecastActivity", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        chosenCityIndex = getIntent().getExtras().getInt("cityItemIndex", 0);
        chosenCityId = getIntent().getExtras().getLong("cityId", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        Uri uri = Uri.withAppendedPath(WeatherContentProvider.CONTENT_URI_CITIES, chosenCityId+"");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        getSupportActionBar().setTitle(cursor.getString(cursor.getColumnIndex(CityTable.NAME_COLUMN)));
        getSupportActionBar().setHomeButtonEnabled(true);
        //toolbar.setTitle("Places");

        ForecastFragment f = new ForecastFragment();
        f.setArguments(savedInstanceState);
        getFragmentManager().beginTransaction().add(R.id.forecast_container, f).commit();

        if (getResources().getBoolean(R.bool.has_two_panes)) {
            finish();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                /*Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
