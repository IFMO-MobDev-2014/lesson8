package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherDataAdapter extends RecyclerView.Adapter<WeatherDataAdapter.WeatherDataViewHolder> {
    //public static final int LIGHT_BLUE_COLOR = 0xff4767bb;
    //public static final int DARK_BLUE_COLOR = 0xffaba0ff;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE\nd\\MMM", Locale.US);
    private List<WeatherData> weatherData = new ArrayList<>();
    private Activity parent;

    public WeatherDataAdapter(Activity parent) {
        this.parent = parent;
    }

    public void add(WeatherData w) {
        weatherData.add(w);
    }

    public void clear() {
        weatherData.clear();
    }

    public WeatherData getItem(int pos) {
        return weatherData.get(pos);
    }

    @Override
    public WeatherDataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_line_weather, viewGroup, false);
        return new WeatherDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeatherDataViewHolder weatherDataViewHolder, int i) {
        WeatherData currentWeatherData = weatherData.get(i);
        weatherDataViewHolder.setTemperature(
                WeatherData.formatTemperature(currentWeatherData.getTemperatureMin()) + " \\ " +
                WeatherData.formatTemperature(currentWeatherData.getTemperatureMax()));
        Date date = new Date(currentWeatherData.getDate());
        weatherDataViewHolder.setDateTextView(DATE_FORMAT.format(date));
        //weatherDataViewHolder.setDayTextView(DAY_FORMAT.format(date).toUpperCase());
        AssetManager manager = parent.getAssets();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(manager.open(currentWeatherData.getWeatherInfo().getIconName()));
            DisplayMetrics metrics = new DisplayMetrics();
            parent.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Configuration config = parent.getResources().getConfiguration();
            weatherDataViewHolder.setIcon(new BitmapDrawable(new Resources(manager, metrics, config), bitmap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherData.size();
    }

    public static class WeatherDataViewHolder extends RecyclerView.ViewHolder {
        public View view;
        private TextView dateTextView;
        private TextView temperatureTextView;
        private ImageView iconView;

        public WeatherDataViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            temperatureTextView = (TextView) itemView.findViewById(R.id.temperatureTextView);
            iconView = (ImageView) itemView.findViewById(R.id.weatherIcon);
        }

        public void setTemperature(String temperature) {
            temperatureTextView.setText(temperature);
        }

        public void setDateTextView(String date) {
            dateTextView.setText(date);
        }

        public void setIcon(Drawable drawable) {
            iconView.setBackground(drawable);
        }
    }
}
