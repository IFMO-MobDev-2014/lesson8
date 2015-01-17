package ru.ifmo.md.lesson8.database;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by sergey on 15.11.14.
 */
public class WeatherTable {

    public static final String TABLE_NAME = "WEATHER_TABLE";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WOEID = "woeid";
    public static final String COLUMN_LASTUPD = "last_upd";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_CITY = "city";

    public static final String COLUMN_CONDITION_DESCRIPTION = "condition_description";
    public static final String COLUMN_CONDITION_TEMP = "condition_temp";
    public static final String COLUMN_CONDITION_CODE = "condition_code";
    public static final String COLUMN_CONDITION_DATE = "condition_date";

    public static final String COLUMN_ATMOSPHERE_PRESSURE = "atmosphere_pressure";
    public static final String COLUMN_ATMOSPHERE_HUMIDITY = "atmosphere_humidity";

    public static final String COLUMN_WIND_DIRECTION = "wind_direction";
    public static final String COLUMN_WIND_SPEED = "wind_speed";

    public static final String COLUMN_FORECAST = "forecast";

    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_WOEID + " INTEGER NOT NULL, "
            + COLUMN_LASTUPD  + " TEXT, "
            + COLUMN_COUNTRY + " TEXT, "
            + COLUMN_CITY + " TEXT NOT NULL, "

            + COLUMN_CONDITION_DESCRIPTION + " TEXT NOT NULL, "
            + COLUMN_CONDITION_TEMP + " INTEGER NOT NULL, "
            + COLUMN_CONDITION_CODE + " INTEGER, "
            + COLUMN_CONDITION_DATE + " TEXT, "

            + COLUMN_ATMOSPHERE_PRESSURE + " REAL, "
            + COLUMN_ATMOSPHERE_HUMIDITY + " INTEGER, "

            + COLUMN_WIND_DIRECTION + " INTEGER, "
            + COLUMN_WIND_SPEED + " INTEGER, "

            + COLUMN_FORECAST + " TEXT"
            + ");";

    private static String addDefaultCity(int woeid, String lastUpd, String city, String description, int temp) {
        return "INSERT INTO " + TABLE_NAME + "(" +
                COLUMN_WOEID + ", " +
                COLUMN_LASTUPD + ", " +
                COLUMN_CITY + ", " +
                COLUMN_CONDITION_DESCRIPTION + ", " +
                COLUMN_CONDITION_TEMP +
                ") VALUES (" +
                woeid + ", " +
                DatabaseUtils.sqlEscapeString(lastUpd) + ", " +
                DatabaseUtils.sqlEscapeString(city) + ", " +
                DatabaseUtils.sqlEscapeString(description) + ", " +
                temp +
                ");";
    }

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        database.execSQL(addDefaultCity(2123260, "Tue, 06 Jan 2015 3:30 pm MSK", "Saint-Petersburg", "Rain", -4));
        database.execSQL(addDefaultCity(2122641, "Tue, 06 Jan 2015 3:30 pm MSK", "Omsk", "Drifting Snow", -20));
        database.execSQL(addDefaultCity(2122265, "Tue, 06 Jan 2015 3:30 pm MSK", "Moscow", "Light Snow", -5));
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

}
