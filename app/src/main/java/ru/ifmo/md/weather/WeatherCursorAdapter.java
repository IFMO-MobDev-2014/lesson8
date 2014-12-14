package ru.ifmo.md.weather;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ru.ifmo.md.weather.db.model.WeatherTable;

/**
 * Created by Kirill on 01.12.2014.
 */
public class WeatherCursorAdapter extends CursorAdapter {
    private LayoutInflater inflater;
    private Context context;

    public WeatherCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.weather_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String time = getNormalTimeFromUnixStamp(cursor.getString(cursor.getColumnIndex(WeatherTable.WEATHER_TIME_COLUMN)));
        String temp = cursor.getString(cursor.getColumnIndex(WeatherTable.TEMP_COLUMN));
        String wind = cursor.getString(cursor.getColumnIndex(WeatherTable.WIND_SPEED_COLUMN));
        String pressure = cursor.getString(cursor.getColumnIndex(WeatherTable.PRESSURE_COLUMN));
        String humidity = cursor.getString(cursor.getColumnIndex(WeatherTable.HUMIDITY_COLUMN));
        String iconName = cursor.getString(cursor.getColumnIndex(WeatherTable.ICON_NAME_COLUMN));


        TextView timeView = (TextView) view.findViewById(R.id.forecast_time);
        timeView.setText(time);
        TextView tempView = (TextView) view.findViewById(R.id.forecast_temp);
        tempView.setText(temp);
        TextView windView = (TextView) view.findViewById(R.id.forecast_wind);
        windView.setText(wind);
        TextView pressureView = (TextView) view.findViewById(R.id.forecast_pressure);
        pressureView.setText(pressure);
        TextView humidityView = (TextView) view.findViewById(R.id.forecast_humidity);
        humidityView.setText(humidity);
        ImageView imageView = (ImageView) view.findViewById(R.id.forecast_icon);
        imageView.setImageResource(getDrawable(context, "_" + iconName));
    }

    private static int getDrawable(Context context, String name)
    {
        return context.getResources().getIdentifier(name,
                "drawable", context.getPackageName());
    }

    private static String getNormalTimeFromUnixStamp(String stamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        simpleDateFormat.setTimeZone(utcZone);
        Date myDate = null;
        try {
             myDate = simpleDateFormat.parse(stamp);
        } catch (ParseException e) {
            return "";
        }
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        String rv = simpleDateFormat.format(myDate);
        return rv;
    }

}

