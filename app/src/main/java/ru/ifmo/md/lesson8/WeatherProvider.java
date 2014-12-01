package ru.ifmo.md.lesson8;

import android.net.Uri;
import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;

public class WeatherProvider extends ContentProvider {

    static final String PROVIDER_NAME = "ru.ifmo.md.lesson8.WeatherProvider";
    static final String URL_WEATHER = "content://" + PROVIDER_NAME + "/weather";
    static final String URL_CITIES = "content://" + PROVIDER_NAME + "/cities";
    static final Uri WEATHER_URI = Uri.parse(URL_WEATHER);
    static final Uri CITIES_URI = Uri.parse(URL_CITIES);

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String ZMW = "zwm";
    static final String ICON = "icon_raw";
    static final String YEAR = "year";
    static final String YDAY = "year_day";
    static final String TXT = "text";
    static final String WDAY = "week_day";

    private static HashMap<String, String> WEATHER_PROJECTION_MAP, CITIES_PROJECTION_MAP;

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
    static final String DATABASE_NAME = "Weather";
    static final String WEATHER_TABLE_NAME = "weather";
    static final String CITIES_TABLE_NAME = "cities";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_CITIES_TABLE =
            " CREATE TABLE " + CITIES_TABLE_NAME+
                    " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ZMW + " TEXT NOT NULL, " +
                    NAME + " TEXT NOT NULL);";
    static final String CREATE_WEATHER_TABLE =
            " CREATE TABLE " + WEATHER_TABLE_NAME +
                    " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ZMW + " TEXT NOT NULL, " +
                    NAME + " TEXT NOT NULL, " +
                    ICON + " BLOB, " +
                    YEAR + " INTEGER," +
                    YDAY + " INTEGER," +
                    WDAY + " INTEGER," +
                    TXT + " TEXT);";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CITIES_TABLE);
            db.execSQL(CREATE_WEATHER_TABLE);
            // default cities
            db.execSQL("INSERT INTO " + CITIES_TABLE_NAME + " (" + ZMW + ", " + NAME + ")" +
                                        "VALUES ('00000.1.26063', 'Saint Petersburg, Russia')");
            db.execSQL("INSERT INTO " + CITIES_TABLE_NAME + " (" + ZMW + ", " + NAME + ")" +
                                        "VALUES ('00000.1.27612', 'Moscow, Russia')");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + WEATHER_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CITIES_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = -1;
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                rowID = db.insert(WEATHER_TABLE_NAME, "", values);
                break;
            case CITIES:
                rowID = db.insert(CITIES_TABLE_NAME, "", values);
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
                qb.setTables(WEATHER_TABLE_NAME);
                qb.setProjectionMap(WEATHER_PROJECTION_MAP);
                break;
            case WEATHER_ID:
                qb.setTables(WEATHER_TABLE_NAME);
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;
            case CITIES:
                qb.setTables(CITIES_TABLE_NAME);
                qb.setProjectionMap(CITIES_PROJECTION_MAP);
                break;
            case CITIES_ID:
                qb.setTables(CITIES_TABLE_NAME);
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")){
            sortOrder = NAME;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,
                null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case WEATHER:
                count = db.delete(WEATHER_TABLE_NAME, selection, selectionArgs);
                break;
            case WEATHER_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(WEATHER_TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            case CITIES:
                count = db.delete(CITIES_TABLE_NAME, selection, selectionArgs);
                break;
            case CITIES_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(CITIES_TABLE_NAME, _ID +  " = " + id +
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
        int count = 0;

        switch (uriMatcher.match(uri)){
            case WEATHER:
                count = db.update(WEATHER_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case WEATHER_ID:
                count = db.update(WEATHER_TABLE_NAME, values, _ID +
                        " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            case CITIES:
                count = db.update(CITIES_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case CITIES_ID:
                count = db.update(CITIES_TABLE_NAME, values, _ID +
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