package ru.ifmo.md.lesson8;


import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LocationLoader extends android.support.v4.content.AsyncTaskLoader<Boolean>{
    private final String LOCATION_URL = "http://api.wunderground.com/api/73eb6388e0639359/geolookup/q/autoip.json";

    private CitiesAdapter citiesAdapter;

    public LocationLoader(Context c, CitiesAdapter citiesAdapter) {
        super(c);
        this.citiesAdapter = citiesAdapter;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Boolean loadInBackground() {
        JSONObject data;
        URL url;

        try {
            url = new URL(LOCATION_URL);
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
            String[] zmwExtract = zmw.split(":");

            citiesAdapter.addCity(name, zmwExtract[zmwExtract.length - 1]);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }
}
