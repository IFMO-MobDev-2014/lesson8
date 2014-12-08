package ru.ifmo.md.lesson8.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mariashka on 11/28/14.
 */
public class WeatherDB extends SQLiteOpenHelper {

    static final String DB_NAME = "my_db";
    static public final int DB_VERSION = 1;

    static public final String CITY_TABLE = "cities";

    static public final String CITY_ID = "_id";
    static public final String CITY_NAME = "name";


    static public final String WEATHER_TABLE = "weather";

    static public final String WEATHER_ID = "_id";
    static public final String WEATHER_NAME = "name";
    static public final String WEATHER_T = "curr_t";
    static public final String WEATHER_FEEL = "feel";
    static public final String WEATHER_COND = "cond";
    static public final String WEATHER_DATE = "date_t";

    static public final String WEATHER_HOUR_T1 = "hour_t1";
    static public final String WEATHER_HOUR_T2 = "hour_t2";
    static public final String WEATHER_HOUR_T3 = "hour_t3";
    static public final String WEATHER_HOUR_T4 = "hour_t4";

    static public final String WEATHER_COND1 = "hour_c1";
    static public final String WEATHER_COND2 = "hour_c2";
    static public final String WEATHER_COND3 = "hour_c3";
    static public final String WEATHER_COND4 = "hour_c4";

    static public final String WEATHER_NEXT_MIN1 = "next_min1";
    static public final String WEATHER_NEXT_MIN2 = "next_min2";
    static public final String WEATHER_NEXT_MIN3 = "next_min3";

    static public final String WEATHER_NEXT_MAX1 = "next_max1";
    static public final String WEATHER_NEXT_MAX2 = "next_max2";
    static public final String WEATHER_NEXT_MAX3 = "next_max3";

    static public final String WEATHER_NEXT_COND1 = "next_c1";
    static public final String WEATHER_NEXT_COND2 = "next_c2";
    static public final String WEATHER_NEXT_COND3 = "next_c3";

    static final String DB_WEATHER_CREATE = "create table " + WEATHER_TABLE + "("
      + WEATHER_ID + " integer primary key autoincrement, "
      + WEATHER_NAME + " text, " + WEATHER_COND + " text, "
      + WEATHER_T + " int, " + WEATHER_DATE + " text, "
      + WEATHER_FEEL + " int, "
      + WEATHER_HOUR_T1 + " int, " + WEATHER_COND1 + " text, "
      + WEATHER_HOUR_T2 + " int, " + WEATHER_COND2 + " text, "
      + WEATHER_HOUR_T3 + " int, " + WEATHER_COND3 + " text, "
      + WEATHER_HOUR_T4 + " int, " + WEATHER_COND4 + " text, "
      + WEATHER_NEXT_MIN1 + " int, " + WEATHER_NEXT_MAX1 + " int, " + WEATHER_NEXT_COND1 + " text, "
      + WEATHER_NEXT_MIN2 + " int, " + WEATHER_NEXT_MAX2 + " int, " + WEATHER_NEXT_COND2 + " text, "
      + WEATHER_NEXT_MIN3 + " int, " + WEATHER_NEXT_MAX3 + " int, " + WEATHER_NEXT_COND3 + " text"
      + ");";

    static final String DB_CITY_CREATE = "create table " + CITY_TABLE + "("
      + CITY_ID + " integer primary key autoincrement, "
      + CITY_NAME + " text" + ");";


    WeatherDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_WEATHER_CREATE);
        db.execSQL(DB_CITY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
