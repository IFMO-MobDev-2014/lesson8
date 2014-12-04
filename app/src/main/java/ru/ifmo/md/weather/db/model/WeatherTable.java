package ru.ifmo.md.weather.db.model;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by Kirill on 01.12.2014.
 */
public class WeatherTable implements BaseColumns{
    public static final String TABLE_NAME = "WeatherTable";

    public static final String TEMP_COLUMN = "temp";
    public static final String HUMIDITY_COLUMN = "humidity";
    public static final String TEMP_MIN_COLUMN = "tempMin";
    public static final String TEMP_MAX_COLUMN = "tempMax";
    public static final String PRESSURE_COLUMN = "pressure";
    public static final String WIND_SPEED_COLUMN = "windSpeed";
    public static final String ICON_NAME_COLUMN = "iconName";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String CITY_NAME_COLUMN = "cityName";
    public static final String CITY_ID_COLUMN = "cityId";

    private static String DB_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    BaseColumns._ID + " integer PRIMARY KEY autoincrement, " +
                    TEMP_COLUMN + " REAL, " +
                    HUMIDITY_COLUMN + " REAL, " +
                    TEMP_MIN_COLUMN + " REAL, " +
                    TEMP_MAX_COLUMN + " REAL " +
                    PRESSURE_COLUMN + " REAL " +
                    WIND_SPEED_COLUMN + " REAL " +
                    CITY_NAME_COLUMN + " TEXT not NULL " +
                    ICON_NAME_COLUMN + " TEXT not NULL " +
                    DESCRIPTION_COLUMN + " TEXT not NULL " +
                    CITY_ID_COLUMN + " INTEGER REFERENCES CityTable(_ID) ON DELETE CASCADE); ";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
