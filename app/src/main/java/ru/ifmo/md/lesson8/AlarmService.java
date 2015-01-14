package ru.ifmo.md.lesson8;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by 107476 on 12.01.2015.
 */
public class AlarmService extends IntentService {

    public AlarmService() {
        super("Alarm");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent in = new Intent(this, AlarmReceiver.class);
        in.putExtra("receiver", intent.getParcelableExtra("receiver"));
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR * 2,
                pending);

    }
}
