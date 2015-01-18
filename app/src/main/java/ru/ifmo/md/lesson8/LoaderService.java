package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


public class LoaderService extends IntentService {
    public static final String EXTRA_SINGLE_ID = "SINGLE_ID";

    public LoaderService() {
        super("LoaderService");
    }

    private void fetchCity(int cityId) {
        try {
            HttpURLConnection conn;
            URL url;
            if (cityId == 0) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                url = new URL("http://api.openweathermap.org/data/2.5/weather?units=metric&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
            } else {
                url = new URL("http://api.openweathermap.org/data/2.5/weather?units=metric&id=" + cityId);
            }
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            Scanner scanner = new Scanner(conn.getInputStream());
            scanner.useDelimiter("\\A");
            JSONObject object = new JSONObject(scanner.next());
            JSONObject main = object.getJSONObject("main");
            conn.disconnect();
            ContentValues cv = CitiesTable.makeContentValues(object.getJSONObject("wind"),
                    object.getJSONArray("weather").getJSONObject(0),
                    object.getJSONObject("clouds").getInt("all"),
                    main.getInt("humidity"),
                    main.getInt("pressure"),
                    main.getDouble("temp"));
            getContentResolver().update(DatabaseContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityId).build(), cv, null, null);
            getContentResolver().notifyChange(DatabaseContentProvider.URI_CITY_DIR, null);
            MainActivity.updateSuccessful();
        } catch (MalformedURLException ex) {
        } catch (NullPointerException e) {
        } catch (IOException | JSONException ioe) {
        }
    }

    private void fetchWeather(int cityId) {
        try {
            HttpURLConnection conn;
            URL url;
            if (cityId == 0) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?units=metric&cnt=16&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
            } else {
                url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?units=metric&cnt=16&id=" + cityId);
            }
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            Scanner scanner = new Scanner(conn.getInputStream());
            scanner.useDelimiter("\\A");
            JSONObject object = new JSONObject(scanner.next());
            JSONArray arr = object.getJSONArray("list");
            conn.disconnect();
            ContentValues cv;
            if (arr.length() > 0) {
                getContentResolver().delete(DatabaseContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityId).build(), null, null);
            }

            for (int i = 0; i < arr.length(); i++) {
                JSONObject cw = arr.getJSONObject(i);
                JSONObject temps = cw.getJSONObject("temp");

                cv = WeatherTable.makeContentValues(cw,
                        cw.getJSONArray("weather").getJSONObject(0),
                        cw.getInt("clouds"),
                        cw.getInt("humidity"),
                        cw.getInt("pressure"),
                        temps.getDouble("min"),
                        temps.getDouble("max"));

                getContentResolver().insert(DatabaseContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityId).build(), cv);
                getContentResolver().notifyChange(DatabaseContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityId).build(), null);

            }
        } catch (MalformedURLException ex) {
        } catch (NullPointerException e) {
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final int cityId = intent.getIntExtra(EXTRA_SINGLE_ID, -1);
            if (cityId != -1) {
                fetchCity(cityId);
                fetchWeather(cityId);
            }
        }
    }
}
