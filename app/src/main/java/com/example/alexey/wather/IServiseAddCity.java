package com.example.alexey.wather;

import android.app.IntentService;
import android.content.ContentValues;
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
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Alexey on 08.12.2014.
 */
public class IServiseAddCity extends IntentService {


    static String RECEIVER = "1";
    static int STATUS_RUNNING = 2;
    static String RECEIVER_DATA = "4";
    static int STATUS_FINISHED = 5;

    public IServiseAddCity() {
        this("IServise");
    }

    public IServiseAddCity(String name) {
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

        String s = intent.getStringExtra("name");
        URL url = null;
        try {
            url = new URL("http://api.wunderground.com/api/0c4d0979336b962f/geolookup/q/"+s+".json");
            Bundle bundle=parseJ(getJ(url), s);
            data.putString(RECEIVER_DATA, "Sample result data");
            data.putBundle("ans",bundle);
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

    Bundle parseJ(String strJson, String name) {
        String LOG_TAG = "JSON_PARS";
        Log.d(LOG_TAG, strJson);
        JSONObject dataJsonObj = null;

        try {
            Bundle bundle=new Bundle();
            dataJsonObj = new JSONObject(strJson);
            if (dataJsonObj.getJSONObject("response").has("error"))
            {
                bundle.putString("result","mistake");
                return bundle;
            }
            if (dataJsonObj.has("location"))
            {
                bundle.putString("result","one");
                return bundle;
            }
            bundle.putString("result","many");
            ArrayList<String> list=new ArrayList<String>();
            ArrayList<String> links=new ArrayList<String>();
            JSONArray results = dataJsonObj.getJSONObject("response").getJSONArray("results");
            String city;
            JSONObject temp;
            String state;
            String country;
            ContentValues cv;
            String zmw;
            for (int i = 0; i < results.length(); i++) {
                temp = results.getJSONObject(i);
                city=temp.getString("city");
                state=temp.getString("state");
                country=temp.getString("country_name");
                if (state.equals("")) city=city+", "+country;
                else city=city+", "+state+", "+country;
                list.add(city);
                zmw=temp.getString("l");
                //zmw = "http://api.wunderground.com/api/" + "0c4d0979336b962f" + "/forecast10day/q/" + zmw + ".json";
                links.add(zmw);
                }
            bundle.putStringArrayList("links",links);
            bundle.putStringArrayList("list",list);
            return bundle;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
