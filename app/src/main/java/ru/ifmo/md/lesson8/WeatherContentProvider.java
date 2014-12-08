package ru.ifmo.md.lesson8;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WeatherContentProvider extends ContentProvider {
    public static final String AUTHORITY = "ru.ifmo.md.lesson8.weather";
    public static final String BASE_URI = "cities";
    public static final Uri URI_CITY_DIR = Uri.parse("content://" + AUTHORITY + "/" + BASE_URI);
    public static final int URITYPE_CITY_DIR = 1;
    public static final int URITYPE_CITY_WEATHER = 2;

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_URI, URITYPE_CITY_DIR);
        uriMatcher.addURI(AUTHORITY, BASE_URI + "/#", URITYPE_CITY_WEATHER);
    }
    private WeatherDatabase database;

    public WeatherContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        String dTable;
        switch (uriType) {
            case URITYPE_CITY_WEATHER:
                dTable = WeatherDatabase.Structure.WEATHER_TABLE;
                selection = WeatherDatabase.Structure.COLUMN_CITY_ID + " = ?";
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
            case URITYPE_CITY_DIR:
                dTable = WeatherDatabase.Structure.CITIES_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Bad URI to delete of WeatherContentProvider");
        }

        SQLiteDatabase db = database.getWritableDatabase();
        return db.delete(dTable, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("This operation is unsupported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        String tableName = null;
        switch (uriType) {
            case URITYPE_CITY_DIR:
                tableName = WeatherDatabase.Structure.CITIES_TABLE;
                break;
            case URITYPE_CITY_WEATHER:
                tableName = WeatherDatabase.Structure.WEATHER_TABLE;
                values.put(WeatherDatabase.Structure.COLUMN_CITY_ID, uri.getLastPathSegment());
                break;
        }

        SQLiteDatabase db = database.getWritableDatabase();
        long rowid = db.insert(tableName, null, values);

        return uri.buildUpon().appendPath("" + rowid).build();
    }

    @Override
    public boolean onCreate() {
        database = new WeatherDatabase(getContext(), null);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        int uriType = uriMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        if (uriType == URITYPE_CITY_DIR) {
            builder.setTables(WeatherDatabase.Structure.CITIES_TABLE);
        } else {
            builder.setTables(WeatherDatabase.Structure.WEATHER_TABLE);
        }

        switch (uriType) {
            case URITYPE_CITY_DIR:
                break; // select *
            case URITYPE_CITY_WEATHER:
                builder.appendWhere(WeatherDatabase.Structure.COLUMN_CITY_ID + "=" + uri.getLastPathSegment());
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
            throw new IllegalArgumentException("Invalid URI to provider: " + uri.toString());
        }

        switch (uriType) {
            case URITYPE_CITY_WEATHER:
                break;
            case URITYPE_CITY_DIR:
            default:
                throw new UnsupportedOperationException("Unsupported update type");
        }

        SQLiteDatabase db = database.getWritableDatabase();
        return db.update(WeatherDatabase.Structure.CITIES_TABLE, values, WeatherDatabase.Structure.COLUMN_URL + " = ?", new String[]{uri.getLastPathSegment()});
    }
}
