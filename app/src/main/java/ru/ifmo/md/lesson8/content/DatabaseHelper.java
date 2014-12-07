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

        // TODO: Remove hardcoded adding
        Place place = new Place.Builder()
                        .setCountry("Russia")
                        .setName("St. Petersburg")
                        .setWoeid(2123260)
                        .createPlace();
        ContentValues values = new ContentValues();
        values.put(Places.COUNTRY_COLUMN, place.getCountry());
        values.put(Places.NAME_COLUMN, place.getName());
        values.put(Places.WOEID_COLUMN, place.getWoeid());
        db.insert(Places.TABLE_NAME, null, values);

        values.clear();
        values.put(WeatherInfo.WOEID_COLUMN, place.getWoeid());
        values.put(WeatherInfo.TEMPERATURE_COLUMN, 100);
        db.insert(WeatherInfo.TABLE_NAME, null, values);
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
