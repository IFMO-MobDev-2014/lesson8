package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Евгения on 30.11.2014.
 */
public class WeatherFetchingService extends IntentService {
    public static final String ACTION_UPDATE_WEATHER
            = "ru.ifmo.md.lesson8.action.updateWeather";
    public static final String ACTION_UPDATE_ALL_WEATHER
            = "ru.ifmo.md.lesson8.action.updateAllWeather";

    private static final String WEATHER_ID = "ru.ifmo.ctddev.katununa.rss_readerhw5.extra.weather_id";

    public static void startActionUpdateWeather(Context context, long weatherId) {
        Intent intent = new Intent(context, WeatherFetchingService.class);
        intent.setAction(ACTION_UPDATE_WEATHER);
        intent.putExtra(WEATHER_ID, weatherId);
        context.startService(intent);
    }

    public static void startActionUpdateAllWeather(Context context) {
        Intent intent = new Intent(context, WeatherFetchingService.class);
        intent.setAction(ACTION_UPDATE_ALL_WEATHER);
        context.startService(intent);
    }


    public WeatherFetchingService() {
        super("ru.ifmo.md.lesson8.WeatherFetchingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WEATHER.equals(action)) {
                final long channelId = intent.getLongExtra(WEATHER_ID, -1);
                handleActionUpdateWeather(channelId);
            } else if (ACTION_UPDATE_ALL_WEATHER.equals(action)) {
                handleActionUpdateAllWeather();
            }
        }
    }

    public static final String BROADCAST_ACTION_WEATHER_UPDATED = "weather updated";

    public static final String URL_PART1 = "https://query.yahooapis.com/v1/public/" +
            "yql?q=select%20%2A%20from%20weather.bylocation%20where%20location%3D%22";
    public static final String URL_PART2 = "%22%" +
            "20and%20unit%3D%27c%27&" +
            "format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

    private void handleActionUpdateWeather(final long weatherId) {
        final DBAdapter db = DBAdapter.getOpenedInstance(this);
        String name = db.getCityByWeatherId(weatherId).replace(' ', '_').replace("'", "");
        fetchWeatherByCityName(weatherId, db, name);
    }

    private void fetchWeatherByCityName(final long weatherId, final DBAdapter db, String name) {
        try {
            name = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        String url = null;
        url = URL_PART1 + name + URL_PART2;
        GsonGetter.Callback<ResponseContainer> callback = new GsonGetter.Callback<ResponseContainer>() {
            @Override
            public void onComplete(GsonGetter sender, ResponseContainer result) {
                db.deleteForecasts(weatherId);
                if (result.query.results.weather.rss.channel.item.forecast != null) {
                    ContentValues weatherValues = db.getContentValuesByChannel(result.query.results.weather.rss.channel);
                    weatherValues.put(DBAdapter.KEY_ID, weatherId);
                    getContentResolver().update(Uri.parse(WeatherContentProvider.WEATHER_URI.toString()), weatherValues, null, null);
                    long newWeatherId = DBAdapter.getOpenedInstance(getApplicationContext())
                            .getIdByCityName(weatherValues.getAsString(DBAdapter.KEY_WEATHER_CITY));
                    for (ForecastItem item :
                            result.query.results.weather.rss.channel.item.forecast) {
                        ContentValues values = db.getContentValuesByForecastItem(item, newWeatherId);
                        getContentResolver().insert(Uri.parse(WeatherContentProvider.FORECAST_URI.toString()
                                + "/" + newWeatherId), values);
                        getContentResolver().notifyChange(Uri.parse(WeatherContentProvider.FORECAST_URI.toString() + "/"
                                + newWeatherId), null);
                    }
                    weatherValues.put(DBAdapter.KEY_ID, weatherId);
                    getContentResolver().notifyChange(Uri.parse(WeatherContentProvider.WEATHER_URI.toString() + "/"
                            + weatherId), null);
                }
            }
        };
        new GsonGetter<>(ResponseContainer.class).get(
                url,
                callback);
    }

    private void handleActionUpdateAllWeather() {
        final DBAdapter db = DBAdapter.getOpenedInstance(this);
        Cursor c = db.getAllWeather();
        if (!c.moveToFirst()) return;
        do {
            handleActionUpdateWeather(c.getLong(c.getColumnIndex(DBAdapter.KEY_ID)));
        } while (c.moveToNext());
        c.close();
    }
}

