package com.alex700.AWeather;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.Date;

/**
 * Created by Алексей on 15.12.2014.
 */
public class UpdateAllService extends IntentService {

    public UpdateAllService() {
        super(UpdateAllService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("UPDATING", "start");
        Cursor cities = getApplication().getContentResolver().query(WeatherContentProvider.CITY_CONTENT_URI, null, null, null, null);
        while (cities.moveToNext()) {
            City city = WeatherDatabaseHelper.CityCursor.getCity(cities);
            WeatherData[] weather;
            try {
                weather = WeatherLoaderService.loadWeatherInCity(city);
            } catch (Exception e) {
                Log.d("UPDATING", "error");
                continue;
            }
            update(weather, city);

        }
        Log.d("UPDATING", "finish");
    }

    private void update(WeatherData[] weather, City city) {
        if (weather == null || weather.length == 0) {
            return;
        }
        getContentResolver().delete(WeatherContentProvider.WEATHER_CONTENT_URI, WeatherDatabaseHelper.WEATHER_CITY_ID + " = " + city.getId(), null);
        for (WeatherData weatherData : weather) {
            getContentResolver().insert(WeatherContentProvider.WEATHER_CONTENT_URI, weatherData.getContentValues());
        }
        ContentValues cv = new ContentValues();
        cv.put(WeatherDatabaseHelper.CITY_UPDATE_TIME, new Date().getTime());
        getContentResolver().update(WeatherContentProvider.CITY_CONTENT_URI, cv, WeatherDatabaseHelper.CITY_ID + " = " + city.getId(), null);
       }
}
