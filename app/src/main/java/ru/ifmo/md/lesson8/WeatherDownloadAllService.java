package ru.ifmo.md.lesson8;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class WeatherDownloadAllService extends IntentService {
    public WeatherDownloadAllService() {
        super("weatherDownloadAllService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("debug1", "weatherDownloadAllService onHandleIntent");
        Cursor c = getContentResolver().query(WeatherContentProvider.CITIES_URI, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast() && !c.isBeforeFirst()) {
            String name = c.getString(c.getColumnIndex(DBWeather.CITY_NAME));
            String url = c.getString(c.getColumnIndex(DBWeather.CITY_URL));
            startService(new Intent(getApplicationContext(), WeatherDownloadService.class).
                            putExtra(WeatherDownloadService.CITY_TAG, name).
                            putExtra(WeatherDownloadService.URL_TAG, url)
            );
            c.moveToNext();
        }
        c.close();
    }
}
