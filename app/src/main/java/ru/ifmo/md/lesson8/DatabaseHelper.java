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
    public static final int ver = 3;

    public static final String CITIES_TABLE_NAME = "cities";
    public static final String CITIES_ID = "_ID";
    public static final String CITIES_NAME = "name";
    public static final String CITIES_CREATE =
            "CREATE TABLE " + CITIES_TABLE_NAME + " (" +
                    CITIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CITIES_NAME + " TEXT)";

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
        onCreate(db);
    }

    static public City getCity(Cursor cursor) {
        return new City(cursor.getString(cursor.getColumnIndex(CITIES_NAME)));
    }
}