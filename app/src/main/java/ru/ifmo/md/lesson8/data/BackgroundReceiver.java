package ru.ifmo.md.lesson8.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by mariashka on 12/8/14.
 */
public class BackgroundReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WeatherBroadcast broadcast = new WeatherBroadcast();
        Intent service = new Intent(context, WeatherService.class);
        service.putExtra("FLAG", "all");
        context.startService(service);

        IntentFilter intentFilter = new IntentFilter(WeatherService.ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(broadcast, intentFilter);
    }
}
