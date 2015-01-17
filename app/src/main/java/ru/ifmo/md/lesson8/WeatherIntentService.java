package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherIntentService extends IntentService {
    public static final String EXTRA_CITY = "extra_city";
    public static final String EXTRA_TODAY = "extra_today";
    public static final String EXTRA_FORECAST = "extra_forecast";

    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    private static final int cnt = 16;
    private static final String OPEN_WEATHER_FORECAST =
            "http://api.openweathermap.org/data/2.5/forecast/daily?q=%s&mode=json&units=metric&cnt=" + cnt;

    public static final String BROADCAST_ACTION =
            "ru.ifmo.md.lesson8.BROADCAST";

    private Handler handler = new Handler();

    public WeatherIntentService() {
        super("WeatherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String city = intent.getStringExtra(EXTRA_CITY);

            String today = load(city, OPEN_WEATHER_MAP_API);
            String forecast = load(city, OPEN_WEATHER_FORECAST);

            Intent localIntent =
                    new Intent(BROADCAST_ACTION)
                            .putExtra(EXTRA_TODAY, today)
                            .putExtra(EXTRA_FORECAST, forecast);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            ContentValues cv = new ContentValues();
            cv.put(Weather.JustWeather.CITY_NAME, city);
            cv.put(Weather.JustWeather.TODAY_NAME, today);
            cv.put(Weather.JustWeather.FUTURE_NAME, forecast);
            getContentResolver().update(Weather.JustWeather.CONTENT_URI, cv,
                    Weather.JustWeather.CITY_NAME + "=\"" + city + "\"" , null);

        } catch (Exception e) {
            showToast(getString(R.string.error_message));
            e.printStackTrace();
        }
    }

    private String load(String city, String from) throws IOException, JSONException {
        URL url = new URL(String.format(from, city));
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.addRequestProperty("x-api-key",
                getString(R.string.open_weather_maps_app_id));

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

        StringBuilder json = new StringBuilder(1024);
        String tmp;
        while ((tmp = reader.readLine()) != null)
            json.append(tmp).append("\n");
        reader.close();

        JSONObject data = new JSONObject(json.toString());

        // This value will be 404 if the request was not
        // successful
        if (data.getInt("cod") != 200) {
            showToast(getString(R.string.error_message));
            return null;
        }

        return json.toString();
    }

    private void showToast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WeatherIntentService.this,
                        text,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
