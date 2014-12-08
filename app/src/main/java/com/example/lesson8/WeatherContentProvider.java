package com.example.lesson8;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;


public class WeatherContentProvider extends ContentProvider {

    private WeatherDB database;

    private static final int channelCount;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        channelCount = 10;
        uriMatcher.addURI("com.example.lesson8.weathers", "weather", channelCount);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) == channelCount) {
            if (database.mDB.delete(WeatherDB.DB_TABLE, WeatherDB.COLUMN_ID + "=" + (Long.parseLong(selection)), null) == 1) {
                getContext().getContentResolver().notifyChange(uri, null);
                return 1;
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long number = -1;
        if (uriMatcher.match(uri) == channelCount) {
            number = database.mDB.insert(WeatherDB.DB_TABLE, null, contentValues);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(uri.toString() + "/" + number);
    }

    @Override
    public boolean onCreate() {
        database = WeatherDB.getDataBase(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        if (uriMatcher.match(uri) == channelCount) {
            cursor = database.mDB.query(WeatherDB.DB_TABLE, new String[]{
                    WeatherDB.COLUMN_ID,
                    WeatherDB.COLUMN_CITY,
                    WeatherDB.COLUMN_TEMPRATURE,
                    WeatherDB.COLUMN_WEATHER,
                    WeatherDB.COLUMN_DATE + "0",
                    WeatherDB.COLUMN_TEMPRATURE + "0",
                    WeatherDB.COLUMN_WEATHER + "0",
                    WeatherDB.COLUMN_DATE + "1",
                    WeatherDB.COLUMN_TEMPRATURE + "1",
                    WeatherDB.COLUMN_WEATHER + "1",
                    WeatherDB.COLUMN_DATE + "2",
                    WeatherDB.COLUMN_TEMPRATURE + "2",
                    WeatherDB.COLUMN_WEATHER + "2"}
                    , null, null, null, null, null);
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) != channelCount)
            throw new UnsupportedOperationException();
        if (database.mDB.update(WeatherDB.DB_TABLE, contentValues, WeatherDB.COLUMN_ID + "=" + (Long.parseLong(selection)), null) == 1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return 1;
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
            return 0;
        }

    }
}
