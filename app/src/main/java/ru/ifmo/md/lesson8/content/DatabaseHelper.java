package ru.ifmo.md.lesson8.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.ifmo.md.lesson8.places.Place;

import static ru.ifmo.md.lesson8.content.WeatherContract.*;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCitiesTable(db);
        createWeatherInfoTable(db);
    }

    private void createWeatherInfoTable(SQLiteDatabase db) {
        db.execSQL(WeatherInfo.CREATE_TABLE);
    }

    private void createCitiesTable(SQLiteDatabase db) {
        db.execSQL(Places.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropCitiesTable(db);
        dropWeatherInfoTable(db);
        onCreate(db);
    }

    private void dropCitiesTable(SQLiteDatabase db) {
        db.execSQL(Places.DELETE_TABLE);
    }

    private void dropWeatherInfoTable(SQLiteDatabase db) {
        db.execSQL(WeatherInfo.DELETE_TABLE);
    }
}
