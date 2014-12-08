package ru.ifmo.md.lesson8.content;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class CursorLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    private final CursorAdapter cursorAdapter;
    private final android.content.Context context;
    private final Uri uri;
    private final String[] projection;

    public CursorLoaderCallbacks(Context context, CursorAdapter cursorAdapter, Uri uri, String[] projection) {
        this.cursorAdapter = cursorAdapter;
        this.context = context;
        this.uri = uri;
        this.projection = projection;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(context, uri, projection, null, null, null);
    }

    protected Cursor getNewCursor(Loader<Cursor> loader, Cursor data) {
        return data;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.changeCursor(getNewCursor(loader, data));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }
}
