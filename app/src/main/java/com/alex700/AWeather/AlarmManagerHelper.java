package com.alex700.AWeather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Алексей on 15.12.2014.
 */
public class AlarmManagerHelper {

    public static void enableServiceAlarm(Context context, int interval) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateAllService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC,
                System.currentTimeMillis() + interval, interval, pendingIntent);
    }

    public static void disableServiceAlarm(Context context) {
        if (checkIfServiceAlarmEnabled(context)) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, UpdateAllService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static boolean checkIfServiceAlarmEnabled(Context context) {
        Intent intent = new Intent(context, UpdateAllService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }
}
