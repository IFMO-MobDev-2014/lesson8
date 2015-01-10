package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by izban on 10.01.15.
 */
public class CityLoadService extends IntentService {
    public CityLoadService() {
        super(CityLoadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("", "trying to add city");
        String city = intent.getStringExtra("city");
        Uri uri = Uri.parse("content://" + MyContentProvider.AUTHORITY + "/" + DatabaseHelper.CITIES_TABLE_NAME);
        String selection = DatabaseHelper.CITIES_NAME + " = \"" + city + "\"";
        if (getContentResolver().query(uri, null, selection, null, null).getCount() != 0) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.CITIES_NAME, city);
        getContentResolver().insert(uri, cv);
        startService(new Intent(this, ForecastLoadService.class).putExtra("city", city));
        Log.i("", "city added ok");
    }
}
