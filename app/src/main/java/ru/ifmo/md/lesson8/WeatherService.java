package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        broadcast(true, false, zmw);
        boolean force = intent.getExtras().getBoolean("force");

        if (zmw == null) {
            return;
        }

        Cursor c = getContentResolver().query(WeatherProvider.WEATHER_URI, null,
                WeatherProvider.ZMW + "=?", new String[]{zmw}, null);
        if (c.getCount() != 0 && !force) {
            c.close();
            broadcast(false, false, zmw);
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
            broadcast(false, true, zmw);
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
                // in android calendar DAY_OF_YEAR starts with 1
                cv.put(WeatherProvider.YDAY, simpleRow.getJSONObject("date").getInt("yday") + 1);
                cv.put(WeatherProvider.ICON, getRawImage(txtRow.getString("icon_url")));
                cv.put(WeatherProvider.WDAY, txtRow.getString("title"));
                cv.put(WeatherProvider.TXT, txtRow.getString("fcttext_metric"));
                getContentResolver().insert(WeatherProvider.WEATHER_URI, cv);
                txtRow = txtForecast.getJSONObject(2 * i + 1);
                cv.put(WeatherProvider.ICON, getRawImage(txtRow.getString("icon_url")));
                cv.put(WeatherProvider.WDAY, txtRow.getString("title"));
                cv.put(WeatherProvider.TXT, txtRow.getString("fcttext_metric"));
                getContentResolver().insert(WeatherProvider.WEATHER_URI, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
            broadcast(false, true, zmw);
            return;
        }

        broadcast(false, false, zmw);
    }

    private void broadcast(boolean isLoadActive, boolean error, String zmw) {
        Intent intent = new Intent(zmw);
        intent.putExtra("error", error);
        intent.putExtra("isLoadActive", isLoadActive);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private byte[] getRawImage(String url) {
        InputStream in;
        try {
            in = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Bitmap icon = BitmapFactory.decodeStream(in);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }
}
