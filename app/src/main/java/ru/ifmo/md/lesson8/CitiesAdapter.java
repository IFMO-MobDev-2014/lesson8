package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.ifmo.md.lesson8.dummy.DummyContent;

/**
 * Created by 107476 on 10.01.2015.
 */
public class CitiesAdapter extends ArrayAdapter<DummyContent.CitiesItem> {
    ArrayList<DummyContent.CitiesItem> items;
    Context context;



    public CitiesAdapter(Context context, ArrayList<DummyContent.CitiesItem> items) {
        super(context, R.layout.cities_element, items);
        this.context = context;
        this.items  = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cities_element, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        ImageButton button = (ImageButton) convertView.findViewById(R.id.button);
        textView.setText(items.get(position).toString());
        final int pos = position;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = items.get(pos).id;
                Uri uri = Uri.parse(MyContentProvider.CITIES_CONTENT_URI + "/" + id);
                context.getContentResolver().delete(uri, null, null);
                DummyContent.ITEM_MAP = new HashMap<>();
                Cursor cursor = context.getContentResolver().query(MyContentProvider.CITIES_CONTENT_URI, null, null, null, null);
                cursor.moveToFirst();
                int count = 0;
                while (!cursor.isAfterLast()) {
                    int city_id = cursor.getInt(0);
                    int woeid = cursor.getInt(3);
                    String title = cursor.getString(1);
                    String country = cursor.getString(2);
                    DummyContent.CitiesItem item = new DummyContent.CitiesItem(city_id,title,country,woeid);
                    DummyContent.ITEM_MAP.put(count , item);
                    cursor.moveToNext();
                    count++;
                }
                cursor.close();
                items.remove(pos);
                DummyContent.ITEMS.remove(pos);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
}
