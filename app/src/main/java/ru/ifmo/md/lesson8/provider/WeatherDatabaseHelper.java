package ru.ifmo.md.lesson8.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.ifmo.md.lesson8.City;
import ru.ifmo.md.lesson8.ShortWeatherData;

/**
 * Created by pva701 on 17.10.14.
 */
public class WeatherDatabaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String DB_NAME = "weather";

    public static final String TABLE_CITY = "city";
    public static final String CITY_ID = "_id";
    public static final String CITY_NAME = "name";
    public static final String CITY_IS_SELECTED = "is_selected";
    public static final String CITY_LAST_UPDATE = "last_update";

    public static final String TABLE_FORECAST = "forecast";
    public static final String FORECAST_ID = "_id";
    public static final String FORECAST_TEMP_MIN = "temp_min";
    public static final String FORECAST_TEMP_MAX = "temp_max";
    public static final String FORECAST_TEMP = "temp";
    public static final String FORECAST_WIND_SPEED = "wind_speed";
    public static final String FORECAST_HUMIDITY = "humidity";
    public static final String FORECAST_WEATHER_MAIN = "weather_main";
    public static final String FORECAST_WEATHER_DESCRIPTION = "weather_description";
    public static final String FORECAST_ICON = "icon";
    public static final String FORECAST_DATE = "date";
    public static final String FORECAST_CONDITION_CODE = "condition_code";
    public static final String FORECAST_CITY_ID = "city_id";

    public static class WeatherCursor extends CursorWrapper {
        private Cursor cursor;
        public WeatherCursor(Cursor cur) {
            super(cur);
            cursor = cur;
        }

        public ShortWeatherData getWeather() {
            return getWeather(cursor);

        }

        public static ShortWeatherData getWeather(Cursor cur) {
            return new ShortWeatherData(
                    cur.getInt(cur.getColumnIndex(FORECAST_TEMP_MIN)),
                    cur.getInt(cur.getColumnIndex(FORECAST_TEMP_MAX)),
                    cur.getInt(cur.getColumnIndex(FORECAST_TEMP)),
                    cur.getInt(cur.getColumnIndex(FORECAST_WIND_SPEED)),
                    cur.getInt(cur.getColumnIndex(FORECAST_HUMIDITY)),
                    cur.getString(cur.getColumnIndex(FORECAST_WEATHER_MAIN)),
                    cur.getString(cur.getColumnIndex(FORECAST_WEATHER_DESCRIPTION)),
                    cur.getString(cur.getColumnIndex(FORECAST_ICON)),
                    cur.getInt(cur.getColumnIndex(FORECAST_DATE)),
                    cur.getInt(cur.getColumnIndex(FORECAST_CONDITION_CODE)),
                    cur.getInt(cur.getColumnIndex(FORECAST_CITY_ID)));
        }
    }


    public static class CityCursor extends CursorWrapper {
        private Cursor cursor;
        public CityCursor(Cursor cur) {
            super(cur);
            cursor = cur;
        }

        public City getCity() {
            return getCity(cursor);

        }


        public static City getCity(Cursor cur) {
            return new City(
                    cur.getInt(cur.getColumnIndex(CITY_ID)),
                    cur.getString(cur.getColumnIndex(CITY_NAME)),
                    cur.getInt(cur.getColumnIndex(CITY_IS_SELECTED)),
                    cur.getInt(cur.getColumnIndex(CITY_LAST_UPDATE)));

        }
    }

    public WeatherDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table city (_id integer primary key autoincrement, " +
                "name varchar(64), " +
                "is_selected integer, " +
                "last_update integer)");

        db.execSQL("create table forecast (" + FORECAST_ID + " integer primary key autoincrement, "
                    + FORECAST_TEMP_MIN + " integer, " + FORECAST_TEMP_MAX + " integer, " +
                        FORECAST_TEMP + " integer, " + FORECAST_WIND_SPEED + " integer, " +
                        FORECAST_HUMIDITY + " integer, " +
                        FORECAST_WEATHER_MAIN + " varchar(30), " + FORECAST_WEATHER_DESCRIPTION + " varchar(100), " +
                        FORECAST_ICON + " varchar(5), " + FORECAST_DATE + " integer, " + FORECAST_CONDITION_CODE + " integer, " +
                        FORECAST_CITY_ID + " integer)");

        ContentValues cv = new ContentValues();
        cv.put(CITY_NAME, "Saint Petersburg");
        cv.put(CITY_IS_SELECTED, 0);
        cv.put(CITY_LAST_UPDATE, 0);
        db.insert(TABLE_CITY, null, cv);

        ContentValues cv1 = new ContentValues();
        cv1.put(CITY_NAME, "Stavropol");
        cv1.put(CITY_IS_SELECTED, 0);
        cv1.put(CITY_LAST_UPDATE, 0);
        db.insert(TABLE_CITY, null, cv1);

        ContentValues cv2 = new ContentValues();
        cv2.put(CITY_NAME, "Moscow");
        cv2.put(CITY_IS_SELECTED, 1);
        cv2.put(CITY_LAST_UPDATE, 0);
        db.insert(TABLE_CITY, null, cv2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //none
    }
}