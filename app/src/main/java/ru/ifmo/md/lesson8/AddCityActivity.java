package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.service.AsyncResponseReceiver;
import ru.ifmo.md.lesson8.service.CitySearchTask;
import ru.ifmo.md.lesson8.service.ForecastParser;
import ru.ifmo.md.lesson8.service.ForecastService;
import ru.ifmo.md.lesson8.service.Throttler;


public class AddCityActivity extends ActionBarActivity implements TextWatcher, AsyncResponseReceiver<ForecastParser.LocationResult> {

    EditText searchField;
    TextView suggestion;
    ForecastParser.LocationResult currentResult;
    Throttler mThrottler = new Throttler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        searchField = (EditText) findViewById(R.id.search_field);
        suggestion = (TextView) findViewById(R.id.suggestion);

        searchField.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(s.length() > 0) {
            final String name = s.toString();
            final CitySearchTask task = new CitySearchTask(this);
            Throttler.Callback foo = new Throttler.Callback() {
                @Override
                public void call() {
                    task.execute(name);
                }
            };
            mThrottler.throttle(foo, 500);
        } else {
            mThrottler.cancel();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void processFinish(ForecastParser.LocationResult result) {
        if(result == null) {
            suggestion.setText("An error occured, sorry :(");
        } else {
            suggestion.setText(result.cityName);
            currentResult = result;
        }
    }

    public void addCity(View v) {
        ContentValues row = new ContentValues();
        row.put(CitiesTable.COLUMN_NAME_NAME, currentResult.cityName);
        row.put(CitiesTable.COLUMN_NAME_WOEID, currentResult.woeid);
        Uri newUri = getContentResolver().insert(WeatherContentProvider.CITIES_CONTENT_URL, row);
        ForecastService.fetchForecasts(this, newUri.getLastPathSegment(), currentResult.woeid);
        Intent intent = new Intent(ForecastService.ACTION_UPDATE_CITIES_LIST);
        intent.putExtra(ForecastService.STATUS, ForecastService.STATUS_OK);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }

    public void cancel(View v) {
        finish();
    }
}
