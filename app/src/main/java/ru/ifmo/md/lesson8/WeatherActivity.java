package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import static ru.ifmo.md.lesson8.WeatherColumns.CITY_NAME;
import static ru.ifmo.md.lesson8.WeatherColumns.TIME;

public class WeatherActivity extends Activity implements LoaderManager.LoaderCallbacks, DateSelector.OnTimeChangedListener, ActionBar.OnNavigationListener {
    static final int[] apprTimes = new int[]{0, 6, 9, 16, 24};
    SpinnerAdapter adapter = new SpinnerAdapter(this);
    WeatherSoon activated;
    MenuItem refreshButton;
    boolean progress;

    private ContentObserver observer = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            Bundle args = new Bundle(1);
            args.putLong("date", ((DateSelector) findViewById(R.id.calendar)).getDate());
            getLoaderManager().restartLoader(2, args, WeatherActivity.this).forceLoad();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            long cPeriod[] = getDayRange(((DateSelector) findViewById(R.id.calendar)).getDate());
            long reqPeriod = Long.parseLong(uri.getQueryParameter("time"));
            if (getCity() != null && getCity().equals(uri.getQueryParameter("city"))
                    && reqPeriod / 1000 >= cPeriod[0] && reqPeriod / 1000 <= cPeriod[1]) {
                Bundle args = new Bundle(1);
                args.putLong("date", reqPeriod);
                getLoaderManager().restartLoader(2, args, WeatherActivity.this).forceLoad();
            }
        }
    };

    public static WeatherInfo[] findAppropriateTimes(long[] period, Cursor data) {
        WeatherInfo[] result = new WeatherInfo[4];
        Log.v("WeatherActivity", "Searching in period [" + period[0] + ", " + period[1] + "]");
        int cnt = 0;
        for (data.moveToFirst(); cnt < 4 && !data.isAfterLast(); data.moveToNext()) {
            long time = data.getLong(data.getColumnIndex(TIME));
            while (cnt < 4 && time > (period[0] * (24 - apprTimes[cnt + 1]) + period[1] * apprTimes[cnt + 1]) / 24)
                cnt++;
            if (cnt < 4 && time >= (period[0] * (24 - apprTimes[cnt]) + period[1] * apprTimes[cnt]) / 24
                    && time <= (period[0] * (24 - apprTimes[cnt + 1]) + period[1] * apprTimes[cnt + 1]) / 24)
                result[cnt++] = new WeatherInfo(data);
        }
        return result;
    }

    public static long[] getDayRange(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long result[] = new long[2];
        result[0] = calendar.getTimeInMillis() / 1000;
        calendar.add(Calendar.DATE, 1);
        result[1] = calendar.getTimeInMillis() / 1000;
        return result;
    }

    @Override
    public void onTimeChanged(DateSelector selector, long newTime) {
        Bundle args = new Bundle(1);
        args.putLong("date", newTime);
        getLoaderManager().restartLoader(1, args, this).forceLoad();
    }

    String getCity() {
        return ((TextView) findViewById(R.id.city_name)).getText().toString();
    }

    void setCity(String cityName) {
        ((TextView) findViewById(R.id.city_name)).setText(cityName);
        switchToPresent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        switchToPresent();
    }

    public void setProgressShown(boolean shown) {
        progress = shown;
        if (refreshButton != null) {
            refreshButton.setVisible(!shown);
            invalidateOptionsMenu();
        }
        setProgressBarIndeterminateVisibility(shown);
    }

    public void onActivate(WeatherSoon activated) {
        if (this.activated != null)
            this.activated.setActive(false);
        this.activated = activated;
        this.activated.setActive(true);
        WeatherNow weatherNow = (WeatherNow) getFragmentManager().findFragmentById(R.id.detailed_fragment);
        weatherNow.setTimeOfDay(activated.getTimeOfDay());
        if (activated.getWeatherInfo() != null)
            weatherNow.inflateWeatherInfo(activated.getWeatherInfo());
        ((WeatherView) findViewById(R.id.calendar)).setTimeOfDay(activated.getTimeOfDay());
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == 0)
            return new CursorLoader(this, Uri.parse("content://net.dimatomp.weather.provider/cities"), null, null, null, null);
        if (id == 3)
            return new DeviceLocationLoader(this);
        return new CursorLoader(this, Uri.parse("content://net.dimatomp.weather.provider/weather?" +
                "city=" + Uri.encode(getCity()) + "&time=" + args.getLong("date")), null, null, null, null);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        setCity(adapter.getItem(itemPosition).toString());
        return true;
    }

    void forceSwitchToCity(int index) {
        ActionBar actionBar = getActionBar();
        if (actionBar.getSelectedNavigationIndex() != index)
            actionBar.setSelectedNavigationItem(index);
        else
            onNavigationItemSelected(index, -1);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if (data instanceof String) {
            for (int i = 0; i < adapter.getCount(); i++)
                if (adapter.getItem(i).equals(data)) {
                    getActionBar().setSelectedNavigationItem(i);
                    return;
                }
            adapter.addCity(data.toString());
            getActionBar().setSelectedNavigationItem(adapter.getCount() - 1);
        } else
            onLoadFinished(loader, (Cursor) data);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 0) {
            if (data.getCount() > 0) {
                for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext())
                    adapter.addCity(data.getString(data.getColumnIndex(CITY_NAME)));
                if (adapter.getCount() > 0)
                    forceSwitchToCity(0);
            } else
                Toast.makeText(this, R.string.first_use_location, Toast.LENGTH_LONG).show();
            getLoaderManager().initLoader(3, null, this).forceLoad();
        } else {
            int[] tabNames = new int[]{R.id.night_tab, R.id.morning_tab, R.id.daytime_tab, R.id.evening_tab};
            WeatherInfo info[] = findAppropriateTimes(
                    getDayRange(((DateSelector) findViewById(R.id.calendar)).getDate()), data);
            FragmentManager manager = getFragmentManager();
            boolean notFull = false;
            for (int i = 0; i < tabNames.length; i++) {
                WeatherSoon tab = (WeatherSoon) manager.findFragmentById(tabNames[i]);
                tab.setWeatherInfo(info[i]);
                if (activated == tab)
                    ((WeatherNow) manager.findFragmentById(R.id.detailed_fragment)).inflateWeatherInfo(info[i]);
                notFull |= info[i] == null;
            }
            if (notFull) {
                if (loader.getId() == 1)
                    refreshWeather();
                else {
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    setProgressShown(false);
                }
            } else
                setProgressShown(false);
        }
    }

    void refreshWeather() {
        if (getCity() != null && !getCity().isEmpty()) {
            Intent intent = new Intent(this, WeatherUpdater.class);
            DateSelector calendar = (DateSelector) findViewById(R.id.calendar);
            intent.setData(Uri.parse("content://net.dimatomp.weather.provider/weather?" +
                    "city=" + Uri.encode(getCity()) + "&time=" + calendar.getDate()));
            startService(intent);
            setProgressShown(true);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.city_weather);

        FragmentManager manager = getFragmentManager();
        ((WeatherSoon) manager.findFragmentById(R.id.night_tab)).setTimeOfDay(WeatherView.TimeOfDay.NIGHT);
        ((WeatherSoon) manager.findFragmentById(R.id.morning_tab)).setTimeOfDay(WeatherView.TimeOfDay.MORNING);
        ((WeatherSoon) manager.findFragmentById(R.id.daytime_tab)).setTimeOfDay(WeatherView.TimeOfDay.DAYTIME);
        ((WeatherSoon) manager.findFragmentById(R.id.evening_tab)).setTimeOfDay(WeatherView.TimeOfDay.EVENING);

        setProgressShown(true);
        getLoaderManager().restartLoader(0, null, this);

        getContentResolver().registerContentObserver(
                Uri.parse("content://net.dimatomp.weather.provider/weather"), false, observer);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(adapter, this);
        getActionBar().setTitle("");
    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(observer);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        refreshButton = menu.findItem(R.id.action_refresh);
        refreshButton.setVisible(!progress);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshWeather();
                break;
            case R.id.action_add:
                new AddCityDialog().show(getFragmentManager(), "addCityDialog");
                break;
            case R.id.action_remove:
                String city = getCity();
                int index = getActionBar().getSelectedNavigationIndex();
                adapter.removeCity(index);
                if (adapter.getCount() == 0) {
                    setProgressShown(true);
                    Toast.makeText(this, R.string.first_use_location, Toast.LENGTH_LONG).show();
                    getLoaderManager().restartLoader(3, null, this).forceLoad();
                } else
                    onNavigationItemSelected(Math.min(index, adapter.getCount() - 1), 0);

                Intent intent = new Intent(this, WeatherUpdater.class);
                intent.setData(Uri.parse("content://net.dimatomp.weather.provider/city?name=" + Uri.encode(city)));
                startService(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void switchToPresent() {
        Calendar calendar = Calendar.getInstance();
        DateSelector selector = (DateSelector) findViewById(R.id.calendar);
        calendar.add(Calendar.DATE, 4);
        selector.setMaxDate(calendar.getTimeInMillis());
        selector.setOnTimeChangedListener(null);
        selector.setDate(System.currentTimeMillis(), false, false);
        onTimeChanged(selector, System.currentTimeMillis());
        selector.setOnTimeChangedListener(this);

        long time = System.currentTimeMillis();
        long[] period = WeatherActivity.getDayRange(time);
        time = (time / 1000 - period[0]) * 24 / (period[1] - period[0]);
        WeatherSoon toActivate;
        if (time < 7)
            toActivate = (WeatherSoon) getFragmentManager().findFragmentById(R.id.night_tab);
        else if (time < 11)
            toActivate = (WeatherSoon) getFragmentManager().findFragmentById(R.id.morning_tab);
        else if (time < 19)
            toActivate = (WeatherSoon) getFragmentManager().findFragmentById(R.id.daytime_tab);
        else
            toActivate = (WeatherSoon) getFragmentManager().findFragmentById(R.id.evening_tab);
        onActivate(toActivate);
    }
}
