package ru.ifmo.md.lesson8;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CalendarView;

/**
 * Created by dimatomp on 29.11.14.
 */
public class CalendarWithBackground extends CalendarView implements WeatherView {
    TimeOfDay timeOfDay;

    public CalendarWithBackground(Context context) {
        super(context);
    }

    public CalendarWithBackground(Context context, AttributeSet attrs) {
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
