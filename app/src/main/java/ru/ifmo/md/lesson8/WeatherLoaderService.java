package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

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

    public static final long INTERVAL_MANUALLY = -1;
    public static final long INTERVAL_ONE_HOUR = AlarmManager.INTERVAL_HOUR;
    public static final long INTERVAL_HALF_HOUR = INTERVAL_ONE_HOUR / 2;
    public static final long INTERVAL_TWO_HOURS = INTERVAL_ONE_HOUR * 2;
    public static final long INTERVAL_SIX_HOURS = INTERVAL_ONE_HOUR * 6;
    public static final long INTERVAL_TWELVE_HOURS = INTERVAL_ONE_HOUR * 12;
    public static final long INTERVAL_DAY = AlarmManager.INTERVAL_DAY;

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

    public static long getInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong("shared_interval", INTERVAL_ONE_HOUR);
    }

    public static void setInterval(Context context, long interval) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong("shared_interval", interval)
                .commit();
    }

    public static int getCurrentCity(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("shared_current_city", 2123260);
    }

    public static void setCurrentCity(Context context, int cityWoeid) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt("shared_current_city", cityWoeid)
                .commit();
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, WeatherLoaderService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_UPDATE_ALL);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), getInterval(context), pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    private CityWeather loadWeather(int woeid) {
        return YahooClient.getWeather(woeid);
    }

    private void actionAddCity(int woeid) {
        Cursor cursor = getContentResolver().query(
                WeatherProvider.CONTENT_URI,
                new String[] {WeatherTable.COLUMN_ID, WeatherTable.COLUMN_WOEID},
                WeatherTable.COLUMN_WOEID + " = ?",
                new String[] {String.valueOf(woeid)},
                null);
        final boolean updateNotInsert = cursor.getCount() > 0;
        cursor.close();

        CityWeather weather = loadWeather(woeid);
        ContentValues contentValues = weather.getContentValues();
        contentValues.put(WeatherTable.COLUMN_WOEID, woeid);

        if (updateNotInsert)
            getContentResolver().update(WeatherProvider.buildCityUri(String.valueOf(woeid)), contentValues, null, null);
        else
            getContentResolver().insert(WeatherProvider.CONTENT_URI, contentValues);
    }

    private void actionAddCity(double latitude, double longitude) {
        int woeid;
        if (Math.abs(latitude - 59.97) < 0.3 && Math.abs(longitude - 30.20) < 0.3)
            woeid = 2123260; // Saint Petersburg
        else
            woeid = YahooClient.getWoeidByCoord(latitude, longitude);
        actionAddCity(woeid);
        setCurrentCity(getApplicationContext(), woeid);
    }

    private void actionUpdateCity(int cityId, int cityWoeid) {
//        Log.d("TAG", "action update city_id = " + cityId + "cityWoeid = " + cityWoeid);
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
