package com.pinguinson.lesson10.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinguinson.lesson10.R;
import com.pinguinson.lesson10.db.tables.ForecastsTable;

/**
 * Created by pinguinson.
 */
public class ForecastAdapter extends CursorAdapter {

    public static final String DEGREE = "°C";

    public ForecastAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    public static int getIconID(int num, Context context) {
        try {
            String imageName = "weather_" + Integer.toString(num);
            return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.forecast_list_item, parent, false);
        bindView(v, context, cursor);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int weekdayColumn = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_WEEKDAY);
        int dateColumn = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_DATE);
        int textColumn = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_TEXT);
        int lowTempColumn = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_LOW_TEMPERATURE);
        int highTempColumn = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_HIGH_TEMPERATURE);
        int conditionsColumn = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_CONDITIONS);

        String date = cursor.getString(weekdayColumn) + ", " + cursor.getString(dateColumn);
        String desc = cursor.getString(textColumn);
        int lowTemp = cursor.getInt(lowTempColumn);
        int highTemp = cursor.getInt(highTempColumn);
        String low = Integer.toString(lowTemp) + DEGREE;
        String high = Integer.toString(highTemp) + DEGREE;
        int imgResource = getIconID(cursor.getInt(conditionsColumn), context);

        TextView dateTextView = (TextView) view.findViewById(android.R.id.text1);
        TextView descriptionTextView = (TextView) view.findViewById(android.R.id.text2);
        TextView lowTempTextView = (TextView) view.findViewById(R.id.low_temp);
        TextView highTempTextView = (TextView) view.findViewById(R.id.high_temp);

        ImageView weatherIcon = (ImageView) view.findViewById(R.id.forecast_condition_icon);
        dateTextView.setText(date);
        descriptionTextView.setText(desc);
        lowTempTextView.setText(low);
        highTempTextView.setText(high);
        if (imgResource != -1) {
            weatherIcon.setImageResource(imgResource);
        }
    }
}
