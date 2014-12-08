package ru.ifmo.md.lesson8.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ru.ifmo.md.lesson8.R;

/**
 * Created by mariashka on 11/30/14.
 */
public class WeatherListAdapter extends BaseAdapter {

    List<WeatherItem> data;
    public WeatherListAdapter(List<WeatherItem> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View l = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city, parent, false);

        WeatherItem item = (WeatherItem) getItem(position);


        TextView title = (TextView) l.findViewById(R.id.textView2);
        title.setText(item.getName());
        return l;
    }
}
