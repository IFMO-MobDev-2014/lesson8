package ru.ifmo.md.weather;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ifmo.md.weather.db.model.CityTable;

/**
 * Created by Kirill on 01.12.2014.
 */
public class CityCursorAdapter extends CursorAdapter {
    private LayoutInflater inflater;
    private Context context;

    public CityCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.city_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(CityTable.NAME_COLUMN));
        String temp = cursor.getString(cursor.getColumnIndex(CityTable.WEATHER_TEMP_COLUMN));
        temp += "°C";
        String iconName = cursor.getString(cursor.getColumnIndex(CityTable.WEATHER_ICON_NAME_COLUMN));

        TextView nameView = (TextView) view.findViewById(R.id.city_name);
        nameView.setText(name);
        TextView tempView = (TextView) view.findViewById(R.id.city_temp);
        tempView.setText(temp);
        ImageView imageView = (ImageView) view.findViewById(R.id.city_weather_icon);
        int id = context.getResources().getIdentifier("_" + iconName, "drawable", context.getPackageName());
        imageView.setImageResource(id);
    }
}
