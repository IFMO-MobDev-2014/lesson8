package ru.ifmo.md.lesson8.content;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import static ru.ifmo.md.lesson8.content.WeatherContract.*;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class ContentProvider extends android.content.ContentProvider {
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY,
                Places.PATH,
                Places.LIST_ID);
        URI_MATCHER.addURI(AUTHORITY,
                Places.PATH + "/#",
                Places.ITEM_ID);
        URI_MATCHER.addURI(AUTHORITY,
                WeatherInfo.PATH,
                WeatherInfo.LIST_ID);
        URI_MATCHER.addURI(AUTHORITY,
                WeatherInfo.PATH + "/#",
                WeatherInfo.ITEM_ID);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (URI_MATCHER.match(uri)) {
            case Places.LIST_ID:
                cursor = db.query(Places.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), Places.URI);
                break;
            case Places.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += Places._ID + " = " + uri.getLastPathSegment();
                cursor = db.query(Places.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), Places.URI);
                break;
            case WeatherInfo.LIST_ID:
                cursor = db.query(WeatherInfo.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), WeatherInfo.URI);
                break;
            case WeatherInfo.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += WeatherInfo._ID + " = " + uri.getLastPathSegment();
                cursor = db.query(WeatherInfo.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), WeatherInfo.URI);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case Places.LIST_ID:
                return Places.DIR_CONTENT_TYPE;
            case Places.ITEM_ID:
                return Places.ITEM_CONTENT_TYPE;
            case WeatherInfo.LIST_ID:
                return WeatherInfo.DIR_CONTENT_TYPE;
            case WeatherInfo.ITEM_ID:
                return WeatherInfo.ITEM_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        switch (URI_MATCHER.match(uri)) {
            case Places.LIST_ID:
                id = db.insert(Places.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(Places.PATH + "/" + id);
            case WeatherInfo.LIST_ID:
                id = db.insert(WeatherInfo.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(WeatherInfo.PATH + "/" + id);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (URI_MATCHER.match(uri)) {
            case Places.LIST_ID:
                rowsDeleted = db.delete(Places.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case Places.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += "_id = " + uri.getLastPathSegment();
                rowsDeleted = db.delete(Places.TABLE_NAME, selection, null);
                break;
            case WeatherInfo.LIST_ID:
                rowsDeleted = db.delete(WeatherInfo.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case WeatherInfo.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += "_id = " + uri.getLastPathSegment();
                rowsDeleted = db.delete(WeatherInfo.TABLE_NAME, selection, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (URI_MATCHER.match(uri)) {
            case Places.LIST_ID:
                rowsUpdated = db.update(Places.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case Places.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += Places._ID + " = " + uri.getLastPathSegment();
                rowsUpdated = db.update(Places.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case WeatherInfo.LIST_ID:
                rowsUpdated = db.update(WeatherInfo.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case WeatherInfo.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += WeatherInfo._ID + " = " + uri.getLastPathSegment();
                rowsUpdated = db.update(WeatherInfo.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
