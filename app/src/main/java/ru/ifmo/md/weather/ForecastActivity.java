package ru.ifmo.md.weather;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;


/**
 * Created by Kirill on 08.12.2014.
 */
public class ForecastActivity extends Activity {
    int chosenCityIndex;
    long chosenCityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ForecastActivity", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        chosenCityIndex = getIntent().getExtras().getInt("cityItemIndex", 0);
        chosenCityId = getIntent().getExtras().getLong(ForecastFragment.CITY_ID, 0);

        ForecastFragment f = new ForecastFragment();
        f.setArguments(savedInstanceState);
        getFragmentManager().beginTransaction().add(R.id.forecast_container, f).commit();

        //f.display(chosenCityId);

        if (getResources().getBoolean(R.bool.has_two_panes)) {
            finish();
        }
    }
}
