package ru.ifmo.md.lesson8.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.content.ContentHelper;
import ru.ifmo.md.lesson8.places.Place;
import ru.ifmo.md.lesson8.weather.Temperature;
import ru.ifmo.md.lesson8.weather.Weather;

/**
 * @author Zakhar Voit (zakharvoit@gmail.com)
 */
public class PlacesListAdapter extends CursorAdapter {
    private final ContentHelper contentHelper;

    public PlacesListAdapter(Context context, Cursor c) {
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
        TextView temperature = (TextView) view.findViewById(R.id.temperature);
        TextView condition = (TextView) view.findViewById(R.id.condition);
        ImageView conditionPicture = (ImageView) view.findViewById(R.id.condition_picture);

        final Place place = contentHelper.getPlace(cursor);
        placeName.setText(place.formattedName());

        Weather weather = contentHelper.getWeatherInPlace(place);
        if (weather == null) {
            temperature.setText("Fetching...");
            condition.setText("");
            conditionPicture.setAlpha(1.0f);
        } else {
            temperature.setText(weather.getCurrent()
                    .representAs(Temperature.celsius()));
            condition.setText(weather.getDescription());
            conditionPicture.setImageResource(
                    ContentHelper.getResourceForConditionString(weather.getDescription()));
        }
    }
}
