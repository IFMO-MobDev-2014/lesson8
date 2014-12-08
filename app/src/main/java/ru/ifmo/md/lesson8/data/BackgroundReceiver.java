package ru.ifmo.md.lesson8.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by mariashka on 12/8/14.
 */
public class BackgroundReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, WeatherService.class);
        service.putExtra("FLAG", "all");
        context.startService(service);
    }
}
