package ru.ifmo.md.lesson8;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by izban on 09.01.15.
 */
public class ItemAdapter extends ArrayAdapter<Item> {

    public ItemAdapter(Context context, int resource) {
        super(context, resource);
    }
}
