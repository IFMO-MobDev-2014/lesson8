package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class WeatherActivity extends FragmentActivity implements WeatherSoon.Callbacks {
    @Override
    public void onActivate(WeatherSoon activated) {
        ((WeatherNow) getSupportFragmentManager().findFragmentById(R.id.main_view)).setActiveTab(activated);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_weather);
        FragmentManager manager = getSupportFragmentManager();
        ((WeatherSoon) manager.findFragmentById(R.id.morning_view)).setTimeOfDay(TimeOfDay.MORNING);
        ((WeatherSoon) manager.findFragmentById(R.id.day_view)).setTimeOfDay(TimeOfDay.DAYTIME);
        ((WeatherSoon) manager.findFragmentById(R.id.evening_view)).setTimeOfDay(TimeOfDay.EVENING);
        ((WeatherSoon) manager.findFragmentById(R.id.night_view)).setTimeOfDay(TimeOfDay.NIGHT);
        ((WeatherNow) manager.findFragmentById(R.id.main_view)).setActiveTab(
                (WeatherSoon) manager.findFragmentById(R.id.morning_view)
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
