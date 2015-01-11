package ru.ifmo.md.lesson8;

/**
 * Created by 107476 on 23.12.2014.
 */
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public class MyContentProvider extends ContentProvider {


    static final String DB_NAME = "mydb";
    static final int DB_VERSION = 5;

    private static final int CITIES = 0;
    private static final int CITIES_ID = 1;
    private static final int CURRENT_WEATHER = 2;
    private static final int CURRENT_WEATHER_ID = 3;
    private static final int FORECAST = 4;
    private static final int FORECAST_ID = 5;


    static final String CITIES_TABLE = "cities";
    static final String CURRENT_WEATHER_TABLE = "current_weather";
    static final String FORECAST_TABLE = "forecast";


    static final String CITY_ID ="id";
    static final String CITY_NAME = "name";
    static final String CITY_COUNTRY = "country";
    static final String WOEID = "woeid";

    static final String CURRENT_ID = "id";
    static final String CURRENT_CITY_ID = "city_id";
    static final String CURRENT_DATE = "date";
    static final String CURRENT_TEMP = "temp";
    static final String CURRENT_HUMIDITY = "humidity";
    static final String CURRENT_PRESSURE = "pressure";
    static final String CURRENT_WIND = "wind";
    static final String CURRENT_WEATHER_TYPE = "type";

    static final String FORECAST_DB_ID = "id";
    static final String FORECAST_CITY_ID = "city_id";
    static final String FORECAST_DATE = "date";
    static final String FORECAST_LOW_TEMP = "low";
    static final String FORECAST_HIGH_TEMP = "high";
    static final String FORECAST_WEATHER_TYPE = "type";


    static final String AUTHORITY = "ru.ifmo.md.lesson8";

    static final String CITIES_PATH = CITIES_TABLE;
    static final String CURRENT_WEATHER_PATH = CURRENT_WEATHER_TABLE;
    static final String FORECAST_PATH = FORECAST_TABLE;


    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        matcher.addURI(AUTHORITY, CITIES_PATH, CITIES);
        matcher.addURI(AUTHORITY, CURRENT_WEATHER_PATH, CURRENT_WEATHER);
        matcher.addURI(AUTHORITY, FORECAST_PATH, FORECAST);
         matcher.addURI(AUTHORITY, CITIES_PATH + "/#", CITIES_ID);
        matcher.addURI(AUTHORITY, CURRENT_WEATHER_PATH+"/#", CURRENT_WEATHER_ID);
        matcher.addURI(AUTHORITY, FORECAST_PATH+"/#",FORECAST_ID);
    }


    public static final Uri CITIES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + CITIES_PATH);

    public static final Uri CURRENT_WEATHER_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + CURRENT_WEATHER_PATH);

    public static final Uri CURRENT_FORECAST_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + FORECAST_PATH);

    static final String CITIES_TABLE_CREATE = "create table " + CITIES_TABLE + "("
            + CITY_ID + " integer primary key autoincrement, "
            + CITY_NAME + " text, " + CITY_COUNTRY + " text, " + WOEID + " text" + ");";

    static final String CURRENT_WEATHER_TABLE_CREATE = "create table " + CURRENT_WEATHER_TABLE + "("
            + CURRENT_ID + " integer primary key autoincrement, "
            + CURRENT_CITY_ID + " integer, "
            + CURRENT_DATE + " integer, " + CURRENT_TEMP + " integer, "
            + CURRENT_HUMIDITY + " integer, " + CURRENT_PRESSURE + " integer, " + CURRENT_WIND + " integer, "
            + CURRENT_WEATHER_TYPE + " text" +  ");";

    static  final String FORECAST_TABLE_CREATE = "create table " + FORECAST_TABLE + "("
            + FORECAST_DB_ID + " integer primary key autoincrement, "
            + FORECAST_CITY_ID + " integer, "
            + FORECAST_DATE + " integer, " + FORECAST_WEATHER_TYPE + " text, " + FORECAST_LOW_TEMP + " integer, "
            + FORECAST_HIGH_TEMP + " integer"  +  ");";





    DBHelper dbHelper;
    SQLiteDatabase db;

    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (matcher.match(uri)) {
            case CITIES:
                queryBuilder.setTables(CITIES_TABLE);
                break;
            case CITIES_ID:
                queryBuilder.setTables(CITIES_TABLE);
                queryBuilder.appendWhere("id=" + uri.getLastPathSegment());
                break;
            case CURRENT_WEATHER:
                queryBuilder.setTables(CURRENT_WEATHER_TABLE);
                break;
            case CURRENT_WEATHER_ID:
                queryBuilder.setTables(CURRENT_WEATHER_TABLE);
                queryBuilder.appendWhere("id=" + uri.getLastPathSegment());
                break;
            case FORECAST:
                queryBuilder.setTables(FORECAST_TABLE);
                break;
            case FORECAST_ID:
                queryBuilder.setTables(FORECAST_TABLE);
                queryBuilder.appendWhere("id=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),
                uri);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        db = dbHelper.getWritableDatabase();
        long rowID;
        switch (matcher.match(uri)) {
            case CITIES:
                rowID = db.insert(CITIES_TABLE, null, values);
                break;
            case CURRENT_WEATHER:
                rowID = db.insert(CURRENT_WEATHER_TABLE, null, values);
                break;
            case FORECAST:
                rowID = db.insert(FORECAST_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        Uri resultUri = Uri.withAppendedPath(uri, ""+rowID);
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int deleted = 0;
        String id;
        switch (matcher.match(uri)) {
            case CITIES:
                deleted = db.delete(CITIES_TABLE, selection, selectionArgs);
                break;
            case CITIES_ID:
                id = uri.getLastPathSegment();
                deleted = db.delete(CITIES_TABLE, CITY_ID + "=" + id, null);
                break;
            case CURRENT_WEATHER_ID:
                id = uri.getLastPathSegment();
                deleted = db.delete(CURRENT_WEATHER_TABLE, CURRENT_ID + "=" + id, null);
                break;
            case CURRENT_WEATHER:
                deleted = db.delete(CURRENT_WEATHER_TABLE, selection, selectionArgs);
                break;
            case FORECAST:
                deleted = db.delete(FORECAST_TABLE, selection, selectionArgs);
                break;
            case FORECAST_ID:
                id = uri.getLastPathSegment();
                deleted = db.delete(FORECAST_TABLE, FORECAST_DB_ID + "=" + id, null);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int updated = 0;
        String id;
        switch (matcher.match(uri)) {
            case CITIES:
                updated = db.update(CITIES_TABLE, values, selection, selectionArgs);
                break;
            case CITIES_ID:
                id = uri.getLastPathSegment();
                updated = db.update(CITIES_TABLE, values, CITY_ID + "=" + id, null);
                break;
            case CURRENT_WEATHER:
                updated = db.update(CURRENT_WEATHER_TABLE, values, selection, selectionArgs);
                break;
            case CURRENT_WEATHER_ID:
                id = uri.getLastPathSegment();
                updated =db.update(CURRENT_WEATHER_TABLE, values, CURRENT_ID + "=" + id, null);
                break;
            case FORECAST:
                updated = db.update(FORECAST_TABLE, values, selection, selectionArgs);
                break;
            case FORECAST_ID:
                id = uri.getLastPathSegment();
                updated = db.update(FORECAST_TABLE, values, FORECAST_DB_ID + "=" + id, null);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return updated;
    }

    public String getType(Uri uri) {
        return Integer.toString(matcher.match(uri));
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            db.execSQL(CITIES_TABLE_CREATE);
            db.execSQL(CURRENT_WEATHER_TABLE_CREATE);
            db.execSQL(FORECAST_TABLE_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists cities");
            db.execSQL("drop table if exists current_weather");
            db.execSQL("drop table if exists forecast");
            onCreate(db);
        }
    }
}