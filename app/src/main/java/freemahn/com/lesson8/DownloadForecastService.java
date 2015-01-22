package freemahn.com.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Freemahn on 22.01.2015.
 */
public class DownloadForecastService extends IntentService {
    public static String serviceName = "downloadForecastService";
    public static final String ACTION_RESPONSE = "freemahn.com.lesson8.downloadForecastService.RESPONSE";
    ArrayList<Item> entries;

    public DownloadForecastService() {
        super(serviceName);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        List<Item> list = new ArrayList<Item>();
        HttpClient httpclient = new DefaultHttpClient();

        String city = intent.getStringExtra("city");
        Log.e("GET CITY?", city);
        String url = "https://query.yahooapis.com/v1/public/yql?";

        String q = "select * from weather.bylocation where location=\"" + city + "\"and unit='c'";
        HttpPost http = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("q", q));
        nameValuePairs.add(new BasicNameValuePair("format", "json"));
        nameValuePairs.add(new BasicNameValuePair("env", "store://datatables.org/alltableswithkeys"));
        StringBuilder total = new StringBuilder();
        try {
            http.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(http);
            String line = "";

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((line = rd.readLine()) != null)
                total.append(line);
            Log.d("Responce", total.toString());
            JSONObject json = new JSONObject(total.toString());
            JSONObject condition = json.getJSONObject("query").getJSONObject("results").getJSONObject("weather").getJSONObject("rss").getJSONObject("channel").getJSONObject("item").getJSONObject("condition");
            JSONArray forecast = json.getJSONObject("query").getJSONObject("results").getJSONObject("weather").getJSONObject("rss").getJSONObject("channel").getJSONObject("item").getJSONArray("forecast");

            list.add(new Item(condition));
            for (int i = 0; i < forecast.length(); i++)
                list.add(new Item(forecast.getJSONObject(i)));


            getContentResolver().delete(
                    ForecastContentProvider.FORECAST_URI,
                    null,
                    null
            );
            for (int i = 0; i < list.size(); i++) {
                Item item  =list.get(i);
                Log.d("DOWNLOAD", item + "");
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.DATE_COLUMN, item.date);
                cv.put(DatabaseHelper.CODE_COLUMN, item.code);
                cv.put(DatabaseHelper.TEXT_COLUMN, item.text);
                cv.put(DatabaseHelper.TEMP_COLUMN, item.temp);
                cv.put(DatabaseHelper.TEMP_HIGH_COLUMN, item.high);
                getContentResolver().insert(ForecastContentProvider.FORECAST_URI, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent response = new Intent();
        response.setAction(ACTION_RESPONSE);
        response.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(response);
    }
}
