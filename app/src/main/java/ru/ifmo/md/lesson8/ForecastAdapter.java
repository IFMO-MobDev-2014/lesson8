package ru.ifmo.md.lesson8;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.WeatherViewHolder> {
    public ArrayList<WeatherContainer> weathers = new ArrayList<>();
    public final String name, code;
    private Context mainContext;
    private Fragment parent;

    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        protected TextView desc, day;
        protected ImageView icon;

        public WeatherViewHolder(View v) {
            super(v);
            desc = (TextView) v.findViewById(R.id.info_text);
            day = (TextView) v.findViewById(R.id.day);
            icon = (ImageView) v.findViewById(R.id.iconView);
        }
    }

    public ForecastAdapter(Context c, Fragment parent, String name, String code) {
        this.name = name;
        this.code = code;
        mainContext = c;
        this.parent = parent;
    }

    @Override
    public ForecastAdapter.WeatherViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);

        return new WeatherViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WeatherViewHolder holder, int position) {
        Calendar c = Calendar.getInstance();
        Calendar cTomorrow = Calendar.getInstance();
        cTomorrow.add(Calendar.DATE, 1);

        if (c.get(Calendar.DAY_OF_YEAR) == weathers.get(position).day &&
                c.get(Calendar.YEAR) == weathers.get(position).year) {
            if (weathers.get(position).weekday.endsWith("Night")) {
                holder.day.setText(mainContext.getString(R.string.today_night));
            } else {
                holder.day.setText(mainContext.getString(R.string.today));
            }
        }
        else if (cTomorrow.get(Calendar.DAY_OF_YEAR) == weathers.get(position).day &&
                cTomorrow.get(Calendar.YEAR) == weathers.get(position).year) {
            if (weathers.get(position).weekday.endsWith("Night"))
                holder.day.setText(mainContext.getString(R.string.tomorrow_night));
            else
                holder.day.setText(mainContext.getString(R.string.tomorrow));
        } else {
            holder.day.setText(weathers.get(position).weekday + ", " +
                                  weathers.get(position).getDayMonth());
        }

        holder.desc.setText(weathers.get(position).text);
        holder.icon.setImageBitmap(BitmapFactory.decodeByteArray(weathers.get(position).icon,
                0, weathers.get(position).icon.length));
    }

    @Override
    public int getItemCount() {
        return weathers.size();
    }
}
