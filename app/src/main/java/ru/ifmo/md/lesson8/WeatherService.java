package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService extends IntentService{
    public final String REQUEST_URL = "http://api.wunderground.com/api/f59a47febebdfe9a/conditions/forecast10day/lang:EN/q/zmw:";

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String zmw = intent.getStringExtra(WeatherProvider.ZMW);
        String name = intent.getStringExtra(WeatherProvider.NAME);
        boolean force = intent.getExtras().getBoolean("force");

        if (zmw == null) {
            return;
        }

        Cursor c = getContentResolver().query(WeatherProvider.WEATHER_URI, null,
                WeatherProvider.ZMW + "=?", new String[]{zmw}, null);
        if (c.getCount() != 0 && !force) {
            c.close();
            return;
        }
        c.close();

        JSONObject data = null;
        URL url = null;
        try {
            url = new URL(REQUEST_URL + zmw + ".json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            data = new JSONObject(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        getContentResolver().delete(WeatherProvider.WEATHER_URI, WeatherProvider.ZMW + "=?",
                                                                            new String[] {zmw});

        try {
            JSONArray txtForecast = data.getJSONObject("forecast").getJSONObject("txt_forecast").
                    getJSONArray("forecastday");
            JSONArray simpleForecast = data.getJSONObject("forecast").getJSONObject("simpleforecast").
                    getJSONArray("forecastday");
            for (int i = 0; 2 * i + 1 < txtForecast.length() && i < simpleForecast.length(); i++) {
                JSONObject txtRow = txtForecast.getJSONObject(2 * i);
                JSONObject simpleRow = simpleForecast.getJSONObject(i);
                ContentValues cv = new ContentValues();
                cv.put(WeatherProvider.ZMW, zmw);
                cv.put(WeatherProvider.NAME, name);
                cv.put(WeatherProvider.YEAR, simpleRow.getJSONObject("date").getInt("year"));
                cv.put(WeatherProvider.YDAY, simpleRow.getJSONObject("date").getInt("yday"));
                cv.put(WeatherProvider.ICON, txtRow.getString("icon_url"));
                cv.put(WeatherProvider.WDAY, txtRow.getString("title"));
                cv.put(WeatherProvider.TXT, txtRow.getString("fcttext_metric"));
                getContentResolver().insert(WeatherProvider.WEATHER_URI, cv);
                txtRow = txtForecast.getJSONObject(2 * i + 1);
                cv.put(WeatherProvider.ICON, txtRow.getString("icon_url"));
                cv.put(WeatherProvider.WDAY, txtRow.getString("title"));
                cv.put(WeatherProvider.TXT, txtRow.getString("fcttext_metric"));
                getContentResolver().insert(WeatherProvider.WEATHER_URI, cv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
