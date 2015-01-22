package ru.ifmo.md.lesson8;

import android.net.Uri;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;

/**
 * Created by Ilya on 22.01.2015.
 */
public class WeatherContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "ru.ifmo.md.lesson8.WeatherContentProvider";
    static final String URL_WEATHER = "content://" + PROVIDER_NAME + "/weather";
    static final String URL_CITIES = "content://" + PROVIDER_NAME + "/cities";
    static final Uri WEATHER_URI = Uri.parse(URL_WEATHER);
    static final Uri CITIES_URI = Uri.parse(URL_CITIES);

    static final int WEATHER = 1;
    static final int WEATHER_ID = 2;
    static final int CITIES = 3;
    static final int CITIES_ID = 4;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "weather", WEATHER);
        uriMatcher.addURI(PROVIDER_NAME, "weather/#", WEATHER_ID);
        uriMatcher.addURI(PROVIDER_NAME, "cities", CITIES);
        uriMatcher.addURI(PROVIDER_NAME, "cities/#", CITIES_ID);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        MyDatabase.DatabaseHelper dbHelper = new MyDatabase.DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = -1;
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                rowID = db.insert(MyDatabase.WEATHER_TABLE_NAME, "", values);
                break;
            case CITIES:
                rowID = db.insert(MyDatabase.CITIES_TABLE_NAME, "", values);
                break;
        }

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case WEATHER:
                qb.setTables(MyDatabase.WEATHER_TABLE_NAME);
                break;
            case WEATHER_ID:
                qb.setTables(MyDatabase.WEATHER_TABLE_NAME);
                qb.appendWhere( MyDatabase._ID + "=" + uri.getPathSegments().get(1));
                break;
            case CITIES:
                qb.setTables(MyDatabase.CITIES_TABLE_NAME);
                break;
            case CITIES_ID:
                qb.setTables(MyDatabase.CITIES_TABLE_NAME);
                qb.appendWhere( MyDatabase._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")){
            sortOrder = MyDatabase.NAME;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,
                null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)){
            case WEATHER:
                count = db.delete(MyDatabase.WEATHER_TABLE_NAME, selection, selectionArgs);
                break;
            case WEATHER_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(MyDatabase.WEATHER_TABLE_NAME, MyDatabase._ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            case CITIES:
                count = db.delete(MyDatabase.CITIES_TABLE_NAME, selection, selectionArgs);
                break;
            case CITIES_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(MyDatabase.CITIES_TABLE_NAME, MyDatabase._ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)){
            case WEATHER:
                count = db.update(MyDatabase.WEATHER_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case WEATHER_ID:
                count = db.update(MyDatabase.WEATHER_TABLE_NAME, values, MyDatabase._ID +
                        " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            case CITIES:
                count = db.update(MyDatabase.CITIES_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case CITIES_ID:
                count = db.update(MyDatabase.CITIES_TABLE_NAME, values, MyDatabase._ID +
                        " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}