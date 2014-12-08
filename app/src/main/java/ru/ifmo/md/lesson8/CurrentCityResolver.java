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

public class CurrentCityResolver extends AsyncTask<Double, Void, String> {

    @Override
    protected String doInBackground(Double... params) {
        if (params.length > 0) {
            HttpParams p = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(p, 1000);
            HttpConnectionParams.setSoTimeout(p, 5000);
            HttpClient client = new DefaultHttpClient();
            String requestStr = String.format(
                    "http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f"
                    , params[0], params[1]);
            HttpGet request = new HttpGet(requestStr);
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
                String json1 = sb.toString();
                JSONObject json = new JSONObject(json1);
                if (json.has("status") && "OK".equals(json.getString("status"))) {
                    if (json.has("results")) {
                        JSONArray results = json.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject o = results.getJSONObject(i);
                            JSONArray components = o.getJSONArray("address_components");
                            for (int c = 0; c < components.length(); ++c) {
                                JSONObject a = components.getJSONObject(i);
                                JSONArray types = a.getJSONArray("types");
                                for (int g = 0; g < types.length(); g++)
                                    if ("locality".equals(types.getString(g)))
                                        if (a.has("long_name"))
                                            return a.getString("long_name");
                            }
                        }
                    }
                    return null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }
}
