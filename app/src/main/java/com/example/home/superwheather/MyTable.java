package com.example.home.superwheather;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Home on 23.11.2014.
 */

public class MyTable {

    public static final String TABLE_NAME = "forecasting_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_T_ID = "t_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TEMP = "temp";
    public static final String COLUMN_CLOUD = "cloud";
    public static final String COLUMN_HUM = "hum";
    public static final String COLUMN_PRESS = "press";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PIC_ID = "pic_id";

    private static final String DB_CREATE = "CREATE TABLE " +
            TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_T_ID + " INTEGER, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_TEMP + " TEXT, " +
            COLUMN_CLOUD + " TEXT, " +
            COLUMN_HUM + " TEXT, " +
            COLUMN_PRESS + " TEXT, " +
            COLUMN_DATE + " TEXT," +
            COLUMN_PIC_ID + " TEXT);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DB_CREATE);
        database.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_T_ID + ", " + COLUMN_TITLE + ", "
                + COLUMN_TEMP + ", " + COLUMN_CLOUD + ", "
                + COLUMN_HUM + ", " + COLUMN_PRESS + ", "
                + COLUMN_DATE + ") VALUES (1, 'Saint-Petersburg', '', '', '', '', '')");
        database.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_T_ID + ", " + COLUMN_TITLE + ", "
                + COLUMN_TEMP + ", " + COLUMN_CLOUD + ", "
                + COLUMN_HUM + ", " + COLUMN_PRESS + ", "
                + COLUMN_DATE + ") VALUES (1, 'Moscow', '', '', '', '', '')");
        database.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_T_ID + ", " + COLUMN_TITLE + ", "
                + COLUMN_TEMP + ", " + COLUMN_CLOUD + ", "
                + COLUMN_HUM + ", " + COLUMN_PRESS + ", "
                + COLUMN_DATE + ") VALUES (1, 'London', '', '', '', '', '')");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

}
