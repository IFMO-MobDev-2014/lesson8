package ru.ifmo.md.lesson8.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ru.ifmo.md.lesson8.ItemData;
import ru.ifmo.md.lesson8.database.DataProvider;
import ru.ifmo.md.lesson8.fragments.WhetherFragment;

/**
 * Created by sugakandrey on 19.09.14.
 */
public class LoadInfoTask extends AsyncTask<String, Void, ItemData> {
    public DataProvider dp;

    public LoadInfoTask(DataProvider dp) {
        this.dp = dp;
    }

    @Override
    protected ItemData doInBackground(String... strings) {
        String request =  "http://api.openweathermap.org/data/2.5/forecast/daily?q=".concat(strings[0]).concat("&mode=json&cnt=3");
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response;
        ItemData result = null;
        try {
            response = httpClient.execute(new HttpGet(request));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                String responseString = out.toString();
                result = parseJSON(responseString, strings[0]);
            } else {
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException el) {
            Log.e("ERROR", "ClientProtocolException in TranslateWordTask");
        } catch (IOException e) {
            Log.e("ERROR", "IOException in TranslateWordTask");
        }
        return result;
    }

    @Override
    protected void onPostExecute(ItemData s) {
        super.onPostExecute(s);
        dp.updateCityInfo(s);
        dp.cityListFragment.adapter.notifyDataSetChanged();
    }

    private ItemData parseJSON(String responseString, String name) {
        ItemData cur = new ItemData(name, 0, 0, 0, 0, 0, 0, "", "", "", "", "", "");
        try {
            JSONObject answer = new JSONObject(responseString);
            JSONArray list = answer.getJSONArray("list");
            cur.name = name;

            JSONObject today = new JSONObject(list.getString(0));
            JSONObject temp = today.getJSONObject("temp");
            cur.temperatureToday = round(temp.getDouble("eve") - 273.15);
            cur.pressure = round(today.getDouble("pressure") * 0.00750061683);
            cur.humidity = today.getInt("humidity");
            JSONArray weather = today.getJSONArray("weather");
            JSONObject w = new JSONObject(weather.getString(0));
            cur.srcToday = "i".concat(w.getString("icon"));
            cur.descriptionToday = w.getString("description");
            cur.wind = today.getDouble("speed");

            JSONObject tomorrow = new JSONObject(list.getString(1));
            temp = tomorrow.getJSONObject("temp");
            cur.temperatureTomorrow = round(temp.getDouble("eve") - 273.15);
            weather = tomorrow.getJSONArray("weather");
            w = new JSONObject(weather.getString(0));
            cur.descriptionTomorrow = w.getString("description");
            cur.srcTomorrow = "i".concat(w.getString("icon"));

            JSONObject after = new JSONObject(list.getString(2));
            temp = after.getJSONObject("temp");
            cur.temperatureAfter = round(temp.getDouble("eve") - 273.15);
            weather = after.getJSONArray("weather");
            w = new JSONObject(weather.getString(0));
            cur.descriptionAfter = w.getString("description");
            cur.srcAfter = "i".concat(w.getString("icon"));


        } catch (JSONException ex) {
            Log.e("ERROR", "While parsing JSON answer", ex);
        }
        return cur;
    }

    private double round(double a) {
        return Math.floor(a * 100.0)/100.0;
    }
}
