package ru.ifmo.md.lesson8;

/**
 * Created by dimatomp on 06.12.14.
 */
public interface DateSelector extends WeatherView {
    long getDate();

    void setMaxDate(long maxDate);

    void setMinDate(long minDate);

    void setOnTimeChangedListener(OnTimeChangedListener listener);

    void setDate(long date, boolean arg1, boolean arg2);

    interface OnTimeChangedListener {
        void onTimeChanged(DateSelector selector, long newTime);
    }
}
