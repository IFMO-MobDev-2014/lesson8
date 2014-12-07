package odeen.weatherpredictor;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Женя on 24.11.2014.
 */
public class WeatherManager {

    private Context mContext;
    private static WeatherManager sManager;

    private Uri mLocationUri = Uri.parse("content://odeen.weatherpredictor.providers.weather_provider/location");
    private Uri mWeatherUri = Uri.parse("content://odeen.weatherpredictor.providers.weather_provider/weather");

    public static final String CurWeatherFetched = "CurWeatherFetched";
    public static final String ForWeatherFetched = "ForWeatherFetched";


    private WeatherManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static WeatherManager getInstance(Context context) {
        if (sManager == null)
            sManager = new WeatherManager(context);
        return sManager;
    }

    private ContentValues packWeather(Weather w) {
        ContentValues cv = new ContentValues();
        cv.put(WeatherProvider.COLUMN_WEATHER_NAME, w.getMainName());
        cv.put(WeatherProvider.COLUMN_WEATHER_DESCRIPTION, w.getDescription());
        cv.put(WeatherProvider.COLUMN_WEATHER_ICON_ID, w.getIconId());
        cv.put(WeatherProvider.COLUMN_WEATHER_CLOUDS, w.getClouds());
        cv.put(WeatherProvider.COLUMN_WEATHER_HUMIDITY, w.getHumidity());
        cv.put(WeatherProvider.COLUMN_WEATHER_MAX_TEMP, w.getMaxTemperature());
        cv.put(WeatherProvider.COLUMN_WEATHER_MIN_TEMP, w.getMinTemperature());
        cv.put(WeatherProvider.COLUMN_WEATHER_TEMP, w.getTemperature());
        cv.put(WeatherProvider.COLUMN_WEATHER_PRESSURE, w.getPressure());
        cv.put(WeatherProvider.COLUMN_WEATHER_TIME, w.getTime());
        cv.put(WeatherProvider.COLUMN_WEATHER_WIND_SPEED, w.getWindSpeed());
        cv.put(WeatherProvider.COLUMN_WEATHER_WIND_DIRECTION, w.getWindDirection());
        return cv;
    }

    public void onFetch(Weather current, int cityId) {
        mContext.getContentResolver().delete(mWeatherUri,
                WeatherProvider.COLUMN_WEATHER_LOCATION_ID + " = " + cityId + " and " + WeatherProvider.COLUMN_WEATHER_CODE + " < 0",
                null
        );
        ContentValues cv = packWeather(current);
        cv.put(WeatherProvider.COLUMN_WEATHER_LOCATION_ID, cityId);
        mContext.getContentResolver().insert(mWeatherUri, cv);
        mContext.sendBroadcast(new Intent(CurWeatherFetched));
    }

    public void onFetch(ArrayList<Weather> forecasts, int cityId) {
        mContext.getContentResolver().delete(mWeatherUri,
                WeatherProvider.COLUMN_WEATHER_LOCATION_ID + " = " + cityId + " and " + WeatherProvider.COLUMN_WEATHER_CODE + " = 0",
                null
        );
        for (Weather f : forecasts) {
            ContentValues cv = packWeather(f);
            cv.put(WeatherProvider.COLUMN_WEATHER_CODE, 0);
            cv.put(WeatherProvider.COLUMN_WEATHER_LOCATION_ID, cityId);
            mContext.getContentResolver().insert(mWeatherUri, cv);
        }
        mContext.sendBroadcast(new Intent(ForWeatherFetched));
    }

    private static Random rnd = new Random();
    public int addLocation(String name) {
        ContentValues cv = new ContentValues();
        cv.put(WeatherProvider.COLUMN_LOCATION_NAME, name);
        int color = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        cv.put(WeatherProvider.COLUMN_LOCATION_COLOR, color);
        return Integer.parseInt(mContext.getContentResolver().insert(mLocationUri, cv).getLastPathSegment());
    }

    public void removeLocation(Location loc) {
        mContext.getContentResolver().delete(Uri.withAppendedPath(mLocationUri, loc.getId() + ""), null, null);
    }

    public Location getLocationById(int id) {
        Cursor c = mContext.getContentResolver().query(mLocationUri, null,
                WeatherProvider.COLUMN_LOCATION_ID + " = " + id, null, null);
        c.moveToFirst();
        if (c.isAfterLast() || c.isBeforeFirst()) {
            return new Location(id, "ololo", 0);
        }
        Location ans = new Location(id, c.getString(c.getColumnIndex(WeatherProvider.COLUMN_LOCATION_NAME)), c.getInt(c.getColumnIndex(WeatherProvider.COLUMN_LOCATION_COLOR)));
        c.close();
        return ans;
    }

    public WeatherProvider.LocationCursor getLocations() {
        Cursor c = mContext.getContentResolver().query(mLocationUri, null, null, null, null);
        return new WeatherProvider.LocationCursor(c);
    }

    public Weather getCurrentWeather(Location loc) {
        Cursor c = mContext.getContentResolver().query(mWeatherUri, null, WeatherProvider.COLUMN_WEATHER_LOCATION_ID + " = " + loc.getId() +
                " and " + WeatherProvider.COLUMN_WEATHER_CODE + " = -1",
                null, null);
        c.moveToFirst();
        Weather ans = new WeatherProvider.WeatherCursor(c).getWeather();
        c.close();
        return ans;
    }

    public ArrayList<Weather> getForecast(Location loc) {
        Cursor c = mContext.getContentResolver().query(mWeatherUri,
                null,
                WeatherProvider.COLUMN_WEATHER_LOCATION_ID + " = " + loc.getId() + " and " +
                WeatherProvider.COLUMN_WEATHER_CODE + " = 0",
                null,
                null);
        c.moveToFirst();
        WeatherProvider.WeatherCursor wc = new WeatherProvider.WeatherCursor(c);
        wc.moveToFirst();
        ArrayList<Weather> ans = new ArrayList<Weather>();
        while (!wc.isBeforeFirst() && !wc.isAfterLast()) {
            Weather cur = wc.getWeather();
            ans.add(cur);
            wc.moveToNext();
        }
        Log.d("MANAGEREBANIY", ans.size()+"");
        c.close();
        return ans;
    }

    public void insertOrUpdateLocation(int id, String name) {
        if (id == -1)
            addLocation(name);
        else {
            ContentValues cv = new ContentValues();
            cv.put(WeatherProvider.COLUMN_LOCATION_NAME, name);
            mContext.getContentResolver().update(Uri.withAppendedPath(mLocationUri, id+""), cv, null, null);
        }
    }
}
