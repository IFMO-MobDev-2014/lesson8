package year2013.ifmo.weather;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Юлия on 16.01.2015.
 */
public class DownloadingForecast extends IntentService {

    private static final String OPEN_WEATHER_MAP_API_CURRENT =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    private static final String OPEN_WEATHER_MAP_API_DAYLY =
            "http://api.openweathermap.org/data/2.5/forecast/daily?q=%s&mode=json&units=metric&cnt=5";
    public static final String EXTRA_CITY_NAME = "city";
    private Handler handler = new Handler();

    public DownloadingForecast() {
        super("DownloadingForecast");
    }

    public static String BROADCAST_ACTION = "bro";

    @Override
    protected void onHandleIntent(Intent intent) {
        String CITY_NAME = intent.getStringExtra(EXTRA_CITY_NAME);
        URL url = null;
        try {
            //Log.d("DownloadingForecast", "I'm in onHandleIntent method!");
            url = new URL(String.format(OPEN_WEATHER_MAP_API_CURRENT, CITY_NAME));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    getString(R.string.open_weather_maps_app_id));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            //Log.d("DownloadingForecast", "current " + ((Integer)json.length()).toString());
            //JSONObject data = new JSONObject(json.toString());

            ContentValues cv = new ContentValues();
            cv.put(Forecast.CITY_NAME, CITY_NAME);
            cv.put(Forecast.CURRENT_FORECAST, json.toString());

            url = new URL(String.format(OPEN_WEATHER_MAP_API_DAYLY, CITY_NAME));
            connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    getString(R.string.open_weather_maps_app_id));

            reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            json = new StringBuffer(1024);
            tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            //Log.d("DownloadingForecast", "daily " + ((Integer)json.length()).toString());
            cv.put(Forecast.DAYS_FORECAST, json.toString());
            getContentResolver().update(Forecast.CONTENT_URI, cv,
                    Forecast.CITY_NAME + "=\"" + CITY_NAME + "\"", null);

            Intent localIntent =
                    new Intent(BROADCAST_ACTION).putExtra(EXTRA_CITY_NAME, CITY_NAME);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            //data = new JSONObject(json.toString());
        } catch (MalformedURLException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DownloadingForecast.this,
                            "Wrong URL, check your city name",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });
            e.printStackTrace();
        } catch (IOException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DownloadingForecast.this,
                            "Something went wrong",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });
       /* } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Something went wrong",
                    Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();*/
        }

    }

}
