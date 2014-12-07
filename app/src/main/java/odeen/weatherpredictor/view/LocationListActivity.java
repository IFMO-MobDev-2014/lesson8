package odeen.weatherpredictor.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import odeen.weatherpredictor.AlarmReceiver;

/**
 * Created by Женя on 27.11.2014.
 */
public class LocationListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new LocationListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmReceiver.INTERVAL, AlarmReceiver.INTERVAL, pi);
    }
}
