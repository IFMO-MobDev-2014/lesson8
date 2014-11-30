package com.example.home.superwheather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Home on 23.11.2014.
 */

public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "forecasting.db";
    private static final int DB_VERSION = 14;

    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        MyTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i2) {
        MyTable.onUpgrade(database, i, i2);
    }
}
