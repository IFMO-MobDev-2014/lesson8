package ru.ifmo.md.lesson8.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by sergey on 09.11.14.
 */
public class WeatherProvider extends ContentProvider {

    private WeatherDatabase weatherDatabase;

    private static final int CITIES = 10;
    private static final int CITIES_ID = 20;

    private static final String CONTENT_AUTHORITY = "ru.ifmo.md.lesson8.weather";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_WEATHER = "cities";

    public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + PATH_WEATHER);

    public static Uri buildCityUri(String cityId) {
        return CONTENT_URI.buildUpon().appendPath(cityId).build();
    }

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER, CITIES);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_WEATHER + "/#", CITIES_ID);
    }

    @Override
    public boolean onCreate() {
        weatherDatabase = new WeatherDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = uriMatcher.match(uri);
        queryBuilder.setTables(WeatherTable.TABLE_NAME);
        switch (uriType) {
            case CITIES:
                break;
            case CITIES_ID:
                queryBuilder.appendWhere(WeatherTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        SQLiteDatabase db = weatherDatabase.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = weatherDatabase.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case CITIES:
                id = db.insert(WeatherTable.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, String.valueOf(id));
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = weatherDatabase.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case CITIES:
                rowsDeleted = db.delete(WeatherTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CITIES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(WeatherTable.TABLE_NAME, WeatherTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(WeatherTable.TABLE_NAME, WeatherTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase db = weatherDatabase.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case CITIES:
                rowsUpdated = db.update(WeatherTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CITIES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(WeatherTable.TABLE_NAME, values, WeatherTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(WeatherTable.TABLE_NAME, values, WeatherTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}