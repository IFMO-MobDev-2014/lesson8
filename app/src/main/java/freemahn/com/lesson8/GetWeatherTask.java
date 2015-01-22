package freemahn.com.lesson8;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Freemahn on 29.11.2014.
 */
//TODO implement request to api
public class GetWeatherTask extends AsyncTask<String, Integer, List<Item>> {


    @Override
    protected List<Item> doInBackground(String... params) {
        List<Item> list = new ArrayList<Item>();
        HttpClient httpclient = new DefaultHttpClient();

        String city = params[0];
        Log.e("GET CITY?", city);
        String url = "https://query.yahooapis.com/v1/public/yql?";

        String q = "select * from weather.bylocation where location=\"" + city + "\"and unit='c'";
        HttpPost http = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("q", q));
        nameValuePairs.add(new BasicNameValuePair("format", "json"));
        // nameValuePairs.add(new BasicNameValuePair("units", "c"));
        nameValuePairs.add(new BasicNameValuePair("env", "store://datatables.org/alltableswithkeys"));

        StringBuilder total = new StringBuilder();
        try {
            http.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(http);
            String line = "";

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((line = rd.readLine()) != null) {
                total.append(line);

            }
//            Log.d("Responce", total.toString());
            JSONObject json = new JSONObject(total.toString());
            JSONObject condition = json.getJSONObject("query").getJSONObject("results").getJSONObject("weather").getJSONObject("rss").getJSONObject("channel").getJSONObject("item").getJSONObject("condition");
            JSONArray forecast = json.getJSONObject("query").getJSONObject("results").getJSONObject("weather").getJSONObject("rss").getJSONObject("channel").getJSONObject("item").getJSONArray("forecast");

            list.add(new Item(condition));
            for (int i = 0; i < forecast.length(); i++)
                list.add(new Item(forecast.getJSONObject(i)));

            for (int i = 0; i < list.size(); i++) {
                Log.d("Getting", list.get(i).toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
