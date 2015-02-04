package com.pinguinson.lesson10.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by pinguinson.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, ForecastService.class);
        newIntent.setAction(ForecastService.ACTION_UPDATE_ALL);
        context.startService(newIntent);
    }
}
