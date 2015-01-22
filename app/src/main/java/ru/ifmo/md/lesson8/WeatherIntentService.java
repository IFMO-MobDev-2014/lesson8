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

public class WeatherIntentService extends IntentService{
    public final String REQUEST_URL = "http://api.wunderground.com/api/73eb6388e0639359/conditions/forecast10day/lang:EN/q/zmw:";

    public WeatherIntentService() {
        super("WeatherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String zmw = intent.getStringExtra(MyDatabase.CODE);
        if (zmw == null) {
            return;
        }
        String name = intent.getStringExtra(MyDatabase.NAME);
        Intent intentStart = new Intent(zmw);
        intentStart.putExtra("error", false);
        intentStart.putExtra("isLoadActive", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentStart);
        boolean force = intent.getExtras().getBoolean("force");

        Cursor c = getContentResolver().query(WeatherContentProvider.WEATHER_URI, null,
                MyDatabase.CODE + "=?", new String[]{zmw}, null);
        if (c.getCount() != 0 && !force) {
            c.close();
            Intent intentError = new Intent(zmw);
            intentError.putExtra("error", false);
            intentError.putExtra("isLoadActive", false);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            return;
        }
        c.close();

        JSONObject data;
        URL url;
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
            Intent intentError = new Intent(zmw);
            intentError.putExtra("error", true);
            intentError.putExtra("isLoadActive", false);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentError);
            return;
        }

        getContentResolver().delete(WeatherContentProvider.WEATHER_URI, MyDatabase.CODE + "=?",
                                                                            new String[] {zmw});
        try {
            JSONArray txtForecast = data.getJSONObject("forecast").getJSONObject("txt_forecast").
                    getJSONArray("forecastday");
            JSONArray simpleForecast = data.getJSONObject("forecast").getJSONObject("simpleforecast").
                    getJSONArray("forecastday");
            for (int i = 0; 2 * i + 1 < txtForecast.length() && i < simpleForecast.length(); i++) {
                JSONObject txtRow = txtForecast.getJSONObject(2 * i);
                JSONObject simpleRow = simpleForecast.getJSONObject(i).getJSONObject("date");
                ContentValues cv = new ContentValues();
                cv.put(MyDatabase.CODE, zmw);
                cv.put(MyDatabase.NAME, name);
                cv.put(MyDatabase.YEAR, simpleRow.getInt("year"));
                cv.put(MyDatabase.DAY, simpleRow.getInt("yday") + 1);
                cv.put(MyDatabase.ICON, getRawImage(txtRow.getString("icon_url")));
                cv.put(MyDatabase.WEEK_DAY, txtRow.getString("title"));
                cv.put(MyDatabase.TXT, txtRow.getString("fcttext_metric"));
                getContentResolver().insert(WeatherContentProvider.WEATHER_URI, cv);
                txtRow = txtForecast.getJSONObject(2 * i + 1);
                cv.put(MyDatabase.ICON, getRawImage(txtRow.getString("icon_url")));
                cv.put(MyDatabase.WEEK_DAY, txtRow.getString("title"));
                cv.put(MyDatabase.TXT, txtRow.getString("fcttext_metric"));
                getContentResolver().insert(WeatherContentProvider.WEATHER_URI, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Intent intentFinal = new Intent(zmw);
        intentFinal.putExtra("error", false);
        intentFinal.putExtra("isLoadActive", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentFinal);
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
