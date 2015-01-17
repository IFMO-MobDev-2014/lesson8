package ru.ifmo.md.lesson8.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by MSviridenkov on 30.11.2014.
 */
public class WeatherContentProvider extends ContentProvider {
    private static String AUTHORITY = "ru.ifmo.md.lesson8.db.WeatherContentProvider";

    public static final Uri CITY_CONTENT_URL = Uri.parse("content://" + AUTHORITY + "/cities");
    public static final Uri NOW_CONTENT_URL = Uri.parse("content://" + AUTHORITY + "/nows");
    public static final Uri FORECAST_CONTENT_URL = Uri.parse("content://" + AUTHORITY + "/forecasts");

    private WeatherDBHelper wDbHelper;

    private String getTableName(Uri uri) {
        return getTableName(uri.getLastPathSegment());
    }

    private String getTableName(String type) {
        String tableName;
        if(type.equals("cities")) {
            tableName = wDbHelper.TABLE_CITY;
        } else if(type.equals("nows")) {
            tableName = wDbHelper.TABLE_NOW;
        } else if(type.equals("forecasts")) {
            tableName = wDbHelper.TABLE_FORECAST;
        } else {
            throw new UnsupportedOperationException("Invalid data type");
        }
        return tableName;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = wDbHelper.getWritableDatabase();
        return db.delete(getTableName(uri), selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = wDbHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        long id = db.insert(tableName, null, values);
        return Uri.parse("content://" + AUTHORITY + "/" + tableName + "/" + Long.toString(id));
    }

    @Override
    public boolean onCreate() {
        wDbHelper = new WeatherDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = wDbHelper.getReadableDatabase();
        return db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = wDbHelper.getWritableDatabase();
        return db.update(getTableName(uri), values, selection, selectionArgs);
    }
}
