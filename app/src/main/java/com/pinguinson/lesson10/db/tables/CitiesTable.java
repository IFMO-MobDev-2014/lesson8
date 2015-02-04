package com.pinguinson.lesson10.db.tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by pinguinson.
 */
public class CitiesTable extends BaseTable {
    public static final String TABLE_NAME = "cities";
    private static final String SQL_DELETE_CITIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String COLUMN_NAME_CITY_NAME = "name";
    public static final String COLUMN_NAME_WOEID = "woeid";
    public static final String COLUMN_NAME_CURRENT_CONDITIONS = "code";
    public static final String COLUMN_NAME_CURRENT_TEMPERATURE = "temp";
    public static final String COLUMN_NAME_СURRENT_DESCRIPTION = "text";
    public static final String COLUMN_NAME_IS_CURRENT = "current";
    private static final String SQL_CREATE_CITIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_CITY_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_CURRENT_CONDITIONS + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_CURRENT_TEMPERATURE + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_СURRENT_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_IS_CURRENT + INTEGER_TYPE + " DEFAULT 0" + COMMA_SEP +
                    COLUMN_NAME_WOEID + INTEGER_TYPE + " UNIQUE ON CONFLICT IGNORE" +
                    " );";

    public static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CITIES);
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_CITY_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Saint-Petersburg', 2123260);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_CITY_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Turin', 725003);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_CITY_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Paris', 615702);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_CITY_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Florence', 715496);");
    }

    public static void delete(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_CITIES);
    }
}

