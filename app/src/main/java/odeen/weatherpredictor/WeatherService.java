package odeen.weatherpredictor;

import android.app.IntentService;
import android.content.Intent;
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

    public WeatherService() {
        super("WeatherService");
    }

    private String getCurrentWeatherURL(String city) {
        return "http://api.openweathermap.org/data/2.5/weather?q=" + city;
    }

    private String getForecastWeatherURL(String city) {
        return "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "&cnt=10";
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        int cityId = intent.getIntExtra(EXTRA_CITY, -1);
        String cityName = WeatherManager.getInstance(getApplicationContext()).getLocationById(cityId).getCity();
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
