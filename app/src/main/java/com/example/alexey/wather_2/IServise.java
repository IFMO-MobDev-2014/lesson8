package com.example.alexey.wather_2;

/**
 * Created by Alexey on 01.12.2014.
 */

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class IServise extends IntentService {


    static String RECEIVER = "1";
    static int STATUS_RUNNING = 2;
    static String RECEIVER_DATA = "4";
    static int STATUS_FINISHED = 5;

    public IServise() {
        this("IServise");
    }

    public IServise(String name) {
        super(name);
    }


    public void onCreate() {
        super.onCreate();

        Log.i("Started", "IService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("started", "onhandle");
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);


        receiver.send(STATUS_RUNNING,
                Bundle.EMPTY);


        final Bundle data = new Bundle();


        URL url = null;
        try {
            String CITY = null;
            String s = intent.getStringExtra("task");
            if (s.charAt(0) == 'S') CITY = "zmw:00000.1.26063";
            if (s.charAt(0) == 'M') CITY = "zmw:00000.1.27612";
            if (s.charAt(0) == 'N') CITY = "zmw:00000.1.37036";
            String KEY = "0c4d0979336b962f";
            String link = "http://api.wunderground.com/api/" + KEY + "/forecast10day/q/" + CITY + ".json";
            //http://api.wunderground.com/api/0c4d0979336b962f/forecast10day/q/zmw:00000.1.27612.json
            // CHANGE PARSER
            url = new URL(link);
            parseJ(getJ(url), s);

            data.putString(RECEIVER_DATA, "Sample result data");
        } catch (IOException e) {
            data.putString(RECEIVER_DATA, "Error");
        }
        receiver.send(STATUS_FINISHED, data);
    }

    String getJ(URL url) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";


        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            resultJson = buffer.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    void parseJ(String strJson, String name) {
        String LOG_TAG = "JSON_PARS";
        Log.d(LOG_TAG, strJson);
        JSONObject dataJsonObj = null;

        try {
            dataJsonObj = new JSONObject(strJson);

            JSONObject friends = dataJsonObj.getJSONObject("forecast").getJSONObject("txt_forecast");
            JSONArray kol = friends.getJSONArray("forecastday");
            JSONArray simp = dataJsonObj.getJSONObject("forecast").getJSONObject("simpleforecast").getJSONArray("forecastday");
            String data;
            JSONObject temp;
            String day;
            String night;
            ContentValues cv;
            String temper;
            String img1;
            String img2;
            provider.match_name(name);
            for (int i = 0; i < 10; i++) {
                cv = new ContentValues();
                temp = simp.getJSONObject(i).getJSONObject("date");
                data = Integer.toString(temp.getInt("day")) + "." + Integer.toString(temp.getInt("month")) + "." + Integer.toString(temp.getInt("year"));
                day = kol.getJSONObject(2 * i).getString("fcttext_metric");
                night = kol.getJSONObject(2 * i + 1).getString("fcttext_metric");
                temper = simp.getJSONObject(i).getJSONObject("high").getString("celsius");
                img1 = kol.getJSONObject(2 * i).getString("icon_url");
                img2 = kol.getJSONObject(2 * i + 1).getString("icon_url");
                Bitmap bm = BitmapFactory.decodeStream((InputStream) new URL(img1).getContent());
                cv.put(provider.DATE, data);
                cv.put(provider.DAY, day);
                cv.put(provider.NIGHT, night);
                cv.put(provider.TEMPERATURE, temper);
                cv.put(provider.FIVE_PATH, ImageConverter.getBytes(bm));
                bm = BitmapFactory.decodeStream((InputStream) new URL(img2).getContent());
                cv.put(provider.SIX_PATH, ImageConverter.getBytes(bm));
                getContentResolver().insert(provider.CONTENT_URI, cv);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}




