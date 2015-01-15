package ru.eugene.weather.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by eugene on 12/16/14.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "my_weather2.db";
    public static final int DATABASE_VERSION = 12;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CityDataSource.CREATE_COMMAND);
        db.execSQL(WeatherInfoDataSource.CREATE_COMMAND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CityDataSource.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherInfoDataSource.TABLE_NAME);
        onCreate(db);
    }
}
