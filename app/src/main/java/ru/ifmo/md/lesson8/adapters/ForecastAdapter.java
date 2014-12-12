package ru.ifmo.md.lesson8.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.db.ForecastsTable;

/**
 * Created by flyingleafe on 01.12.14.
 */
public class ForecastAdapter extends CursorAdapter {

    public static final String CELSIUM = "°C";

    public static int getTempColor(int temp) {
        if (temp <= -25) {
            return 0xFF0000C8;
        } else if (temp <= -15) {
            return 0xFF0048FA;
        } else if(temp <= 0) {
            return 0xFF3896DA;
        } else if(temp <= 15) {
            return 0xFFCDB027;
        } else if(temp <= 25) {
            return 0xFFE26431;
        } else {
            return 0xFFE83F33;
        }
    }

    public ForecastAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    public static int getImageId(int num, Context context)
    {
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
        View v = inflater.inflate(R.layout.forecast_item, parent, false);
        bindView(v, context, cursor);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int dayCol = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_DAY);
        int dateCol = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_DATE);
        int textCol = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_TEXT);
        int lowCol = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_LOW_TEMP);
        int highCol = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_HIGH_TEMP);
        int condCol = cursor.getColumnIndex(ForecastsTable.COLUMN_NAME_COND);

        String date = cursor.getString(dayCol) + ", " + cursor.getString(dateCol);
        String desc = cursor.getString(textCol);
        int lowTemp = cursor.getInt(lowCol);
        int highTemp = cursor.getInt(highCol);
        String low = Integer.toString(lowTemp) + CELSIUM;
        String high = Integer.toString(highTemp) + CELSIUM;
        int imgResource = getImageId(cursor.getInt(condCol), context);

        TextView dateText = (TextView) view.findViewById(android.R.id.text1);
        TextView descText = (TextView) view.findViewById(android.R.id.text2);
        TextView lowText = (TextView) view.findViewById(R.id.low_temp);
        TextView highText = (TextView) view.findViewById(R.id.high_temp);
        ImageView icon = (ImageView) view.findViewById(R.id.forecast_cond_icon);
        dateText.setText(date);
        descText.setText(desc);
        lowText.setText(low);
        lowText.setTextColor(getTempColor(lowTemp));
        highText.setText(high);
        highText.setTextColor(getTempColor(highTemp));
        if(imgResource != -1) {
            icon.setImageResource(imgResource);
        }
    }
}
