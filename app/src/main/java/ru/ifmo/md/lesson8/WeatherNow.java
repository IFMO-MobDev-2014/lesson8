package ru.ifmo.md.lesson8;

import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class WeatherNow extends Fragment {
    private WeatherSoon tab;

    public void setActiveTab(WeatherSoon tab) {
        if (this.tab != null)
            this.tab.setActive(false);
        this.tab = tab;
        tab.setActive(true);
        updateBackground();
    }

    void updateBackground(View view) {
        if (tab != null && tab.timeOfDay != null) {
            // TODO add transition effect
            view.setBackgroundResource(tab.timeOfDay.mainBackground);
            ((ImageView) view.findViewById(R.id.weather_image)).setImageResource(
                    tab.timeOfDay.weatherPictures[tab.weatherImg]);
        }
    }

    void updateBackground() {
        updateBackground(getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.weather_now, container);
        final ImageView arrow = (ImageView) result.findViewById(R.id.wind_direction);
        final Matrix turnMatrix = new Matrix();
        arrow.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                float scale = (float) arrow.getMeasuredHeight() / arrow.getDrawable().getMinimumHeight();
                turnMatrix.setScale(scale, scale);
                turnMatrix.postRotate(-90, arrow.getMeasuredWidth() / 2, arrow.getMeasuredHeight() / 2);
                arrow.setScaleType(ImageView.ScaleType.MATRIX);
                arrow.setImageMatrix(turnMatrix);
                return true;
            }
        });
        updateBackground(result);
        return result;
    }
}
