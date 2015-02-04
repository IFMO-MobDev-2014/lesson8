package com.pinguinson.lesson10.db.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by pinguinson.
 */
public class ForecastsTable extends BaseTable {
    public static final String TABLE_NAME = "forecasts";
    private static final String SQL_DELETE_FORECASTS =
            "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String COLUMN_NAME_CITY_ID = "city_id";
    public static final String COLUMN_NAME_WEEKDAY = "day";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_LOW_TEMPERATURE = "low";
    public static final String COLUMN_NAME_HIGH_TEMPERATURE = "high";
    public static final String COLUMN_NAME_CONDITIONS = "code";
    public static final String COLUMN_NAME_TEXT = "text";
    private static final String SQL_CREATE_FORECASTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_WEEKDAY + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LOW_TEMPERATURE + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_HIGH_TEMPERATURE + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_CONDITIONS + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_CITY_ID + INTEGER_TYPE + COMMA_SEP +
                    "FOREIGN KEY(" + COLUMN_NAME_CITY_ID + ") REFERENCES " + CitiesTable.TABLE_NAME +
                    "(" + _ID + ") ON DELETE CASCADE" +
                    " );";

    public static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FORECASTS);
    }

    public static void delete(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_FORECASTS);
    }
}
