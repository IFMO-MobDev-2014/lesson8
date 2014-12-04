package ru.ifmo.md.weather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kirill on 01.12.2014.
 */
public class WeatherDownloader {
    static private Context mContext;

    public WeatherDownloader(Context mContext) {
        WeatherDownloader.mContext = mContext;
    }

    public static boolean isOnline(Context mContext) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static String load(String urlString) {
        InputStream is = null;
        String rv = null;
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            is = urlConnection.getInputStream();
            rv = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rv;
    }

    public static String createForecastUrlFromName(String name) {
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=";
        String units = "&units=metric";
        return url + name + units;
    }

    public static String createWeatherUrlFromName(String name) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=";
        String units = "&units=metric";
        return url + name + units;
    }

    public static String loadWeatherForNow(String name) {
        return load(createWeatherUrlFromName(name));
    }

    public static String loadForecastForFiveDays(String name) {
        return load(createForecastUrlFromName(name));
    }

}

