package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME =  "weather.db";
    public static final String TABLE_NEWS = "news";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CHANNEL_ID = "channel_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SUMMARY = "summary";
    public static final String COLUMN_LINK = "link";
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_CREATE = "create table "
            + TABLE_NEWS + " ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_CHANNEL_ID + " integer, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_SUMMARY + " text not null, "
            + COLUMN_LINK + " text not null, "
            +");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
        onCreate(db);
    }
}
