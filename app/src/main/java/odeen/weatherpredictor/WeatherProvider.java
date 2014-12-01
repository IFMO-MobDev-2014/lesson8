package odeen.weatherpredictor;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Женя on 24.11.2014.
 */
public class WeatherProvider extends ContentProvider {

    private static final String TAG = "WeatherProvider";

    //----------------
    //DB constants
    //----------------
    private static final String DB_NAME = "weatherpredictor.sqlite";
    private static final int VERSION = 1;

    public static final String TABLE_WEATHER = "weather";
    public static final String COLUMN_WEATHER_ID = "_id";
    public static final String COLUMN_WEATHER_LOCATION_ID = "location_id";
    public static final String COLUMN_WEATHER_CODE = "code";
    public static final String COLUMN_WEATHER_NAME = "main_name";
    public static final String COLUMN_WEATHER_DESCRIPTION = "description";
    public static final String COLUMN_WEATHER_ICON_ID = "icon_id";
    public static final String COLUMN_WEATHER_TEMP = "temp";
    public static final String COLUMN_WEATHER_MIN_TEMP = "min_temp";
    public static final String COLUMN_WEATHER_MAX_TEMP = "max_temp";
    public static final String COLUMN_WEATHER_PRESSURE = "pressure";
    public static final String COLUMN_WEATHER_HUMIDITY = "humidity";
    public static final String COLUMN_WEATHER_CLOUDS = "clouds";
    public static final String COLUMN_WEATHER_WIND_SPEED = "wind_speed";
    public static final String COLUMN_WEATHER_WIND_DIRECTION = "wind_direction";
    public static final String COLUMN_WEATHER_TIME = "time";

    public static final String TABLE_LOCATION = "location";
    public static final String COLUMN_LOCATION_ID = "_id";
    public static final String COLUMN_LOCATION_NAME = "name";
    public static final String COLUMN_LOCATION_COLOR = "color";


