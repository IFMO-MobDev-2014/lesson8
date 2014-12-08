package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;

public class WeatherUpdater extends IntentService {
    public WeatherUpdater() {
        super("WeatherUpdater");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentResolver resolver = getContentResolver();
        if (intent != null)
            if (intent.getData().getLastPathSegment().equals("city")) {
                resolver.delete(intent.getData(), null, null);
            } else {
                resolver.update(intent.getData(), null, null, null);
                resolver.notifyChange(intent.getData(), null, false);
                AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
                manager.set(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_DAY,
                        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            }
    }
}
