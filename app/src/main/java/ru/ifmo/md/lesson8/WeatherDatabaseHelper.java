package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "weather_db";
    public static final int DB_VERSION = 4;

    public static final String CITY_TABLE_NAME = "city";
    public static final String CITY_ID = "_id";
    public static final String CITY_NAME = "name";
    public static final String CITY_UPDATE_DATE = "update_date";
    public static final String CITY_TABLE_CREATE_QUERY =
            "create table " + CITY_TABLE_NAME + " (" + CITY_ID +
                    " integer primary key autoincrement, " +
                    CITY_NAME + " varchar(64)," +
                    CITY_UPDATE_DATE + " integer)";
    public static final String WEATHER_TABLE_NAME = "weather";
    public static final String WEATHER_ID = "_id";
    public static final String WEATHER_TEMPERATURE_MIN = "temperature_min";
    public static final String WEATHER_TEMPERATURE_MAX = "temperature_max";
    public static final String WEATHER_TEMPERATURE = "temperature";
    public static final String WEATHER_WIND_SPEED = "wind_speed";
    public static final String WEATHER_HUMIDITY = "humidity";
    public static final String WEATHER_PRESSURE = "pressure";
    public static final String WEATHER_DATE = "date";
    public static final String WEATHER_CITY_ID = "city_id";
    public static final String WEATHER_WEATHER_MAIN = "weather_main";
    public static final String WEATHER_TABLE_CREATE_QUERY =
            "create table " + WEATHER_TABLE_NAME + " (" + WEATHER_ID +
                    " integer primary key autoincrement, " +
                    WEATHER_TEMPERATURE_MIN + " integer, " +
                    WEATHER_TEMPERATURE_MAX + " integer, " +
                    WEATHER_TEMPERATURE + " integer, " +
                    WEATHER_WIND_SPEED + " integer, " +
                    WEATHER_HUMIDITY + " integer, " +
                    WEATHER_PRESSURE + " integer, " +
                    WEATHER_DATE + " integer, " +
                    WEATHER_CITY_ID + " integer," +
                    WEATHER_WEATHER_MAIN + " varchar(30))";


    public WeatherDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CITY_TABLE_CREATE_QUERY);

        sqLiteDatabase.execSQL(WEATHER_TABLE_CREATE_QUERY);

        ContentValues cv = new ContentValues();
        cv.put(CITY_NAME, "Saint-Petersburg");
        sqLiteDatabase.insert(CITY_TABLE_NAME, null, cv);

        cv = new ContentValues();
        cv.put(CITY_NAME, "Moscow");
        sqLiteDatabase.insert(CITY_TABLE_NAME, null, cv);

        cv = new ContentValues();
        cv.put(CITY_NAME, "Uralsk");
        sqLiteDatabase.insert(CITY_TABLE_NAME, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("drop table " + CITY_TABLE_NAME);
        sqLiteDatabase.execSQL("drop table " + WEATHER_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public static class CityCursor extends CursorWrapper {

        public CityCursor(Cursor cursor) {
            super(cursor);
        }

        public static City getCity(Cursor cursor) {
            return new City(cursor.getInt(cursor.getColumnIndex(CITY_ID)), cursor.getString(cursor.getColumnIndex(CITY_NAME)), cursor.getLong(cursor.getColumnIndex(CITY_UPDATE_DATE)));
        }

    }

    public static class WeatherDataCursor extends CursorWrapper {
        private Cursor cursor;

        public WeatherDataCursor(Cursor cursor) {
            super(cursor);
            this.cursor = cursor;
        }

        public static WeatherData getWeatherData(Cursor cursor) {
            return new WeatherData(cursor.getInt(cursor.getColumnIndex(WEATHER_TEMPERATURE_MIN)), cursor.getInt(cursor.getColumnIndex(WEATHER_TEMPERATURE_MAX)), cursor.getInt(cursor.getColumnIndex(WEATHER_TEMPERATURE)), cursor.getInt(cursor.getColumnIndex(WEATHER_WIND_SPEED)), cursor.getInt(cursor.getColumnIndex(WEATHER_HUMIDITY)), cursor.getInt(cursor.getColumnIndex(WEATHER_PRESSURE)), cursor.getLong(cursor.getColumnIndex(WEATHER_DATE)), cursor.getInt(cursor.getColumnIndex(WEATHER_CITY_ID)), cursor.getString(cursor.getColumnIndex(WEATHER_WEATHER_MAIN)));
        }

        public WeatherData getWeatherData() {
            return getWeatherData(cursor);
        }
    }
}
