package ru.ifmo.md.lesson8;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.ifmo.md.lesson8.dummy.DummyContent;

/**
 * Created by 107476 on 11.01.2015.
 */
public class ForecastAdapter extends ArrayAdapter<DummyContent.ForecastItem> {

    ArrayList<DummyContent.ForecastItem> forecast;
    public ForecastAdapter(Context context, ArrayList<DummyContent.ForecastItem> forecast) {
        super(context, R.layout.forecast_element, forecast);
        this.forecast = forecast;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.forecast_element, parent, false);
        TextView lowTemp = (TextView) convertView.findViewById(R.id.low);
        TextView highTemp = (TextView) convertView.findViewById(R.id.high);
        TextView date = (TextView) convertView.findViewById(R.id.forDate);
        ImageView icon = (ImageView) convertView.findViewById(R.id.forIcon);
        lowTemp.setText("Low: "  + forecast.get(position).lowTemp + " C");
        highTemp.setText("High: " + forecast.get(position).highTemp + " C");
        Date date1 = new Date(forecast.get(position).date*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
        String formattedDate = sdf.format(date1);
        date.setText(formattedDate);
        switch (forecast.get(position).type) {
            case "01d":
                icon.setImageResource(R.drawable.pic01d);
                break;
            case "01n":
                icon.setImageResource(R.drawable.pic01n);
                break;
            case "02d":
                icon.setImageResource(R.drawable.pic02d);
                break;
            case "02n":
                icon.setImageResource(R.drawable.pic02n);
                break;
            case "03d":
                icon.setImageResource(R.drawable.pic03d);
                break;
            case "03n":
                icon.setImageResource(R.drawable.pic03d);
                break;
            case "04d":
                icon.setImageResource(R.drawable.pic03d);
                break;
            case "04n":
                icon.setImageResource(R.drawable.pic03d);
                break;
            case "09d":
                icon.setImageResource(R.drawable.pic09d);
                break;
            case "09n":
                icon.setImageResource(R.drawable.pic09d);
                break;
            case "10d":
                icon.setImageResource(R.drawable.pic10d);
                break;
            case "10n":
                icon.setImageResource(R.drawable.pic10n);
                break;
            case "11d":
                icon.setImageResource(R.drawable.pic11d);
                break;
            case "11n":
                icon.setImageResource(R.drawable.pic11d);
                break;
            case "13d":
                icon.setImageResource(R.drawable.pic13d);
                break;
            case "13n":
                icon.setImageResource(R.drawable.pic13d);
                break;
            case "50d":
                icon.setImageResource(R.drawable.pic50d);
                break;
            case "50n":
                icon.setImageResource(R.drawable.pic50d);
                break;
            default:
                icon.setImageResource(R.drawable.na);
                break;
        }
        return convertView;
    }
}
