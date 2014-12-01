package com.example.home.superwheather;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Home on 28.11.2014.
 */

public class MyService extends IntentService {

    public static final String ACTION_MYSERVICE = "com.example.home.superwheather.action1";

    public MyService() {
        super("superservice");
    }

    public void onCreate() {
        super.onCreate();
    }

    private static final String apiKey = "54b1a3ede57af76efeacd8d8e5dc7";

    private static String readStream(InputStream data) {
        String ans = "";
        try {
            StringBuilder inStr = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(data));
            String line = br.readLine();
            while (line != null) {
                inStr.append(line);
                inStr.append('\n');
                line = br.readLine();
            }
            ans = inStr.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ans;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String city = intent.getStringExtra("city");

        String queryUrl = null;
        try {
            queryUrl = "http://api.worldweatheronline.com/free/v2/weather.ashx?q=" + URLEncoder.encode(city, "utf-8") +
                    "&format=json&num_of_days=5&key=" + apiKey;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String[] result = new String[]{};
        String[][][] forecast = new String[][][] {};
        String[] dates = new String[5];
        String cityName = city;

        boolean cityTroubles = false;

        try {
            URL url = new URL(queryUrl);
            URLConnection urlConnection = url.openConnection();
            JSONObject response = new JSONObject(readStream(urlConnection.getInputStream()));
            JSONArray results = response.getJSONObject("data").getJSONArray("current_condition");
            JSONObject res = results.getJSONObject(0);
            result = new String[]{res.getString("cloudcover"), res.getString("temp_C"), res.getString("pressure"), res.getString("humidity"), res.getString("observation_time"),
                    res.getJSONArray("weatherIconUrl").getJSONObject(0).getString("value")};

            cityName = response.getJSONObject("data").getJSONArray("request").getJSONObject(0).getString("query");

            results = response.getJSONObject("data").getJSONArray("weather");
            forecast = new String[5][][];
            for (int i = 0; i < 5; i++) {
                dates[i] = results.getJSONObject(i).getString("date");
                JSONArray hourly = results.getJSONObject(i).getJSONArray("hourly");
                forecast[i] = new String[8][];
                for (int j = 0; j < 8; j++) {
                    JSONObject currentHour = hourly.getJSONObject(j);
                    forecast[i][j] = new String[]{currentHour.getString("cloudcover"), currentHour.getString("tempC"),
                            currentHour.getString("pressure"), currentHour.getString("humidity"), currentHour.getString("time")};
                }
            }

        }  catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            cityTroubles = true;
        }


        if (result.length > 0) {
            Intent intentResponse = new Intent();
            intentResponse.setAction(ACTION_MYSERVICE);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);

            intentResponse.putExtra("city", city);
            intentResponse.putExtra("cityName", cityName);
            intentResponse.putExtra("temp", result[1]);
            intentResponse.putExtra("cloud", result[0]);
            intentResponse.putExtra("hum", result[3]);
            intentResponse.putExtra("press", result[2]);
            intentResponse.putExtra("1_00:00", forecast[0][0]);
            intentResponse.putExtra("1_12:00", forecast[0][4]);
            intentResponse.putExtra("1_21:00", forecast[0][7]);
            intentResponse.putExtra("2_00:00", forecast[1][0]);
            intentResponse.putExtra("2_12:00", forecast[1][4]);
            intentResponse.putExtra("2_21:00", forecast[1][7]);
            intentResponse.putExtra("3_00:00", forecast[2][0]);
            intentResponse.putExtra("3_12:00", forecast[2][4]);
            intentResponse.putExtra("3_21:00", forecast[2][7]);
            intentResponse.putExtra("4_00:00", forecast[3][0]);
            intentResponse.putExtra("4_12:00", forecast[3][4]);
            intentResponse.putExtra("4_21:00", forecast[3][7]);
            intentResponse.putExtra("5_00:00", forecast[4][0]);
            intentResponse.putExtra("5_12:00", forecast[4][4]);
            intentResponse.putExtra("5_21:00", forecast[4][7]);
            intentResponse.putExtra("dates", dates);
            intentResponse.putExtra("succeed", true);

            sendBroadcast(intentResponse);
        } else {
            Intent intentResponse = new Intent();
            intentResponse.setAction(ACTION_MYSERVICE);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);

            intentResponse.putExtra("succeed", false);
            if (cityTroubles) {
                intentResponse.putExtra("cause", "city fail");
            } else {
                intentResponse.putExtra("cause", "internet fail");
            }

            sendBroadcast(intentResponse);
        }

    }

}