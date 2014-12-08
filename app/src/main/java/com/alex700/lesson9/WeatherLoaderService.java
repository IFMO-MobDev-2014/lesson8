package com.alex700.lesson9;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Алексей on 30.11.2014.
 */
public class WeatherLoaderService extends IntentService {
    public static final String API_KEY = "d690342f9a15003127f67bc6aff8418c";
    public static final String SERVICE_NAME = WeatherLoaderService.class.getName();

    public static final String CITY_NAME = "city_name";

    public static final int UPDATED = 0;
    public static final int ALREADY_UPDATED = 1;
    public static final int UPDATING = 2;
    public static final int ERROR = 3;
    public static final int NUMBER_OF_DAYS = 7;
    public static final long UPDATE_INTERVAL = 10L * 60L * 1000L; // 10 minutes
    private static final List<String> tasks = new ArrayList<>();
    private static Handler handler;

    public WeatherLoaderService() {
        super(SERVICE_NAME);
    }

    public static void loadCity(Context context, String city) {
        context.startService(new Intent(context, WeatherLoaderService.class).putExtra(CITY_NAME, city));
    }


    public static boolean isLoading(String city) {
        return tasks.contains(city);
    }

    public void setHandler(Handler handler) {
        WeatherLoaderService.handler = handler;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        String cityName = intent.getStringExtra(CITY_NAME);
        if (!tasks.contains(cityName)) {
            tasks.add(cityName);
            super.onStart(intent, startId);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String cityName = intent.getStringExtra(CITY_NAME);
        Log.d("service", "start for " + cityName);
        Cursor cursor = getApplication().getContentResolver()
                .query(WeatherContentProvider.CITY_CONTENT_URI, null, WeatherDatabaseHelper.CITY_NAME + "='" + cityName + "'", null, null);
        cursor.moveToNext();
        if (cursor.isAfterLast()) {
            tasks.remove(0);
            return;
        }

        City city = WeatherDatabaseHelper.CityCursor.getCity(cursor);
        Log.d("service","city id " + city.getId());
        if (isAlreadyUpdated(city)) {
            if (handler != null) {
                handler.obtainMessage(ALREADY_UPDATED).sendToTarget();
            }
            tasks.remove(0);
            return;
        }
        if (handler != null) {
            handler.obtainMessage(UPDATING).sendToTarget();
        }
        WeatherData[] weatherData = loadWeatherInCity(city);

        if (update(weatherData, city)) {
            if (handler != null) {
                handler.obtainMessage(UPDATED).sendToTarget();
            }
        } else {
            if (handler != null) {
                handler.obtainMessage(ERROR).sendToTarget();
            }
        }
        tasks.remove(0);
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
        contentValues.put(WeatherDatabaseHelper.CITY_UPDATE_TIME, new Date().getTime());
        getContentResolver().update(WeatherContentProvider.CITY_CONTENT_URI, contentValues,
                WeatherDatabaseHelper.CITY_ID + " = " + city.getId(), null);
        return true;
    }

    private WeatherData[] loadWeatherInCity(City city) {
        Log.d("load", "start load " + city.getId());
        return WeatherFetcher.fetch(city, NUMBER_OF_DAYS, API_KEY);
    }

    private boolean isAlreadyUpdated(City city) {
        Date date = new Date();
        Log.d("IS_ALREADY_UPDATED", date.getTime() + " " + city.getUpdateDate());
        return date.getTime() <= city.getUpdateDate() + UPDATE_INTERVAL;
    }
}
