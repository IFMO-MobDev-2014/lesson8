package ru.ifmo.md.lesson8;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by dimatomp on 30.11.14.
 */
public class CityWeather extends Fragment implements WeatherNow.Callbacks, WeatherSoon.Callbacks, LoaderManager.LoaderCallbacks<Cursor> {
    WeatherSoon activated;
    View[] briefViews = new View[4];

    public String getCity() {
        return getArguments().getString("cityName");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CalendarView calendar = (CalendarView) getView().findViewById(R.id.calendar);
        return new CursorLoader(getActivity(), Uri.parse("content://net.dimatomp.weather.provider/weather?" +
                "city=" + Uri.encode(getCity()) + "&time=" + calendar.getDate()), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Toast.makeText(getActivity(), "You have loaded something.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Toast.makeText(getActivity(), "You have reset the loader.", Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void addDetailedView(View view) {
        ((ViewGroup) getView().findViewById(R.id.city_weather_view)).addView(view, 0);
    }

    @Override
    public void onActivate(WeatherSoon activated) {
        if (this.activated != null)
            this.activated.setActive(false);
        this.activated = activated;
        this.activated.setActive(true);
        ((WeatherNow) getChildFragmentManager().findFragmentByTag("detailedInfo")).setTimeOfDay(activated.getTimeOfDay());
        ((WeatherView) getView().findViewById(R.id.calendar)).setTimeOfDay(activated.getTimeOfDay());
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
}
