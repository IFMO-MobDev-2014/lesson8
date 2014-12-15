package ru.ifmo.md.weather.db.model;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by Kirill on 15.12.2014.
 */
public class SettingsTable implements BaseColumns {
    public static final String TABLE_NAME = "SettingsTable";

    public static final String LAST_WEATHER_UPDATE_COLUMN = "lastWeatherUpdate";
    public static final String LAST_FORECAST_UPDATE_COLUMN  = "lastForecastUpdate";
    public static final String UPDATE_INTERVAL_COLUMN = "updateInterval";

    private static String DB_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    BaseColumns._ID + " integer PRIMARY KEY autoincrement, "
                    + LAST_WEATHER_UPDATE_COLUMN + " text, "
                    + LAST_FORECAST_UPDATE_COLUMN + " text, "
                    + UPDATE_INTERVAL_COLUMN + " text ); ";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

