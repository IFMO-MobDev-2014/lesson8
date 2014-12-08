package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kna on 10.11.2014.
 */
public class WeatherDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "weather.db";

    private static final int DB_VERSION = 10;
    public WeatherDatabase(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Structure.CITIES_TABLE +
                "(" +
                Structure.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Structure.COLUMN_NAME + " TEXT NOT NULL, " +

                Structure.COLUMN_TEMPERATURE + " INTEGER, " +
                Structure.COLUMN_PRESSURE + " INTEGER, " +
                Structure.COLUMN_WIND + " INTEGER, " +
                Structure.COLUMN_WIND_DIR + " INTEGER, " +
                Structure.COLUMN_CLOUDS + " INTEGER, " +
                Structure.COLUMN_HUMIDITY + " INTEGER, " +

                Structure.COLUMN_DESCRIPTION + " INTEGER, " +
                Structure.COLUMN_URL + " INTEGER NOT NULL UNIQUE " +

                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + Structure.WEATHER_TABLE +
                "(" +
                Structure.COLUMN_DESCRIPTION + " INTEGER NOT NULL, \n" +
                Structure.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                Structure.COLUMN_TEMPERATURE_MIN + " INTEGER NOT NULL, " +
                Structure.COLUMN_TEMPERATURE_MAX + " INTEGER NOT NULL, " +
                Structure.COLUMN_PRESSURE + " INTEGER NOT NULL, " +
                Structure.COLUMN_WIND + " INTEGER NOT NULL, " +
                Structure.COLUMN_WIND_DIR + " INTEGER NOT NULL, " +
                Structure.COLUMN_CLOUDS + " INTEGER NOT NULL, " +
                Structure.COLUMN_HUMIDITY + " INTEGER NOT NULL, " +

                Structure.COLUMN_TIME + " INTEGER NOT NULL, \n" +
                Structure.COLUMN_CITY_ID + " INTEGER REFERENCES \n" +
                Structure.CITIES_TABLE + "(" + Structure.COLUMN_URL + ") ON DELETE CASCADE\n" +
                ");");

        // todo: test data, remove
        ContentValues cv = new ContentValues();
        cv.put(Structure.COLUMN_NAME, "My Location");
        cv.put(Structure.COLUMN_URL, 0);
        db.insert(Structure.CITIES_TABLE, null, cv);
        cv = new ContentValues();
        cv.put(Structure.COLUMN_NAME, "Saint-Petersburg");
        cv.put(Structure.COLUMN_URL, 498817);
        db.insert(Structure.CITIES_TABLE, null, cv);
        cv.put(Structure.COLUMN_NAME, "Moscow");
        cv.put(Structure.COLUMN_URL, 524901);
        db.insert(Structure.CITIES_TABLE, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE " + Structure.CITIES_TABLE + ";");
            db.execSQL("DROP TABLE " + Structure.WEATHER_TABLE + ";");
            onCreate(db);
        }
    }

    public static class Structure {
        public static final String CITIES_TABLE = "cities";
        public static final String WEATHER_TABLE = "weather";

        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_URL = "url";

        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CITY_ID = "city_id";
        public static final String COLUMN_TIME = "time";

        public static final String COLUMN_TEMPERATURE = "temp";
        public static final String COLUMN_TEMPERATURE_MIN = "temp_min";
        public static final String COLUMN_TEMPERATURE_MAX = "temp_max";
        public static final String COLUMN_CLOUDS = "clouds";
        public static final String COLUMN_WIND = "wind";
        public static final String COLUMN_WIND_DIR = "wind_dir";
        public static final String COLUMN_PRESSURE = "pres";
        public static final String COLUMN_HUMIDITY = "hum";

        public static final String[] FULL_WEATHER_PROJECTION = {COLUMN_DESCRIPTION, COLUMN_TEMPERATURE_MIN, COLUMN_TEMPERATURE_MAX, COLUMN_PRESSURE, COLUMN_HUMIDITY, COLUMN_WIND, COLUMN_WIND_DIR, COLUMN_CLOUDS, COLUMN_TIME, COLUMN_CITY_ID, COLUMN_ID };
        public static final String[] FULL_CITY_PROJECTION = {COLUMN_DESCRIPTION, COLUMN_TEMPERATURE, COLUMN_PRESSURE, COLUMN_HUMIDITY, COLUMN_WIND, COLUMN_WIND_DIR, COLUMN_CLOUDS, COLUMN_ID };
    }
}
