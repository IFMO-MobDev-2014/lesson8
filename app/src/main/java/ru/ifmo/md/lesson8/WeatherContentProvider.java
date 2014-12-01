package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class WeatherContentProvider extends ContentProvider {
    public static final String AUTHORITY = WeatherContentProvider.class.getName();

    public static final Uri CITY_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + WeatherDatabaseHelper.CITY_TABLE_NAME);
    public static final Uri WEATHER_CONTENT_URI = Uri.parse(
            "content://" + AUTHORITY + "/" + WeatherDatabaseHelper.WEATHER_TABLE_NAME);
    static final String CITY_CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + WeatherDatabaseHelper.CITY_TABLE_NAME;
    static final String WEATHER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." +
            WeatherDatabaseHelper.WEATHER_TABLE_NAME;
    public static final int URI_CITY_ID = 1;
    public static final int URI_WEATHER_ID = 2;
    public static final String WRONG_URI = "Wrong URI";
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, WeatherDatabaseHelper.CITY_TABLE_NAME, URI_CITY_ID);
        uriMatcher.addURI(AUTHORITY, WeatherDatabaseHelper.WEATHER_TABLE_NAME, URI_WEATHER_ID);
    }
    private WeatherDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new WeatherDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_CITY_ID:
                Cursor cursor = dbHelper.getReadableDatabase().query(WeatherDatabaseHelper.CITY_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), CITY_CONTENT_URI);
                return cursor;
            case URI_WEATHER_ID:
                cursor = dbHelper.getReadableDatabase().query(WeatherDatabaseHelper.WEATHER_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), WEATHER_CONTENT_URI);
                return cursor;
            default:
                throw new IllegalArgumentException(WRONG_URI);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_CITY_ID:
                return CITY_CONTENT_TYPE;
            case URI_WEATHER_ID:
                return WEATHER_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException(WRONG_URI);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        switch (uriMatcher.match(uri)) {
            case URI_CITY_ID:
                long id = dbHelper.getWritableDatabase().insert(WeatherDatabaseHelper.CITY_TABLE_NAME, null, contentValues);
                Uri result = ContentUris.withAppendedId(CITY_CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                return result;
            case URI_WEATHER_ID:
                id = dbHelper.getWritableDatabase().insert(WeatherDatabaseHelper.WEATHER_TABLE_NAME, null, contentValues);
                result = ContentUris.withAppendedId(WEATHER_CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                return result;
            default:
                throw new IllegalArgumentException(WRONG_URI);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_CITY_ID:
                int cnt = dbHelper.getWritableDatabase().delete(WeatherDatabaseHelper.CITY_TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return cnt;
            case URI_WEATHER_ID:
                cnt = dbHelper.getWritableDatabase().delete(WeatherDatabaseHelper.WEATHER_TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return cnt;
            default:
                throw new IllegalArgumentException(WRONG_URI);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_CITY_ID:
                int cnt = dbHelper.getWritableDatabase().update(WeatherDatabaseHelper.CITY_TABLE_NAME, contentValues, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return cnt;
            case URI_WEATHER_ID:
                cnt = dbHelper.getWritableDatabase().update(WeatherDatabaseHelper.WEATHER_TABLE_NAME, contentValues, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return cnt;
            default:
                throw new IllegalArgumentException(WRONG_URI);
        }
    }
}
