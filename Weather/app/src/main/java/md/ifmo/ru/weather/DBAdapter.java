package md.ifmo.ru.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Илья on 01.12.2014.
 */
public class DBAdapter {

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "Weather";
    private static final int DATABASE_VERSION = 1;
    private static final String CITIES_TABLE = "cities";
    private static final String LAST_TABLE = "last";

    public static final String KEY_ID = "_id";
    public static final String KEY_CITY = "city";
    public static final String KEY_LAST = "last";

    private static final String INIT_CITIES =
            "create table if not exists " + CITIES_TABLE + " ("
                    + KEY_ID + " integer primary key autoincrement, "
                    + KEY_CITY + " text not null)";

    private static final String INIT_LAST =
            "create table if not exists " + LAST_TABLE + " ("
                    + KEY_ID + " integer primary key autoincrement, "
                    + KEY_LAST + " integer not null)";

    private static final String REMOVE_CITIES =
            "drop table if exists " + CITIES_TABLE;

    private static final String REMOVE_LAST =
            "drop table if exists " + LAST_TABLE;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(INIT_CITIES);
            db.execSQL(INIT_LAST);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(REMOVE_CITIES);
            db.execSQL(REMOVE_LAST);
            onCreate(db);
        }
    }

    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    private void init() {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LAST, -1);
        mDb.insert(LAST_TABLE, null, initialValues);
    }

    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        init();
        return this;
    }

    public void close() {
        mDb.close();
        mDbHelper.close();
    }

    public long addCity(String city) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CITY, city);
        return mDb.insert(CITIES_TABLE, null, initialValues);
    }

    public boolean updateCity(long rowID, String city) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_CITY, city);
        return mDb.update(CITIES_TABLE, newValues, KEY_ID + "=" + rowID, null) > 0;
    }

    public boolean deleteCity(long rowID) {
        return mDb.delete(CITIES_TABLE, KEY_ID + "=" + rowID, null) > 0;
    }

    public Cursor fetchCities() {
        return mDb.query(CITIES_TABLE, new String[] {KEY_ID, KEY_CITY}, null, null, null, null, null);
    }

    public Cursor fetchCity(int cityID) {
        return mDb.query(CITIES_TABLE, new String[] {KEY_ID, KEY_CITY}, KEY_ID + "=" + cityID, null, null, null, null);
    }

    public boolean setLast(int cityID) {
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_LAST, cityID);
        return mDb.update(LAST_TABLE, newValues, null, null) > 0;
    }

    public Cursor getLast() {
        return mDb.query(LAST_TABLE, new String[] {KEY_LAST}, null, null, null, null, null);
    }
}
