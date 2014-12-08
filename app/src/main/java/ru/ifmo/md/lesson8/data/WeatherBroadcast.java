package ru.ifmo.md.lesson8.data;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import ru.ifmo.md.lesson8.WeatherMain;

/**
 * Created by mariashka on 12/1/14.
 */
public class WeatherBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("response", false)) {
            String done = intent.getStringExtra("done");
            if (!done.equals("current")) {
                WeatherLoader loader = new WeatherLoader(context);
                List<WeatherItem> list = loader.loadInBackground();
                ((WeatherMain) context).setItems(list);
                ((WeatherMain) context).setFragments();
            } else {
                //TODO
            }
        }
    }
}
