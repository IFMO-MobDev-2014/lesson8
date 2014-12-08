package ru.ifmo.md.lesson8.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ru.ifmo.md.lesson8.MainActivity;
import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.ForecastsTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ForecastService extends IntentService {

    private static final String ACTION_FORECASTS_FETCH = "fetch_forecasts";
    private static final String ACTION_GET_LOCATION_WOEID = "location_woeid";

    private static final String ACTION = "action";
    private static final String WOEID = "woeid";
    private static final String CITY_ID = "city_id";
    private static final String RECEIVER = "receiver";
    private static final String LOCATION = "location";

    public static final int STATUS_OK = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_ERROR = -1;

    public static final String FORECAST_FETCH_URI = "http://weather.yahooapis.com/forecastrss?w=%d&u=c";
    public static final String LOCATION_URI =
            "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where" +
                    "%20text%3D%22{{ lat }}%2C{{ long }}%22%20and%20gflags%3D%22R%22&format=xml";

    public static void getCurrentLocation(Context context, Location location, ResultReceiver receiver) {
        // TODO: get this shit done
        Intent intent = new Intent(context, ForecastService.class);
        intent.putExtra(ACTION, ACTION_GET_LOCATION_WOEID);
        intent.putExtra(LOCATION, location);
        intent.putExtra(RECEIVER, receiver);
        context.startService(intent);
    }

    public static void fetchForecasts(Context context, String cityId, long woeid, ResultReceiver receiver) {
        Intent intent = new Intent(context, ForecastService.class);
        intent.putExtra(ACTION, ACTION_FORECASTS_FETCH);
        intent.putExtra(CITY_ID, cityId);
        intent.putExtra(WOEID, woeid);
        intent.putExtra(RECEIVER, receiver);
        context.startService(intent);
    }

    public ForecastService() {
        super("ForecastService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final ResultReceiver receiver;
            switch (intent.getStringExtra(ACTION)) {
                case ACTION_FORECASTS_FETCH:
                    final long woeid = intent.getLongExtra(WOEID, -1);
                    final String cityId = intent.getStringExtra(CITY_ID);
                    receiver = intent.getParcelableExtra(RECEIVER);
                    handleForecastFetching(woeid, cityId, receiver);
                    break;
                case ACTION_GET_LOCATION_WOEID:
                    final Location loc = intent.getParcelableExtra(LOCATION);
                    receiver = intent.getParcelableExtra(RECEIVER);
                    handleLocationGetting(loc, receiver);
                    break;
            }
        }
    }

    private void handleForecastFetching(long woeid, String cityId, ResultReceiver receiver) {
        try {
            InputStream response = new URL(String.format(FORECAST_FETCH_URI, woeid)).openStream();
            ForecastParser.Result result = new ForecastParser().parse(response);
            onForecastSuccess(cityId, result, receiver);
        } catch (IOException e) {
            onError(getBaseContext().getString(R.string.fetching_error), receiver);
        } catch (SAXException e) {
            onError(getBaseContext().getString(R.string.parsing_error), receiver);
        }
    }

    protected void onForecastSuccess(String cityId, ForecastParser.Result result, ResultReceiver receiver) {
        getContentResolver().update(WeatherContentProvider.CITIES_CONTENT_URL,
                result.condition, CitiesTable._ID + "=?", new String[] {cityId});
        getContentResolver().delete(WeatherContentProvider.FORECASTS_CONTENT_URL,
                ForecastsTable.COLUMN_NAME_CITY_ID + "=?", new String[] {cityId});
        for(ContentValues row: result.forecasts) {
            row.put(ForecastsTable.COLUMN_NAME_CITY_ID, cityId);
            getContentResolver().insert(WeatherContentProvider.FORECASTS_CONTENT_URL, row);
        }
        receiver.send(STATUS_OK, new Bundle());
    }

    private void handleLocationGetting(Location location, ResultReceiver receiver) {
        try {
            String url = LOCATION_URI
                    .replace("{{ lat }}", Double.toString(location.getLatitude()))
                    .replace("{{ long }}", Double.toString(location.getLongitude()));
            InputStream response = new URL(url).openStream();
            ForecastParser.LocationResult result = new ForecastParser().parseLocation(response);
            onLocationSuccess(result, receiver);
        } catch (IOException e) {
            onError(getBaseContext().getString(R.string.fetching_error), receiver);
        } catch (SAXException e) {
            onError(getBaseContext().getString(R.string.parsing_error), receiver);
        }
    }

    protected void onLocationSuccess(ForecastParser.LocationResult res, ResultReceiver receiver) {
        ContentValues row = new ContentValues();
        row.put(CitiesTable.COLUMN_NAME_IS_CURRENT, 1);
        Cursor current = getContentResolver().query(WeatherContentProvider.CITIES_CONTENT_URL,
                new String[]{"*"},
                CitiesTable.COLUMN_NAME_WOEID + "=?",
                new String[]{Long.toString(res.woeid)},
                null);
        if(current.getCount() == 0) {
            row.put(CitiesTable.COLUMN_NAME_NAME, res.cityName);
            row.put(CitiesTable.COLUMN_NAME_WOEID, res.woeid);
            getContentResolver().insert(WeatherContentProvider.CITIES_CONTENT_URL, row);
        } else {
            getContentResolver().update(WeatherContentProvider.CITIES_CONTENT_URL,
                    row,
                    CitiesTable.COLUMN_NAME_WOEID + "=?",
                    new String[] {Long.toString(res.woeid)});
        }

        Bundle data = new Bundle();
        data.putLong(MainActivity.WOEID, res.woeid);
        receiver.send(STATUS_OK, data);
    }

    protected void onError(String err, ResultReceiver receiver) {
        Bundle bundle = new Bundle();
        bundle.putString(Intent.EXTRA_TEXT, err);
        receiver.send(STATUS_ERROR, bundle);
    }
}
