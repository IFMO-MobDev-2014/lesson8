package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Евгения on 30.11.2014.
 */
public class WeatherContentProvider extends ContentProvider {

    // database
    private DBAdapter database;

    // used for the UriMacher
    private static final int WEATHER = 10;
    private static final int FORECASTS = 20;

    public static final String AUTHORITY = "ru.ifmo.ctddev.katununa.lesson8.content";

    private static final String BASE_WEATHER = "weather";
    private static final String BASE_FORECASTS = "forecasts";

    public static final Uri WEATHER_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_WEATHER);
    public static final Uri FORECAST_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_FORECASTS);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/feeds";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/news";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_WEATHER, WEATHER);
        sURIMatcher.addURI(AUTHORITY, BASE_FORECASTS + "/#", FORECASTS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        if (uriType == WEATHER) {
            int result = database.deleteWeather(Long.parseLong(selection)) ? 1 : 0;
            getContext().getContentResolver().notifyChange(uri, null);
            return result;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id = -1;
        if (uriType == FORECASTS) {
            id = database.createForecast(values);
            getContext().getContentResolver().notifyChange(Uri.parse(WEATHER_URI + "/" + values.getAsLong(DBAdapter.KEY_FORECASTS_WEATHER_ID)), null);
        } else if (uriType == WEATHER) {
            id = database.createWeather(values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(uri.toString() + "/" + id);
    }

    @Override
    public boolean onCreate() {
        database = DBAdapter.getOpenedInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int uriType = sURIMatcher.match(uri);
        Cursor result = null;
        if (uriType == FORECASTS) {
            String lastPathSegment = uri.getLastPathSegment();
            long channelId = Long.parseLong(lastPathSegment);
            result = database.getForecastsByWeatherId(channelId);
        } else if (uriType == WEATHER) {
            result = database.getAllWeather();
        }
        if (result != null)
            result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        if (uriType != WEATHER) throw new UnsupportedOperationException();
        long id = values.getAsLong(DBAdapter.KEY_ID);
        int result = database.changeWeather(values, id) ? 1 : 0;
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
}

