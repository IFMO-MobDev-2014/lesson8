package ru.ifmo.md.lesson8;

/**
 * Created by dimatomp on 06.12.14.
 */
public interface DateSelector extends WeatherView {
    long getDate();

    void setMaxDate(long maxDate);

    void setOnTimeChangedListener(OnTimeChangedListener listener);

    void setDate(long date, boolean animate, boolean center);

    interface OnTimeChangedListener {
        void onTimeChanged(DateSelector selector, long newTime);
    }
}
