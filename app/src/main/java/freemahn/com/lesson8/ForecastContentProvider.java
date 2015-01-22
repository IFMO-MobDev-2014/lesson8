package freemahn.com.lesson8;

/**
 * Created by Freemahn on 22.01.2015.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class ForecastContentProvider extends ContentProvider {
    private static String AUTHORITY = "freemahn.com.lesson8.forecastContentProvider";
    public static final Uri FORECAST_URI = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.DATABASE_TABLE);
    private DatabaseHelper dbImg;

    @Override
    public boolean onCreate() {
        dbImg = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbImg.getReadableDatabase();
        return db.query(uri.getLastPathSegment(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbImg.getWritableDatabase();
        String tableName = uri.getLastPathSegment();
        long id = db.insert(tableName, null, values);
        return Uri.parse("content://" + AUTHORITY + "/" + tableName + "/" + Long.toString(id));
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbImg.getWritableDatabase();
        return db.delete(uri.getLastPathSegment(), selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbImg.getWritableDatabase();
        return db.update(uri.getLastPathSegment(), values, selection, selectionArgs);
    }

}