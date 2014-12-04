package ru.ifmo.md.lesson8;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dimatomp on 02.12.14.
 */
public class SpinnerAdapter extends BaseAdapter {
    final Context context;
    ArrayList<String> cities = new ArrayList<>();

    public SpinnerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return cities.size();
    }

    @Override
    public Object getItem(int position) {
        return cities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(getItem(position).toString());
        return convertView;
    }

    public int addCity(String cityName) {
        for (int i = 0; i < cities.size(); i++)
            if (cities.get(i).equals(cityName))
                return i;
        int result = cities.size();
        cities.add(cityName);
        notifyDataSetChanged();
        return result;
    }

    public void removeCity(int number) {
        cities.remove(number);
        notifyDataSetChanged();
    }
}
