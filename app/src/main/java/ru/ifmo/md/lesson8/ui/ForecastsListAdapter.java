package ru.ifmo.md.lesson8.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.content.ContentHelper;
import ru.ifmo.md.lesson8.weather.Forecast;
import ru.ifmo.md.lesson8.weather.Temperature;
import ru.ifmo.md.lesson8.weather.Weather;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class ForecastsListAdapter extends CursorAdapter {
    public ForecastsListAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.forecast_day, null);
        bindView(view, context, cursor);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView date = (TextView) view.findViewById(R.id.forecast_date);
        TextView temperature = (TextView) view.findViewById(R.id.high_low_temperature);
        TextView condition = (TextView) view.findViewById(R.id.condition_description);
        ImageView image = (ImageView) view.findViewById(R.id.description_image);

        Forecast forecast = ContentHelper.getForecast(cursor);
        Weather weather = forecast.getWeather();

        date.setText(new SimpleDateFormat("dd MMM").format(forecast.getDate()));
        if (weather.getLow().equals(weather.getHigh())) {
            temperature.setText(weather.getLow().representAs(Temperature.celsius()));
        } else {
            temperature.setText(weather.getLow().representAs(Temperature.celsius())
                    + " - " + weather.getHigh().representAs(Temperature.celsius()));
        }
        condition.setText(weather.getDescription());
        image.setImageResource(ContentHelper.getResourceForConditionString(weather.getDescription()));
    }
}
