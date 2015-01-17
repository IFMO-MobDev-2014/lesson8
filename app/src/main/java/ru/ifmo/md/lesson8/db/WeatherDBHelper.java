package ru.ifmo.md.lesson8.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "weather";

    public static final String TABLE_CITY = "cities";
    public static final String TABLE_NOW = "nows";
    public static final String TABLE_FORECAST = "forecasts";

    public static final String _ID = "_id";
    public static final String COLUMN_NAME_CREATED_AT = "created_at";

    public static final String COLUMN_NAME_CITY_NAME = "city_name";
    public static final String COLUMN_NAME_WOEID = "woeid";
    public static final String COLUMN_NAME_GPS = "gps";

    public static final String COLUMN_NAME_DAY_NAME = "day_name";
    public static final String COLUMN_NAME_CONDITION = "condition";
    public static final String COLUMN_NAME_CONDITION_CODE = "condition_code";
    public static final String COLUMN_NAME_FORECAST = "forecast";

    public static final String COLUMN_NAME_TEMP = "temp";
    public static final String COLUMN_NAME_WIND_SPEED = "wind_speed";
    public static final String COLUMN_NAME_HUMIDITY = "humidity";

    private static final String CREATE_TABLE_CITY = "CREATE TABLE "
            + TABLE_CITY + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_CITY_NAME + " TEXT," +
            COLUMN_NAME_WOEID + " TEXT," +
            COLUMN_NAME_GPS + " TEXT," +
            COLUMN_NAME_CREATED_AT + " DATETIME" + " );";

    private static final String CREATE_TABLE_NOW = "CREATE TABLE "
            + TABLE_NOW + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_WOEID + " TEXT," +
            COLUMN_NAME_TEMP + " TEXT," +
            COLUMN_NAME_CONDITION + " TEXT," +
            COLUMN_NAME_CONDITION_CODE + " TEXT," +
            COLUMN_NAME_WIND_SPEED + " TEXT," +
            COLUMN_NAME_HUMIDITY + " TEXT," +
            COLUMN_NAME_CREATED_AT + " DATETIME" + " );";

    private static final String CREATE_TABLE_FORECAST = "CREATE TABLE "
            + TABLE_FORECAST + " (" +
            _ID + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_WOEID + " TEXT," +
            COLUMN_NAME_DAY_NAME + " TEXT," +
            COLUMN_NAME_CONDITION_CODE + " TEXT," +
            COLUMN_NAME_FORECAST + " TEXT," +
            COLUMN_NAME_CREATED_AT + " DATETIME" + " );";

    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CITY);
        db.execSQL(CREATE_TABLE_NOW);
        db.execSQL(CREATE_TABLE_FORECAST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOW);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FORECAST);

        onCreate(db);
    }
}
