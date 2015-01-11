package ru.ifmo.md.lesson8;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by izban on 09.01.15.
 */
public class CityAdapter extends ArrayAdapter<City> {
    public CityAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setTextColor(Color.WHITE);
        return view;
    }
}
