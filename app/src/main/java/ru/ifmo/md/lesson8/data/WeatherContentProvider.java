package ru.ifmo.md.lesson8.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by mariashka on 11/28/14.
 */
public class WeatherContentProvider extends ContentProvider {

    static public final String WEATHER_TABLE = "weather";
    static public final String CITY_TABLE = "cities";

    static public final String WEATHER_ID = "_id";

    static public final String CITY_ID = "_id";

    static public final String AUTHORITY = "ru.ifmo.lesson8.provider.WeatherContentProvider";
    static public final String WEATHER_PATH = "weather";
    static public  final String CITY_PATH = "cities";

    public static final Uri WEATHER_URI = Uri.parse("content://ru.ifmo.lesson8.provider.WeatherContentProvider/weather");
    public static final Uri CITY_URI = Uri.parse("content://ru.ifmo.lesson8.provider.WeatherContentProvider/cities");

    public static final String FEED_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + WEATHER_PATH;

    public static final String FEED_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + WEATHER_PATH;

    public static final String SUB_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + CITY_PATH;

    public static final String SUB_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + CITY_PATH;


    static final int URI_WEATHER_ID = 1;
    static final int URI_CITY_ID = 2;
    static final int URI_WEATHER = 3;
    static final int URI_CITY = 4;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, WEATHER_PATH + "/#", URI_WEATHER_ID);
        uriMatcher.addURI(AUTHORITY, CITY_PATH + "/#", URI_CITY_ID);
        uriMatcher.addURI(AUTHORITY, WEATHER_PATH, URI_WEATHER);
        uriMatcher.addURI(AUTHORITY, CITY_PATH, URI_CITY);
    }

    WeatherDB dbHelper;
    SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new WeatherDB(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String id;
        db = dbHelper.getWritableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case URI_WEATHER:
                cursor = db.query(WEATHER_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case URI_CITY:
                cursor = db.query(CITY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case URI_WEATHER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = WEATHER_ID + " = " + id;
                } else {
                    selection = selection + " AND " + WEATHER_ID + " = " + id;
                }
                cursor = db.query(WEATHER_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case URI_CITY_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = CITY_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CITY_ID + " = " + id;
                }
                cursor = db.query(CITY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), WEATHER_URI);
        cursor.setNotificationUri(getContext().getContentResolver(), CITY_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_CITY:
                return SUB_CONTENT_TYPE;
            case URI_WEATHER:
                return FEED_CONTENT_TYPE;
            case URI_WEATHER_ID:
                return FEED_CONTENT_ITEM_TYPE;
            case URI_CITY_ID:
                return SUB_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = dbHelper.getWritableDatabase();
        Uri resultUri;
        long rowID;
        switch (uriMatcher.match(uri)) {
            case URI_WEATHER:
                rowID = db.insert(WEATHER_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(WEATHER_URI, rowID);
                break;

            case URI_CITY:
                rowID = db.insert(CITY_TABLE, null, values);
                resultUri = ContentUris.withAppendedId(CITY_URI, rowID);
                break;

            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String id;
        db = dbHelper.getWritableDatabase();
        int cnt = 0;

        switch (uriMatcher.match(uri)) {
            case URI_WEATHER:
                break;
            case URI_CITY:
                break;
            case URI_WEATHER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = WEATHER_ID + " = " + id;
                } else {
                    selection = selection + " AND " + WEATHER_ID + " = " + id;
                }
                cnt = db.delete(WEATHER_TABLE, selection, selectionArgs);
                break;
            case URI_CITY_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = CITY_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CITY_ID + " = " + id;
                }
                cnt = db.delete(CITY_TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int cnt = 0;
        String id;
        switch (uriMatcher.match(uri)) {
            case URI_WEATHER:
                break;
            case URI_CITY:
                break;
            case URI_WEATHER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = WEATHER_ID + " = " + id;
                } else {
                    selection = selection + " AND " + WEATHER_ID + " = " + id;
                }
                cnt = db.update(WEATHER_TABLE, values, selection, selectionArgs);
                break;
            case URI_CITY_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = CITY_ID + " = " + id;
                } else {
                    selection = selection + " AND " + CITY_ID + " = " + id;
                }
                cnt = db.update(CITY_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}
