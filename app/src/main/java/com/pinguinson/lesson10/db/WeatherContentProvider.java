package com.pinguinson.lesson10.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.db.tables.ForecastsTable;

/**
 * Created by pinguinson.
 */
public class WeatherContentProvider extends ContentProvider {

    private static String AUTHORITY = "com.pinguinson.lesson10.db.WeatherContentProvider";

    public static final Uri CITIES_CONTENT_URL = Uri.parse("content://" + AUTHORITY + "/cities");
    public static final Uri FORECASTS_CONTENT_URL = Uri.parse("content://" + AUTHORITY + "/forecasts");

    private WeatherDBHelper mDbHelper;

    private String getTableName(Uri uri) {
        return getTableName(uri.getLastPathSegment());
    }

    private String getTableName(String type) {
        String tableName;
        switch (type) {
            case "cities":
                tableName = CitiesTable.TABLE_NAME;
                break;
            case "forecasts":
                tableName = ForecastsTable.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Invalid data type");
        }
        return tableName;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(getTableName(uri), selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        long id = db.insert(tableName, null, values);
        return Uri.parse("content://" + AUTHORITY + "/" + tableName + "/" + Long.toString(id));
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new WeatherDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        return db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.update(getTableName(uri), values, selection, selectionArgs);
    }
}
