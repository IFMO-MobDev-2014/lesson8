package ru.ifmo.md.lesson8.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.ifmo.md.lesson8.ItemData;
import ru.ifmo.md.lesson8.fragments.CityListFragment;
import ru.ifmo.md.lesson8.fragments.WhetherFragment;

/**
 * Created by Svet on 30.11.2014.
 */
public class DataProvider {
    DatabaseHelper db;
    SQLiteDatabase sql;
    public CityListFragment cityListFragment;


    public DataProvider(DatabaseHelper db) {
        this.db = db;
    }

    public int getCitiesCount() {
        sql = db.getReadableDatabase();

        int result = sql.rawQuery("SELECT * FROM " + db.TABLE_NAME, null).getCount();

        db.close();
        return result;
    }

    public ItemData getCityInfoByPosition(int position) {
        sql = db.getReadableDatabase();

        Cursor cursor = sql.query(db.TABLE_NAME, new String[]{db.CITY_NAME,
                db.TEMPERATURE_TODAY, db.TEMPERATURE_TOMORROW, db.TEMPERATURE_AFTER,
                db.DESCRIPTION_TODAY, db.DESCRIPTION_TOMORROW, db.DESCRIPTION_AFTER,
                db.HUMIDITY, db.PRESSURE, db.WIND_SPEED,
                db.SRC_TODAY, db.SRC_TOMORROW, db.SRC_AFTER}, null, null, null, null, null);

        cursor.moveToFirst();
        for(int i = 1; i < position && !cursor.isAfterLast(); i++, cursor.moveToNext()) {

        }
        String name = null, srcToday = null, srcTomorrow = null, srcAfter = null,
        descriptionToday = null, descriptionTomorrow = null, descriptionAfter = null;
        int  humidity = 0, pressure = 0;
        double temperatureToday = 0, temperatureTomorrow = 0, temperatureAfter = 0, wind = 0;
        if(cursor.getCount() > 0 && !cursor.isAfterLast()) {
            name = cursor.getString(cursor.getColumnIndex(db.CITY_NAME));

            temperatureToday = cursor.getDouble(cursor.getColumnIndex(db.TEMPERATURE_TODAY));
            temperatureTomorrow = cursor.getDouble(cursor.getColumnIndex(db.TEMPERATURE_TOMORROW));
            temperatureAfter = cursor.getDouble(cursor.getColumnIndex(db.TEMPERATURE_AFTER));

            descriptionToday = cursor.getString(cursor.getColumnIndex(db.DESCRIPTION_TODAY));
            descriptionTomorrow = cursor.getString(cursor.getColumnIndex(db.DESCRIPTION_TOMORROW));
            descriptionAfter = cursor.getString(cursor.getColumnIndex(db.DESCRIPTION_AFTER));

            humidity = cursor.getInt(cursor.getColumnIndex(db.HUMIDITY));
            pressure = cursor.getInt(cursor.getColumnIndex(db.PRESSURE));
            wind = cursor.getDouble(cursor.getColumnIndex(db.WIND_SPEED));

            srcToday = cursor.getString(cursor.getColumnIndex(db.SRC_TODAY));
            srcTomorrow = cursor.getString(cursor.getColumnIndex(db.SRC_TOMORROW));
            srcAfter = cursor.getString(cursor.getColumnIndex(db.SRC_AFTER));
        }

        db.close();
        return new ItemData(name, temperatureToday, temperatureTomorrow, temperatureAfter, wind, humidity, pressure,
                srcToday, srcTomorrow, srcAfter, descriptionToday, descriptionTomorrow, descriptionAfter);
    }

    public void putCity(ItemData item) {
        sql = db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(db.CITY_NAME, item.name);

        values.put(db.TEMPERATURE_TODAY, item.temperatureToday);
        values.put(db.TEMPERATURE_TOMORROW, item.temperatureTomorrow);
        values.put(db.TEMPERATURE_AFTER, item.temperatureAfter);

        values.put(db.DESCRIPTION_TODAY, item.descriptionToday);
        values.put(db.DESCRIPTION_TOMORROW, item.descriptionTomorrow);
        values.put(db.DESCRIPTION_AFTER, item.descriptionAfter);

        values.put(db.HUMIDITY, item.humidity);
        values.put(db.PRESSURE, item.pressure);
        values.put(db.WIND_SPEED, item.wind);

        values.put(db.SRC_TODAY, item.srcToday);
        values.put(db.SRC_TOMORROW, item.srcTomorrow);
        values.put(db.SRC_AFTER, item.srcAfter);

        sql.insert(db.TABLE_NAME, null, values);
        db.close();
    }

    public void updateCityInfo(ItemData item) {
        sql = db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(db.CITY_NAME, item.name);
        values.put(db.WIND_SPEED, item.wind);
        values.put(db.PRESSURE, item.pressure);
        values.put(db.HUMIDITY, item.humidity);

        values.put(db.TEMPERATURE_TODAY, item.temperatureToday);
        values.put(db.TEMPERATURE_TOMORROW, item.temperatureTomorrow);
        values.put(db.TEMPERATURE_AFTER, item.temperatureAfter);

        values.put(db.DESCRIPTION_TODAY, item.descriptionToday);
        values.put(db.DESCRIPTION_TOMORROW, item.descriptionTomorrow);
        values.put(db.DESCRIPTION_AFTER, item.descriptionAfter);

        values.put(db.SRC_TODAY, item.srcToday);
        values.put(db.SRC_TOMORROW, item.srcTomorrow);
        values.put(db.SRC_AFTER, item.srcAfter);

        sql.update(db.TABLE_NAME, values, db.CITY_NAME + " = ?", new String[]{item.name});

        db.close();
    }

    public void deleteCity(String name) {
        sql = db.getWritableDatabase();

        int d = sql.delete(db.TABLE_NAME, db.CITY_NAME + " = ?", new String[]{name});

        db.close();
    }
}
