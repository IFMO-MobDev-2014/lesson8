package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by izban on 09.01.15.
 */
public class ForecastLoadService extends IntentService {
    public ForecastLoadService() {
        super(ForecastLoadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("", "service started");
        try {
            URL url = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22saint-petersburg%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            InputStream is = connect.getInputStream();
            Scanner in = new Scanner(is);
            String s = "";
            while (in.hasNext()) {
                s += in.next();
            }
            JSONObject json = new JSONObject(s);
            JSONArray jItems = json.getJSONObject("query").getJSONObject("results").getJSONObject("channel").getJSONArray("forecast");
            ArrayList<Item> items = new ArrayList<Item>();
            for (int i = 0; i < jItems.length(); i++) {
                items.add(new Item(jItems.getJSONObject(i)));
                Log.i("", items.get(i).toString());
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.i("", "service failed");
        }
    }
}