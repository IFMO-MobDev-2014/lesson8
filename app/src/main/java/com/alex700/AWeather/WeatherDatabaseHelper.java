package com.alex700.AWeather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by Алексей on 30.11.2014.
 */
public class WeatherDatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    public static final String DB_NAME = "weather.db";
    public static final int DB_VERSION = 5;

    public static final String CITY_TABLE_NAME = "cities";
    public static final String CITY_ID = _ID;
    public static final String CITY_NAME = "name";
    public static final String CITY_UPDATE_TIME = "update_time";
    public static final String CREATE_CITY_TABLE = "create table "
            + CITY_TABLE_NAME + " ("
            + CITY_ID + " integer primary key autoincrement, "
            + CITY_NAME + " text, "
            + CITY_UPDATE_TIME + " integer)";

    public static final String WEATHER_TABLE_NAME = "weather";
    public static final String WEATHER_ID = _ID;
    public static final String WEATHER_T_MIN = "t_min";
    public static final String WEATHER_T_MAX = "t_max";
    public static final String WEATHER_T = "t";
    public static final String WEATHER_WIND_SPEED = "wind_speed";
    public static final String WEATHER_HUMIDITY = "humidity";
    public static final String WEATHER_PRESSURE = "pressure";
    public static final String WEATHER_DATE = "date";
    public static final String WEATHER_CITY_ID = "city_id";
    public static final String WEATHER_MAIN = "main";
    public static final String CREATE_WEATHER_TABLE = "create table "
            + WEATHER_TABLE_NAME + " ("
            + WEATHER_ID + " integer primary key autoincrement, "
            + WEATHER_T_MIN + " integer, "
            + WEATHER_T_MAX + " integer, "
            + WEATHER_T + " integer, "
            + WEATHER_WIND_SPEED + " integer, "
            + WEATHER_HUMIDITY + " integer, "
            + WEATHER_PRESSURE + " integer, "
            + WEATHER_DATE + " integer, "
            + WEATHER_CITY_ID + " integer, "
            + WEATHER_MAIN + " text)";


    public WeatherDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d("base", "start");
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CITY_TABLE);
        sqLiteDatabase.execSQL(CREATE_WEATHER_TABLE);

        ContentValues cv = new ContentValues();
        cv.put(CITY_NAME, "Saint Petersburg");
        sqLiteDatabase.insert(CITY_TABLE_NAME, null, cv);
        Log.d("base", "spb");

        cv = new ContentValues();
        cv.put(CITY_NAME, "Moscow");
        sqLiteDatabase.insert(CITY_TABLE_NAME, null, cv);
        Log.d("base", "msc");

        cv = new ContentValues();
        cv.put(CITY_NAME, "Kirov");
        sqLiteDatabase.insert(CITY_TABLE_NAME, null, cv);
        Log.d("base", "kir");

        cv = new ContentValues();
        cv.put(CITY_NAME, "Antananarivo");
        sqLiteDatabase.insert(CITY_TABLE_NAME, null, cv);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("drop table " + CITY_TABLE_NAME);
        sqLiteDatabase.execSQL("drop table " + WEATHER_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public static class CityCursor extends CursorWrapper {
        private Cursor cursor;

        public CityCursor(Cursor cursor) {
            super(cursor);
            this.cursor = cursor;
        }

        public static City getCity(Cursor cursor) {
            Log.d("name", "/" + cursor.getColumnName(0).equals(CITY_ID) + "/");
            int id = cursor.getInt(cursor.getColumnIndex(CITY_ID));
            String name = cursor.getString(cursor.getColumnIndex(CITY_NAME));
            long update = cursor.getLong(cursor.getColumnIndex(CITY_UPDATE_TIME));

            return new City(id, name, update);
        }

        public City getCity() {
            return getCity(cursor);
        }
    }

    public static class WeatherDataCursor extends CursorWrapper {
        private Cursor cursor;

        public WeatherDataCursor(Cursor cursor) {
            super(cursor);
            this.cursor = cursor;
        }

        public static WeatherData getWeatherData(Cursor cursor) {
            return new WeatherData(cursor.getInt(cursor.getColumnIndex(WEATHER_T_MIN)),
                    cursor.getInt(cursor.getColumnIndex(WEATHER_T_MAX)),
                    cursor.getInt(cursor.getColumnIndex(WEATHER_T)),
                    cursor.getInt(cursor.getColumnIndex(WEATHER_WIND_SPEED)),
                    cursor.getInt(cursor.getColumnIndex(WEATHER_HUMIDITY)),
                    cursor.getInt(cursor.getColumnIndex(WEATHER_PRESSURE)),
                    cursor.getLong(cursor.getColumnIndex(WEATHER_DATE)),
                    cursor.getInt(cursor.getColumnIndex(WEATHER_CITY_ID)),
                    cursor.getString(cursor.getColumnIndex(WEATHER_MAIN)));
        }

        public WeatherData getWeatherData() {
            return getWeatherData(cursor);
        }
    }
}
