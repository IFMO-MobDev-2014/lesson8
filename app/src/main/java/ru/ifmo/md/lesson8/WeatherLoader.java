package ru.ifmo.md.lesson8;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class WeatherLoader extends AsyncTaskLoader<Weather> {
    Context context;
    String city;

    public WeatherLoader(Context context, String city) {
        super(context);
        this.context = context;
        this.city = city;
    }

    @Override
    public Weather loadInBackground() {
        Log.d("debug1", "WeatherLoader started for city " + city);
        Weather weather = new Weather();
        Cursor c = context.getContentResolver().query(
                WeatherContentProvider.WEATHER1_URI,
                null,
                DBWeather.CITY1 + " = ?",
                new String[]{String.valueOf(city)},
                null
        );
        if (c != null) {
            c.moveToFirst();
            if (!c.isBeforeFirst() && !c.isAfterLast()) {
                weather.city = city;
                weather.type = c.getString(c.getColumnIndex(DBWeather.WEATHER_TYPE1));
                weather.date = c.getString(c.getColumnIndex(DBWeather.DATE1));
                weather.wind = c.getString(c.getColumnIndex(DBWeather.WIND1));
                weather.humidity = c.getString(c.getColumnIndex(DBWeather.HUMIDITY1));
                weather.tempr = c.getString(c.getColumnIndex(DBWeather.TEMPR1));
            }
            weather.weather5Days.clear();
        }
        c.close();
        c = context.getContentResolver().query(
                WeatherContentProvider.WEATHER2_URI,
                null,
                DBWeather.CITY2 + " = ?",
                new String[]{String.valueOf(city)},
                null
        );
        if (c != null) {
            c.moveToFirst();
            while (!c.isBeforeFirst() && !c.isAfterLast()) {
                weather.weather5Days.add(new Weather5Days(
                        city,
                        c.getString(c.getColumnIndex(DBWeather.DATE2)),
                        c.getString(c.getColumnIndex(DBWeather.TEMPR_MIN2)),
                        c.getString(c.getColumnIndex(DBWeather.TEMPR_MAX2)),
                        c.getString(c.getColumnIndex(DBWeather.WEATHER_TYPE2))
                ));
                c.moveToNext();
            }
        }
        c.close();
        Log.d("debug1", "WeatherLoader finished for city " + city);
        return weather;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
