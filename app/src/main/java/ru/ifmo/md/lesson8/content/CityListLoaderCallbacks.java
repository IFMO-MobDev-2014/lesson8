package ru.ifmo.md.lesson8.content;

import android.content.Context;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.widget.CursorAdapter;

import static ru.ifmo.md.lesson8.content.WeatherContract.*;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class CityListLoaderCallbacks extends CursorLoaderCallbacks {

    private static final Uri URI = WeatherInfo.URI;
    private static final String[] PROJECTION = {
            WeatherInfo.WOEID_COLUMN,
            WeatherInfo.TEMPERATURE_COLUMN
    };

    private final ContentHelper contentHelper;

    public CityListLoaderCallbacks(Context context, CursorAdapter cursorAdapter) {
        super(context, cursorAdapter, URI, PROJECTION);

        contentHelper = new ContentHelper(context);
    }

    @Override
    protected Cursor getNewCursor(Loader<Cursor> loader, Cursor data) {
        return contentHelper.getPlacesCursor();
    }

}
