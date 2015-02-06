package com.pinguinson.lesson10.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pinguinson.lesson10.R;
import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.db.WeatherContentProvider;
import com.pinguinson.lesson10.services.AsyncResponseReceiver;
import com.pinguinson.lesson10.services.CitySearchTask;
import com.pinguinson.lesson10.services.ForecastParser;
import com.pinguinson.lesson10.services.ForecastService;

/**
 * Created by pinguinson.
 */
public class AddCityActivity extends ActionBarActivity implements TextWatcher, AsyncResponseReceiver<ForecastParser.LocationResult> {

    EditText searchField;
    TextView suggestedCityName;
    ForecastParser.LocationResult currentResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        searchField = (EditText) findViewById(R.id.search_field);
        suggestedCityName = (TextView) findViewById(R.id.suggestion);

        searchField.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            final String name = s.toString();
            final CitySearchTask task = new CitySearchTask(this);
            task.execute(name);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void processFinish(ForecastParser.LocationResult result) {
        if (result == null) {
            suggestedCityName.setText(R.string.add_city_error);
        } else {
            suggestedCityName.setText(result.cityName);
            currentResult = result;
        }
    }

    public void addCity(View v) {
        if (currentResult != null) {
            ContentValues row = new ContentValues();
            row.put(CitiesTable.COLUMN_NAME_CITY_NAME, currentResult.cityName);
            row.put(CitiesTable.COLUMN_NAME_WOEID, currentResult.woeid);
            Uri newUri = getContentResolver().insert(WeatherContentProvider.CITIES_CONTENT_URL, row);
            ForecastService.fetchForecasts(this, newUri.getLastPathSegment(), currentResult.woeid);
            Intent intent = new Intent(ForecastService.ACTION_UPDATE_CITIES_LIST);
            intent.putExtra(ForecastService.STATUS, ForecastService.STATUS_OK);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        finish();
    }

    public void cancel(View v) {
        finish();
    }
}
