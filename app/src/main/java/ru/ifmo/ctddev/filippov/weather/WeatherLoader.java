package ru.ifmo.ctddev.filippov.weather;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Dima_2 on 01.04.2015.
 */
public class WeatherLoader extends IntentService {
    private static final String ACTION_GET_ALL = "ru.ifmo.ctddev.filippov.weather.action.GET_ALL";
    private static final String ACTION_GET_SINGLE = "ru.ifmo.ctddev.filippov.weather.action.GET_SINGLE";
    private static final String EXTRA_SINGLE_ID = "ru.ifmo.ctddev.filippov.weather.extra.SINGLE_ID";

    public WeatherLoader() {
        super("WeatherLoader");
    }

    public static void getAll(Context context) {
        Intent intent = new Intent(context, WeatherLoader.class);
        intent.setAction(ACTION_GET_ALL);
        context.startService(intent);
    }

    public static void getSingle(Context context, int rowId) {
        Intent intent = new Intent(context, WeatherLoader.class);
        intent.setAction(ACTION_GET_SINGLE);
        intent.putExtra(EXTRA_SINGLE_ID, rowId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        if (ACTION_GET_ALL.equals(action)) {
            handleActionGetAll();
        } else if (ACTION_GET_SINGLE.equals(action)) {
            final int argument = intent.getIntExtra(EXTRA_SINGLE_ID, -1);
            handleActionGetSingle(argument);
        }
    }

    private void handleActionGetAll() {
        int[] ids;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                WeatherContentProvider.URI_CITY_DIRECTORY,
                new String[]{WeatherDatabase.COLUMN_URL},
                null,
                null,
                null
        );
        ids = new int[cursor.getCount()];
        int indexNumber = 0;
        cursor.moveToNext();
        while (!cursor.isAfterLast()) {
            ids[indexNumber] = cursor.getInt(0);
            indexNumber++;
            cursor.moveToNext();
        }
        cursor.close();
        for (int index : ids) {
            loadSingleCity(index);
        }
    }

    private void handleActionGetSingle(int cityId) {
        loadSingleCity(cityId);
    }

    public static String streamToString(InputStream s) {
        Scanner scanner = new Scanner(s);
        scanner.useDelimiter("\\A");
        return scanner.next();
    }

    private void loadSingleCity(int cityId) {
        URL url = null;
        try {
            url = new URL("http://api.openweathermap.org/data/2.5/weather?units=metric&id=" + cityId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        boolean fail = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            JSONObject jsonObject = new JSONObject(streamToString(inputStream));
            JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
            JSONObject main = jsonObject.getJSONObject("main");
            JSONObject wind = jsonObject.getJSONObject("wind");
            double temperature = main.getDouble("temp");
            int pressure = main.getInt("pressure");
            int wet = main.getInt("humidity");
            int clouds = jsonObject.getJSONObject("clouds").getInt("all");

            ContentValues contentValues = new ContentValues();
            contentValues.put(WeatherDatabase.COLUMN_CLOUDS, clouds);
            contentValues.put(WeatherDatabase.COLUMN_PRESSURE, pressure);
            contentValues.put(WeatherDatabase.COLUMN_TEMPERATURE, temperature);
            contentValues.put(WeatherDatabase.COLUMN_WIND, wind.getDouble("speed"));
            contentValues.put(WeatherDatabase.COLUMN_WIND_DIRECTION, wind.getInt("deg"));
            contentValues.put(WeatherDatabase.COLUMN_DESCRIPTION, weather.getInt("id"));
            contentValues.put(WeatherDatabase.COLUMN_WET, wet);

            getContentResolver().update(
                    WeatherContentProvider.URI_CITY_DIRECTORY.buildUpon().appendPath("" + cityId).build(),
                    contentValues,
                    null,
                    null
            );
            getContentResolver().notifyChange(WeatherContentProvider.URI_CITY_DIRECTORY, null);
            fail = false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (!fail) {
            MainActivity.updateTime();
        }

        try {
            url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?units=metric&cnt=5&id=" + cityId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            JSONObject jsonObject = new JSONObject(streamToString(inputStream));
            JSONArray jsonArray = jsonObject.getJSONArray("list");

            ContentValues contentValues = new ContentValues();
            if (jsonArray.length() > 0) {
                getContentResolver().delete(
                        WeatherContentProvider.URI_CITY_DIRECTORY.buildUpon().appendPath("" + cityId).build(),
                        null,
                        null
                );
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonItem = jsonArray.getJSONObject(i);

                JSONObject temps = jsonItem.getJSONObject("temp");
                JSONObject weather = jsonItem.getJSONArray("weather").getJSONObject(0);
                double minTemperature = temps.getDouble("min");
                double maxTemperature = temps.getDouble("max");
                int pressure = jsonItem.getInt("pressure");
                int wet = jsonItem.getInt("humidity");
                int clouds = jsonItem.getInt("clouds");

                contentValues.put(WeatherDatabase.COLUMN_CLOUDS, clouds);
                contentValues.put(WeatherDatabase.COLUMN_PRESSURE, pressure);
                contentValues.put(WeatherDatabase.COLUMN_TEMPERATURE_MIN, minTemperature);
                contentValues.put(WeatherDatabase.COLUMN_TEMPERATURE_MAX, maxTemperature);
                contentValues.put(WeatherDatabase.COLUMN_WIND, jsonItem.getDouble("speed"));
                contentValues.put(WeatherDatabase.COLUMN_WIND_DIRECTION, jsonItem.getInt("deg"));
                contentValues.put(WeatherDatabase.COLUMN_DESCRIPTION, weather.getInt("id"));
                contentValues.put(WeatherDatabase.COLUMN_WET, wet);
                contentValues.put(WeatherDatabase.COLUMN_TIME, jsonItem.getInt("dt"));

                getContentResolver().insert(
                        ContentUris.withAppendedId(WeatherContentProvider.URI_CITY_DIRECTORY, cityId),
                        contentValues
                );
                getContentResolver().notifyChange(
                        ContentUris.withAppendedId(WeatherContentProvider.URI_CITY_DIRECTORY, cityId),
                        null
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }
}
