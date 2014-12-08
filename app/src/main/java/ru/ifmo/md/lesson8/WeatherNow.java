package ru.ifmo.md.lesson8;

import android.app.Fragment;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherNow extends Fragment implements ViewTreeObserver.OnPreDrawListener {
    float windAngle;
    String weatherImg;
    private WeatherView.TimeOfDay timeOfDay;

    public void setTimeOfDay(WeatherView.TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        updateBackground();
    }

    public void inflateWeatherInfo(WeatherInfo weatherInfo) {
        if (weatherInfo != null) {
            ((TextView) getView().findViewById(R.id.temperature)).setText(
                    String.format(getString(R.string.single_temperature), (int) weatherInfo.mainInfo.temp));
            ((TextView) getView().findViewById(R.id.wind_speed)).setText(
                    String.format(getString(R.string.wind_speed), weatherInfo.wind.speed));
            ((TextView) getView().findViewById(R.id.humidity)).setText(
                    String.format(getString(R.string.humidity), (int) weatherInfo.mainInfo.humidity));
            ((TextView) getView().findViewById(R.id.pressure)).setText(
                    String.format(getString(R.string.pressure), (int) (weatherInfo.mainInfo.pressure * 0.75)));
            ((TextView) getView().findViewById(R.id.weather_description)).setText(weatherInfo.description.description);
            weatherImg = weatherInfo.description.icon;
            // TODO Verify correctness with another forecast service
            windAngle = -45 - weatherInfo.wind.deg;
        } else {
            ((TextView) getView().findViewById(R.id.temperature)).setText("");
            ((TextView) getView().findViewById(R.id.wind_speed)).setText("");
            ((TextView) getView().findViewById(R.id.humidity)).setText("");
            ((TextView) getView().findViewById(R.id.pressure)).setText("");
            ((TextView) getView().findViewById(R.id.weather_description)).setText("");
            weatherImg = null;
            windAngle = -45;
        }
        onPreDraw();
        updateBackground();
    }

    void updateBackground() {
        if (getView() != null && timeOfDay != null) {
            WeatherView view = (WeatherView) getView();
            view.setTimeOfDay(timeOfDay);
            ((ImageView) getView().findViewById(R.id.weather_image)).setImageResource(
                    weatherImg != null
                            ? getResources().getIdentifier("weather_" + weatherImg.substring(0, 2)
                                    + (timeOfDay == WeatherView.TimeOfDay.NIGHT ? "n" : "d"), "drawable",
                            getActivity().getPackageName())
                            : R.drawable.na);
        }
    }

    @Override
    public boolean onPreDraw() {
        if (getView() == null || getView().findViewById(R.id.wind_direction) == null)
            return false;
        ImageView arrow = (ImageView) getView().findViewById(R.id.wind_direction);
        Matrix turnMatrix = new Matrix();
        float scale = 1.2f * arrow.getMeasuredHeight() / arrow.getDrawable().getIntrinsicHeight();
        turnMatrix.setScale(scale, scale);
        turnMatrix.postScale((float) Math.sqrt(0.4), (float) Math.sqrt(0.4),
                arrow.getMeasuredWidth() / 2, arrow.getMeasuredHeight() / 2);
        turnMatrix.postRotate(windAngle, arrow.getMeasuredWidth() / 2, arrow.getMeasuredHeight() / 2);
        arrow.setImageMatrix(turnMatrix);
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.weather_now, container);
        result.getViewTreeObserver().addOnPreDrawListener(this);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        updateBackground();
    }
}
