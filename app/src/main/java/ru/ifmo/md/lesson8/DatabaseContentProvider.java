package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DatabaseContentProvider extends ContentProvider {
    public static final String AUTHORITY = "ru.ifmo.md.lesson8.weather";
    public static final String BASE_URI = "cities";
    public static final Uri URI_CITY_DIR = Uri.parse("content://" + AUTHORITY + "/" + BASE_URI);
    public static final int URI_TYPE_CITY_DIR = 1;
    public static final int URI_TYPE_WEATHER_DIR = 2;
    private static final String BAD_URI = "Invalid Uri";
    private static final String UNSUPPORTED_OPERATION = "Unsupported operation";
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, BASE_URI, URI_TYPE_CITY_DIR);
        uriMatcher.addURI(AUTHORITY, BASE_URI + "/#", URI_TYPE_WEATHER_DIR);
    }

    private DatabaseHelper databaseHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        String dTable;
        if (uriType == URI_TYPE_CITY_DIR) {
            dTable = CitiesTable.CITIES_TABLE;
        } else if (uriType == URI_TYPE_WEATHER_DIR) {
            dTable = WeatherTable.WEATHER_TABLE;
            selection = WeatherTable.CITY_ID + " = ?";
            selectionArgs = new String[]{uri.getLastPathSegment()};
        } else {
            throw new IllegalArgumentException(BAD_URI);
        }
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        return sqLiteDatabase.delete(dTable, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        String tableName;
        if (uriType == URI_TYPE_CITY_DIR) {
            tableName = CitiesTable.CITIES_TABLE;
        } else if (uriType == URI_TYPE_WEATHER_DIR) {
            tableName = WeatherTable.WEATHER_TABLE;
            values.put(WeatherTable.CITY_ID, uri.getLastPathSegment());
        } else {
            throw new IllegalArgumentException(BAD_URI);
        }
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        long rowId = sqLiteDatabase.insert(tableName, null, values);
        return uri.buildUpon().appendPath("" + rowId).build();

    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext(), null);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int uriType = uriMatcher.match(uri);
        if (uriType == URI_TYPE_WEATHER_DIR) {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(WeatherTable.WEATHER_TABLE);
            builder.appendWhere(WeatherTable.CITY_ID + "=" + uri.getLastPathSegment());
            SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
            Cursor cursor = builder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        } else if (uriType == URI_TYPE_CITY_DIR) {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(CitiesTable.CITIES_TABLE);
            SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
            Cursor cursor = builder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_OPERATION);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException(BAD_URI);
        } else if (uriType == URI_TYPE_WEATHER_DIR) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            return db.update(CitiesTable.CITIES_TABLE, values, CitiesTable.URL + " = ?", new String[]{uri.getLastPathSegment()});
        } else {
            throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
        }
    }
}
/*
package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DatabaseContentProvider extends ContentProvider {
    public static final String AUTHORITY = "ru.ifmo.md.lesson8.weather";
    public static final String BASE_URI = "cities";
    public static final Uri URI_CITY_DIR = Uri.parse("content://" + AUTHORITY + "/" + BASE_URI);
    public static final int URI_TYPE_CITY_DIR = 1;
    public static final int URI_TYPE_WEATHER_DIR = 2;
    private static final String BAD_URI = "Invalid Uri";
    private static final String UNSUPPORTED_OPERATION = "Unsupported operation";
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, BASE_URI, URI_TYPE_CITY_DIR);
        uriMatcher.addURI(AUTHORITY, BASE_URI + "/#", URI_TYPE_WEATHER_DIR);
    }

    private DatabaseHelper database;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException(BAD_URI);
        }

        String dTable;
        switch (uriType) {
            case URI_TYPE_WEATHER_DIR:
                dTable = WeatherTable.WEATHER_TABLE;
                selection = WeatherTable.CITY_ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            case URI_TYPE_CITY_DIR:
                dTable = CitiesTable.CITIES_TABLE;
                break;
            default:
                throw new IllegalArgumentException(BAD_URI);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        return db.delete(dTable, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException(BAD_URI);
        }

        String tableName = null;
        switch (uriType) {
            case URI_TYPE_CITY_DIR:
                tableName = CitiesTable.CITIES_TABLE;
                break;
            case URI_TYPE_WEATHER_DIR:
                tableName = WeatherTable.WEATHER_TABLE;
                values.put(WeatherTable.CITY_ID, uri.getLastPathSegment());
                break;
        }

        SQLiteDatabase db = database.getWritableDatabase();

        long rowid = db.insert(tableName, null, values);
        return uri.buildUpon().appendPath("" + rowid).build();

    }

    @Override
    public boolean onCreate() {
        database = new DatabaseHelper(getContext(), null);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException(UNSUPPORTED_OPERATION);
        }

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        if (uriType == URI_TYPE_CITY_DIR) {
            builder.setTables(CitiesTable.CITIES_TABLE);
        } else {
            builder.setTables(WeatherTable.WEATHER_TABLE);
        }

        switch (uriType) {
            case URI_TYPE_CITY_DIR:
                break; // select *
            case URI_TYPE_WEATHER_DIR:
                builder.appendWhere(WeatherTable.CITY_ID + "=" + uri.getLastPathSegment());
                break;
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cr = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cr.setNotificationUri(getContext().getContentResolver(), uri);

        return cr;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException(BAD_URI);
        }

        switch (uriType) {
            case URI_TYPE_WEATHER_DIR:
                break;
            case URI_TYPE_CITY_DIR:
            default:
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        return db.update(CitiesTable.CITIES_TABLE, values, CitiesTable.URL + " = ?", new String[]{uri.getLastPathSegment()});
    }
}*/