package ru.ifmo.md.lesson8;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
    ArrayList<String> citiesName = new ArrayList<>();
    ArrayList<String> citiesCode = new ArrayList<>();
    private Context mainContext;
    private SwipeFragment drawer;

    public CitiesAdapter(Context c, SwipeFragment ndf) {
        mainContext = c;
        drawer = ndf;

        Cursor cursor = mainContext.getContentResolver().
                query(WeatherContentProvider.CITIES_URI, null, null, null, MyDatabase._ID);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                citiesName.add(cursor.getString(cursor.getColumnIndex(MyDatabase.NAME)));
                citiesCode.add(cursor.getString(cursor.getColumnIndex(MyDatabase.CODE)));
            }
            cursor.close();
        }
    }

    public void addCity(String c, String code) {
        citiesName.add(c);
        citiesCode.add(code);
        drawer.mCurrentSelectedPosition = citiesName.size() - 1;

        ContentValues cv = new ContentValues();
        cv.put(MyDatabase.NAME, c);
        cv.put(MyDatabase.CODE, code);
        mainContext.getContentResolver().insert(WeatherContentProvider.CITIES_URI, cv);

        Intent loadForecast = new Intent(mainContext, WeatherIntentService.class);
        loadForecast.putExtra(MyDatabase.NAME, c);
        loadForecast.putExtra(MyDatabase.CODE, code);
        loadForecast.putExtra("force", true);
        mainContext.startService(loadForecast);
    }

    public void delCity(String name) {
        int pos = citiesName.indexOf(name);
        mainContext.getContentResolver().delete(WeatherContentProvider.CITIES_URI,
                MyDatabase.CODE + "=?", new String[]{citiesCode.get(pos)});

        if (pos != -1) {
            citiesName.remove(pos);
            citiesCode.remove(pos);
            drawer.displayFragment(Math.min(drawer.mCurrentSelectedPosition, citiesName.size() - 1));
        }

        notifyDataSetChanged();
    }

    public void goTo(String s) {
        int pos = citiesName.indexOf(s);
        if (pos != -1) {
            drawer.selectItem(pos);
            drawer.mCurrentSelectedPosition = pos;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return citiesName.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= citiesName.size())
            return null;

        return citiesName.get(position);
    }

    public Object getItemZMW(int position) {
        if (position >= citiesCode.size())
            return null;

        return citiesCode.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String cityName = citiesName.get(position);

        RelativeLayout entry = (RelativeLayout)
                LayoutInflater.from(mainContext).inflate(R.layout.item_city, null);
        ImageButton button = (ImageButton) entry.findViewById(R.id.deleteCity);
        button.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        button.setOnClickListener(new OnCityDelete(this, cityName));
        TextView text = (TextView) entry.findViewById(R.id.cityName);
        text.setText(cityName);
        text.setOnClickListener(new OnCityChoose(this, cityName));
        if (position == drawer.mCurrentSelectedPosition) {
            entry.setBackgroundColor(mainContext.getResources().getColor(R.color.transperent_gray));
        }
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

    static class OnCityDelete implements View.OnClickListener {
        CitiesAdapter parent;
        String name;

        public OnCityDelete(CitiesAdapter parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        @Override
        public void onClick(View v) {
            parent.delCity(name);
        }
    }
}
