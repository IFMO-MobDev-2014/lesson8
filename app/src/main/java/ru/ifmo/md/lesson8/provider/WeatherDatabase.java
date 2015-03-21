package ru.ifmo.md.lesson8.provider;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static ru.ifmo.md.lesson8.provider.WeatherContract.CityColumns;

/**
 * @author Aydar Gizatullin a.k.a. lightning95
 */

public class WeatherDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";

    private static final int VERSION = 1;

    interface Tables {
        String CITIES = "cities";
    }

    public WeatherDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CITIES + " ("
                        + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + CityColumns.CITY_ID + " INTEGER NOT NULL,"
                        + CityColumns.CITY_NAME + " TEXT NOT NULL,"
                        + CityColumns.CITY_LAST_UPDATE + " TEXT NOT NULL,"
                        + CityColumns.CITY_TEMPERATURE + " REAL NOT NULL,"
                        + CityColumns.CITY_HUMIDITY + " INTEGER NOT NULL,"
                        + CityColumns.CITY_WIND_SPEED + " INTEGER NOT NULL,"
                        + CityColumns.WEATHER_FORECAST + " TEXT,"
                        + CityColumns.WEATHER_DESCRIPTION + " TEXT NOT NULL,"
                        + CityColumns.WEATHER_ICON_ID + " TEXT NOT NULL"
                        + ");"
        );

        db.execSQL(createEntry(
                551487, "Kazan", "2014-12-01T15:00:00", 3.15, "light snow", "13n"));
        db.execSQL(createEntry(
                2643743, "London", "2014-12-01T15:41:14", 3.15, "haze", "50d"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
    }

    private static String createEntry(int cityId, String cityName, String lastUpdate, double temperature,
                                      String description, String iconId) {
        return "INSERT INTO " + Tables.CITIES + "(" +
                CityColumns.CITY_ID + ", " +
                CityColumns.CITY_NAME + ", " +
                CityColumns.CITY_LAST_UPDATE + ", " +
                CityColumns.CITY_TEMPERATURE + ", " +
                CityColumns.CITY_HUMIDITY + ", " +
                CityColumns.CITY_WIND_SPEED + "," +
                CityColumns.WEATHER_DESCRIPTION + ", " +
                CityColumns.WEATHER_ICON_ID +
                ") VALUES (" +
                cityId + ", " +
                DatabaseUtils.sqlEscapeString(cityName) + ", " +
                DatabaseUtils.sqlEscapeString(lastUpdate) + ", " +
                Double.toString(temperature) + ", " +
                "70, " +
                "1," +
                DatabaseUtils.sqlEscapeString(description) + ", " +
                DatabaseUtils.sqlEscapeString(iconId) +
                ");";
    }
}
