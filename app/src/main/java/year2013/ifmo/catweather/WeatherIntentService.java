package year2013.ifmo.catweather;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    private static final int cnt = 5;
    private static final String OPEN_WEATHER_FORECAST =
            "http://api.openweathermap.org/data/2.5/forecast/daily?q=%s&mode=json&units=metric&cnt=" + cnt;
    private static final String OPEN_WEATHER_LAT_AND_LON = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&mode=json&units=metric";


    public static final String BROADCAST_ACTION =
            "year2013.ifmo.catweather.BROADCAST";

    private String cityLat;

    public static final String ACTION_WEATHER = "action_weather";
    public static final String ACTION_LATLON = "action_latlon";
    public static final String ACTION_CITIES = "action_cities";

    private Handler handler = new Handler();

    public WeatherIntentService() {
        super("WeatherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (intent != null) {
                String action = intent.getAction();
                switch(action) {
                    case ACTION_WEATHER: {
                        String city = intent.getStringExtra(EXTRA_CITY);

                        String today = load(city, null, OPEN_WEATHER_MAP_API);
                        String forecast = load(city, null, OPEN_WEATHER_FORECAST);

                        if (today != null && forecast != null) {
                            Intent localIntent =
                                    new Intent(BROADCAST_ACTION)
                                            .putExtra(EXTRA_CITY, city)
                                            .putExtra(EXTRA_TODAY, today)
                                            .putExtra(EXTRA_FORECAST, forecast);

                            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

                            ContentValues cv = new ContentValues();
                            cv.put(Weather.JustWeather.CITY_NAME, city);
                            cv.put(Weather.JustWeather.TODAY_NAME, today);
                            cv.put(Weather.JustWeather.FUTURE_NAME, forecast);
                            getContentResolver().update(Weather.JustWeather.CONTENT_URI, cv,
                                    Weather.JustWeather.CITY_NAME + "=\"" + city + "\"", null);
                        } else {
                            showToast(WeatherIntentService.this.getString(R.string.place_not_found));
                        }
                        break;
                    }
                    case ACTION_LATLON: {

                        cityLat = intent.getStringExtra(EXTRA_LATITUDE);
                        String lon = intent.getStringExtra(EXTRA_LONGITUDE);

                        String today = load(cityLat, lon, OPEN_WEATHER_LAT_AND_LON);
                        try {
                            Cursor cursor = getContentResolver().query(Weather.JustWeather.CONTENT_URI, null,
                                    Weather.JustWeather.CITY_NAME + "=\"" + cityLat + "\"", null, null);
                            cursor.moveToLast();
                            cursor.isNull(Weather.JustWeather.CITY_COLUMN);
                            cursor.close();
                            showToast(getString(R.string.exist));
                        } catch (android.database.CursorIndexOutOfBoundsException e) {
                            String forecast = load(cityLat, null, OPEN_WEATHER_FORECAST);

                            Intent localIntent =
                                    new Intent(BROADCAST_ACTION)
                                            .putExtra(EXTRA_CITY, cityLat)
                                            .putExtra(EXTRA_TODAY, today)
                                            .putExtra(EXTRA_FORECAST, forecast);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

                            ContentValues cv = new ContentValues();
                            cv.put(Weather.JustWeather.CITY_NAME, cityLat);
                            cv.put(Weather.JustWeather.TODAY_NAME, today);
                            cv.put(Weather.JustWeather.FUTURE_NAME, forecast);
                            getContentResolver().insert(Weather.JustWeather.CONTENT_URI, cv);
                            break;
                        }

                    }
                    case ACTION_CITIES: {

                    }
                }
            }

        } catch (Exception e) {
            showToast(getString(R.string.error_message));
            e.printStackTrace();
        }
    }

    private String load(String cityLat, String lon, String from) throws IOException, JSONException {
        URL url;
        if (lon == null) {
            url = new URL(String.format(from, change(cityLat)));
        } else {
            url = new URL(String.format(from, cityLat, lon));
        }
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.addRequestProperty(WeatherIntentService.this.getString(R.string.api_key),
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
        if (data.getInt(WeatherIntentService.this.getString(R.string.cod)) != 200) {
            showToast(getString(R.string.error_message));
            return null;
        }

        if (lon != null) {
            this.cityLat = data.getString(WeatherIntentService.this.getString(R.string.name));
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

    private String change(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
                s = s.substring(0, i) + WeatherIntentService.this.getString(R.string.space) + s.substring(i + 1);
                i += 2;
            }
        }
        return s;
    }
}
