package com.pinguinson.lesson10.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.pinguinson.lesson10.activities.MainActivity;
import com.pinguinson.lesson10.R;
import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.db.tables.ForecastsTable;
import com.pinguinson.lesson10.db.WeatherContentProvider;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by pinguinson.
 */
public class ForecastService extends IntentService {

    public static final String PACKAGE_NAME = "com.pinguinson.lesson10";
    public static final String ACTION_FORECASTS_FETCH = PACKAGE_NAME + ".FORECASTS_FETCH";
    public static final String ACTION_GET_LOCATION_WOEID = PACKAGE_NAME + ".LOCATION_FETCH";
    public static final String ACTION_UPDATE_ALL = PACKAGE_NAME + ".ALL_UPDATE";
    public static final String ACTION_UPDATE_CITIES_LIST = PACKAGE_NAME + ".CITIES_UPDATE";

    public static final String LOCATION = "location";
    public static final String STATUS = "status";

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = -1;

    public static final String FORECAST_FETCH_URI = "http://weather.yahooapis.com/forecastrss?w=%d&u=c";
    public static final String LOCATION_URI =
            "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where" +
                    "%20text%3D%22  *lat*  %2C  *long*  %22%20and%20gflags%3D%22R%22&format=xml";

    public ForecastService() {
        super("ForecastService");
    }

    public static void getCurrentLocation(Context context, Location location) {
        Intent intent = new Intent(context, ForecastService.class);
        intent.setAction(ACTION_GET_LOCATION_WOEID);
        intent.putExtra(LOCATION, location);
        context.startService(intent);
    }

    public static void fetchForecasts(Context context, String cityId, long woeid) {
        Intent intent = new Intent(context, ForecastService.class);
        intent.setAction(ACTION_FORECASTS_FETCH);
        intent.putExtra(MainActivity.CITY_ID, cityId);
        intent.putExtra(MainActivity.WOEID, woeid);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            long woeid;
            String cityId;
            switch (intent.getAction()) {
                case ACTION_FORECASTS_FETCH:
                    woeid = intent.getLongExtra(MainActivity.WOEID, -1);
                    cityId = intent.getStringExtra(MainActivity.CITY_ID);
                    handleForecastFetching(woeid, cityId);
                    break;
                case ACTION_GET_LOCATION_WOEID:
                    final Location loc = intent.getParcelableExtra(LOCATION);
                    handleLocationGetting(loc);
                    break;
                case ACTION_UPDATE_ALL:
                    Cursor allCities = getContentResolver().query(WeatherContentProvider.CITIES_CONTENT_URL,
                            new String[]{"*"},
                            null, null, null);
                    allCities.moveToFirst();
                    while (!allCities.isAfterLast()) {
                        woeid = allCities.getLong(allCities.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_WOEID));
                        cityId = allCities.getString(allCities.getColumnIndexOrThrow(CitiesTable._ID));
                        handleForecastFetching(woeid, cityId);
                        allCities.moveToNext();
                    }
                    break;
            }
        }
    }

    private void handleForecastFetching(long woeid, String cityId) {
        try {
            InputStream response = new URL(String.format(FORECAST_FETCH_URI, woeid)).openStream();
            ForecastParser.Result result = new ForecastParser().parse(response);
            onForecastSuccess(cityId, result);
        } catch (IOException e) {
            onError(ACTION_FORECASTS_FETCH, getBaseContext().getString(R.string.fetching_error));
        } catch (SAXException e) {
            //this should never happen
            onError(ACTION_FORECASTS_FETCH, getBaseContext().getString(R.string.parsing_error));
        }
    }

    protected void onForecastSuccess(String cityId, ForecastParser.Result result) {
        getContentResolver().update(WeatherContentProvider.CITIES_CONTENT_URL,
                result.condition, CitiesTable._ID + "=?", new String[]{cityId});
        getContentResolver().delete(WeatherContentProvider.FORECASTS_CONTENT_URL,
                ForecastsTable.COLUMN_NAME_CITY_ID + "=?", new String[]{cityId});
        for (ContentValues row : result.forecasts) {
            row.put(ForecastsTable.COLUMN_NAME_CITY_ID, cityId);
            getContentResolver().insert(WeatherContentProvider.FORECASTS_CONTENT_URL, row);
        }
        Intent localIntent = new Intent(ACTION_FORECASTS_FETCH)
                .putExtra(STATUS, STATUS_OK)
                .putExtra(MainActivity.CITY_ID, cityId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void handleLocationGetting(Location location) {
        try {
            String url = LOCATION_URI
                    .replace("  *lat*  ", Double.toString(location.getLatitude()))
                    .replace("  *long*  ", Double.toString(location.getLongitude()));
            InputStream response = new URL(url).openStream();
            ForecastParser.LocationResult result = new ForecastParser().parseLocation(response);
            onLocationSuccess(result);
        } catch (IOException e) {
            onError(ACTION_GET_LOCATION_WOEID, getBaseContext().getString(R.string.fetching_error));
        } catch (SAXException e) {
            //this should never happen
            onError(ACTION_GET_LOCATION_WOEID, getBaseContext().getString(R.string.parsing_error));
        }
    }

    protected void onLocationSuccess(ForecastParser.LocationResult res) {
        ContentValues row = new ContentValues();
        row.put(CitiesTable.COLUMN_NAME_IS_CURRENT, 1);
        Cursor current = getContentResolver().query(WeatherContentProvider.CITIES_CONTENT_URL,
                new String[]{"*"},
                CitiesTable.COLUMN_NAME_WOEID + "=?",
                new String[]{Long.toString(res.woeid)},
                null);
        if (current.getCount() == 0) {
            row.put(CitiesTable.COLUMN_NAME_CITY_NAME, res.cityName);
            row.put(CitiesTable.COLUMN_NAME_WOEID, res.woeid);
            getContentResolver().insert(WeatherContentProvider.CITIES_CONTENT_URL, row);
        } else {
            ContentValues reset = new ContentValues();
            reset.put(CitiesTable.COLUMN_NAME_IS_CURRENT, 0);
            getContentResolver().update(WeatherContentProvider.CITIES_CONTENT_URL,
                    reset, null, null);
            getContentResolver().update(WeatherContentProvider.CITIES_CONTENT_URL,
                    row,
                    CitiesTable.COLUMN_NAME_WOEID + "=?",
                    new String[]{Long.toString(res.woeid)});
        }

        Intent localIntent = new Intent(ACTION_GET_LOCATION_WOEID)
                .putExtra(STATUS, STATUS_OK)
                .putExtra(MainActivity.WOEID, res.woeid);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    protected void onError(String action, String err) {
        Bundle bundle = new Bundle();
        bundle.putString(Intent.EXTRA_TEXT, err);
        Intent intent = new Intent(action)
                .putExtra(STATUS, STATUS_ERROR)
                .putExtra(Intent.EXTRA_TEXT, err);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
