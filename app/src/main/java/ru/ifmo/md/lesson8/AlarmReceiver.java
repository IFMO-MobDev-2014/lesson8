package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Mikhail on 17.01.15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, WeatherIntentService.class);
        newIntent.putExtra("receiver", intent.getParcelableExtra("receiver"));
        newIntent.putExtra("all", true);

        context.startService(newIntent);
    }
}
