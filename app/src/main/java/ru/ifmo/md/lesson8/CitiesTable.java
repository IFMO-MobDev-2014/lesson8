package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by timur on 08.01.15.
 */
public class CitiesTable {
    public static final String CITIES_TABLE = "cities";
    private static final String DESTROY_TABLE = "DROP TABLE " + CITIES_TABLE + ";";
    public static final String ID = "_id";
    public static final String TEMPERATURE = "temp";
    public static final String CLOUDS = "clouds";
    public static final String WIND = "wind";
    public static final String URL = "url";
    public static final String DESCRIPTION = "description";
    public static final String HUMIDITY = "hum";
    public static final String WIND_DIR = "wind_dir";
    public static final String PRESSURE = "pres";
    public static final String[] FULL_CITY_PROJECTION = {DESCRIPTION,
            TEMPERATURE, PRESSURE, HUMIDITY, WIND,
            WIND_DIR, CLOUDS, ID};
    public static final String NAME = "name";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + CITIES_TABLE +
            "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NAME + " TEXT NOT NULL, " +
            TEMPERATURE + " INTEGER, " +
            PRESSURE + " INTEGER, " +
            WIND + " INTEGER, " +
            WIND_DIR + " INTEGER, " +
            CLOUDS + " INTEGER, " +
            HUMIDITY + " INTEGER, " +
            DESCRIPTION + " INTEGER, " +
            URL + " INTEGER NOT NULL UNIQUE " +
            ");";

    public CitiesTable(SQLiteDatabase sqLiteDatabase) {
        onCreate(sqLiteDatabase);
    }

    public static ContentValues makeContentValues(JSONObject wind, JSONObject weather,
                                                  int clouds, int humidity,
                                                  int pressure, double temperature) {
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put(WIND, (int) (wind.getDouble("speed") * 10.0d));
            contentValues.put(WIND_DIR, wind.getInt("deg"));
            contentValues.put(DESCRIPTION, weather.getInt("id"));
            contentValues.put(CLOUDS, clouds);
            contentValues.put(PRESSURE, pressure);
            contentValues.put(TEMPERATURE, (int) (temperature * 10.0d));
            contentValues.put(HUMIDITY, humidity);
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
