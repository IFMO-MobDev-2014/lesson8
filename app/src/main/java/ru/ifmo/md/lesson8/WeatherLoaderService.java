package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;

import org.json.JSONException;

import java.util.Date;

public class WeatherLoaderService extends IntentService {
    public static final String API_KEY = "af1fd362b895c36e1a37e182defcf246";
    public static final String SERVICE_NAME = WeatherLoaderService.class.getSimpleName();

    public static final String CITY_NAME = "city_name";
    //Handler codes:
    public static final int UPDATED = 0;
    public static final int ALREADY_UPDATED = 1;
    public static final int UPDATING = 2;
    public static final int ERROR = -1;
    public static final int NUMBER_OF_DAYS = 7;
    public static final long UPDATE_INTERVAL = 10L * 60L * 1000L; //Ten minutes
    private static Handler handler;

    public WeatherLoaderService() {
        super(SERVICE_NAME);
    }

    public static void setHandler(Handler handler) {
        WeatherLoaderService.handler = handler;
    }

    public static void loadCity(Context context, String name) {
        context.startService(new Intent(context, WeatherLoaderService.class).putExtra(CITY_NAME, name));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String cityName = intent.getStringExtra(CITY_NAME);
        Cursor cursor = getApplicationContext().getContentResolver().
                query(WeatherContentProvider.CITY_CONTENT_URI, null,
                        WeatherDatabaseHelper.CITY_NAME + " = '" + cityName + "'", null, null);
        cursor.moveToNext();
        if (cursor.isAfterLast()) {
            return;
        }

        City city = WeatherDatabaseHelper.CityCursor.getCity(cursor);
        if (isAlreadyUpdated(city)) {
            if (handler != null) {
                //It's already updated, so we don't need to do anything
                handler.obtainMessage(ALREADY_UPDATED).sendToTarget();
            }
            return;
        }

        if (handler != null) {
            //We began updating
            handler.obtainMessage(UPDATING).sendToTarget();
        }

        WeatherData[] weatherData;
        try {
            weatherData = loadWeatherInCity(city);
        } catch (JSONException e) {
            if (handler != null) {
                //We successfully updated the database
                handler.obtainMessage(ERROR).sendToTarget();
            }
            return;
        }
        if (update(weatherData, city)) {
            if (handler != null) {
                //We successfully updated the database
                handler.obtainMessage(UPDATED).sendToTarget();
            }
        } else {
            if (handler != null) {
                //Error has occurred
                handler.obtainMessage(ERROR).sendToTarget();
            }
        }
    }

    public static WeatherData[] loadWeatherInCity(City city) throws JSONException {
        return FetchWeatherTask.fetch(city, NUMBER_OF_DAYS, API_KEY);
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

    private boolean isAlreadyUpdated(City city) {
        Date currentDate = new Date();
        return currentDate.getTime() <= (city.getUpdateDate() + UPDATE_INTERVAL);
    }
}
