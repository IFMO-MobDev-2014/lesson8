package ru.ifmo.md.lesson8.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by flyingleafe on 01.12.14.
 */
public class ForecastsTable extends BaseTable {
    public static final String TABLE_NAME = "forecasts";
    public static final String COLUMN_NAME_CITY_ID = "city_id";
    public static final String COLUMN_NAME_DAY = "day";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_LOW_TEMP = "low";
    public static final String COLUMN_NAME_HIGH_TEMP = "high";
    public static final String COLUMN_NAME_COND = "code";
    public static final String COLUMN_NAME_TEXT = "text";

    private static final String SQL_CREATE_FORECASTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_DAY + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LOW_TEMP + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_HIGH_TEMP + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_COND + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_CITY_ID + INTEGER_TYPE + COMMA_SEP +
                    "FOREIGN KEY(" + COLUMN_NAME_CITY_ID + ") REFERENCES " + CitiesTable.TABLE_NAME +
                    "(" + CitiesTable._ID + ") ON DELETE CASCADE" +
                    " );";

    private static final String SQL_DELETE_FORECASTS =
            "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    public static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FORECASTS);
    }

    public static void delete(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_FORECASTS);
    }
}
