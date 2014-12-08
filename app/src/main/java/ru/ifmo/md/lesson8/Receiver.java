package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, WeatherFetchingService.class);
        i.setAction(WeatherFetchingService.ACTION_UPDATE_ALL_WEATHER);
        PendingIntent pi = PendingIntent.getService(context, 0, i, Intent.FILL_IN_DATA);
        m.cancel(pi);
        m.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 300000, AlarmManager.INTERVAL_HOUR, pi);
    }
}