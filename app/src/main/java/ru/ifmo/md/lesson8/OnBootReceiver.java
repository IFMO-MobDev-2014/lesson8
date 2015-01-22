package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {
    private final int SIX_HOURS = 1000 * 60 * 60 * 6;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                                        new Intent(context, UpdateReceiver.class), 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), SIX_HOURS, pendingIntent);
    }
}
