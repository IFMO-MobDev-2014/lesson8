package ru.ifmo.md.lesson8;

/**
 * Created by dimatomp on 29.11.14.
 */
public interface WeatherView {
    int[] DAYTIME_STATE_SET = new int[]{R.attr.state_night, R.attr.state_morning, R.attr.state_daytime, R.attr.state_evening};

    void setTimeOfDay(TimeOfDay timeOfDay);
}
