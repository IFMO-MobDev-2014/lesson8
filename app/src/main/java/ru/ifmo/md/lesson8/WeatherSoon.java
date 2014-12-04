package ru.ifmo.md.lesson8;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dimatomp on 28.11.14.
 */
public class WeatherSoon extends Fragment {
    WeatherView.TimeOfDay timeOfDay;
    boolean active;
    WeatherInfo weatherInfo;

    public WeatherInfo getWeatherInfo() {
        return weatherInfo;
    }

    public void setWeatherInfo(WeatherInfo weatherInfo) {
        this.weatherInfo = weatherInfo;

        TextView view = (TextView) getView().findViewById(R.id.temperature_range);
        if (weatherInfo != null) {
            if (Math.round(weatherInfo.mainInfo.tempMin) == Math.round(weatherInfo.mainInfo.tempMax))
                view.setText(Integer.toString(Math.round(weatherInfo.mainInfo.temp)) + "°C");
            else
                view.setText(String.format(getString(R.string.temperature_range), Math.round(weatherInfo.mainInfo.tempMin), Math.round(weatherInfo.mainInfo.tempMax)));
        } else {
            view.setText("");
        }
        updateBackground();
    }

    public WeatherView.TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(WeatherView.TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        updateBackground();
    }

    public void setActive(boolean active) {
        this.active = active;
        if (getView() != null)
            getView().setSelected(active);
    }

    void updateBackground() {
        if (getView() != null && timeOfDay != null) {
            WeatherView view = (WeatherView) getView();
            view.setTimeOfDay(timeOfDay);

            ImageView imageView = (ImageView) getView().findViewById(R.id.weather_image);
            if (weatherInfo != null)
                imageView.setImageResource(getResources().getIdentifier(
                        "weather_" + weatherInfo.description.icon.substring(0, 2)
                                + (timeOfDay == WeatherView.TimeOfDay.NIGHT ? "n" : "d")
                        , "drawable", getActivity().getPackageName()));
            else
                imageView.setImageResource(R.drawable.na);
            ((TextView) getView().findViewById(R.id.time_of_day)).setText(
                    getResources().getStringArray(R.array.times_of_day)[timeOfDay.ordinal()]);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.weather_soon, container);
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WeatherActivity) getActivity()).onActivate(WeatherSoon.this);
            }
        });
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        updateBackground();
    }
}
