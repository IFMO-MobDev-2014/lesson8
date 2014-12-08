package ru.ifmo.md.lesson8.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.service.ForecastService;

/**
 * Created by flyingleafe on 01.12.14.
 */
public class CitiesAdapter extends CursorAdapter {
    public CitiesAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.city_item, parent, false);
        bindView(v, context, cursor);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameCol = cursor.getColumnIndex(CitiesTable.COLUMN_NAME_NAME);
        int condCol = cursor.getColumnIndex(CitiesTable.COLUMN_NAME_CUR_COND);
        int tempCol = cursor.getColumnIndex(CitiesTable.COLUMN_NAME_CUR_TEMP);

        String name = cursor.getString(nameCol);
        int temp = cursor.getInt(tempCol);
        int cond = cursor.getInt(condCol);

        String tempStr = Integer.toString(temp) + ForecastAdapter.CELSIUM;

        ((TextView) view.findViewById(android.R.id.text1)).setText(name);
        TextView tempView = (TextView) view.findViewById(android.R.id.text2);
        tempView.setText(tempStr);
        tempView.setTextColor(ForecastAdapter.getTempColor(temp));
        int imgResource = ForecastAdapter.getImageId(cond, context);
        if(imgResource != -1) {
            ((ImageView) view.findViewById(R.id.city_cond_icon)).setImageResource(imgResource);
        }
    }
}
