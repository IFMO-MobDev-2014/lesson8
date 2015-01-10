package ru.ifmo.md.lesson8;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by izban on 09.01.15.
 */
public class ItemAdapter extends ArrayAdapter<Item> {

    public ItemAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //View view = super.getView(position, convertView, parent);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        Item item = getItem(position);
        TextView date = (TextView)view.findViewById(R.id.textView);
        TextView temp = (TextView)view.findViewById(R.id.textView2);
        TextView cond = (TextView)view.findViewById(R.id.textView3);

        date.setText(new SimpleDateFormat("EEE, dd MMM yyyy").format(new Date(item.date)));
        temp.setText(item.low + " C ~ " + item.high + " C");
        cond.setText(item.text);

        return view;
    }
}
