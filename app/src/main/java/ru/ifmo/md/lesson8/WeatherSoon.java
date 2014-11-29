package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dimatomp on 28.11.14.
 */
public class WeatherSoon extends Fragment {
    TimeOfDay timeOfDay;
    int weatherImg;
    boolean active;

    public void setWeatherImg(int weatherImg) {
        this.weatherImg = weatherImg;
        updateBackground();
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

    void updateBackground(View view) {
        if (timeOfDay != null) {
            // TODO add transition effect
            view.setBackgroundResource(timeOfDay.tabBackground);
            ((ImageView) view.findViewById(R.id.weather_image)).setImageResource(
                    timeOfDay.weatherPictures[weatherImg]);
            ((TextView) view.findViewById(R.id.time_of_day)).setText(
                    getResources().getStringArray(R.array.times_of_day)[timeOfDay.ordinal()]);
        }
    }

    void updateBackground() {
        updateBackground(getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.weather_soon, container);
        updateBackground(result);
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Callbacks) getActivity()).onActivate(WeatherSoon.this);
            }
        });
        result.setSelected(active);
        return result;
    }

    public interface Callbacks {
        void onActivate(WeatherSoon activated);
    }
}
