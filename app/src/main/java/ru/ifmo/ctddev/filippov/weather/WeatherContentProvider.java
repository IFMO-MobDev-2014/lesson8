package ru.ifmo.ctddev.filippov.weather;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Dima_2 on 01.04.2015.
 */
public class WeatherContentProvider extends ContentProvider {
    public static final String AUTHORITY = "ru.ifmo.ctddev.filippov.weather.weather";
    public static final String CITIES_URI = "cities";

    public static final Uri URI_CITY_DIRECTORY = Uri.parse("content://" + AUTHORITY + "/" + CITIES_URI);

    public static final int URI_TYPE_CITY_DIRECTORY = 1;
    public static final int URI_TYPE_CITY_WEATHER = 2;

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, CITIES_URI, URI_TYPE_CITY_DIRECTORY);
        uriMatcher.addURI(AUTHORITY, CITIES_URI + "/#", URI_TYPE_CITY_WEATHER);
    }

    private WeatherDatabase database;

    public WeatherContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        String databaseTable = null;
        switch (uriType) {
            case URI_TYPE_CITY_WEATHER:
                databaseTable = WeatherDatabase.WEATHER_TABLE;
                selection = WeatherDatabase.COLUMN_CITY_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            case URI_TYPE_CITY_DIRECTORY:
                databaseTable = WeatherDatabase.CITIES_TABLE;
                break;
        }
        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        return writableDatabase.delete(databaseTable, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        String tableName = null;
        switch (uriType) {
            case URI_TYPE_CITY_DIRECTORY:
                tableName = WeatherDatabase.CITIES_TABLE;
                break;
            case URI_TYPE_CITY_WEATHER:
                tableName = WeatherDatabase.WEATHER_TABLE;
                values.put(WeatherDatabase.COLUMN_CITY_ID, uri.getLastPathSegment());
                break;
        }

        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        long rowId = writableDatabase.insert(tableName, null, values);
        return uri.buildUpon().appendPath("" + rowId).build();
    }

    @Override
    public boolean onCreate() {
        database = new WeatherDatabase(getContext(), null);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriType = uriMatcher.match(uri);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        if (uriType == URI_TYPE_CITY_DIRECTORY) {
            builder.setTables(WeatherDatabase.CITIES_TABLE);
        } else {
            builder.setTables(WeatherDatabase.WEATHER_TABLE);
        }

        switch (uriType) {
            case URI_TYPE_CITY_DIRECTORY:
                break;
            case URI_TYPE_CITY_WEATHER:
                builder.appendWhere(WeatherDatabase.COLUMN_CITY_ID + "=" + uri.getLastPathSegment());
                break;
        }
        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        Cursor cursor = builder.query(writableDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase writableDatabase = database.getWritableDatabase();
        return writableDatabase.update(WeatherDatabase.CITIES_TABLE, values, WeatherDatabase.COLUMN_URL + " = ?", new String[]{uri.getLastPathSegment()});
    }
}
