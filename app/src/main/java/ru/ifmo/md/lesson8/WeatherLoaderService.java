package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
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
    public static final String ACTION_UPDATE_CITY = "ru.ifmo.md.lesson8.action.UPDATE_CITY";
    public static final String ACTION_UPDATE_ALL = "ru.ifmo.md.lesson8.action.UPDATE_ALL";

    public static final String EXTRA_WOEID = "KEY_WOEID";

    private static final String YAHOO_GEO_URL = "http://where.yahooapis.com/v1";
    private static final String YAHOO_WEATHER_URL = "http://weather.yahooapis.com/forecastrss";
    private static final String APPID = "dj0yJmk9ZGd1YzhOd2VWbW9yJmQ9WVdrOWRVSTNkelJETkRJbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD02MA--";

    public WeatherLoaderService() {
        super("XmlLoaderIntentService");
    }

    public WeatherLoaderService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String woeid = intent.getStringExtra(EXTRA_WOEID);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        YahooClient.getWeather(woeid, "c", requestQueue, new YahooClient.WeatherClientListener() {
            @Override
            public void onWeatherResponse(CityWeather cityWeather) {
//                Log.d("Weather!", weather.lastUpdate);
                ContentValues contentValues = new ContentValues();
                contentValues.put(WeatherTable.COLUMN_WOEID, woeid);
                contentValues.put(WeatherTable.COLUMN_LASTUPD, cityWeather.lastUpdate);

                contentValues.put(WeatherTable.COLUMN_COUNTRY, cityWeather.location.country);
                contentValues.put(WeatherTable.COLUMN_CITY, cityWeather.location.name);

                contentValues.put(WeatherTable.COLUMN_CONDITION_DESCRIPTION, cityWeather.condition.description);
                contentValues.put(WeatherTable.COLUMN_CONDITION_TEMP, cityWeather.condition.temp);
                contentValues.put(WeatherTable.COLUMN_CONDITION_CODE, cityWeather.condition.code);
                contentValues.put(WeatherTable.COLUMN_CONDITION_DATE, cityWeather.condition.date);

                contentValues.put(WeatherTable.COLUMN_ATMOSPHERE_PRESSURE, cityWeather.atmosphere.pressure);
                contentValues.put(WeatherTable.COLUMN_ATMOSPHERE_HUMIDITY, cityWeather.atmosphere.humidity);

                contentValues.put(WeatherTable.COLUMN_WIND_DIRECTION, cityWeather.wind.direction);
                contentValues.put(WeatherTable.COLUMN_WIND_SPEED, cityWeather.wind.speed);

                StringBuilder builder = new StringBuilder();
                for (CityWeather.Forecast forecast : cityWeather.forecasts) {
                    builder.append(forecast.day).append("|");
                    builder.append(forecast.date).append("|");
                    builder.append(forecast.description).append("|");
                    builder.append(forecast.tempMin).append("|");
                    builder.append(forecast.tempMax).append("|");
                    builder.append(forecast.code).append("|");
                }
                contentValues.put(WeatherTable.COLUMN_FORECAST, builder.toString());

                getContentResolver().insert(WeatherProvider.CONTENT_URI, contentValues);
            }
        });
    }

}
