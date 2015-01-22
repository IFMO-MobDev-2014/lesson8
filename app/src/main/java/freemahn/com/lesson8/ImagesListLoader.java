package freemahn.com.lesson8;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Freemahn on 22.01.2015.
 */
public class ImagesListLoader extends AsyncTaskLoader<ArrayList<Item>> {
    Context context;

    public ImagesListLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public ArrayList<Item> loadInBackground() {
        ArrayList<Item> list = new ArrayList<Item>();

        Cursor c = context.getContentResolver().query(
                ForecastContentProvider.FORECAST_URI,
                null,
                null,
                null,
                null
        );

        if (c != null) {
            c.moveToFirst();
            while (!c.isBeforeFirst() && !c.isAfterLast()) {

                Item item = new Item();
                item.text = c.getString(c.getColumnIndex(DatabaseHelper.TEXT_COLUMN));
                item.code = c.getInt(c.getColumnIndex(DatabaseHelper.CODE_COLUMN));
                item.date = c.getString(c.getColumnIndex(DatabaseHelper.DATE_COLUMN));
                item.temp = c.getInt(c.getColumnIndex(DatabaseHelper.TEMP_COLUMN));
                item.high = c.getInt(c.getColumnIndex(DatabaseHelper.TEMP_HIGH_COLUMN));
                Log.d("LOADER", item + "");
                list.add(item);
                c.moveToNext();
            }
            c.close();
        }


        return list;
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
