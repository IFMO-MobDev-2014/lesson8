package ru.ifmo.md.weather.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import ru.ifmo.md.weather.db.model.CityTable;
import ru.ifmo.md.weather.db.model.Settings;
import ru.ifmo.md.weather.db.model.SettingsTable;
import ru.ifmo.md.weather.db.model.Weather;
import ru.ifmo.md.weather.db.model.WeatherTable;

/**
 * Created by Kirill on 01.12.2014.
 */
public class WeatherContentProvider extends ContentProvider {

    private static final String CITIES_TABLE = CityTable.TABLE_NAME;
    private static final String WEATHER_TABLE = WeatherTable.TABLE_NAME;
    private static final String SETTINGS_TABLE = SettingsTable.TABLE_NAME;

    private DBHelper helper;

    private static final int SINGLE_CITY = 1;
    private static final int SINGLE_WEATHER = 2;
    private static final int ALL_WEATHER = 3;
    private static final int CITIES = 4;
    private static final int SETTINGS = 5;
    
    private static final String AUTHORITY = "ru.ifmo.md.weather";
    private static final String PATH_CITIES = "Cities";
    private static final String PATH_WEATHER = "Weather";
    private static final String PATH_SETTINGS = "Settings";
    public static final Uri CONTENT_URI_WEATHER =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_WEATHER);
    public static final Uri CONTENT_URI_CITIES =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_CITIES);
    public static final Uri CONTENT_URI_SETTINGS =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_SETTINGS);

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, PATH_WEATHER, ALL_WEATHER);
        uriMatcher.addURI(AUTHORITY, PATH_CITIES, CITIES);
        uriMatcher.addURI(AUTHORITY, PATH_CITIES + "/#", SINGLE_CITY);
        uriMatcher.addURI(AUTHORITY, PATH_WEATHER + "/#", SINGLE_WEATHER);
        uriMatcher.addURI(AUTHORITY, PATH_SETTINGS + "/#", SETTINGS);
    }
    public boolean onCreate() {
        helper = new DBHelper(getContext());
        return true;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case ALL_WEATHER:
                queryBuilder.setTables(WEATHER_TABLE);
                break;
            case CITIES:
                queryBuilder.setTables(CITIES_TABLE);
                break;
            case SINGLE_CITY:
                queryBuilder.setTables(CITIES_TABLE);
                queryBuilder.appendWhere(CityTable._ID + "=" + uri.getLastPathSegment());
                break;
            case SINGLE_WEATHER:
                queryBuilder.setTables(WEATHER_TABLE);
                queryBuilder.appendWhere(WeatherTable._ID + "=" + uri.getLastPathSegment());
                break;
            case SETTINGS:
                queryBuilder.setTables(SETTINGS_TABLE);
                queryBuilder.appendWhere(SettingsTable._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id;
        switch (uriMatcher.match(uri)) {
            case CITIES:
                id = db.insert(CityTable.TABLE_NAME, null, contentValues);
                break;
            case SINGLE_CITY:
                contentValues.put(CityTable._ID, uri.getLastPathSegment());
                id = db.insert(CityTable.TABLE_NAME, null, contentValues);
                break;
            case ALL_WEATHER:
                id = db.insert(WeatherTable.TABLE_NAME, null, contentValues);
                break;
            case SINGLE_WEATHER:
                contentValues.put(WeatherTable._ID, uri.getLastPathSegment());
                id = db.insert(WeatherTable.TABLE_NAME, null, contentValues);
                break;
            case SETTINGS:
                contentValues.put(SettingsTable._ID, uri.getLastPathSegment());
                id = db.insert(SettingsTable.TABLE_NAME, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, String.valueOf(id));
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int removed;
        String id = "";
        switch (uriMatcher.match(uri)) {
            case ALL_WEATHER:
                removed = db.delete(WeatherTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CITIES:
                removed = db.delete(CityTable.TABLE_NAME, selection, selectionArgs);
                break;
            case SINGLE_CITY:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    removed = db.delete(CityTable.TABLE_NAME, CityTable._ID + "=" + id, selectionArgs);
                } else {
                    removed = db.delete(CityTable.TABLE_NAME, CityTable._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case SINGLE_WEATHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    removed = db.delete(WeatherTable.TABLE_NAME, WeatherTable._ID + "=" + id, selectionArgs);
                } else {
                    removed = db.delete(WeatherTable.TABLE_NAME, WeatherTable._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case SETTINGS:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    removed = db.delete(SettingsTable.TABLE_NAME, SettingsTable._ID + "=" + id, selectionArgs);
                } else {
                    removed = db.delete(SettingsTable.TABLE_NAME, SettingsTable._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return removed;
    }
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();

        /*Cursor ti = db.rawQuery("PRAGMA table_info(" + CityTable.TABLE_NAME + ")", null);
        if ( ti.moveToFirst() ) {
            do {
                System.out.println("col: " + ti.getString(1));
            } while (ti.moveToNext());
        }*/
        int updated;
        String id = "";
        switch (uriMatcher.match(uri)) {
            case CITIES:
                updated = db.update(CityTable.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case SINGLE_CITY:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    updated = db.update(CityTable.TABLE_NAME, contentValues, CityTable._ID + "=" + id, selectionArgs);
                } else {
                    updated = db.update(CityTable.TABLE_NAME, contentValues, CityTable._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case SINGLE_WEATHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    updated = db.update(WeatherTable.TABLE_NAME, contentValues, WeatherTable._ID + "=" + id, selectionArgs);
                } else {
                    updated = db.update(WeatherTable.TABLE_NAME, contentValues, WeatherTable._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case SETTINGS:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    updated = db.update(SettingsTable.TABLE_NAME, contentValues, SettingsTable._ID + "=" + id, selectionArgs);
                } else {
                    updated = db.update(SettingsTable.TABLE_NAME, contentValues, SettingsTable._ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return updated;
    }

}