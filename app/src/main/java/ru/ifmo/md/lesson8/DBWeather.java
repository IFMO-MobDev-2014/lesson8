package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBWeather extends SQLiteOpenHelper {
    private static final String DB_NAME = "weather_db";
    private static final int VERSION = 1;

    public static final String TABLE_WEATHER1 = "weather1";
    public static final String ID1 = "_id";
    public static final String CITY1 = "city";
    public static final String TEMPR1 = "tempr";
    public static final String DATE1 = "date";
    public static final String WEATHER_TYPE1 = "weathertype";
    public static final String WIND1 = "wind";
    public static final String HUMIDITY1 = "humidity";

    public static final String TABLE_WEATHER2 = "weather2";
    public static final String ID2 = "_id";
    public static final String CITY2 = "city";
    public static final String TEMPR_MIN2 = "temprmin";
    public static final String TEMPR_MAX2 = "temprmax";
    public static final String DATE2 = "date";
    public static final String WEATHER_TYPE2 = "weathertype";

    public static final String TABLE_CITIES = "cities";
    public static final String CITY_ID = "_id";
    public static final String CITY_NAME = "name";
    public static final String CITY_URL = "url";

    private static final String INIT_WEATHER1_TABLE =
            "CREATE TABLE " + TABLE_WEATHER1 + " (" +
                    ID1 + " INTEGER " + "PRIMARY KEY AUTOINCREMENT, " +
                    CITY1 + " TEXT, " +
                    TEMPR1 + " TEXT, " +
                    DATE1 + " TEXT, " +
                    WEATHER_TYPE1 + " TEXT, " +
                    WIND1 + " TEXT, " +
                    HUMIDITY1 + " TEXT );";

    private static final String INIT_WEATHER2_TABLE =
            "CREATE TABLE " + TABLE_WEATHER2 + " (" +
                    ID2 + " INTEGER " + "PRIMARY KEY AUTOINCREMENT, " +
                    CITY2 + " TEXT, " +
                    TEMPR_MIN2 + " TEXT, " +
                    TEMPR_MAX2 + " TEXT, " +
                    DATE2 + " TEXT, " +
                    WEATHER_TYPE2 + " TEXT );";

    private static final String INIT_CITIES_TABLE =
            "CREATE TABLE " + TABLE_CITIES + " (" +
                    CITY_ID + " INTEGER " + "PRIMARY KEY AUTOINCREMENT, " +
                    CITY_NAME + " TEXT, " +
                    CITY_URL + " TEXT );";

    public DBWeather(Context context) {
        super(context, DB_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(INIT_WEATHER1_TABLE);
        db.execSQL(INIT_WEATHER2_TABLE);
        db.execSQL(INIT_CITIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("DROP TABLE IF IT EXIST " + TABLE_WEATHER1);
        db.execSQL("DROP TABLE IF IT EXIST " + TABLE_WEATHER2);
        db.execSQL("DROP TABLE IF IT EXIST " + TABLE_CITIES);
        onCreate(db);
    }
}
