package com.example.alexey.wather_2;

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
import android.util.Log;

/**
 * Created by Alexey on 30.11.2014.
 */
public class provider extends ContentProvider {

    final String LOG_TAG = "myLogs";
    static String DB_NAME = "mdb";
    static int DB_VERSION = 1;
    static String TABLE_NAME = "contacts";
    static final String _ID = "_id";
    static final String DATE = "date";
    static final String DAY = "day";
    static final String NIGHT = "night";
    static final String TEMPERATURE = "temperature";
    static final String FIVE_PATH = "five";
    static final String SIX_PATH = "six";
    static final String AUTHORITY = "com.example.alexey.wather_2.provider";
    static final String PATH = "mdb";
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
                selectionArgs, null, null, sortOrder);
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

    public static void match_name(String name) {
        TABLE_NAME = name;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (isTableExists(db, TABLE_NAME)) return;
            String DB_CREATE = "create table " + TABLE_NAME + "("
                    + _ID + " integer primary key autoincrement, "
                    + DATE + " text, " + DAY + " text" + ");";
            db.execSQL(DB_CREATE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public static boolean add_table(String NAME) {
        if (isTableExists(db, NAME)) return false;
        String DB_CREATE = "create table " + NAME + "("
                + _ID + " integer primary key autoincrement, "
                + DATE + " text, " + DAY + " text, " + NIGHT + " text, " + TEMPERATURE + " text, " + FIVE_PATH + " BLOB," + SIX_PATH + " BLOB" + ");";
        db.execSQL(DB_CREATE);
        return true;
    }

    public static boolean isTableExists(SQLiteDatabase db, String tableName) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        if (!cursor.moveToFirst()) {
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }
}
