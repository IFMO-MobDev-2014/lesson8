package com.example.alexey.wather;

import android.app.IntentService;
import android.content.Intent;
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
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


public class IServiceAddCity extends IntentService {


    public IServiceAddCity() {
        this("Iservice");
    }

    public IServiceAddCity(String name) {
        super(name);
    }


    public void onCreate() {
        super.onCreate();

        Log.i("Started", "IService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("started", "onhandle");
        ResultReceiver receiver = intent.getParcelableExtra(Consts.RECEIVER);
        final Bundle data = new Bundle();
        Boolean connect = ifExistConnection();
        if (connect) {
            data.putBoolean("status", true);
        } else {
            data.putBoolean("status", false);
            receiver.send(Consts.STATUS_FINISHED, data);
            return;
        }
        String cityName = intent.getStringExtra("city");
        URL url;
        try {
            url = new URL("http://api.wunderground.com/api/0c4d0979336b962f/geolookup/q/" + cityName + ".json");
            Bundle bundle = parseJ(getJ(url), cityName);
            data.putString(Consts.RECEIVER_DATA, "cities");
            data.putBundle("ans", bundle);
        } catch (IOException e) {
            data.putString(Consts.RECEIVER_DATA, "Error");
        }
        receiver.send(Consts.STATUS_FINISHED, data);
    }

    Boolean ifExistConnection() {
        URL url;
        try {
            url = new URL("http://www.google.ru/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            urlConnection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
            return false;
        }
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    String getJ(URL url) {
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String resultJson = "";
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            resultJson = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    Bundle parseJ(String strJson, String name) {
        String LOG_TAG = "JSON_PARS";
        Log.d(LOG_TAG, strJson);
        JSONObject dataJsonObj = null;

        try {
            Bundle bundle = new Bundle();
            dataJsonObj = new JSONObject(strJson);
            if (dataJsonObj.getJSONObject("response").has("error")) {
                bundle.putString("result", "mistake");
                return bundle;
            }
            if (dataJsonObj.has("location")) {
                String shortLink = dataJsonObj.getJSONObject("location").getString("l");
                bundle.putString("result", "one");
                bundle.putString("link", shortLink);
                return bundle;
            }
            bundle.putString("result", "many");
            ArrayList<String> list = new ArrayList<String>();
            ArrayList<String> links = new ArrayList<String>();
            JSONArray results = dataJsonObj.getJSONObject("response").getJSONArray("results");
            String city;
            JSONObject temp;
            String state;
            String country;
            String zmw;
            int t = 0;
            for (int i = 0; i < results.length(); i++) {
                temp = results.getJSONObject(i);
                city = temp.getString("city");
                state = temp.getString("state");
                country = temp.getString("country_name");
                if (state.equals("")) city = city + ", " + country;
                else city = city + ", " + state + ", " + country;
                list.add(city);
                zmw = temp.getString("l");
                links.add(zmw);
                t++;
            }
            bundle.putStringArrayList("links", links);
            bundle.putStringArrayList("list", list);
            bundle.putInt("size", t);
            return bundle;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
