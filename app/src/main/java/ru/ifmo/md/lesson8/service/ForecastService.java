package ru.ifmo.md.lesson8.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

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

    private static final String WOEID = "woeid";
    private static final String CITY_ID = "city_id";
    private static final String RECEIVER = "receiver";

    public static final int STATUS_OK = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_ERROR = -1;

    public static final String API_URI = "http://weather.yahooapis.com/forecastrss?w=%d&u=c";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void fetchForecasts(Context context, String cityId, long woeid, ResultReceiver receiver) {
        Intent intent = new Intent(context, ForecastService.class);
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
            final long woeid = intent.getLongExtra(WOEID, -1);
            final String cityId = intent.getStringExtra(CITY_ID);
            final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
            handleForecastFetching(woeid, cityId, receiver);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleForecastFetching(long woeid, String cityId, ResultReceiver receiver) {
        try {
            InputStream response = new URL(String.format(API_URI, woeid)).openStream();
            ForecastParser.Result result = new ForecastParser().parse(response);
            onSuccess(cityId, result, receiver);
        } catch (IOException e) {
            onError(getBaseContext().getString(R.string.fetching_error), receiver);
        } catch (SAXException e) {
            onError(getBaseContext().getString(R.string.parsing_error), receiver);
        }
    }

    protected void onError(String err, ResultReceiver receiver) {
        Bundle bundle = new Bundle();
        bundle.putString(Intent.EXTRA_TEXT, err);
        receiver.send(STATUS_ERROR, bundle);
    }

    protected void onSuccess(String cityId, ForecastParser.Result result, ResultReceiver receiver) {
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
}
