package ru.ifmo.md.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by Kirill on 15.12.2014.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Date now = new Date(System.currentTimeMillis());
        Log.i("onReceive()", now.toString());
        Intent i = new Intent(context, LoadWeatherService.class);
        i.putExtra(LoadWeatherService.REQUEST_TYPE, LoadWeatherService.UPDATE_ALL_REQUEST);
        context.startService(i);
        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
    }
}
