package ru.ifmo.md.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import ru.ifmo.md.weather.db.WeatherContentProvider;
import ru.ifmo.md.weather.db.model.CityTable;

/**
 * Created by Kirill on 15.12.2014.
 */
public class Util {

    public static boolean addCityWithNameToBase(Context context, ContentValues values) {
        Uri uri = WeatherContentProvider.CONTENT_URI_CITIES;
        String cityName = values.getAsString(CityTable.NAME_COLUMN);
        Cursor cursor = context.getContentResolver()
                .query(uri, null, CityTable.NAME_COLUMN + " LIKE ?", new String[]{cityName}, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Log.i("addCityWithNameToBase", "find city with same name");
            return false;
        } else {
            Log.i("addCityWithNameToBase", "add city");
            context.getContentResolver().insert(uri, values);
            return true;
        }
    }
}
