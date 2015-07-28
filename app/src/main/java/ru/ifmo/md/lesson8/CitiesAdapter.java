package ru.ifmo.md.lesson8;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CitiesAdapter extends BaseAdapter {
    private int selected = 0;
    private List<City> cityList = new ArrayList<>();

    public CitiesAdapter() {

    }

    public void clear() {
        cityList.clear();
    }

    public void add(City city) {
        cityList.add(city);
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    @Override
    public int getCount() {
        return cityList.size();
    }

    @Override
    public City getItem(int i) {
        return cityList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d("CitiesAdapter", "getting View");
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.city_adapter_item, viewGroup, false);
        }
        TextView itemDetail = (TextView) view.findViewById(R.id.item_detail);
        itemDetail.setText(cityList.get(i).getName());
        Log.d("CitiesAdapter", "City: " + cityList.get(i).getName());
        if (i == selected) {
            itemDetail.setBackgroundColor(Color.parseColor("#1E88E5"));
        } else {
            itemDetail.setBackgroundColor(Color.TRANSPARENT);
        }
        return view;
    }
}
