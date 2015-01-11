package ru.ifmo.md.lesson8;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;


import java.util.ArrayList;

import ru.ifmo.md.lesson8.dummy.DummyContent;

/**
 * Created by 107476 on 10.01.2015.
 */
public class WeatherLoader extends AsyncTaskLoader<DummyContent.Pair> {
    int id;
    private DummyContent.WeatherItem item;
    public WeatherLoader(Context context, int id) {
        super(context);
        this.id = id;
    }


    @Override
    public DummyContent.Pair loadInBackground() {
        if (isReset()) {
            return null;
        }
        int cityId = 0;
        int woeid = 0;
        int date = 0;
        int temp = 0;
        int humidity = 0;
        int pressure = 0;
        int wind = 0;
        String type ="";
        Cursor cursor = getContext().getContentResolver().query(MyContentProvider.CURRENT_WEATHER_CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        int cursorId = 0;
        while (!cursor.isAfterLast()) {
            cursorId = cursor.getInt(1);
            if (cursorId==id) {
                cityId = cursor.getInt(0);
                woeid = id;
                date = cursor.getInt(2);
                temp = cursor.getInt(3);
                humidity = cursor.getInt(4);
                pressure = cursor.getInt(5);
                wind = cursor.getInt(6);
                type = cursor.getString(7);
                break;
            }
            cursor.moveToNext();
        }
        item = new DummyContent.WeatherItem(cityId, woeid, date, temp, humidity, pressure, wind, type);
        cursor.close();
        String selection = "city_id = ?";
        String[] selectionArgs = {""+woeid};
        cursor = getContext().getContentResolver().query(MyContentProvider.CURRENT_FORECAST_CONTENT_URI, null, selection, selectionArgs, null);
        ArrayList<DummyContent.ForecastItem> forecastItems = new ArrayList<>();
        cursor.moveToFirst();
        int lowTemp = 0;
        int highTemp = 0;
        while (!cursor.isAfterLast()) {
            cityId = cursor.getInt(0);
            woeid = cursor.getInt(1);
            date = cursor.getInt(2);
            type = cursor.getString(3);
            lowTemp = cursor.getInt(4);
            highTemp = cursor.getInt(5);
            forecastItems.add(new DummyContent.ForecastItem(cityId, woeid, date, lowTemp, highTemp, type));
            cursor.moveToNext();
        }
        cursor.close();
        return new DummyContent.Pair(forecastItems, item);
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        item = null;
    }
}
