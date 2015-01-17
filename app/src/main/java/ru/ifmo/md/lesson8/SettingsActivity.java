package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.RadioGroup;

/**
 * Created by sergey on 10.01.15.
 */
public class SettingsActivity extends ActionBarActivity {

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        checkCurrentInterval();
    }

    private void checkCurrentInterval() {
        long interval = WeatherLoaderService.getInterval(getApplicationContext());
        if (interval == WeatherLoaderService.INTERVAL_MANUALLY)
            radioGroup.check(R.id.rb_manually);
        else if (interval == WeatherLoaderService.INTERVAL_HALF_HOUR)
            radioGroup.check(R.id.rb_halfhour);
        else if (interval == WeatherLoaderService.INTERVAL_ONE_HOUR)
            radioGroup.check(R.id.rb_hour1);
        else if (interval == WeatherLoaderService.INTERVAL_TWO_HOURS)
            radioGroup.check(R.id.rb_hour2);
        else if (interval == WeatherLoaderService.INTERVAL_SIX_HOURS)
            radioGroup.check(R.id.rb_hour6);
        else if (interval == WeatherLoaderService.INTERVAL_TWELVE_HOURS)
            radioGroup.check(R.id.rb_hour12);
        else if (interval == WeatherLoaderService.INTERVAL_DAY)
            radioGroup.check(R.id.rb_hour24);
    }

    public void acceptClicked(View view) {
        int index = radioGroup.getCheckedRadioButtonId();
        if (index == -1 || index == R.id.rb_manually) {
            boolean isServiceOn = WeatherLoaderService.isServiceAlarmOn(getApplicationContext());
            WeatherLoaderService.setInterval(getApplicationContext(), WeatherLoaderService.INTERVAL_MANUALLY);
            if (isServiceOn) {
                WeatherLoaderService.setServiceAlarm(getApplicationContext(), false);
            }
        } else {
            long interval;
            switch (index) {
                case R.id.rb_halfhour:
                    interval = WeatherLoaderService.INTERVAL_HALF_HOUR;
                    break;
                case R.id.rb_hour1:
                    interval = WeatherLoaderService.INTERVAL_ONE_HOUR;
                    break;
                case R.id.rb_hour2:
                    interval = WeatherLoaderService.INTERVAL_TWO_HOURS;
                    break;
                case R.id.rb_hour6:
                    interval = WeatherLoaderService.INTERVAL_SIX_HOURS;
                    break;
                case R.id.rb_hour12:
                    interval = WeatherLoaderService.INTERVAL_TWELVE_HOURS;
                    break;
                case R.id.rb_hour24:
                    interval = WeatherLoaderService.INTERVAL_DAY;
                    break;
                default:
                    interval = WeatherLoaderService.INTERVAL_MANUALLY;
            }
            WeatherLoaderService.setServiceAlarm(getApplicationContext(), true);
            WeatherLoaderService.setInterval(getApplicationContext(), interval);
        }
        finish();
    }

}
