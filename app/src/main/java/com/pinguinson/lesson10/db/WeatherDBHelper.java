package com.pinguinson.lesson10.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.db.tables.ForecastsTable;

/**
 * Created by pinguinson.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WeatherApp.db";

    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        CitiesTable.create(db);
        ForecastsTable.create(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CitiesTable.delete(db);
        ForecastsTable.delete(db);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON");
    }
}

