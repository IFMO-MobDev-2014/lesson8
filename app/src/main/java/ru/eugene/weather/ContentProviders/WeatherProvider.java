package ru.eugene.weather.ContentProviders;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import ru.eugene.weather.WeatherInfo;
import ru.eugene.weather.database.CityDataSource;
import ru.eugene.weather.database.CityItem;
import ru.eugene.weather.database.DBHelper;
import ru.eugene.weather.database.WeatherInfoDataSource;
import ru.eugene.weather.database.WeatherItem;

/**
 * Created by eugene on 12/16/14.
 */
public class WeatherProvider extends ContentProvider {
    private static final String AUTHORITY = "ru.eugene.weather.ContentProviders";

    // ------- define some Uris
    private static final String PATH_CITY = "city";
    private static final String PATH_WEATHER_INFO = "weather_info";

    public static final Uri CONTENT_URI_CITY = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_CITY);

    public static final Uri CONTENT_URI_WEATHER_INFO = Uri.parse("content://" + AUTHORITY
            + "/" + PATH_WEATHER_INFO);

    public static final String CITY_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd." + AUTHORITY + "." + PATH_CITY;

    public static final String CITY_ONE_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd." + AUTHORITY + "." + PATH_CITY;

    public static final String WEATHER_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd." + AUTHORITY + "." + PATH_WEATHER_INFO;

    public static final String WEATHER_ONE_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd." + AUTHORITY + "." + PATH_WEATHER_INFO;


    // ------- setup UriMatcher
    private static final int CITY = 10;
    private static final int CITY_ONE = 20;
    private static final int WEATHER_INFO = 30;
    private static final int WEATHER_INFO_ONE = 40;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private DBHelper weatherDB;

    static {
        sURIMatcher.addURI(AUTHORITY, PATH_CITY, CITY);
        sURIMatcher.addURI(AUTHORITY, PATH_CITY + "/#", CITY_ONE);
        sURIMatcher.addURI(AUTHORITY, PATH_WEATHER_INFO, WEATHER_INFO);
        sURIMatcher.addURI(AUTHORITY, PATH_WEATHER_INFO + "/#", WEATHER_INFO_ONE);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        weatherDB = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = sURIMatcher.match(uri);
        if (match == CITY_ONE || match == WEATHER_INFO_ONE) {
            String id = uri.getLastPathSegment();
            selection = selection == null ? "_id=" + id : selection + " and _id=" + id;
        }

        db = weatherDB.getReadableDatabase();
        Cursor result = db.query(getTableName(match, uri), projection,
                selection, selectionArgs, null, null, sortOrder);

        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    private String getTableName(int match, Uri uri) {
        switch (match) {
            case CITY:
            case CITY_ONE:
                return CityDataSource.TABLE_NAME;
            case WEATHER_INFO:
            case WEATHER_INFO_ONE:
                return WeatherInfoDataSource.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case CITY:
                return CITY_MIME_TYPE;
            case CITY_ONE:
                return CITY_ONE_MIME_TYPE;
            case WEATHER_INFO:
                return WEATHER_MIME_TYPE;
            case WEATHER_INFO_ONE:
                return WEATHER_ONE_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = weatherDB.getWritableDatabase();
        String table = "";
        switch (sURIMatcher.match(uri)) {
            case CITY:
                table = CityDataSource.TABLE_NAME;
                break;
            case WEATHER_INFO:
                table = WeatherInfoDataSource.TABLE_NAME;
                break;
        }

        long id = db.insert(table, null, values);
        if (id > 0) {
            uri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = weatherDB.getWritableDatabase();
        String table = "";
        switch (sURIMatcher.match(uri)) {
            case CITY:
                table = CityDataSource.TABLE_NAME;
                break;
            case WEATHER_INFO:
                table = WeatherInfoDataSource.TABLE_NAME;
                break;
        }
        int cnt = 0;
        for (ContentValues value : values) {
            long id = db.insert(table, null, value);
            if (id > 0) {
                cnt++;
                uri = ContentUris.withAppendedId(uri, id);
            }
        }
        if (cnt > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        db.close();
        return cnt;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = weatherDB.getWritableDatabase();
        int id = 0;
        switch (sURIMatcher.match(uri)) {
            case CITY:
                id = db.delete(CityDataSource.TABLE_NAME, selection, selectionArgs);
                break;
            case WEATHER_INFO:
                id = db.delete(WeatherInfoDataSource.TABLE_NAME, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = weatherDB.getWritableDatabase();
        int id = 0;
        switch (sURIMatcher.match(uri)) {
            case CITY:
                id = db.update(CityDataSource.TABLE_NAME, values, selection, selectionArgs);
                break;
            case WEATHER_INFO:
                id = db.update(WeatherInfoDataSource.TABLE_NAME, values, selection, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return id;
    }

    public static ContentValues generateContentValuesFromWeatherItem(WeatherItem weatherItem) {
        ContentValues result = new ContentValues();

        result.put(WeatherInfoDataSource.COLUMN_ID_CITY, weatherItem.getIdCity());
        result.put(WeatherInfoDataSource.COLUMN_TEMP_CUR, weatherItem.getTemp());
        result.put(WeatherInfoDataSource.COLUMN_TEMP_MIN, weatherItem.getTempMin());
        result.put(WeatherInfoDataSource.COLUMN_TEMP_MAX, weatherItem.getTempMax());
        result.put(WeatherInfoDataSource.COLUMN_SPEED, weatherItem.getSpeed());
        result.put(WeatherInfoDataSource.COLUMN_HUMIDITY, weatherItem.getHumidity());
        result.put(WeatherInfoDataSource.COLUMN_VISIBILITY, weatherItem.getVisibility());
        result.put(WeatherInfoDataSource.COLUMN_CODE, weatherItem.getCode());
        result.put(WeatherInfoDataSource.COLUMN_PUB_DATE, weatherItem.getPubDate());
        result.put(WeatherInfoDataSource.COLUMN_TEXT, weatherItem.getText());
        result.put(WeatherInfoDataSource.COLUMN_CHILL, weatherItem.getChill());

        return result;
    }

    public static ContentValues generateContentValuesFromCityItem(CityItem cityItem) {
        ContentValues result = new ContentValues();

        result.put(CityDataSource.COLUMN_CITY, cityItem.getCity());
        result.put(CityDataSource.COLUMN_LINK, cityItem.getLink());

        return result;
    }
}