    //----------------
    //Uri constants
    //----------------
    private static final String AUTHORITY = "odeen.weatherpredictor.providers.weather_provider";
    public static final Uri WEATHER_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_WEATHER);
    public static final Uri LOCATION_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + TABLE_LOCATION);

    private static final int URI_LOCATION = 1;
    private static final int URI_LOCATION_ID = 2;
    private static final int URI_WEATHER = 3;
    private static final int URI_WEATHER_ID = 4;
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_LOCATION, URI_LOCATION);
        mUriMatcher.addURI(AUTHORITY, TABLE_LOCATION + "/#", URI_LOCATION_ID);
        mUriMatcher.addURI(AUTHORITY, TABLE_WEATHER, URI_WEATHER);
        mUriMatcher.addURI(AUTHORITY, TABLE_WEATHER + "/#", URI_WEATHER_ID);
    }

    private DatabaseHelper mHelper;




    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String table = null;
        Uri contentUri = null;
        switch (mUriMatcher.match(uri)) {
            case URI_LOCATION:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COLUMN_LOCATION_ID + " asc";
                }
                table = TABLE_LOCATION;
                contentUri = LOCATION_CONTENT_URI;
                break;
            case URI_LOCATION_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_LOCATION_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_LOCATION_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_LOCATION;
                contentUri = LOCATION_CONTENT_URI;
                break;
            case URI_WEATHER:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COLUMN_WEATHER_TIME + " desc";
                }
                table = TABLE_WEATHER;
                contentUri = WEATHER_CONTENT_URI;
                break;
            case URI_WEATHER_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_WEATHER_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_WEATHER_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_WEATHER;
                contentUri = WEATHER_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        Cursor cursor = mHelper.getReadableDatabase().query(table, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), contentUri);
        Log.d(TAG, contentUri.toString());
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String table = null;
        Uri content = null;
        switch (mUriMatcher.match(uri)) {
            case URI_LOCATION:
                table = TABLE_LOCATION;
                content = LOCATION_CONTENT_URI;
                break;
            case URI_WEATHER:
                table = TABLE_WEATHER;
                content = WEATHER_CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        long id = mHelper.getWritableDatabase().insert(table, null, contentValues);
        Uri resultUri = ContentUris.withAppendedId(content, id);
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_LOCATION:
                table = TABLE_LOCATION;
                break;
            case URI_LOCATION_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_LOCATION_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_LOCATION_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_LOCATION;
                break;
            case URI_WEATHER:
                table = TABLE_WEATHER;
                break;
            case URI_WEATHER_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_WEATHER_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_WEATHER_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_WEATHER;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = mHelper.getWritableDatabase().delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String table = null;
        switch (mUriMatcher.match(uri)) {
            case URI_WEATHER:
                table = TABLE_WEATHER;
                break;
            case URI_WEATHER_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_WEATHER_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_WEATHER_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_WEATHER;
                break;
            case URI_LOCATION:
                table = TABLE_LOCATION;
                break;
            case URI_LOCATION_ID:
                if (TextUtils.isEmpty(selection)) {
                    selection = COLUMN_LOCATION_ID + " = " + uri.getLastPathSegment();
                } else {
                    selection = selection + " and " + COLUMN_LOCATION_ID + " = " + uri.getLastPathSegment();
                }
                table = TABLE_LOCATION;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = mHelper.getWritableDatabase().update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table weather (" +
                            "_id integer primary key autoincrement, " +
                            "location_id integer, " +
                            "code integer default -1, " +
                            "main_name string, " +
                            "description string, " +
                            "icon_id string," +
                            "temp double, " +
                            "min_temp double, " +
                            "max_temp double, " +
                            "pressure double, " +
                            "humidity double, " +
                            "clouds double, " +
                            "wind_speed double, " +
                            "wind_direction double, " +
                            "time long" +
                            ")"
            );
            db.execSQL("create table location (" +
                            "_id integer primary key autoincrement, " +
                            "name string, " +
                            "color integer" +
                            ")"

            );
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_LOCATION_NAME, "Saint Petersburg");
            cv.put(COLUMN_LOCATION_COLOR, Color.argb(255, 255, 221, 110));
            db.insert("location", null, cv);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {}
    }

    public static class WeatherCursor extends CursorWrapper {
        public WeatherCursor(Cursor cursor) {
            super(cursor);
        }
        public Weather getWeather() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Weather res = new Weather();
            res.setMainName(getString(getColumnIndex(COLUMN_WEATHER_NAME)));
            res.setIconId(getString(getColumnIndex(COLUMN_WEATHER_ICON_ID)));
            res.setDescription(getString(getColumnIndex(COLUMN_WEATHER_DESCRIPTION)));
            res.setHumidity(getDouble(getColumnIndex(COLUMN_WEATHER_HUMIDITY)));
            res.setTemperature(getDouble(getColumnIndex(COLUMN_WEATHER_TEMP)));
            res.setMinTemperature(getDouble(getColumnIndex(COLUMN_WEATHER_MIN_TEMP)));
            res.setMaxTemperature(getDouble(getColumnIndex(COLUMN_WEATHER_MAX_TEMP)));
            res.setClouds(getDouble(getColumnIndex(COLUMN_WEATHER_CLOUDS)));
            res.setPressure(getDouble(getColumnIndex(COLUMN_WEATHER_PRESSURE)));
            res.setWindDirection(getDouble(getColumnIndex(COLUMN_WEATHER_WIND_DIRECTION)));
            res.setWindSpeed(getDouble(getColumnIndex(COLUMN_WEATHER_WIND_SPEED)));
            res.setTime(getLong(getColumnIndex(COLUMN_WEATHER_TIME)));
            return res;
        }
    }

    public static class LocationCursor extends CursorWrapper {
        public LocationCursor(Cursor cursor) {
            super(cursor);
        }
        public Location getLocation() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Location res = new Location();
            res.setCity(getString(getColumnIndex(COLUMN_LOCATION_NAME)));
            res.setId(getInt(getColumnIndex(COLUMN_LOCATION_ID)));
            res.setColor(getInt(getColumnIndex(COLUMN_LOCATION_COLOR)));
            return res;
        }
    }

}

