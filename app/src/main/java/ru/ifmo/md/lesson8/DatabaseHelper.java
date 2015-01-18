package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by timur on 08.01.15.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "weather.db";
    private static final int DB_VERSION = 10;
    private CitiesTable citiesTable;
    private WeatherTable weatherTable;

    public DatabaseHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        citiesTable = new CitiesTable(sqLiteDatabase);
        weatherTable = new WeatherTable(sqLiteDatabase);
        ContentValues contentValues = new ContentValues();
        contentValues.put(CitiesTable.NAME, "My Location");
        contentValues.put(CitiesTable.URL, 0);
        sqLiteDatabase.insert(CitiesTable.CITIES_TABLE, null, contentValues);
        contentValues = new ContentValues();
        contentValues.put(CitiesTable.NAME, "Moscow");
        contentValues.put(CitiesTable.URL, 524901);
        sqLiteDatabase.insert(CitiesTable.CITIES_TABLE, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        citiesTable.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        weatherTable.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

}
