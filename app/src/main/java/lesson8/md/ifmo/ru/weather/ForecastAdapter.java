package lesson8.md.ifmo.ru.weather;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.WeatherViewHolder> {

    static class WeatherEntry {
        public String icon, text, wday;
        public int year, yday;

        public WeatherEntry() {}
        public WeatherEntry(String icon, String text, String wday, int year, int yday) {
            this.icon = icon;
            this.text = text;
            this.wday = wday;
            this.year = year;
            this.yday = yday;
        }
    }

    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        protected TextView desc, day;

        public WeatherViewHolder(View v) {
            super(v);
            desc = (TextView) v.findViewById(R.id.info_text);
            day = (TextView) v.findViewById(R.id.day);
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
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.desc.setText(mDataset.get(position).text);
        holder.day.setText(mDataset.get(position).wday);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
