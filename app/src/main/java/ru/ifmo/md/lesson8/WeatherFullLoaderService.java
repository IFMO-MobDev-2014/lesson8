package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import org.json.JSONException;

import java.util.Date;

public class WeatherFullLoaderService extends IntentService {

    public WeatherFullLoaderService() {
        super(WeatherFullLoaderService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Cursor allCities = getApplication().getContentResolver().
                query(WeatherContentProvider.CITY_CONTENT_URI, null, null, null, null);
        while (allCities.moveToNext()) {
            City city = WeatherDatabaseHelper.CityCursor.getCity(allCities);
            WeatherData[] weatherData;
            try {
                weatherData = WeatherLoaderService.loadWeatherInCity(city);
            } catch (JSONException e) {
                return;
            }
            update(weatherData, city);
        }
    }

    private boolean update(WeatherData[] weatherData, City city) {
        if (weatherData == null || weatherData.length == 0) {
            return false;
        }

        getContentResolver().delete(WeatherContentProvider.WEATHER_CONTENT_URI,
                WeatherDatabaseHelper.WEATHER_CITY_ID + " = " + city.getId(), null);

        for (WeatherData data : weatherData) {
            getContentResolver().insert(WeatherContentProvider.WEATHER_CONTENT_URI, data.getContentValues());
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherDatabaseHelper.CITY_UPDATE_DATE, new Date().getTime()); //We updated the city
        getContentResolver().update(WeatherContentProvider.CITY_CONTENT_URI, contentValues,
                WeatherDatabaseHelper.CITY_ID + " = " + city.getId(), null);
        return true;
    }
}