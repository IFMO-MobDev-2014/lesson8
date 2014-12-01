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
    ArrayList<Fragment> fragments = new ArrayList<>();
    FragmentManager manager;

    public CitiesAdapter(FragmentManager fm) {
        super(fm);
        this.manager = fm;
    }

    public void addCity(String cityName) {
        CityWeather fragment = new CityWeather();
        Bundle args = new Bundle(1);
        args.putString("cityName", cityName);
        fragment.setArguments(args);
        manager.beginTransaction().attach(fragment).commit();
        fragments.add(fragment);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        remove(index, true);
    }

    private void remove(int index, boolean notify) {
        manager.beginTransaction().remove(fragments.get(index));
        fragments.remove(index);
        if (notify)
            notifyDataSetChanged();
    }

    public void clear() {
        for (int i = fragments.size() - 1; i >= 0; i--)
            remove(i, false);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
