package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DayCursorAdapter extends CursorAdapter {
    public DayCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView conditionImage = (ImageView) view.findViewById(R.id.condition_image2);
        String conditionCode = "w" + cursor.getString(cursor.getColumnIndex("condition_code"));
        conditionImage.setImageResource(context.getResources().getIdentifier(conditionCode, "drawable", context.getPackageName()));

        TextView dayName = (TextView) view.findViewById(R.id.day_name);
        dayName.setText(dayNameConvert(cursor.getString(cursor.getColumnIndex("day_name"))));

        TextView forecast = (TextView) view.findViewById(R.id.forecast);
        forecast.setText(cursor.getString(cursor.getColumnIndex("forecast")));
    }

    public String dayNameConvert(String shortName) {
        if (shortName.equals("Mon")) {
            return "Monday";
        } else if (shortName.equals("Tue")) {
            return "Tuesday";
        } else if (shortName.equals("Wed")) {
            return "Wednesday";
        } else if (shortName.equals("Thu")) {
            return "Thursday";
        } else if (shortName.equals("Fri")) {
            return "Friday";
        } else if (shortName.equals("Sat")) {
            return "Saturday";
        } else {
            return "Sunday";
        }
    }
}