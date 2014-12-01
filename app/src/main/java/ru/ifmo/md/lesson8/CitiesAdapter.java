package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CitiesAdapter extends BaseAdapter {
    ArrayList<String> citiesNames = new ArrayList<>();
    ArrayList<String> citiesZMW = new ArrayList<>();
    Context mainContext;
    NavigationDrawerFragment drawer;

    public CitiesAdapter(Context c, NavigationDrawerFragment ndf) {
        mainContext = c;
        drawer = ndf;

        Cursor cursor = mainContext.getContentResolver().
                query(WeatherProvider.CITIES_URI, null, null, null, WeatherProvider.NAME);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                citiesNames.add(cursor.getString(cursor.getColumnIndex(WeatherProvider.NAME)));
                citiesZMW.add(cursor.getString(cursor.getColumnIndex(WeatherProvider.ZMW)));
            }
            cursor.close();
        }
    }

    public void addCity(String c, String zwm) {
        citiesNames.add(c);
        citiesZMW.add(zwm);

        ContentValues cv = new ContentValues();
        cv.put(WeatherProvider.NAME, c);
        cv.put(WeatherProvider.ZMW, zwm);
        mainContext.getContentResolver().insert(WeatherProvider.CITIES_URI, cv);
    }

    public void delCity(String name) {
        int pos = citiesNames.indexOf(name);
        mainContext.getContentResolver().delete(WeatherProvider.CITIES_URI,
                                        WeatherProvider.ZMW + "=?", new String[]{citiesZMW.get(pos)});

        if (pos != -1) {
            citiesNames.remove(pos);
            citiesZMW.remove(pos);
        }
        notifyDataSetChanged();
    }

    public void goTo(String s) {
        int pos = citiesNames.indexOf(s);
        if (pos != -1)
            drawer.selectItem(pos - 1);
    }

    @Override
    public int getCount() {
        return citiesNames.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= citiesNames.size())
            return null;

        return citiesNames.get(position);
    }

    public Object getItemZMW(int position) {
        if (position >= citiesZMW.size())
            return null;

        return citiesZMW.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String cityName = citiesNames.get(position);

        RelativeLayout entry = (RelativeLayout)
                LayoutInflater.from(mainContext).inflate(R.layout.city_entry, null);
        ImageButton button = (ImageButton) entry.findViewById(R.id.delCity);
        button.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        button.setOnClickListener(new OnCityDel(this, cityName));
        TextView text = (TextView) entry.findViewById(R.id.cityName);
        text.setText(cityName);
        text.setOnClickListener(new OnCityChoose(this, cityName));
        return entry;
    }

    static class OnCityChoose implements View.OnClickListener {
        CitiesAdapter parent;
        String name;

        public OnCityChoose(CitiesAdapter parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        @Override
        public void onClick(View v) {
            parent.goTo(name);
        }
    }

    static class OnCityDel implements View.OnClickListener {
        CitiesAdapter parent;
        String name;

        public OnCityDel(CitiesAdapter parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        @Override
        public void onClick(View v) {
            parent.delCity(name);
        }
    }
}
