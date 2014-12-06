package ru.ifmo.md.lesson8;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.WeatherViewHolder> {

    static class WeatherEntry {
        public byte[] icon;
        public String text, wday;
        public int year, yday;

        public WeatherEntry() {}
        public WeatherEntry(byte[] icon, String text, String wday, int year, int yday) {
            this.icon = icon;
            this.text = text;
            this.wday = wday;
            this.year = year;
            this.yday = yday;
        }

        public String getDayMonth() {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.DAY_OF_YEAR, yday);
            SimpleDateFormat format = new SimpleDateFormat("d");
            String date = format.format(new Date());

            if(date.endsWith("1") && !date.endsWith("11"))
                return (new SimpleDateFormat("MMMM d'st'", Locale.US)).format(c.getTime());
            else if(date.endsWith("2") && !date.endsWith("12"))
                return (new SimpleDateFormat("MMMM d'nd'", Locale.US)).format(c.getTime());
            else if(date.endsWith("3") && !date.endsWith("13"))
                return (new SimpleDateFormat("MMMM d'rd'", Locale.US)).format(c.getTime());
            else
                return (new SimpleDateFormat("MMMM d'th'", Locale.US)).format(c.getTime());
        }
    }

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

    public ArrayList<WeatherEntry> mDataset = new ArrayList<>();
    public final String name, zmw;
    private Context mainContext;
    private Fragment parent;

    public ForecastAdapter(Context c, Fragment parent, String name, String zmw) {
        this.name = name;
        this.zmw = zmw;
        mainContext = c;
        this.parent = parent;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ForecastAdapter.WeatherViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_cards, parent, false);

        return new WeatherViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(WeatherViewHolder holder, int position) {
        Calendar c = Calendar.getInstance();
        Calendar cTomorrow = Calendar.getInstance();
        cTomorrow.add(Calendar.DATE, 1);

        if (c.get(Calendar.DAY_OF_YEAR) == mDataset.get(position).yday &&
                c.get(Calendar.YEAR) == mDataset.get(position).year) {
            if (mDataset.get(position).wday.endsWith("Night"))
                holder.day.setText(mainContext.getString(R.string.today_night));
            else
                holder.day.setText(mainContext.getString(R.string.today));
        }
        else if (cTomorrow.get(Calendar.DAY_OF_YEAR) == mDataset.get(position).yday &&
                cTomorrow.get(Calendar.YEAR) == mDataset.get(position).year) {
            if (mDataset.get(position).wday.endsWith("Night"))
                holder.day.setText(mainContext.getString(R.string.tomorrow_night));
            else
                holder.day.setText(mainContext.getString(R.string.tomorrow));
        } else {
            holder.day.setText(mDataset.get(position).wday + ", " +
                                  mDataset.get(position).getDayMonth());
        }

        holder.desc.setText(mDataset.get(position).text);
        holder.icon.setImageBitmap(BitmapFactory.decodeByteArray(mDataset.get(position).icon,
                0, mDataset.get(position).icon.length));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
