package ru.ifmo.md.weather;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;


/**
 * Created by Kirill on 08.12.2014.
 */
public class ForecastActivity extends Activity {
    int chosenCityIndex, chosenCityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ForecastActivity", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        chosenCityIndex = getIntent().getExtras().getInt("cityItemIndex", 0);
        chosenCityId = getIntent().getExtras().getInt("cityItemId", 0);

        if (savedInstanceState == null) {
            ForecastFragment f = new ForecastFragment();
            getFragmentManager().beginTransaction().add(R.id.forecast_container, f).commit();
        }

        if (getResources().getBoolean(R.bool.has_two_panes)) {
            finish();
            return;
        }

        /*FragmentManager fm = getFragmentManager();
        ForecastFragment f = new ForecastFragment();
        int id = android.R.id.content;
        fm.beginTransaction().add(id, f).commit();*/
        /*f = (ForecastFragment) getFragmentManager().findFragmentById(R.id.forecast);
        if (f != null)
            getFragmentManager().beginTransaction().replace(android.R.id.content, f).commit();
        else {
            f = new ForecastFragment();
            fm.beginTransaction().add(android.R.id.content, f).commit();
        }*/

        //f.display(chosenCityId);
    }
}
