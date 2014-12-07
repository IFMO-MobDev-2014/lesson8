package odeen.weatherpredictor;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Женя on 24.11.2014.
 */
public class WeatherService extends IntentService {
    private static final String TAG = "WeatherService";

    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_FROM_CURRENT = "current";
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LON = "lon";


    private static final int INTERVAL = 5 * 1000;

    public WeatherService() {
        super("WeatherService");
    }

    private String getCurrentWeatherURL(String city) {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + city;
    }

    private String getForecastWeatherURL(String city) {
        return "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "&cnt=10";
    }

    private void handleAllLocations() {
        Log.d(TAG, "Started");
        WeatherProvider.LocationCursor c = WeatherManager.getInstance(getApplicationContext()).getLocations();
        c.moveToFirst();
        while (!c.isAfterLast() && !c.isBeforeFirst()) {
            handleCity(c.getLocation().getId(), c.getLocation().getCity());
            c.moveToNext();
        }
    }

    private void handleCity(int cityId, String cityName) {
        InputStream inputStream;
        JSONObject curObject;
        Weather current;
        ArrayList<Weather> forecasts;
        try {
            inputStream = downloadUrl(getCurrentWeatherURL(cityName + ""));
            BufferedReader streamReader = null;
            streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            curObject = new JSONObject(responseStrBuilder.toString());
            current = Parser.parseWeather(curObject);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            inputStream = downloadUrl(getForecastWeatherURL(cityName + ""));
            BufferedReader streamReader = null;
            streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            curObject = new JSONObject(responseStrBuilder.toString());
            forecasts = Parser.parseDays(curObject.getJSONArray("list"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Log.d(TAG, forecasts.size()+"");
        WeatherManager.getInstance(getApplicationContext()).onFetch(current, cityId);
        WeatherManager.getInstance(getApplicationContext()).onFetch(forecasts, cityId);
    }

    private void handleFromCurrent(double lat, double lon) {
        Log.d(TAG, "lat = " + lat + " lon = " + lon);
        InputStream inputStream;
        JSONObject curObject;
        try {
            inputStream = downloadUrl("http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon);
            BufferedReader streamReader = null;
            streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            curObject = new JSONObject(responseStrBuilder.toString());
            String city = curObject.getString("name");
            if (!city.equals("Saint Petersburg"))
                city = "Saint Petersburg";
            WeatherManager.getInstance(getApplicationContext()).addLocation(city);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.hasExtra(EXTRA_FROM_CURRENT)) {
            handleFromCurrent(intent.getDoubleExtra(EXTRA_LAT, 0), intent.getDoubleExtra(EXTRA_LON, 0));
            return;
        }

        int cityId = intent.getIntExtra(EXTRA_CITY, -1);
        String cityName = WeatherManager.getInstance(getApplicationContext()).getLocationById(cityId).getCity();
        if (cityId == -1) {
            handleAllLocations();
            return;
        }
        handleCity(cityId, cityName);
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }



}
