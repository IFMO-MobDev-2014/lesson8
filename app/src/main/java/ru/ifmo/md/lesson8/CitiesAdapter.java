package ru.ifmo.md.lesson8;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by dimatomp on 30.11.14.
 */
public class CitiesAdapter extends FragmentStatePagerAdapter {
    ArrayList<String> fragments = new ArrayList<>();

    public CitiesAdapter(FragmentManager fm) {
        super(fm);
    }

    public CityWeather createCity(String cityName) {
        CityWeather fragment = new CityWeather();
        Bundle args = new Bundle(1);
        args.putString("cityName", cityName);
        fragment.setArguments(args);
        return fragment;
    }

    public void addCity(String cityName) {
        fragments.add(cityName);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        fragments.remove(index);
        notifyDataSetChanged();
    }

    public void clear() {
        fragments.clear();
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return createCity(fragments.get(position));
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
