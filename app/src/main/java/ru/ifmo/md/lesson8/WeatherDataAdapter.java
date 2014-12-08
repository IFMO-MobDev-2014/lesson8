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

public class WeatherDataAdapter extends RecyclerView.Adapter<WeatherDataAdapter.WeatherDataViewHolder> {
    public static final int LIGHT_BLUE_COLOR = 0xff4767bb;
    public static final int DARK_BLUE_COLOR = 0xffaba0ff;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d\\MMM");
    public static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEE");
    private List<WeatherData> weatherData = new ArrayList<>();
    private ArrayList<View> views = new ArrayList<>();
    private Activity parent;
    private OnItemClickListener listener;
    private int prevPos = -1;

    public WeatherDataAdapter(Activity parent) {
        this.parent = parent;
    }

    public void add(WeatherData w) {
        weatherData.add(w);
        views.add(null);
    }

    public void clear() {
        weatherData.clear();
        views.clear();
    }

    public WeatherData getItem(int pos) {
        return weatherData.get(pos);
    }

    public void setCurrentItem(int newPos) {
        if (prevPos != newPos) {
            if (prevPos != -1 && prevPos < views.size() && views.get(prevPos) != null) {
                views.get(prevPos).setBackgroundColor(LIGHT_BLUE_COLOR);
            }

            if (views.get(newPos) != null) {
                views.get(newPos).setBackgroundColor(DARK_BLUE_COLOR);
            }

            prevPos = newPos;
        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    @Override
    public WeatherDataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_min_weather, viewGroup, false);
        return new WeatherDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeatherDataViewHolder weatherDataViewHolder, int i) {
        WeatherData currentWeatherData = weatherData.get(i);
        weatherDataViewHolder.setTemperature(
                Integer.toString(currentWeatherData.getTemperature()) + "°C");
        Date date = new Date(currentWeatherData.getDate());
        weatherDataViewHolder.setDateTextView(DATE_FORMAT.format(date));
        weatherDataViewHolder.setDayTextView(DAY_FORMAT.format(date).toUpperCase());
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
        views.set(i, weatherDataViewHolder.view);
        if (prevPos == i) {
            weatherDataViewHolder.view.setBackgroundColor(DARK_BLUE_COLOR);
        } else {
            weatherDataViewHolder.view.setBackgroundColor(LIGHT_BLUE_COLOR);
        }
    }

    @Override
    public int getItemCount() {
        return weatherData.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    public class WeatherDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View view;
        private TextView dayTextView;
        private TextView dateTextView;
        private TextView temperatureTextView;
        private ImageView iconView;

        public WeatherDataViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            dayTextView = (TextView) itemView.findViewById(R.id.dayTextView);
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            temperatureTextView = (TextView) itemView.findViewById(R.id.temperatureTextView);
            iconView = (ImageView) itemView.findViewById(R.id.weatherIcon);
            itemView.setOnClickListener(this);
        }

        public void setTemperature(String temperature) {
            temperatureTextView.setText(temperature);
        }

        public void setDateTextView(String date) {
            dateTextView.setText(date);
        }

        public void setDayTextView(String day) {
            dayTextView.setText(day);
        }

        public void setIcon(Drawable drawable) {
            iconView.setBackground(drawable);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onItemClick(view, getPosition());
            }
        }
    }
}
