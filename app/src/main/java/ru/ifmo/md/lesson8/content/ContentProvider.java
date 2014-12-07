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
                Cities.PATH,
                Cities.LIST_ID);
        URI_MATCHER.addURI(AUTHORITY,
                Cities.PATH + "/#",
                Cities.ITEM_ID);
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
            case Cities.LIST_ID:
                cursor = db.query(Cities.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), Cities.URI);
                break;
            case Cities.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += "_id = " + uri.getLastPathSegment();
                cursor = db.query(Cities.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), Cities.URI);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case Cities.LIST_ID:
                return Cities.DIR_CONTENT_TYPE;
            case Cities.ITEM_ID:
                return Cities.ITEM_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        switch (URI_MATCHER.match(uri)) {
            case Cities.LIST_ID:
                id = db.insert(Cities.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(Cities.PATH + "/" + id);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (URI_MATCHER.match(uri)) {
            case Cities.LIST_ID:
                rowsDeleted = db.delete(Cities.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case Cities.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += "_id =" + uri.getLastPathSegment();
                rowsDeleted = db.delete(Cities.TABLE_NAME, selection, null);
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
            case Cities.LIST_ID:
                rowsUpdated = db.update(Cities.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case Cities.ITEM_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += " and ";
                }
                selection += "_id =" + uri.getLastPathSegment();
                rowsUpdated = db.update(Cities.TABLE_NAME,
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
