package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class WeatherContentProvider extends ContentProvider {
    private static final int CITIES = 0;
    private static final int CITY_ID = 1;
    private static final int WEATHERS = 2;
    private static final int WEATHER_ID = 3;

    private static final String AUTHORITY = "ru.ifmo.md.lesson8";
    private static final String PATH_CITIES = "cities";
    public static final Uri CONTENT_URI_CITIES = Uri.parse("content://" + AUTHORITY + "/" + PATH_CITIES);
    private static final String PATH_WEATHERS = "weathers";
    public static final Uri CONTENT_URI_WEATHERS = Uri.parse("content://" + AUTHORITY + "/" + PATH_WEATHERS);
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, PATH_CITIES, CITIES);
        URI_MATCHER.addURI(AUTHORITY, PATH_CITIES + "/#", CITY_ID);
        URI_MATCHER.addURI(AUTHORITY, PATH_WEATHERS, WEATHERS);
        URI_MATCHER.addURI(AUTHORITY, PATH_WEATHERS + "/#", WEATHER_ID);
    }

    static String DB_NAME = "weather.db";
    static int DB_VERSION = 1;
    private static String CITIES_TABLE = "cities";
    private static String WEATHERS_TABLE = "weathers";
    private FeedDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new FeedDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = URI_MATCHER.match(uri);
        switch (uriType) {
            case CITIES:
                queryBuilder.setTables(CITIES_TABLE);
                break;
            case CITY_ID:
                queryBuilder.setTables(CITIES_TABLE);
                queryBuilder.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case WEATHERS:
                queryBuilder.setTables(WEATHERS_TABLE);
                break;
            case WEATHER_ID:
                queryBuilder.setTables(WEATHERS_TABLE);
                queryBuilder.appendWhere("city_id=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case CITIES:
                id = sqlDB.insert(CITIES_TABLE, null, values);
                break;
            case WEATHERS:
                id = sqlDB.insert(WEATHERS_TABLE, null, values);
                break;
            case WEATHER_ID:
                values.put("city_id", uri.getLastPathSegment());
                id = sqlDB.insert(WEATHERS_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(uri, "" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case CITIES:
                rowsDeleted = sqlDB.delete(CITIES_TABLE, selection, selectionArgs);
                break;
            case CITY_ID:
                String fid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(CITIES_TABLE, "_id=" + fid, null);
                } else {
                    rowsDeleted = sqlDB.delete(CITIES_TABLE, "_id=" + fid + " and " + selection, selectionArgs);
                }
                break;
            case WEATHERS:
                rowsDeleted = sqlDB.delete(WEATHERS_TABLE, selection, selectionArgs);
                break;
            case WEATHER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(WEATHERS_TABLE, "city_id=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(WEATHERS_TABLE, "city_id=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case CITIES:
                rowsUpdated = db.update(CITIES_TABLE, values, selection, selectionArgs);
                break;
            case CITY_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(CITIES_TABLE, values, "_id=" + id, null);
                } else {
                    rowsUpdated = db.update(CITIES_TABLE, values, "_id=" + id + " and " + selection, selectionArgs);
                }
                break;
            case WEATHERS:
                rowsUpdated = db.update(WEATHERS_TABLE, values, selection, selectionArgs);
                break;
            case WEATHER_ID:
                String cityId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(WEATHERS_TABLE, values, "_id=" + cityId, null);
                } else {
                    rowsUpdated = db.update(WEATHERS_TABLE, values, "_id=" + cityId + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Bad URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private final class FeedDatabaseHelper extends SQLiteOpenHelper {
        public FeedDatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            db.execSQL("CREATE TABLE " + CITIES_TABLE +
                            " (_id INTEGER PRIMARY KEY AUTOINCREMENT" +
                            ", woeid INTEGER NOT NULL" +
                            ", name TEXT NOT NULL);"
            );
            db.execSQL("CREATE TABLE " + WEATHERS_TABLE +
                            " (_id INTEGER PRIMARY KEY AUTOINCREMENT" +
                            ", weekday TEXT NOT NULL" +
                            ", weather_type TEXT NOT NULL" +
                            ", weather_code INTEGER NOT NULL" +
                            ", temp INTEGER NOT NULL" +
                            ", city_id INTEGER REFERENCES cities(_id) ON DELETE CASCADE);"
            );
            db.execSQL("INSERT INTO " + CITIES_TABLE + "(woeid, name) VALUES(2122265, 'Moscow');");
            db.execSQL("INSERT INTO " + CITIES_TABLE + "(woeid, name) VALUES(2077746, 'Samara');");
            db.execSQL("INSERT INTO " + CITIES_TABLE + "(woeid, name) VALUES(44418, 'London');");
            db.execSQL("INSERT INTO " + CITIES_TABLE + "(woeid, name) VALUES(2122265, 'Tokyo');");
            db.execSQL("INSERT INTO " + CITIES_TABLE + "(woeid, name) VALUES(2122265, 'Kaliningrad');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i2) {
            db.execSQL("DROP TABLE IF EXISTS " + CITIES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + WEATHERS_TABLE);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int i, int i2) {
            db.execSQL("DROP TABLE IF EXISTS " + CITIES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + WEATHERS_TABLE);
            onCreate(db);
        }
    }
}
