package com.example.home.superwheather;

/**
 * Created by Home on 04.12.2014.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service
{
    Alarm alarm = new Alarm();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.SetAlarm(AlarmService.this);
        return START_STICKY;
    }



    public void onStart(Context context,Intent intent, int startId)
    {
        alarm.SetAlarm(context);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
