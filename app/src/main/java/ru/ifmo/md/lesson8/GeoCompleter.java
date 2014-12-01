package ru.ifmo.md.lesson8;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Евгения on 07.12.2014.
 */

public class GeoCompleter extends AsyncTask<String, Void, ArrayList<String>> {

    private static final String COMPLETION_API_KEY = "AIzaSyBKDroEXGuXd8nK7m32RQ5ppB3ReQv6bQ0";

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        if (params.length > 0) {
            HttpParams p = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(p, 1000);
            HttpConnectionParams.setSoTimeout(p, 5000);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(String.format(
                    "https://maps.googleapis.com/maps/api/place/autocomplete/json?key=%s&input=%s&sensor=true"
                    , COMPLETION_API_KEY, params[0]));
            try {
                HttpResponse response = client.execute(request);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent()));
                StringBuilder sb = new StringBuilder();
                String s = "";
                do {
                    sb.append(s);
                    s = reader.readLine();
                } while (s != null);
                JSONObject json = new JSONObject(sb.toString());
                if (json.has("status") && "OK".equals(json.getString("status"))) {
                    ArrayList<String> result = new ArrayList<String>();
                    if (json.has("predictions")) {
                        JSONArray results = json.getJSONArray("predictions");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject o = results.getJSONObject(i);
                            JSONArray types = o.getJSONArray("types");
                            for (int g = 0; g < types.length(); g++)
                                if ("locality".equals(types.getString(g)))
                                    if (o.has("terms"))
                                        result.add(o.getJSONArray("terms").getJSONObject(0).getString("value"));
                        }
                    }
                    return result;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }
}
