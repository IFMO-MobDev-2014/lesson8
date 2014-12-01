package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import static ru.ifmo.md.lesson8.WeatherColumns.TIME;

/**
 * Created by dimatomp on 30.11.14.
 */
public class CityWeather extends Fragment implements WeatherNow.Callbacks, WeatherSoon.Callbacks, LoaderManager.LoaderCallbacks<Cursor> {
    static final int[][] apprTimes = new int[][]{new int[]{1, 6}, new int[]{7, 9}, new int[]{11, 16}, new int[]{19, 22}};
    WeatherSoon activated;
    View[] briefViews = new View[4];
    boolean progress;
    private ContentObserver observer = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            getLoaderManager().restartLoader(0, null, CityWeather.this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            onChange(selfChange);
        }
    };

    public static WeatherInfo[] findAppropriateTimes(long[] period, Cursor data) {
        WeatherInfo[] result = new WeatherInfo[4];
        int cnt = 0;
        for (data.moveToFirst(); cnt < 4 && !data.isAfterLast(); data.moveToNext()) {
            long time = data.getLong(data.getColumnIndex(TIME));
            while (cnt < 4 && time > (period[0] * (24 - apprTimes[cnt][1]) + period[1] * apprTimes[cnt][1]) / 24)
                cnt++;
            if (cnt < 4 && time >= (period[0] * (24 - apprTimes[cnt][0]) + period[1] * apprTimes[cnt][0]) / 24
                    && time <= (period[0] * (24 - apprTimes[cnt][1]) + period[1] * apprTimes[cnt][1]) / 24)
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

    public void setProgressBarShown(boolean shown) {
        progress = shown;
        ((WeatherActivity) getActivity()).setProgressShown(shown);
    }

    public String getCity() {
        return getArguments().getString("cityName");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        setProgressBarShown(true);
        CalendarView calendar = (CalendarView) getView().findViewById(R.id.calendar);
        return new CursorLoader(getActivity(), Uri.parse("content://net.dimatomp.weather.provider/weather?" +
                "city=" + Uri.encode(getCity()) + "&time=" + calendar.getDate()), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        setProgressBarShown(false);
        String[] tabNames = new String[]{"nightTab", "morningTab", "daytimeTab", "eveningTab"};
        WeatherInfo info[] = findAppropriateTimes(
                getDayRange(((CalendarView) getView().findViewById(R.id.calendar)).getDate()), data);
        FragmentManager manager = getChildFragmentManager();
        for (int i = 0; i < tabNames.length; i++)
            if (info[i] != null) {
                WeatherSoon tab = (WeatherSoon) manager.findFragmentByTag(tabNames[i]);
                tab.setWeatherInfo(info[i]);
                if (activated == tab)
                    ((WeatherNow) manager.findFragmentByTag("detailedInfo")).inflateWeatherInfo(info[i]);
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setProgressBarShown(false);
    }

    @Override
    public void addBriefView(TimeOfDay timeOfDay, View view) {
        briefViews[timeOfDay.ordinal()] = view;
        for (View otherView : briefViews)
            if (otherView == null)
                return;
        LinearLayout briefLayout = (LinearLayout) getView().findViewById(R.id.brief_fragments);
        for (View otherView : briefViews)
            briefLayout.addView(otherView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        briefViews = null;
        getLoaderManager().initLoader(0, null, this).forceLoad();
        ((CalendarView) getView().findViewById(R.id.calendar)).setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                refreshWeather();
            }
        });
    }

    public void refreshWeather() {
        Intent intent = new Intent(getActivity(), WeatherUpdater.class);
        CalendarView calendar = (CalendarView) getView().findViewById(R.id.calendar);
        intent.setData(Uri.parse("content://net.dimatomp.weather.provider/weather?" +
                "city=" + Uri.encode(getCity()) + "&time=" + calendar.getDate()));
        getActivity().startService(intent);
        setProgressBarShown(true);
    }

    @Override
    public void addDetailedView(View view) {
        ((ViewGroup) getView().findViewById(R.id.city_weather_view)).addView(view, 0);
        ((TextView) getView().findViewById(R.id.city_name)).setText(getCity());
    }

    @Override
    public void onActivate(WeatherSoon activated) {
        if (this.activated != null)
            this.activated.setActive(false);
        this.activated = activated;
        this.activated.setActive(true);
        WeatherNow weatherNow = (WeatherNow) getChildFragmentManager().findFragmentByTag("detailedInfo");
        weatherNow.setTimeOfDay(activated.getTimeOfDay());
        if (activated.getWeatherInfo() != null)
            weatherNow.inflateWeatherInfo(activated.getWeatherInfo());
        ((WeatherView) getView().findViewById(R.id.calendar)).setTimeOfDay(activated.getTimeOfDay());
    }

    @Override
    public void onResume() {
        super.onResume();
        Calendar calendar = Calendar.getInstance();
        CalendarView calendarView = (CalendarView) getView().findViewById(R.id.calendar);
        calendar.add(Calendar.DATE, 4);
        calendarView.setMaxDate(calendar.getTimeInMillis());
        calendar.add(Calendar.DATE, -3);
        calendarView.setDate(calendar.getTimeInMillis(), false, false);
        calendar.add(Calendar.MONTH, -1);
        calendarView.setMinDate(calendar.getTimeInMillis());
        calendarView.setDate(System.currentTimeMillis(), false, false);

        long time = System.currentTimeMillis();
        long[] period = getDayRange(time);
        time = (time / 1000 - period[0]) * 24 / (period[1] - period[0]);
        WeatherSoon toActivate;
        if (time < 7)
            toActivate = (WeatherSoon) getChildFragmentManager().findFragmentByTag("nightTab");
        else if (time < 11)
            toActivate = (WeatherSoon) getChildFragmentManager().findFragmentByTag("morningTab");
        else if (time < 19)
            toActivate = (WeatherSoon) getChildFragmentManager().findFragmentByTag("daytimeTab");
        else
            toActivate = (WeatherSoon) getChildFragmentManager().findFragmentByTag("eveningTab");
        onActivate(toActivate);

        setProgressBarShown(progress);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getContentResolver().registerContentObserver(
                Uri.parse("content://net.dimatomp.weather.provider/weather?city=" + Uri.encode(getCity())), true, observer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.city_weather, container, false);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        WeatherNow weatherNow = new WeatherNow();
        weatherNow.setCallbackInstance(this);
        transaction.add(weatherNow, "detailedInfo");

        WeatherSoon morning = new WeatherSoon();
        morning.setTimeOfDay(TimeOfDay.MORNING);
        morning.setCallbackInstance(this);
        morning.setActive(true);
        transaction.add(morning, "morningTab");

        WeatherSoon daytime = new WeatherSoon();
        daytime.setTimeOfDay(TimeOfDay.DAYTIME);
        daytime.setCallbackInstance(this);
        transaction.add(daytime, "daytimeTab");

        WeatherSoon evening = new WeatherSoon();
        evening.setTimeOfDay(TimeOfDay.EVENING);
        evening.setCallbackInstance(this);
        transaction.add(evening, "eveningTab");

        WeatherSoon night = new WeatherSoon();
        night.setTimeOfDay(TimeOfDay.NIGHT);
        night.setCallbackInstance(this);
        transaction.add(night, "nightTab");

        transaction.commit();
        return result;
    }

    @Override
    public void onDestroy() {
        getActivity().getContentResolver().unregisterContentObserver(observer);
        super.onDestroy();
    }
}
