package ru.ifmo.md.lesson8.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import static ru.ifmo.md.lesson8.data.WeatherContentProvider.*;

/**
 * Created by mariashka on 11/30/14.
 */
public class WeatherLoader extends AsyncTaskLoader<List<WeatherItem>> {
    Context context = null;
    public WeatherLoader(Context c) {
        super(c);
        context = c;
    }

    @Override
    public List<WeatherItem> loadInBackground() {
        return offlineHandle();
    }

    public List<WeatherItem> offlineHandle() {
        List<WeatherItem> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(WEATHER_URI, null, null, null, null);
        cursor.moveToFirst();
        do {
            if (cursor.isAfterLast())
                break;
            WeatherItem curr = new WeatherItem();
            curr.setId(cursor.getInt(0));
            curr.setName(cursor.getString(1));
            curr.setCondition(cursor.getString(2));
            curr.setCurrT(cursor.getInt(3));
            curr.setDate(cursor.getString(4));
            curr.setFeels(cursor.getInt(5));

            for (int i = 6; i < 13; i += 2) {
                curr.addHourlyT(cursor.getInt(i));
                curr.addHourlyC(cursor.getString(i + 1));
            }
            for (int i = 14; i < 22; i += 3) {
                WeatherItem tmp = new WeatherItem();
                tmp.setMin(cursor.getInt(i));
                tmp.setMax(cursor.getInt(i + 1));
                tmp.setCondition(cursor.getString(i + 2));
                curr.addNext(tmp);
            }
            list.add(curr);
        } while (cursor.moveToNext());
        cursor.close();
        return list;
    }
}
