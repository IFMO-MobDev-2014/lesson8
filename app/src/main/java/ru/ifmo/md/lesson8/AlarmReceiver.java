package ru.ifmo.md.lesson8;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by 107476 on 12.01.2015.
 */
public class AlarmReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {


        Intent newIntent = new Intent(context, UpdaterService.class);
        newIntent.putExtra("receiver", intent.getParcelableExtra("receiver"));
        newIntent.putExtra("all", true);
        context.startService(newIntent);
    }
}
