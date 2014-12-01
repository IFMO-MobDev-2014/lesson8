package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Евгения on 29.11.2014.
 */
public class DBAdapter {
    public static final String KEY_ID = "_id";

    //Weather
    public static final String TABLE_NAME_WEATHER = "weather";
    public static final String KEY_WEATHER_CITY = "city";
    public static final String KEY_WEATHER_WIND_DIRECTION = "wind_direction";
    public static final String KEY_WEATHER_WIND_SPEED = "wind_speed";
    public static final String KEY_WEATHER_ATMOSPHERE_HUMIDITY = "humidity";
    public static final String KEY_WEATHER_ATMOSPHERE_PRESSURE = "atmosphere_pressure";
    public static final String KEY_WEATHER_CODE = "code";
    public static final String KEY_WEATHER_DATE = "date";
    public static final String KEY_WEATHER_TEMPERATURE = "temperature";
    public static final String KEY_WEATHER_TEXT = "text_description";
    public static final String CREATE_TABLE_WEATHER = "CREATE TABLE " + TABLE_NAME_WEATHER + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_WEATHER_CITY + " TEXT, "
            + KEY_WEATHER_WIND_DIRECTION + " INTEGER NOT NULL, "
            + KEY_WEATHER_WIND_SPEED + " REAL NOT NULL, "
            + KEY_WEATHER_ATMOSPHERE_HUMIDITY + " INTEGER NOT NULL, "
            + KEY_WEATHER_ATMOSPHERE_PRESSURE + " REAL NOT NULL, "
            + KEY_WEATHER_CODE + " INTEGER NOT NULL, "
            + KEY_WEATHER_DATE + " TEXT, "
            + KEY_WEATHER_TEMPERATURE + " INTEGER NOT NULL, "
            + KEY_WEATHER_TEXT + " TEXT, "
            + "UNIQUE (" + KEY_WEATHER_CITY + ") ON CONFLICT IGNORE"
            + ")";

    //Forecasts
    public static final String TABLE_NAME_FORECASTS = "forecasts";
    public static final String KEY_FORECASTS_WEATHER_ID = "weather_id";
    public static final String KEY_FORECASTS_CODE = "code";
    public static final String KEY_FORECASTS_DATE = "date";
    public static final String KEY_FORECASTS_DAY = "day";
    public static final String KEY_FORECASTS_HIGH = "high";
    public static final String KEY_FORECASTS_LOW = "low";
    public static final String KEY_FORECASTS_TEXT = "text";
    public static final String CREATE_TABLE_FORECASTS = "CREATE TABLE " + TABLE_NAME_FORECASTS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_FORECASTS_WEATHER_ID + " INTEGER NOT NULL, "
            + KEY_FORECASTS_CODE + " INTEGER NOT NULL, "
            + KEY_FORECASTS_DATE + " TEXT, "
            + KEY_FORECASTS_DAY + " TEXT, "
            + KEY_FORECASTS_HIGH + " INTEGER NOT NULL, "
            + KEY_FORECASTS_LOW + " INTEGER NOT NULL, "
            + KEY_FORECASTS_TEXT + " TEXT, "
            + "FOREIGN KEY (" + KEY_FORECASTS_WEATHER_ID + ") REFERENCES " + TABLE_NAME_WEATHER + " ("
            + KEY_ID + ") ON DELETE CASCADE"
            + ")";

    private static DBAdapter mInstance = null;
    private Context context;
    private SQLiteDatabase db;

    private DBAdapter(Context context) {
        this.context = context;
    }

    public static DBAdapter getOpenedInstance(Context context) {
        if (mInstance == null)
            mInstance = new DBAdapter(context.getApplicationContext()).open();
        return mInstance;
    }

    private DBAdapter open() {
        DBHelper mDbHelper = new DBHelper(context);
        db = mDbHelper.getWritableDatabase();
        return this;
    }

    public static final String DB_NAME = "database";
    public static final Integer VERSION = 1;

    private static class DBHelper extends SQLiteOpenHelper {
        Context context;

        public DBHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
            this.context = context;
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=ON");
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE_WEATHER);
            ContentValues cv = new ContentValues();
            cv.put(KEY_WEATHER_ATMOSPHERE_HUMIDITY, 0);
            cv.put(KEY_WEATHER_ATMOSPHERE_PRESSURE, 0);
            cv.put(KEY_WEATHER_CODE, 0);
            cv.put(KEY_WEATHER_TEMPERATURE, 0);
            cv.put(KEY_WEATHER_DATE, "");
            cv.put(KEY_WEATHER_WIND_DIRECTION, 0);
            cv.put(KEY_WEATHER_WIND_SPEED, 0);

