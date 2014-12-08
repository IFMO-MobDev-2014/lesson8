package ru.ifmo.md.lesson8;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by dimatomp on 08.12.14.
 */
public class LinearLayoutWithGradient extends LinearLayout implements WeatherView {
    TimeOfDay timeOfDay;

    public LinearLayoutWithGradient(Context context) {
        super(context);
    }

    public LinearLayoutWithGradient(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (timeOfDay != null) {
            int[] result = super.onCreateDrawableState(extraSpace + 1);
            return mergeDrawableStates(result, new int[]{DAYTIME_STATE_SET[timeOfDay.ordinal()]});
        }
        return super.onCreateDrawableState(extraSpace);
    }

    @Override
    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        refreshDrawableState();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        ViewGroup parent = (ViewGroup) getParent();
        setBackground(new AlphaGradientDrawable(
                parent.findViewById(R.id.brief_fragments).getTop(),
                parent.findViewById(R.id.detailed_fragment).getBackground(),
                parent.findViewById(R.id.morning_tab).getBackground()
        ));
        setSelected(true);
    }
}
