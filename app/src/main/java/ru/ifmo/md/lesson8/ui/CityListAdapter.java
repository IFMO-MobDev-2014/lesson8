package ru.ifmo.md.lesson8.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.content.ContentHelper;
import ru.ifmo.md.lesson8.net.YahooQuery;
import ru.ifmo.md.lesson8.places.Place;
import ru.ifmo.md.lesson8.weather.Temperature;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class CityListAdapter extends CursorAdapter {
    private final ContentHelper contentHelper;

    public CityListAdapter(Context context, Cursor c) {
        super(context, c, 0);

        this.contentHelper = new ContentHelper(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = View.inflate(context, R.layout.place_row, null);
        bindView(view, context, cursor);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView placeName = (TextView) view.findViewById(R.id.place_name);
        final TextView temperature = (TextView) view.findViewById(R.id.temperature);
        TextView condition = (TextView) view.findViewById(R.id.condition);

        final Place place = contentHelper.getPlace(cursor);
        placeName.setText(place.formattedName());

        temperature.setText(contentHelper.getWeatherInPlace(place).getTemperature()
                .representAs(Temperature.celsius()));
    }
}