            for (String cityName : context.getResources().getStringArray(R.array.cities_list)) {
                if (cv.containsKey(KEY_WEATHER_CITY))
                    cv.remove(KEY_WEATHER_CITY);
                cv.put(KEY_WEATHER_CITY, cityName);
                sqLiteDatabase.insert(TABLE_NAME_WEATHER, null, cv);
            }

            sqLiteDatabase.execSQL(CREATE_TABLE_FORECASTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        }
    }

    public String getCityByWeatherId(long weatherId) {
        Cursor c = db.query(TABLE_NAME_WEATHER, new String[]{KEY_WEATHER_CITY}, KEY_ID + "=" + weatherId,
                null, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        c.moveToFirst();
        String result = c.getString(c.getColumnIndex(KEY_WEATHER_CITY));
        c.close();
        return result;
    }

    public long createWeather(ContentValues channel) {
        return db.insert(TABLE_NAME_WEATHER, null, channel);
    }

    public long createWeather(Channel channel) {
        ContentValues values = getContentValuesByChannel(channel);
        return createWeather(values);
    }

    public ContentValues getContentValuesByChannel(Channel channel) {
        ContentValues values = new ContentValues();
        values.put(KEY_WEATHER_CITY, channel.location.city);
        values.put(KEY_WEATHER_WIND_DIRECTION, channel.wind.direction);
        values.put(KEY_WEATHER_WIND_SPEED, channel.wind.speed);
        values.put(KEY_WEATHER_ATMOSPHERE_HUMIDITY, channel.atmosphere.humidity);
        values.put(KEY_WEATHER_ATMOSPHERE_PRESSURE, channel.atmosphere.pressure);
        values.put(KEY_WEATHER_CODE, channel.item.condition.code);
        values.put(KEY_WEATHER_DATE, channel.item.condition.date);
        values.put(KEY_WEATHER_TEMPERATURE, channel.item.condition.temp);
        values.put(KEY_WEATHER_TEXT, channel.item.condition.text);
        return values;
    }

    public long createForecast(ContentValues forecast) {
        return db.insert(TABLE_NAME_FORECASTS, null, forecast);
    }

    public long createForecast(ForecastItem forecastItem, long weatherId) {
        ContentValues values = getContentValuesByForecastItem(forecastItem, weatherId);
        return createWeather(values);
    }

    public ContentValues getContentValuesByForecastItem(ForecastItem forecastItem, long weatherId) {
        ContentValues values = new ContentValues();
        values.put(KEY_FORECASTS_WEATHER_ID, weatherId);
        values.put(KEY_FORECASTS_CODE, forecastItem.code);
        values.put(KEY_FORECASTS_DATE, forecastItem.date);
        values.put(KEY_FORECASTS_DAY, forecastItem.day);
        values.put(KEY_FORECASTS_HIGH, forecastItem.high);
        values.put(KEY_FORECASTS_LOW, forecastItem.low);
        values.put(KEY_FORECASTS_TEXT, forecastItem.text);
        return values;
    }

    public boolean changeWeather(ContentValues channel, long channelId) {
        return db.update(TABLE_NAME_WEATHER, channel, KEY_ID + "=" + channelId, null) == 1;
    }

    public boolean changeWeather(Channel channel, long weatherId) {
        ContentValues values = getContentValuesByChannel(channel);
        return changeWeather(values, weatherId);
    }

    public Cursor getForecastsByWeatherId(long weatherId) {
        return db.query(TABLE_NAME_FORECASTS, new String[]{
                        KEY_ID, KEY_FORECASTS_CODE, KEY_FORECASTS_DATE, KEY_FORECASTS_DAY,
                        KEY_FORECASTS_HIGH, KEY_FORECASTS_LOW, KEY_FORECASTS_TEXT},
                KEY_FORECASTS_WEATHER_ID + "=" + weatherId, null, null, null, KEY_ID + " ASC");
    }

    public Cursor getAllWeather() {
        return db.query(TABLE_NAME_WEATHER, new String[]{
                KEY_ID, KEY_WEATHER_CITY, KEY_WEATHER_WIND_DIRECTION, KEY_WEATHER_WIND_SPEED, KEY_WEATHER_ATMOSPHERE_HUMIDITY,
                KEY_WEATHER_ATMOSPHERE_PRESSURE, KEY_WEATHER_CODE, KEY_WEATHER_DATE, KEY_WEATHER_TEMPERATURE, KEY_WEATHER_TEXT
        }, null, null, null, null, null);
    }

    public boolean deleteWeather(long weatherId) {
        return db.delete(TABLE_NAME_WEATHER, KEY_ID + "=" + weatherId, null) == 1;
    }

    public boolean deleteForecasts(long weatherId){
        return db.delete(TABLE_NAME_FORECASTS, KEY_FORECASTS_WEATHER_ID + "=" + weatherId, null)>0;
    }
}

