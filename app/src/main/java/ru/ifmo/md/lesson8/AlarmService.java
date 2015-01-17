package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Mikhail on 18.01.15.
 */
public class AlarmService extends IntentService {
    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent in = new Intent(this, AlarmReceiver.class);
        in.putExtra("receiver", intent.getParcelableExtra("receiver"));
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis(),
                AlarmManager.INTERVAL_HALF_HOUR * 2,
                pending);
    }
}
