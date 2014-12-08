package com.example.lesson8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class WeatherDB {

    public static WeatherDB dataBase = null;

    public static final String DB_NAME = "weather";
    public static final int DB_VERSION = 1;
    public static final String DB_TABLE = "cities";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_TEMPRATURE = "temprature";
    public static final String COLUMN_WEATHER = "weather";
    public static final String COLUMN_DATE = "date";

    public static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_CITY + " text not null, " +
                    COLUMN_TEMPRATURE + " text not null, " +
                    COLUMN_WEATHER + " text not null, " +

                    COLUMN_DATE + "0 text not null," +
                    COLUMN_TEMPRATURE + "0 text not null," +
                    COLUMN_WEATHER + "0 text not null," +

                    COLUMN_DATE + "1 text not null," +
                    COLUMN_TEMPRATURE + "1 text not null," +
                    COLUMN_WEATHER + "1 text not null," +

                    COLUMN_DATE + "2 text not null," +
                    COLUMN_TEMPRATURE + "2 text not null," +
                    COLUMN_WEATHER + "2 text not null" +
                    ");";

    private final Context context;

    private DBHelper mDBHelper;
    public SQLiteDatabase mDB;

    public static WeatherDB getDataBase(Context context) {
        if (dataBase == null)
            dataBase = new WeatherDB(context.getApplicationContext()).open();
        return dataBase;
    }

    public WeatherDB(Context context) {
        this.context = context;
    }

    public WeatherDB open() {
        mDBHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
    }


    public Cursor getAllDataCursor() {
        return mDB.query(DB_TABLE,
                new String[]{
                        COLUMN_ID,
                        COLUMN_CITY,
                        COLUMN_TEMPRATURE,
                        COLUMN_WEATHER,

                        COLUMN_DATE + "0",
                        COLUMN_TEMPRATURE + "0",
                        COLUMN_WEATHER + "0",

                        COLUMN_DATE + "1",
                        COLUMN_TEMPRATURE + "1",
                        COLUMN_WEATHER + "1",

                        COLUMN_DATE + "2",
                        COLUMN_TEMPRATURE + "2",
                        COLUMN_WEATHER + "2",
                }, null, null, null, null, null);
    }

    public long insertData(Forecast forecast) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CITY, forecast.getCity());
        contentValues.put(COLUMN_TEMPRATURE, forecast.getTemp());
        contentValues.put(COLUMN_WEATHER, forecast.getWeather());

        ArrayList<DayForecast> arrayList = forecast.getCities();
        if (arrayList.size() != 3) {
            for (int i = 0; i < 3; i++)
                arrayList.add(new DayForecast("", "", ""));
        }
        for (int i = 0; i < arrayList.size(); i++) {
            DayForecast forecastWeather = arrayList.get(i);
            contentValues.put(COLUMN_DATE + String.valueOf(i), forecastWeather.getDate());
            contentValues.put(COLUMN_TEMPRATURE + String.valueOf(i), forecastWeather.getTemp());
            contentValues.put(COLUMN_WEATHER + String.valueOf(i), forecastWeather.getWeather());
        }

        return mDB.insert(DB_TABLE, null, contentValues);
    }


    public void updateCity(String city, Forecast forecast) {
        Cursor cursor = getAllDataCursor();
        long id = 0;
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex(COLUMN_CITY)).equals(city))
                id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        }
        cursor.close();
        updateCity(id, forecast);
    }

    private void updateCity(long id, Forecast forecast) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CITY, forecast.getCity());
        contentValues.put(COLUMN_WEATHER, forecast.getWeather());
        contentValues.put(COLUMN_TEMPRATURE, forecast.getTemp());

        ArrayList<DayForecast> arrayList = forecast.getCities();
        for (int i = 0; i < arrayList.size(); i++) {
            DayForecast forecastWeather = arrayList.get(i);
            contentValues.put(COLUMN_DATE + String.valueOf(i), forecastWeather.getDate());
            contentValues.put(COLUMN_TEMPRATURE + String.valueOf(i), forecastWeather.getTemp());
            contentValues.put(COLUMN_WEATHER + String.valueOf(i), forecastWeather.getWeather());
        }

        mDB.update(DB_TABLE, contentValues, COLUMN_ID + "=" + id, null);
    }
}