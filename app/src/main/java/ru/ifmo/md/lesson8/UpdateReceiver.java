package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = context.getContentResolver().query(WeatherContentProvider.CITIES_URI, null,
                        null, null, MyDatabase._ID);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Intent refresh = new Intent(context, WeatherIntentService.class);
                refresh.putExtra(MyDatabase.NAME,
                        cursor.getString(cursor.getColumnIndex(MyDatabase.NAME)));
                refresh.putExtra(MyDatabase.CODE,
                        cursor.getString(cursor.getColumnIndex(MyDatabase.CODE)));
                refresh.putExtra("force", true);
                context.startService(refresh);
            }
            cursor.close();
        }
    }
}
