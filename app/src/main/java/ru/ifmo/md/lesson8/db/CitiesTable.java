package ru.ifmo.md.lesson8.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by flyingleafe on 01.12.14.
 */
public class CitiesTable extends BaseTable {
    public static final String TABLE_NAME = "cities";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_WOEID = "woeid";
    public static final String СOLUMN_NAME_CUR_COND = "code";
    public static final String COLUMN_NAME_CUR_TEMP = "temp";
    public static final String COLUMN_NAME_СUR_DESC = "text";

    private static final String SQL_CREATE_CITIES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    СOLUMN_NAME_CUR_COND + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_CUR_TEMP + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_СUR_DESC + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_WOEID + INTEGER_TYPE + " UNIQUE ON CONFLICT IGNORE" +
                    " );";

    private static final String SQL_DELETE_CITIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    public static void create(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CITIES);
        // PREDEFINED DATA
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Saint-Petersburg', 2123260);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Moscow', 2122265);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Ufa', 2124045);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" +
                COLUMN_NAME_NAME + ", " + COLUMN_NAME_WOEID +
                ") VALUES ('Munich', 676757);");
    }

    public static void delete(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_CITIES);
    }
}

