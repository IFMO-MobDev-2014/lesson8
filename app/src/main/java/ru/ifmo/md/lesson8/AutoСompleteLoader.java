package ru.ifmo.md.lesson8;

import android.content.Context;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AutoСompleteLoader extends android.support.v4.content.AsyncTaskLoader<Object> {
    private final String SEARCH_URL = "http://autocomplete.wunderground.com/aq?query=";
    private final String query;

    public AutoСompleteLoader(Context c, String query) {
        super(c);
        this.query = query;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<Pair<String, String>> loadInBackground() {
        JSONObject data;

        URL url;
        try {
            url = new URL(SEARCH_URL + query + "&h=0");
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
            return null;
        }

        ArrayList<Pair<String, String>> result = new ArrayList<>();
        try {
            JSONArray resultArray = data.getJSONArray("RESULTS");
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject row = resultArray.getJSONObject(i);
                result.add(new Pair<>(row.getString("name"), row.getString("zmw")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}
