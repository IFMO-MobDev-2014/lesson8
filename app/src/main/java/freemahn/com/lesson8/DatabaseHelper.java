package freemahn.com.lesson8;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Freemahn on 22.01.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    public static final String DATABASE_NAME = "mydatabase2.db";
    public static final String DATABASE_TABLE = "weather";
    public static final int DATABASE_VERSION = 1;
    public static final String TEXT_COLUMN = "text";
    public static final String CODE_COLUMN = "code";
    public static final String TEMP_COLUMN = "temp_current";
    public static final String TEMP_HIGH_COLUMN = "temp_high";
    public static final String DATE_COLUMN = "date";


    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + TEXT_COLUMN
            + " text, " + CODE_COLUMN + " integer, " + TEMP_COLUMN + " integer, " + DATE_COLUMN + " text not null, " + TEMP_HIGH_COLUMN + " integer); ";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + DATABASE_TABLE;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}

