package ru.ifmo.md.lesson8;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by dimatomp on 29.11.14.
 */
public class LinearWeatherLayout extends LinearLayout implements WeatherView {
    TimeOfDay timeOfDay;

    public LinearWeatherLayout(Context context) {
        super(context);
    }

    public LinearWeatherLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (timeOfDay != null) {
            int[] result = super.onCreateDrawableState(extraSpace + 1);
            return mergeDrawableStates(result, new int[]{DAYTIME_STATE_SET[timeOfDay.ordinal()]});
        }
        return super.onCreateDrawableState(extraSpace);
    }
}
