package ru.ifmo.md.weather.db.model;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by Kirill on 01.12.2014.
 */
public class CityTable implements BaseColumns {
    public static final String TABLE_NAME = "CityTable";

    public static final String NAME_COLUMN = "name";
    public static final String ID_COLUMN = "id";
    public static final String COUNTRY_COLUMN = "country";
    public static final String LON_COLUMN = "lon";
    public static final String LAT_COLUMN = "lat";

    public static final String WEATHER_TEMP_COLUMN = "temp";
    public static final String WEATHER_HUMIDITY_COLUMN = "humidity";
    public static final String WEATHER_TEMP_MIN_COLUMN = "tempMin";
    public static final String WEATHER_TEMP_MAX_COLUMN = "tempMax";
    public static final String WEATHER_PRESSURE_COLUMN = "pressure";
    public static final String WEATHER_WIND_SPEED_COLUMN = "windSpeed";
    public static final String WEATHER_ICON_NAME_COLUMN = "iconName";
    public static final String WEATHER_DESCRIPTION_COLUMN = "description";

    private static String DB_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    BaseColumns._ID + " integer PRIMARY KEY autoincrement, " +
                    NAME_COLUMN + " text not null, " +
                    ID_COLUMN + " text not null, " +
                    COUNTRY_COLUMN + " text not null, " +
                    LON_COLUMN + " REAL, " +
                    LAT_COLUMN + " REAL " +

                    WEATHER_TEMP_COLUMN + " REAL, " +
                    WEATHER_HUMIDITY_COLUMN + " REAL, " +
                    WEATHER_TEMP_MIN_COLUMN + " REAL, " +
                    WEATHER_TEMP_MAX_COLUMN + " REAL " +
                    WEATHER_PRESSURE_COLUMN + " REAL " +
                    WEATHER_WIND_SPEED_COLUMN + " REAL " +
                    WEATHER_ICON_NAME_COLUMN + " TEXT not NULL " +
                    WEATHER_DESCRIPTION_COLUMN + " TEXT not NULL " + "); ";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
