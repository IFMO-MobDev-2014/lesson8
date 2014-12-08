package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StuffReceiver extends BroadcastReceiver {
    public StuffReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarms(context);
            // TODO ?
        }
    }

    public static void setAlarms(Context ctx) {
        AlarmManager ss = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if(ss == null)
            return;
        PendingIntent pi = PendingIntent.getService(ctx, 0, WeatherLoaderService.getIntentGetAll(ctx), 0); // now, if anything in this is counter-intuitive, I'm gonna curl up in a corner and cry
        ss.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() % 3600000 + 3600000, AlarmManager.INTERVAL_HALF_HOUR, pi); // because this is why we can't have nice things
    }
}
