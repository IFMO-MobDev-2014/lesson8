package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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

    public static final String EXTRA_LATITUDE = "KEY_LATITUDE";
    public static final String EXTRA_LONGITUDE = "KEY_LONGTITUDE";
    public static final String EXTRA_WOEID = "KEY_WOEID";

    private static final String YAHOO_GEO_URL = "http://where.yahooapis.com/v1";
    private static final String YAHOO_WEATHER_URL = "http://weather.yahooapis.com/forecastrss";
    private static final String APPID = "dj0yJmk9ZGd1YzhOd2VWbW9yJmQ9WVdrOWRVSTNkelJETkRJbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD02MA--";

    public WeatherLoaderService() {
        super("WeatherLoaderService");
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

    public static void startActionUpdateCity(Context context, int woeid) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_UPDATE_CITY);
        intent.putExtra(EXTRA_WOEID, woeid);
        context.startService(intent);
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
                final int cityId = intent.getIntExtra(EXTRA_WOEID, 2123260);
                actionUpdateCity(cityId);
            } else if (actionType.equals(ACTION_UPDATE_ALL)) {
                actionUpdateAll();
            }
        }
    }

    private void actionAddCity(int woeid) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final int fWoeid = woeid;
        YahooClient.getWeather(Integer.toString(woeid), "c", requestQueue, new YahooClient.WeatherClientListener() {
            @Override
            public void onWeatherResponse(CityWeather cityWeather) {
//                Log.d("Weather!", weather.lastUpdate);
                ContentValues contentValues = cityWeather.getContentValues();
                contentValues.put(WeatherTable.COLUMN_WOEID, fWoeid);
                getContentResolver().insert(WeatherProvider.CONTENT_URI, contentValues);
            }
        });
    }

    private void actionAddCity(double latitude, double longitude) {
        int woeid = YahooClient.getWoeidByCoord(latitude, longitude);
        actionAddCity(woeid);
    }

    private void actionUpdateCity(int woeid) {

    }

    private void actionUpdateAll() {

    }

    protected void onHandleIntents(Intent intent) {
        final String woeid = intent.getStringExtra(EXTRA_WOEID);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

    }

}
