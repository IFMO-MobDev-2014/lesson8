package com.example.alexey.wather;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Alexey on 30.11.2014.
 */
public class provider extends ContentProvider {

    final String LOG_TAG = "myLogs";
    static String PATH = "mdb_111";
    static int DB_VERSION = 1;
    static String TABLE_NAME = "contacts";
    static final String _ID = "_id";
    static final String DATE = "date";
    static final String DAY = "day";
    static final String TYPE = "type";
    static final String NIGHT = "night";
    static final String TEMPERATURE = "temperature";
    static final String FIRST_PIC = "five";
    static final String SECOND_PIC = "six";
    static final String HESH = "hesh";
    static final String AUTHORITY = "com.example.alexey.wather.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + PATH);
    static final int URI_TO = 1;
    static final int URI_TO_CH = 2;
    static DBHelper dbHelper;
    static SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        dbHelper = new DBHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return true;
    }

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, URI_TO);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", URI_TO_CH);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, projection, selection,
                selectionArgs, null, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(),
                CONTENT_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(TABLE_NAME, null, values);
        Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(TABLE_NAME, selection,null);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(TABLE_NAME, values, selection, null);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, PATH, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            String DB_CREATE = "create table " + TABLE_NAME + "("
                + _ID + " integer primary key autoincrement, " + DATE + " text, "
                + DAY + " text, " + NIGHT + " text, " + TEMPERATURE + " text, "
                + HESH + " text, " + TYPE + " text, " + FIRST_PIC + " BLOB,"
                + SECOND_PIC + " BLOB" + ");";

            db.execSQL(DB_CREATE);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
