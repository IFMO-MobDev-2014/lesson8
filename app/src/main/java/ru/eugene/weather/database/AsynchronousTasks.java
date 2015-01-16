package ru.eugene.weather.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Created by eugene on 12/17/14.
 */
public class AsynchronousTasks extends AsyncTask<ContentValues, Void, Long> {
    private SQLiteDatabase db;
    private String task;
    private String TABLE_NAME;
    private String selection;
    private String[] selectionArgs;
    public long result;

    AsynchronousTasks(String TABLE_NAME) {
        this.TABLE_NAME = TABLE_NAME;
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }


    public void setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public void setTask(String task) {
        this.task = task;
    }

    @Override
    protected void onPostExecute(Long result) {
        this.result = result;
    }

    @Override
    protected Long doInBackground(ContentValues... params) {
        switch (task) {
            case "insert":
                return db.insert(TABLE_NAME, null, params[0]);
            case "update":
                return (long) db.update(TABLE_NAME, params[0], selection, selectionArgs);
            case "delete":
                return (long) db.delete(TABLE_NAME, selection, selectionArgs);
            default:
                return -1L;
        }
    }
}
