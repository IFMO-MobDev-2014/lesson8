package odeen.weatherpredictor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.location.Location;
import android.util.Log;

/**
 * Created by Женя on 05.12.2014.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_SERVICE = 0;

    public static final long INTERVAL = AlarmManager.INTERVAL_HALF_HOUR;
    public static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.hasExtra("REQUEST_LOCATION")) {
            Intent i = new Intent(context, WeatherService.class);
            context.startService(i);
        } else {
            if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
                Log.d(TAG, "received loc req");
                Location loc = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
                Intent i = new Intent(context, WeatherService.class);
                i.putExtra(WeatherService.EXTRA_FROM_CURRENT, true);
                i.putExtra(WeatherService.EXTRA_LAT, loc.getLatitude());
                i.putExtra(WeatherService.EXTRA_LON, loc.getLongitude());
                context.startService(i);
            }

        }
    }
}
