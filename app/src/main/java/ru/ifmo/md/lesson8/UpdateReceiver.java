package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = context.getContentResolver().query(WeatherProvider.CITIES_URI, null, null, null, WeatherProvider._ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Intent refresh = new Intent(context, WeatherService.class);
                refresh.putExtra(WeatherProvider.NAME,
                        cursor.getString(cursor.getColumnIndex(WeatherProvider.NAME)));
                refresh.putExtra(WeatherProvider.ZMW,
                        cursor.getString(cursor.getColumnIndex(WeatherProvider.ZMW)));
                refresh.putExtra("force", true);
                context.startService(refresh);
            }
            cursor.close();
        }
    }
}
