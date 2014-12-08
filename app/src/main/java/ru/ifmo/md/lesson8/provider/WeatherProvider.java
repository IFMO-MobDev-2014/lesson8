package ru.ifmo.md.lesson8.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class WeatherProvider extends ContentProvider {
    public static final String LOG_TAG = "WeatherProvider";
    public static final String AUTHORITY = "ru.ifmo.md.lesson8.provider.WeatherProvider";
    public static final String CITY_PATH = WeatherDatabaseHelper.TABLE_CITY;
    public static final String FORECAST_PATH = WeatherDatabaseHelper.TABLE_FORECAST;

    public static final Uri CITY_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CITY_PATH);
    public static final Uri FORECAST_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FORECAST_PATH);
    public static final int URI_CITY_ID = 1;
    public static final int URI_FORECAST_ID = 2;

    static final String CITY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CITY_PATH;
    static final String FORECAST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FORECAST_PATH;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, CITY_PATH, URI_CITY_ID);
        uriMatcher.addURI(AUTHORITY, FORECAST_PATH, URI_FORECAST_ID);
    }
    private WeatherDatabaseHelper dbHelper;

    public WeatherProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int cnt;
        if (uriMatcher.match(uri) == URI_CITY_ID)
            cnt = dbHelper.getWritableDatabase().delete(WeatherDatabaseHelper.TABLE_CITY, selection, selectionArgs);
        else
            cnt = dbHelper.getWritableDatabase().delete(WeatherDatabaseHelper.TABLE_FORECAST, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public String getType(Uri uri) {
        //Log.i("RSSContentProvider", "getType");
        if (uriMatcher.match(uri) == URI_CITY_ID)
            return CITY_CONTENT_TYPE;
        else if (uriMatcher.match(uri) == URI_FORECAST_ID)
            return FORECAST_CONTENT_TYPE;
        throw new RuntimeException("incorrect getType");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //Log.d(LOG_TAG, "insert " + uri.toString());
        int m = uriMatcher.match(uri);
        Uri resultUri;
        if (m == URI_CITY_ID) {
            long rowID = dbHelper.getWritableDatabase().insert(WeatherDatabaseHelper.TABLE_CITY, null, values);
            resultUri = ContentUris.withAppendedId(CITY_CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(resultUri, null);
        } else if (m == URI_FORECAST_ID) {
            long rowID = dbHelper.getWritableDatabase().insert(WeatherDatabaseHelper.TABLE_FORECAST, null, values);
            resultUri = ContentUris.withAppendedId(FORECAST_CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(resultUri, null);
        } else
            throw new IllegalArgumentException("Wrong URI: " + uri.toString());
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new WeatherDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        //Log.i(LOG_TAG, "query, " + uri.toString());
        int m = uriMatcher.match(uri);
        Cursor cursor;
        if (m == URI_CITY_ID) {
            cursor = dbHelper.getReadableDatabase().query(WeatherDatabaseHelper.TABLE_CITY, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), CITY_CONTENT_URI);
        } else if (m == URI_FORECAST_ID) {
            cursor = dbHelper.getReadableDatabase().query(WeatherDatabaseHelper.TABLE_FORECAST, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), FORECAST_CONTENT_URI);
        } else
            throw new IllegalArgumentException("Wrong URI: " + uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int cnt;
        if (uriMatcher.match(uri) == URI_CITY_ID)
            cnt = dbHelper.getWritableDatabase().update(WeatherDatabaseHelper.TABLE_CITY, values, selection, selectionArgs);
        else
            cnt = dbHelper.getWritableDatabase().update(WeatherDatabaseHelper.TABLE_FORECAST, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}