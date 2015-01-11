package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import ru.ifmo.md.lesson8.dummy.DummyContent;


/**
 * Created by 107476 on 08.01.2015.
 */
public class UpdaterService extends IntentService {
    double lon;
    double lat;
    String name;
    int id;
    int date;
    String country;
    int temp;
    int humidity;
    int pressure;
    int wind;
    int lowTemp;
    int highTemp;
    boolean updateAll;
    String weatherType;

    int selected = 0;

    public static final String urlFind = "http://api.openweathermap.org/data/2.5/find?";
    public static final String urlCurrent = "http://api.openweathermap.org/data/2.5/weather?";
    public static final String urlPost = "&units=metric";
    public static final String urlForecast =  "http://api.openweathermap.org/data/2.5/forecast/daily?";
    public static final String APIKey = "&APPID=4e27c072cea04e90c416e9110cd59ba7";

    private boolean running = false;

    ResultReceiver receiver;

    public UpdaterService() {
        super("UpdaterService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        running = true;
        name = intent.getStringExtra("name");
        id = intent.getIntExtra("id", -1);
        receiver = intent.getParcelableExtra("receiver");
        lon = intent.getDoubleExtra("lon", 1000);
        lat = intent.getDoubleExtra("lat", 1000);
        updateAll = intent.getBooleanExtra("all", false);
        if (updateAll) {

            for (DummyContent.CitiesItem city : DummyContent.ITEMS) {
                updateCity(city.woeid);
            }
            receiver.send(AppResultReceiver.OK, Bundle.EMPTY);

        }
        else if (id == -1) {
            try {
                StringBuilder builder = new StringBuilder();
                URLConnection connection;
                if (lon==1000 && lat==1000) {
                     connection = new URL(urlFind + "q=" + name + urlPost + APIKey + "/").openConnection();
                } else {
                    connection = new URL(urlFind + "lat=" +lat+"&lon="+lon+"&cnt=1" + APIKey + "/").openConnection();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }
                reader.close();
                String resultJson = builder.toString();
                JSONObject jsonObject = new JSONObject(resultJson);
                int count = jsonObject.getInt("count");
                String[] countries = new String[count];
                JSONArray list = jsonObject.getJSONArray("list");
                if (count == 0) {
                    Bundle bundle = new Bundle();
                    bundle.putString("error", "Can't find this city");
                    receiver.send(AppResultReceiver.ERROR, bundle);
                } else {
                    for (selected = 0; selected<count; selected++) {

                        JSONObject myCity = list.getJSONObject(selected);
                        name = myCity.getString("name");
                        id = myCity.getInt("id");
                        country = myCity.getJSONObject("sys").getString("country");

                        ContentValues values = new ContentValues();
                        values.put(MyContentProvider.WOEID, id);
                        values.put(MyContentProvider.CITY_NAME, name);
                        values.put(MyContentProvider.CITY_COUNTRY, country);
                        getContentResolver().insert(MyContentProvider.CITIES_CONTENT_URI, values);
                    }
                    receiver.send(AppResultReceiver.OK, Bundle.EMPTY);
                }

            } catch (FileNotFoundException e) {
                Bundle bundle = new Bundle();
                bundle.putString("error", "Error at openweathermap.com");
                receiver.send(AppResultReceiver.ERROR, bundle);
                return;
            }
            catch (Exception e) {
                Bundle bundle = new Bundle();
                bundle.putString("error", "Connection error");
                receiver.send(AppResultReceiver.ERROR, bundle);
            }
        } else {
            updateCity(id);
        }



    }

    public void updateCity(int id) {
        try {
            StringBuilder builder = new StringBuilder();
            URLConnection connection = new URL(urlCurrent + "id=" + id + urlPost+APIKey + "/").openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
            reader.close();
            String resultJson = builder.toString();
            JSONObject jsonObject = new JSONObject(resultJson);
            weatherType = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
            date = jsonObject.getInt("dt");
            humidity = jsonObject.getJSONObject("main").getInt("humidity");
            temp = jsonObject.getJSONObject("main").getInt("temp");
            wind = jsonObject.getJSONObject("wind").getInt("speed");
            pressure = jsonObject.getJSONObject("main").getInt("pressure");
            ContentValues values = new ContentValues();
            getContentResolver().delete(MyContentProvider.CURRENT_WEATHER_CONTENT_URI, "city_id = ?", new String[] {""+id});
            values.put(MyContentProvider.CURRENT_CITY_ID, id);
            values.put(MyContentProvider.CURRENT_DATE, date);
            values.put(MyContentProvider.CURRENT_WEATHER_TYPE, weatherType);
            values.put(MyContentProvider.CURRENT_HUMIDITY, humidity);
            values.put(MyContentProvider.CURRENT_TEMP, temp);
            values.put(MyContentProvider.CURRENT_WIND, wind);
            values.put(MyContentProvider.CURRENT_PRESSURE, pressure);
            getContentResolver().insert(MyContentProvider.CURRENT_WEATHER_CONTENT_URI, values);
            builder = new StringBuilder();
            connection = new URL(urlForecast+"id="+id+urlPost).openConnection();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
            reader.close();
            resultJson = builder.toString();
            jsonObject = new JSONObject(resultJson);
            JSONArray array = jsonObject.getJSONArray("list");
            getContentResolver().delete(MyContentProvider.CURRENT_FORECAST_CONTENT_URI, "city_id = ?", new String[] {""+id});

            for (int i = 0; i<7; i++) {
                values = new ContentValues();
                lowTemp = array.getJSONObject(i).getJSONObject("temp").getInt("min");
                highTemp = array.getJSONObject(i).getJSONObject("temp").getInt("max");
                weatherType = array.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
                date = array.getJSONObject(i).getInt("dt");
                values.put(MyContentProvider.FORECAST_CITY_ID, id);
                values.put(MyContentProvider.FORECAST_DATE, date);
                values.put(MyContentProvider.FORECAST_HIGH_TEMP, highTemp);
                values.put(MyContentProvider.FORECAST_LOW_TEMP, lowTemp);
                values.put(MyContentProvider.FORECAST_WEATHER_TYPE, weatherType);
                getContentResolver().insert(MyContentProvider.CURRENT_FORECAST_CONTENT_URI, values);

            }

        } catch (Exception e) {
            receiver.send(AppResultReceiver.ERROR, Bundle.EMPTY);
        }
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
