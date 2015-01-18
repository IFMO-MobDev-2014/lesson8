package year2013.ifmo.weather;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Юлия on 16.01.2015.
 */
public class ForecastContentProvider extends ContentProvider {

    public static final String FORECAST = "forecast";
    public static final String FORECAST_TABLE_NAME = "forecast";

    public ForecastContentProvider() {
    }

    private ForecastDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ForecastDbHelper(getContext());
        return true;
    }

    private static final int FORECASTS = 1;
    private static final int FORECASTS_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Forecast.AUTHORITY, Forecast.FORECAST_NAME, FORECASTS);
        uriMatcher.addURI(Forecast.AUTHORITY, Forecast.FORECAST_NAME + "/#", FORECASTS_ID);
    }

    private static class ForecastDbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = FORECAST + ".db";
        private static int DATABASE_VERSION = 3;

        private ForecastDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String s = "CREATE TABLE " + FORECAST_TABLE_NAME + " ("
                    + Forecast._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Forecast.CITY_NAME + " TEXT, "
                    + Forecast.CURRENT_FORECAST + " TEXT, "
                    + Forecast.DAYS_FORECAST + " TEXT" + ");";
            sqLiteDatabase.execSQL(s);
            ContentValues cv = new ContentValues();
            cv.put(Forecast.CITY_NAME, "Saint-Petersburg");
            cv.put(Forecast.CURRENT_FORECAST, "");
            cv.put(Forecast.DAYS_FORECAST, "");
            sqLiteDatabase.insert(FORECAST_TABLE_NAME, null, cv);
            cv.clear();
            cv.put(Forecast.CITY_NAME, "Moscow");
            cv.put(Forecast.CURRENT_FORECAST, "");
            cv.put(Forecast.DAYS_FORECAST, "");
            sqLiteDatabase.insert(FORECAST_TABLE_NAME, null, cv);
            cv.clear();
            cv.put(Forecast.CITY_NAME, "Volgograd");
            cv.put(Forecast.CURRENT_FORECAST, "");
            cv.put(Forecast.DAYS_FORECAST, "");
            sqLiteDatabase.insert(FORECAST_TABLE_NAME, null, cv);
            cv.clear();
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FORECAST_TABLE_NAME + ";");
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        int u = uriMatcher.match(uri);
        if (u != FORECASTS) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        //Log.d("ForecastContent Provider", "I'm in insert method! " + ((Integer) u).toString());
        // Log.d("LogsLL", ((Integer) u).toString());
        ContentValues values;
        if (initialValues != null) {
            values = initialValues;
        } else {
            values = new ContentValues();
        }


        long rowID = dbHelper.getWritableDatabase().insert(FORECAST_TABLE_NAME, null, values);
        if (rowID > 0) {
            Uri resultUri = ContentUris.withAppendedId(Forecast.CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }


        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int match = uriMatcher.match(uri);
        int affected;

        //Log.d("ForecastContent Provider", "I'm in delete method!");
        switch (match) {
            case FORECASTS:
                affected = dbHelper.getWritableDatabase().delete(FORECAST_TABLE_NAME,
                        (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case FORECASTS_ID:
                long sourceId = ContentUris.parseId(uri);
                affected = dbHelper.getWritableDatabase().delete(FORECAST_TABLE_NAME,
                        Forecast._ID + "=" + sourceId
                                + (!TextUtils.isEmpty(selection) ?
                                " (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("unknown forecast element: " +
                        uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        //Log.d("ForecastContent Provider", "I'm in query method!");

        switch (uriMatcher.match(uri)) {
            case FORECASTS:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Forecast.CITY_NAME + " ASC";
                }
                break;
            case FORECASTS_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = Forecast._ID + " = " + id;
                } else {
                    selection = selection + " AND " + Forecast._ID + " = " + id;
                }
            }
            break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        Cursor cursor = dbHelper.getWritableDatabase().query(FORECAST_TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), Forecast.CONTENT_URI);
        //Log.d("ForecastContent Provider", "I'm in query method!");
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int affected;

        switch (uriMatcher.match(uri)) {
            case FORECASTS:
                affected = dbHelper.getWritableDatabase().update(FORECAST_TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case FORECASTS_ID:
                String feedId = uri.getPathSegments().get(1);
                affected = dbHelper.getWritableDatabase().update(FORECAST_TABLE_NAME, values,
                        Forecast._ID + "=" + feedId
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

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case FORECASTS:
                return Forecast.CONTENT_TYPE;
            case FORECASTS_ID:
                return Forecast.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown type: " + uri);
        }
    }

}
