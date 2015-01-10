package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by izban on 09.01.15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String NAME = "data";
    public static final int ver = 6;

    public static final String CITIES_TABLE_NAME = "cities";
    public static final String CITIES_ID = "_ID";
    public static final String CITIES_NAME = "name";
    public static final String CITIES_CREATE =
            "CREATE TABLE " + CITIES_TABLE_NAME + " (" +
            CITIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CITIES_NAME + " TEXT)";

    public static final String ITEMS_TABLE_NAME = "items";
    public static final String ITEMS_ID = "_ID";
    public static final String ITEMS_CITY = "city";
    public static final String ITEMS_CODE = "code";
    public static final String ITEMS_DATE = "date";
    public static final String ITEMS_DAY = "day";
    public static final String ITEMS_LOW = "low";
    public static final String ITEMS_HIGH = "high";
    public static final String ITEMS_CREATE = "" +
            "CREATE TABLE " + ITEMS_TABLE_NAME + " (" +
            ITEMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ITEMS_CITY + " TEXT, " +
            ITEMS_CODE + " TEXT, " +
            ITEMS_DATE + " INTEGER, " +
            ITEMS_DAY + " TEXT, " +
            ITEMS_LOW + " INTEGER, " +
            ITEMS_HIGH + " INTEGER)";


    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public DatabaseHelper(Context context) {
        super(context, NAME, null, ver);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("", "creating db");
        db.execSQL(CITIES_CREATE);
        db.execSQL(ITEMS_CREATE);
        db.execSQL("INSERT INTO " + CITIES_TABLE_NAME + " (" +
                CITIES_ID + ", " + CITIES_NAME +
                ") VALUES (1, 'Saint-Petersburg');");
        db.execSQL("INSERT INTO " + CITIES_TABLE_NAME + " (" +
                CITIES_ID + ", " + CITIES_NAME +
                ") VALUES (2, 'Moscow');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CITIES_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE_NAME);
        onCreate(db);
    }

    static public City getCity(Cursor cursor) {
        return new City(cursor.getString(cursor.getColumnIndex(CITIES_NAME)));
    }

    static public Item getItem(Cursor cursor) {
        return new Item(
                cursor.getString(cursor.getColumnIndex(ITEMS_CITY)),
                cursor.getString(cursor.getColumnIndex(ITEMS_CODE)),
                cursor.getString(cursor.getColumnIndex(ITEMS_DATE)),
                cursor.getString(cursor.getColumnIndex(ITEMS_DAY)),
                cursor.getString(cursor.getColumnIndex(ITEMS_LOW)),
                cursor.getString(cursor.getColumnIndex(ITEMS_HIGH))
        );
    }
}