package com.alex700.AWeather;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Алексей on 07.12.2014.
 */
public class WeatherAdapter extends BaseAdapter {
    private List<WeatherData> data;
    private AssetManager manager;
    public WeatherAdapter(AssetManager manager) {
        super();
        data = new ArrayList<>();
        this.manager = manager;
    }

    public void add(WeatherData wd) {
        data.add(wd);
    }

    public void clear() {
        data.clear();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public WeatherData getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v;
        if (view == null) {
            v = android.view.LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.small_weather, viewGroup, false);
        } else {
            v = view;
        }
        WeatherData current = getItem(i);

        TextView textView = (TextView) v.findViewById(R.id.small_weather_data);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(current.getDate()));
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);

        textView.setText(day + " of " + month + ", " + dayOfWeek);

        textView = (TextView) v.findViewById(R.id.small_weather_temperature);
        textView.setText("temperature = " + current.getTString());

        textView = (TextView) v.findViewById(R.id.small_weather_humidity);
        textView.setText("humidity = " + (current.getHumidity()==0?"-":current.getHumidity() + "%"));

        textView = (TextView) v.findViewById(R.id.small_weather_wind_speed);
        textView.setText("wind speed = " + current.getWindSpeed() + "m/s");

        textView = (TextView) v.findViewById(R.id.small_weather_pressure);
        textView.setText("pressure = " + current.getPressure() + "mb");

        try {
            ((ImageView) v.findViewById(R.id.small_weather_image)).setImageBitmap(BitmapFactory.decodeStream(manager.open(current.getWeatherInfo().getIconName())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;
    }
}
