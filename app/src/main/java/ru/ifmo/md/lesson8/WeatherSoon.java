package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by dimatomp on 28.11.14.
 */
public class WeatherSoon extends Fragment {
    TimeOfDay timeOfDay;
    int weatherImg;
    boolean active;
    Callbacks callbacks;

    WeatherInfo weatherInfo;

    public void setCallbackInstance(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public WeatherInfo getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(WeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;

        TextView view = (TextView) getView().findViewById(R.id.temperature_range);
        if (Math.round(weatherInfo.mainInfo.tempMin) == Math.round(weatherInfo.mainInfo.tempMax))
            view.setText(Integer.toString(Math.round(weatherInfo.mainInfo.temp)) + "°C");
        else
            view.setText(String.format(getString(R.string.temperature_range), Math.round(weatherInfo.mainInfo.tempMin), Math.round(weatherInfo.mainInfo.tempMax)));
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        updateBackground();
    }

    public void setActive(boolean active) {
        this.active = active;
        if (getView() != null)
            getView().setSelected(active);
    }

    void updateBackground(WeatherView view) {
        if (timeOfDay != null) {
            // TODO add transition effect
            view.setTimeOfDay(timeOfDay);/*
            ((ImageView) ((View) view).findViewById(R.id.weather_image)).setImageResource(
                    timeOfDay.weatherPictures[weatherImg]);*/
            ((TextView) ((View) view).findViewById(R.id.time_of_day)).setText(
                    getResources().getStringArray(R.array.times_of_day)[timeOfDay.ordinal()]);
        }
    }

    void updateBackground() {
        if (getView() != null)
            updateBackground((WeatherView) getView().findViewById(R.id.weather_view));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.weather_soon, container);
        updateBackground((WeatherView) result);
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onActivate(WeatherSoon.this);
            }
        });
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (active)
            callbacks.onActivate(this);
        callbacks.addBriefView(timeOfDay, view);
    }

    public interface Callbacks {
        void onActivate(WeatherSoon activated);

        void addBriefView(TimeOfDay timeOfDay, View view);
    }
}
