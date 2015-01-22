package weathertogo.sergeybudkov.ru.weathertogo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;


public class MyContentProvider extends ContentProvider {
    public static WeatherDataBase database;
    private static final int cityCount;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        cityCount = 300;
        uriMatcher.addURI("content://ru.sergeybudkov.weathertogo", "cities", cityCount);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
                String[] splits = selection.split(" ");
                database.deleteWeatherInCity(splits[0],splits[1]);
                getContext().getContentResolver().notifyChange(uri, null);
                return 0;
    }




    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long number = -1;
        if (uriMatcher.match(uri) == cityCount) {
            number = database.sqLiteDatabase.insert("channel", null, contentValues);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(uri.toString() + "/" + number);
    }

    @Override
    public boolean onCreate() {
        database = new WeatherDataBase(getContext());
        database.open();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        if (uriMatcher.match(uri) == cityCount) {
            cursor = database.sqLiteDatabase.query("channel", new String[]{"_id", "name", "url"}, null, null, null, null, null);
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) != cityCount)
            throw new UnsupportedOperationException();
        if (database.sqLiteDatabase.update("channel", contentValues, "_id=" + contentValues.getAsLong("_id"), null) == 1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return 1;
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
            return 0;
        }

    }
}