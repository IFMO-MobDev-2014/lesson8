package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ilya on 23.01.2015.
 */
public class MyDatabase {

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String CODE = "zwm";
    static final String ICON = "icon_raw";
    static final String YEAR = "year";
    static final String DAY = "year_day";
    static final String TXT = "text";
    static final String WEEK_DAY = "week_day";
    static final String DATABASE_NAME = "db";
    static final String WEATHER_TABLE_NAME = "weather";
    static final String CITIES_TABLE_NAME = "cities";
    static final int DATABASE_VERSION = 1;

    static final String CREATE_CITIES_TABLE =
            " CREATE TABLE " + CITIES_TABLE_NAME+
                    " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CODE + " TEXT NOT NULL, " +
                    NAME + " TEXT NOT NULL);";

    static final String CREATE_WEATHER_TABLE =
            " CREATE TABLE " + WEATHER_TABLE_NAME +
                    " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CODE + " TEXT NOT NULL, " +
                    NAME + " TEXT NOT NULL, " +
                    ICON + " BLOB, " +
                    YEAR + " INTEGER," +
                    DAY + " INTEGER," +
                    WEEK_DAY + " INTEGER," +
                    TXT + " TEXT);";

    public static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CITIES_TABLE);
            db.execSQL(CREATE_WEATHER_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + WEATHER_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CITIES_TABLE_NAME);
            onCreate(db);
        }
    }
}
