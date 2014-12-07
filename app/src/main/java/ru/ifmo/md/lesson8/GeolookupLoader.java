package ru.ifmo.md.lesson8;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GeolookupLoader extends android.support.v4.content.AsyncTaskLoader<Boolean>{
    private final String GEOLOOKUP_URL = "http://api.wunderground.com/api/f59a47febebdfe9a/geolookup/q/autoip.json";
    private CitiesAdapter citiesAdapter;

    public GeolookupLoader(Context c, CitiesAdapter citiesAdapter) {
        super(c);
        this.citiesAdapter = citiesAdapter;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Boolean loadInBackground() {
        JSONObject data = null;

        // h=0: don't search among hurricanes
        URL url = null;
        try {
            url = new URL(GEOLOOKUP_URL);
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
            return false;
        }

        try {
            JSONObject location = data.getJSONObject("location");
            String name = location.getString("city") + ", " + location.getString("country_name");
            String zmw = location.getString("l");
            String[] zmwSplitted = zmw.split(":");

            citiesAdapter.addCity(name, zmwSplitted[zmwSplitted.length - 1]);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }
}
