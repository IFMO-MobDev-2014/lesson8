package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class WeatherLoaderService extends IntentService {
    private static final String ACTION_GET_ALL = "ru.ifmo.md.lesson5.action.GET_ALL";
    private static final String ACTION_GET_SINGLE = "ru.ifmo.md.lesson5.action.GET_SINGLE";

    // TODO: Rename parameters
    private static final String EXTRA_SINGLE_ID = "ru.ifmo.md.lesson5.extra.SINGLE_ID";

    public WeatherLoaderService() {
        super("WeatherLoaderService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetAll(Context context) {
        context.startService(getIntentGetAll(context));
    }

    public static Intent getIntentGetAll(Context context) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_GET_ALL);
        return intent;
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionGetSingle(Context context, int rowid) {
        Intent intent = new Intent(context, WeatherLoaderService.class);
        intent.setAction(ACTION_GET_SINGLE);
        intent.putExtra(EXTRA_SINGLE_ID, rowid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_ALL.equals(action)) {
                handleActionGetAll();
            } else if (ACTION_GET_SINGLE.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_SINGLE_ID, -1);
                handleActionGetSingle(param1);
            }
        }
    }

    private void handleActionGetAll() {
        int[] ids;
        ContentResolver resolver = getContentResolver();
        Cursor cr = resolver.query(WeatherContentProvider.URI_CITY_DIR, new String[]{WeatherDatabase.Structure.COLUMN_URL}, null, null, null);

        ids = new int[cr.getCount()];

        int ui = 0;
        cr.moveToNext();
        while (!cr.isAfterLast()) {
            ids[ui] = cr.getInt(0);
            ui++;
            cr.moveToNext();
        }
        cr.close();

        for (int i = 0; i < ids.length; i++) {
            loadSingleCity(ids[i]);
        }
    }

    private void handleActionGetSingle(int cityid) {
        loadSingleCity(cityid);
    }

    public static String streamToString(InputStream s) {
        Scanner scn = new Scanner(s);
        scn.useDelimiter("\\A");
        return scn.next();
    }

    private void loadSingleCity(int cityid) {
        HttpURLConnection conn = null;
        URL url = null;
        try {
            if(cityid == 0) {
                LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if(mgr == null)
                    return;

                Location location = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                url = new URL("http://api.openweathermap.org/data/2.5/weather?units=metric&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
            } else {
                url = new URL("http://api.openweathermap.org/data/2.5/weather?units=metric&id=" + cityid);
            }
        } catch(MalformedURLException ex) {
            // oops
        }

        boolean good = false;
        // 1: current weather
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            JSONObject o = new JSONObject(streamToString(is));
            JSONObject wea = o.getJSONArray("weather").getJSONObject(0);
            JSONObject main = o.getJSONObject("main");
            JSONObject wind = o.getJSONObject("wind");
            double temp = main.getDouble("temp");
            int pres = main.getInt("pressure");
            int hum = main.getInt("humidity");
            int clouds = o.getJSONObject("clouds").getInt("all");

            ContentValues cv = new ContentValues();
            cv.put(WeatherDatabase.Structure.COLUMN_CLOUDS, clouds);
            cv.put(WeatherDatabase.Structure.COLUMN_PRESSURE, pres);
            cv.put(WeatherDatabase.Structure.COLUMN_TEMPERATURE, (int) (temp * 10.0d));
            cv.put(WeatherDatabase.Structure.COLUMN_WIND, (int) (wind.getDouble("speed") * 10.0d));
            cv.put(WeatherDatabase.Structure.COLUMN_WIND_DIR, wind.getInt("deg"));
            cv.put(WeatherDatabase.Structure.COLUMN_DESCRIPTION, wea.getInt("id"));
            cv.put(WeatherDatabase.Structure.COLUMN_HUMIDITY, hum);

            getContentResolver().update(WeatherContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityid).build(), cv, null, null);
            getContentResolver().notifyChange(WeatherContentProvider.URI_CITY_DIR, null);

            good = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch(JSONException ex) {
            // empty
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        if(good)
            MainActivity.updateSuccessful();

        // 2: forecasts
        try {
            if(cityid == 0) {
                LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if(mgr == null)
                    return;

                Location location = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?units=metric&cnt=5&lat=" + location.getLatitude() + "&lon=" + location.getLongitude());
            } else {
                url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?units=metric&cnt=5&id=" + cityid);
            }

        } catch(MalformedURLException ex) {
            // oops
        }

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            JSONObject o = new JSONObject(streamToString(is));
            JSONArray arr = o.getJSONArray("list");

            ContentValues cv = new ContentValues();
            if(arr.length() > 0) {
                getContentResolver().delete(WeatherContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityid).build(), null, null);
            }

            for(int i = 0; i < arr.length(); i++) {
                JSONObject cw = arr.getJSONObject(i);

                JSONObject temps = cw.getJSONObject("temp");
                JSONObject wea = cw.getJSONArray("weather").getJSONObject(0);
                double tempMin = temps.getDouble("min");
                double tempMax = temps.getDouble("max");
                int pres = cw.getInt("pressure");
                int hum = cw.getInt("humidity");
                int clouds = cw.getInt("clouds");

                cv.put(WeatherDatabase.Structure.COLUMN_CLOUDS, clouds);
                cv.put(WeatherDatabase.Structure.COLUMN_PRESSURE, pres);
                cv.put(WeatherDatabase.Structure.COLUMN_TEMPERATURE_MIN, (int) (tempMin * 10.0d));
                cv.put(WeatherDatabase.Structure.COLUMN_TEMPERATURE_MAX, (int) (tempMax * 10.0d));
                cv.put(WeatherDatabase.Structure.COLUMN_WIND, (int) (cw.getDouble("speed") * 10.0d));
                cv.put(WeatherDatabase.Structure.COLUMN_WIND_DIR, cw.getInt("deg"));
                cv.put(WeatherDatabase.Structure.COLUMN_DESCRIPTION, wea.getInt("id"));
                cv.put(WeatherDatabase.Structure.COLUMN_HUMIDITY, hum);
                cv.put(WeatherDatabase.Structure.COLUMN_TIME, cw.getInt("dt"));

                getContentResolver().insert(WeatherContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityid).build(), cv);
                getContentResolver().notifyChange(WeatherContentProvider.URI_CITY_DIR.buildUpon().appendPath("" + cityid).build(), null);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch(JSONException ex) {
            ex.printStackTrace();
            // empty for now
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

}
