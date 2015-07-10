package year2013.ifmo.catweather;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class WeatherContentProvider extends ContentProvider {

    public static final String JUST_WEATHER = "just_weather";
    public static final String WEATHER_TABLE_NAME = "weather";

    private static final int WEATHER = 1;
    private static final int WEATHER_ID = 2;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Weather.AUTHORITY, Weather.JustWeather.PATH, WEATHER);
        uriMatcher.addURI(Weather.AUTHORITY, Weather.JustWeather.PATH + "/#", WEATHER_ID);
    }

    private static class DbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = JUST_WEATHER + ".db";
        private static int DATABASE_VERSION = 11;

        private DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            createTables(sqLiteDatabase);
        }

        private void createTables(SQLiteDatabase sqLiteDatabase) {
            String qs = "CREATE TABLE " + WEATHER_TABLE_NAME + " ("
                    + Weather.JustWeather._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Weather.JustWeather.CITY_NAME + " TEXT, "
                    + Weather.JustWeather.TODAY_NAME + " TEXT, "
                    + Weather.JustWeather.FUTURE_NAME + " TEXT" + ");";
            sqLiteDatabase.execSQL(qs);
            insertFeeds(sqLiteDatabase);
        }

        private void insertFeeds(SQLiteDatabase sqLiteDatabase) {
            ContentValues cv = new ContentValues();
            cv.put(Weather.JustWeather.CITY_NAME, "Volgograd, RU");
            sqLiteDatabase.insert(WEATHER_TABLE_NAME, null, cv);
            cv.clear();
            cv.put(Weather.JustWeather.CITY_NAME, "Moscow, RU");
            sqLiteDatabase.insert(WEATHER_TABLE_NAME, null, cv);
            cv.clear();
            cv.put(Weather.JustWeather.CITY_NAME, "Saint-Petersburg, RU");
            sqLiteDatabase.insert(WEATHER_TABLE_NAME, null, cv);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WEATHER_TABLE_NAME + ";");
            createTables(sqLiteDatabase);
        }
    }

    public WeatherContentProvider() {
    }

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                return Weather.JustWeather.CONTENT_TYPE;
            case WEATHER_ID:
                return Weather.JustWeather.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown type: " + uri);
        }
    }

    private SQLiteDatabase getDb() {
        return dbHelper.getWritableDatabase();
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        int u = uriMatcher.match(uri);
        if (u != WEATHER) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = initialValues;
        } else {
            values = new ContentValues();
        }

        db = getDb();

        long rowID = db.insert(WEATHER_TABLE_NAME, null, values);
        if (rowID > 0) {
            Uri resultUri = ContentUris.withAppendedId(Weather.JustWeather.CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int affected;

        db = getDb();

        switch (uriMatcher.match(uri)) {
            case WEATHER:
                affected = db.delete(WEATHER_TABLE_NAME,
                        (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case WEATHER_ID:
                long weatherId = ContentUris.parseId(uri);
                affected = db.delete(WEATHER_TABLE_NAME,
                        Weather.JustWeather._ID + "=" + weatherId
                                + (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("unknown feed element: " +
                        uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = uriMatcher.match(uri);
        if (match == WEATHER_ID) {
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                selection = Weather.JustWeather._ID + " = " + id;
            } else {
                selection = selection + " AND " + Weather.JustWeather._ID + " = " + id;
            }
        } else if (match != WEATHER) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = getDb();
        Cursor cursor = db.query(WEATHER_TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),
                Weather.JustWeather.CONTENT_URI);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int affected;

        db = getDb();
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                affected = db.update(WEATHER_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case WEATHER_ID:
                String id = uri.getPathSegments().get(1);
                affected = db.update(WEATHER_TABLE_NAME, values,
                        Weather.JustWeather._ID + "=" + id
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return affected;
    }
}
