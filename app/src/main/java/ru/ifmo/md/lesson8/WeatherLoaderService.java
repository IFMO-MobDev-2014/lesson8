package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ru.ifmo.md.lesson8.database.WeatherProvider;
import ru.ifmo.md.lesson8.database.WeatherTable;
import ru.ifmo.md.lesson8.logic.CityWeather;
import ru.ifmo.md.lesson8.logic.YahooClient;

/**
 * Created by sergey on 01.12.14.
 */
public class WeatherLoaderService extends IntentService {

    public static final String ACTION_ADD_CITY = "ru.ifmo.md.lesson8.action.ADD_CITY";
    public static final String ACTION_ADD_CITY_BY_COORD = "ru.ifmo.md.lesson8.action.ADD_CITY_BY_COORD";
    public static final String ACTION_UPDATE_CITY = "ru.ifmo.md.lesson8.action.UPDATE_CITY";
    public static final String ACTION_UPDATE_ALL = "ru.ifmo.md.lesson8.action.UPDATE_ALL";

    public static final String EXTRA_ID = "KEY_CITY_ID";
    public static final String EXTRA_WOEID = "KEY_CITY_WOEID";
    public static final String EXTRA_LATITUDE = "KEY_LATITUDE";
    public static final String EXTRA_LONGITUDE = "KEY_LONGTITUDE";

    public static final long INTERVAL_NONE = -1;
    public static final long INTERVAL_ONE_HOUR = AlarmManager.INTERVAL_HOUR;
    public static final long INTERVAL_TWO_HOURS = INTERVAL_ONE_HOUR * 2;
    public static final long INTERVAL_SIX_HOURS = INTERVAL_ONE_HOUR * 6;
    public static final long INTERVAL_TWELVE_HOURS = INTERVAL_ONE_HOUR * 12;

    public WeatherLoaderService() {
        super("WeatherLoaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String actionType = intent.getAction();
            if (actionType.equals(ACTION_ADD_CITY)) {
                final int cityId = intent.getIntExtra(EXTRA_WOEID, 2123260);
                actionAddCity(cityId);
            } else if (actionType.equals(ACTION_ADD_CITY_BY_COORD)) {
                final double latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 59.75);
                final double longtitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 30.5);
                actionAddCity(latitude, longtitude);
            } else if (actionType.equals(ACTION_UPDATE_CITY)) {
                final int cityId = intent.getIntExtra(EXTRA_ID, 1);
                final int cityWoeid = intent.getIntExtra(EXTRA_WOEID, 2123260);
                actionUpdateCity(cityId, cityWoeid);
            } else if (actionType.equals(ACTION_UPDATE_ALL)) {
                actionUpdateAll();
            }
        }
    }

    public static void startActionAddNewCity(Context context, int woeid) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_ADD_CITY);
        intent.putExtra(EXTRA_WOEID, woeid);
        context.startService(intent);
    }

    public static void startActionAddNewCity(Context context, double latitude, double longitude) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_ADD_CITY_BY_COORD);
        intent.putExtra(EXTRA_LATITUDE, latitude);
        intent.putExtra(EXTRA_LONGITUDE, longitude);
        context.startService(intent);
    }

    public static void startActionUpdateAll(Context context) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_UPDATE_ALL);
        context.startService(intent);
    }

    public static void startActionUpdateCity(Context context, int cityId, int cityWoeid) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_UPDATE_CITY);
        intent.putExtra(EXTRA_ID, cityId);
        intent.putExtra(EXTRA_WOEID, cityWoeid);
        context.startService(intent);
    }

    private static long readInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong("shared_interval", INTERVAL_ONE_HOUR);
    }

    public static void setInterval(Context context, long interval) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong("shared_interval", interval)
                .commit();
    }

    private CityWeather loadWeather(int woeid) {
        CityWeather result = YahooClient.getWeather(woeid);

        Log.d("TAG", "-----------------------");
        Log.d("TAG", "aaa" + result.location.name);
        Log.d("TAG", "aaa" + result.lastUpdate);
        Log.d("TAG", "aaa" + result.condition.description);
        Log.d("TAG", "aaa" + result.condition.temp);
        Log.d("TAG", "aaa" + result.condition.date);
        Log.d("TAG", "-----------------------");

        return result;
    }

    private void actionAddCity(int woeid) {
        Cursor cursor = getContentResolver().query(
                WeatherProvider.CONTENT_URI,
                new String[] {WeatherTable.COLUMN_ID, WeatherTable.COLUMN_WOEID},
                WeatherTable.COLUMN_WOEID + " = ?",
                new String[] {String.valueOf(woeid)},
                null);
        final boolean updateNotInsert = cursor.getCount() > 0;

        CityWeather weather = loadWeather(woeid);
        ContentValues contentValues = weather.getContentValues();
        contentValues.put(WeatherTable.COLUMN_WOEID, woeid);

        if (updateNotInsert)
            getContentResolver().update(WeatherProvider.buildCityUri(String.valueOf(woeid)), contentValues, null, null);
        else
            getContentResolver().insert(WeatherProvider.CONTENT_URI, contentValues);
    }

    private void actionAddCity(double latitude, double longitude) {
        int woeid = YahooClient.getWoeidByCoord(latitude, longitude);
        actionAddCity(woeid);
    }

    private void actionUpdateCity(int cityId, int cityWoeid) {
        CityWeather weather = loadWeather(cityWoeid);
        ContentValues contentValues = weather.getContentValues();
        contentValues.put(WeatherTable.COLUMN_WOEID, cityWoeid);
        getContentResolver().update(WeatherProvider.buildCityUri(Integer.toString(cityId)), contentValues, null, null);
        Intent intent = new Intent("update");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void actionUpdateAll() {
        Cursor cursor = getContentResolver().query(
                WeatherProvider.CONTENT_URI,
                new String[] {WeatherTable.COLUMN_ID, WeatherTable.COLUMN_WOEID},
                null, null, null);
        cursor.moveToFirst();
        while (!cursor.isBeforeFirst() && !cursor.isAfterLast()) {
            final int cityId = cursor.getInt(cursor.getColumnIndex(WeatherTable.COLUMN_ID));
            final int cityWoeid = cursor.getInt(cursor.getColumnIndex(WeatherTable.COLUMN_WOEID));
            actionUpdateCity(cityId, cityWoeid);
            cursor.moveToNext();
        }
        cursor.close();
    }

}
