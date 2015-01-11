package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.HashMap;

import ru.ifmo.md.lesson8.dummy.DummyContent;

/**
 * Created by 107476 on 09.01.2015.
 */
public class CitiesLoader extends AsyncTaskLoader<ArrayList<DummyContent.CitiesItem>> {
    Context context;
    public CitiesLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public ArrayList<DummyContent.CitiesItem> loadInBackground() {
        ArrayList<DummyContent.CitiesItem> items = new ArrayList<>();
        Cursor cursor = getContext().getContentResolver().query(MyContentProvider.CITIES_CONTENT_URI, null, null ,null ,null);
        cursor.moveToFirst();
        DummyContent.ITEMS = new ArrayList<>();
        DummyContent.ITEM_MAP = new HashMap<>();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            int woeid = cursor.getInt(3);
            String title = cursor.getString(1);
            String country = cursor.getString(2);
            DummyContent.CitiesItem item = new DummyContent.CitiesItem(id,title,country,woeid);
            items.add(new DummyContent.CitiesItem(id,title,country,woeid));
            DummyContent.addItem(item, items.size()-1);
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
