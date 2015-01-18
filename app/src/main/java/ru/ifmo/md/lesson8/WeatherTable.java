package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by timur on 08.01.15.
 */
public class WeatherTable {
    public static final String WEATHER_TABLE = "weather";
    private static final String DESTROY_TABLE = "DROP TABLE " + WEATHER_TABLE + ";";
    public static final String PRESSURE = "pres";
    public static final String HUMIDITY = "hum";
    public static final String CITY_ID = "city_id";
    public static final String TIME = "time";
    public static final String TEMPERATURE_MIN = "temp_min";
    public static final String TEMPERATURE_MAX = "temp_max";
    public static final String CLOUDS = "clouds";
    public static final String ID = "_id";
    public static final String DESCRIPTION = "description";
    public static final String WIND = "wind";
    public static final String WIND_DIR = "wind_dir";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + WEATHER_TABLE +
            "(" +
            DESCRIPTION + " INTEGER NOT NULL, \n" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TEMPERATURE_MIN + " INTEGER NOT NULL, " +
            TEMPERATURE_MAX + " INTEGER NOT NULL, " +
            PRESSURE + " INTEGER NOT NULL, " +
            WIND + " INTEGER NOT NULL, " +
            WIND_DIR + " INTEGER NOT NULL, " +
            CLOUDS + " INTEGER NOT NULL, " +
            HUMIDITY + " INTEGER NOT NULL, " +
            TIME + " INTEGER NOT NULL, \n" +
            CITY_ID + " INTEGER REFERENCES \n" +
            CitiesTable.CITIES_TABLE + "(" + CitiesTable.URL + ") ON DELETE CASCADE\n" +
            ");";
    public static final String[] FULL_WEATHER_PROJECTION = {DESCRIPTION,
            TEMPERATURE_MIN, TEMPERATURE_MAX, PRESSURE,
            HUMIDITY, WIND, WIND_DIR, CLOUDS, TIME,
            CITY_ID, ID};

    public WeatherTable(SQLiteDatabase sqLiteDatabase) {
        onCreate(sqLiteDatabase);
    }

    public static ContentValues makeContentValues(JSONObject weather, JSONObject description,
                                                  int clouds, int hum, int pressure,
                                                  double tempMin, double tempMax) {
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put(CLOUDS, clouds);
            contentValues.put(PRESSURE, pressure);
            contentValues.put(WIND_DIR, weather.getInt("deg"));
            contentValues.put(DESCRIPTION, description.getInt("id"));
            contentValues.put(HUMIDITY, hum);
            contentValues.put(TIME, weather.getInt("dt"));
            contentValues.put(TEMPERATURE_MIN, (int) (tempMin * 10.0d));
            contentValues.put(TEMPERATURE_MAX, (int) (tempMax * 10.0d));
            contentValues.put(WIND, (int) (weather.getDouble("speed") * 10.0d));
        } catch (JSONException e) {
        }
        return contentValues;
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL(DESTROY_TABLE);
            sqLiteDatabase.execSQL(CREATE_TABLE);
        }
    }
}
