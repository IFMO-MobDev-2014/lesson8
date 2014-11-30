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
    Callbacks callbacks;
    private TimeOfDay timeOfDay;
    private int weatherImg;

    public void setCallbackInstance(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        updateBackground();
    }

    public void setWeatherImg(int weatherImg) {
        this.weatherImg = weatherImg;
    }

    void updateBackground(WeatherView view) {
        if (timeOfDay != null) {
            // TODO add transition effect
            view.setTimeOfDay(timeOfDay);
            ((ImageView) ((View) view).findViewById(R.id.weather_image)).setImageResource(
                    timeOfDay.weatherPictures[weatherImg]);
        }
    }

    void updateBackground() {
        if (getView() != null)
            updateBackground((WeatherView) getView().findViewById(R.id.weather_view));
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
        updateBackground((WeatherView) result);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        callbacks.addDetailedView(view);
    }

    public interface Callbacks {
        void addDetailedView(View view);
    }
}
