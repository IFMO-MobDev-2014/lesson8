package ru.ifmo.ctddev.filippov.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Dima_2 on 01.04.2015.
 */
public class WeatherDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 13;

    public static final String CITIES_TABLE = "cities";
    public static final String WEATHER_TABLE = "weather";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CITY_ID = "city_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_TEMPERATURE = "temperature";
    public static final String COLUMN_TEMPERATURE_MIN = "temperature_min";
    public static final String COLUMN_TEMPERATURE_MAX = "temperature_max";
    public static final String COLUMN_CLOUDS = "clouds";
    public static final String COLUMN_WIND = "wind";
    public static final String COLUMN_WIND_DIRECTION = "wind_direction";
    public static final String COLUMN_PRESSURE = "pressure";
    public static final String COLUMN_WET = "wet";

    public static final String[] FULL_WEATHER_DESCRIPTION = {
            COLUMN_DESCRIPTION,
            COLUMN_TEMPERATURE_MIN,
            COLUMN_TEMPERATURE_MAX,
            COLUMN_PRESSURE,
            COLUMN_WET,
            COLUMN_WIND,
            COLUMN_WIND_DIRECTION,
            COLUMN_CLOUDS,
            COLUMN_TIME,
            COLUMN_CITY_ID,
            COLUMN_ID
    };
    public static final String[] FULL_CITY_DESCRIPTION = {
            COLUMN_DESCRIPTION,
            COLUMN_TEMPERATURE,
            COLUMN_PRESSURE,
            COLUMN_WET,
            COLUMN_WIND,
            COLUMN_WIND_DIRECTION,
            COLUMN_CLOUDS,
            COLUMN_ID
    };
    
    public WeatherDatabase(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + CITIES_TABLE + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_TEMPERATURE + " INTEGER, " +
                COLUMN_PRESSURE + " INTEGER, " +
                COLUMN_WIND + " INTEGER, " +
                COLUMN_WIND_DIRECTION + " INTEGER, " +
                COLUMN_CLOUDS + " INTEGER, " +
                COLUMN_WET + " INTEGER, " +
                COLUMN_DESCRIPTION + " INTEGER, " +
                COLUMN_URL + " INTEGER NOT NULL UNIQUE " +
                ");"
        );
        database.execSQL("CREATE TABLE IF NOT EXISTS " + WEATHER_TABLE + "(" +
                COLUMN_DESCRIPTION + " INTEGER NOT NULL, \n" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TEMPERATURE_MIN + " INTEGER NOT NULL, " +
                COLUMN_TEMPERATURE_MAX + " INTEGER NOT NULL, " +
                COLUMN_PRESSURE + " INTEGER NOT NULL, " +
                COLUMN_WIND + " INTEGER NOT NULL, " +
                COLUMN_WIND_DIRECTION + " INTEGER NOT NULL, " +
                COLUMN_CLOUDS + " INTEGER NOT NULL, " +
                COLUMN_WET + " INTEGER NOT NULL, " +
                COLUMN_TIME + " INTEGER NOT NULL, \n" +
                COLUMN_CITY_ID + " INTEGER REFERENCES \n" +
                CITIES_TABLE + "(" + COLUMN_URL + ") ON DELETE CASCADE\n" +
                ");"
        );

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, "Saint-Petersburg");
        contentValues.put(COLUMN_URL, 498817);
        database.insert(CITIES_TABLE, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            database.execSQL("DROP TABLE " + CITIES_TABLE + ";");
            database.execSQL("DROP TABLE " + WEATHER_TABLE + ";");
            onCreate(database);
        }
    }
}
