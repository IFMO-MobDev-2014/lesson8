package ru.ifmo.md.lesson8.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.ifmo.md.lesson8.places.Place;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, WeatherContract.DATABASE_NAME, null, WeatherContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCitiesTable(db);

        // TODO: Remove hardcoded adding
        Place place = new Place.Builder()
                        .setCountry("Russia")
                        .setName("St. Petersburg")
                        .setWoeid(2123260)
                        .createPlace();
        ContentValues values = new ContentValues();
        values.put(WeatherContract.Cities.COUNTRY_COLUMN, place.getCountry());
        values.put(WeatherContract.Cities.NAME_COLUMN, place.getName());
        values.put(WeatherContract.Cities.WOEID_COLUMN, place.getWoeid());
        db.insert(WeatherContract.Cities.TABLE_NAME, null, values);
    }

    private void createCitiesTable(SQLiteDatabase db) {
        db.execSQL(WeatherContract.Cities.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropCitiesTable(db);
        onCreate(db);
    }

    private void dropCitiesTable(SQLiteDatabase db) {
        db.execSQL(WeatherContract.Cities.DELETE_TABLE);
    }
}
