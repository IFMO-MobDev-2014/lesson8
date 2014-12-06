package ru.ifmo.md.lesson8;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class MyStringAdapter extends ArrayAdapter<String> {

    public MyStringAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItem(position).equals("No cities with this prefix :(") ||
                getItem(position).equals("Loading city list..."))
            return false;
        return true;
    }
}
