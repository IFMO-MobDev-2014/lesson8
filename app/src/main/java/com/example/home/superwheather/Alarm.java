package com.example.home.superwheather;

/**
 * Created by Home on 04.12.2014.
 */


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;

public class Alarm extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        Cursor cursor = context.getContentResolver().query(MyContentProvider.CONTENT_URI, new String[]{MyTable.COLUMN_TITLE}, MyTable.COLUMN_T_ID + " = ?", new String[]{"1"}, null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Intent serviceIntent = new Intent(context, AlarmLoaderService.class);
            context.startService(serviceIntent.putExtra("city", cursor.getString(0)));
            cursor.moveToNext();
        }

        wl.release();
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60, pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
