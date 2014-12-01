package ru.ifmo.md.lesson8.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import ru.ifmo.md.lesson8.R;
import ru.ifmo.md.lesson8.database.DataProvider;

/**
* Created by Svet on 30.11.2014.
*/
public class CitiesListAdapter extends BaseAdapter {

    Context context;
    DataProvider dp;

    public CitiesListAdapter(Context context, DataProvider dp) {
        this.context = context;
        this.dp = dp;
    }

    @Override
    public int getCount() {
        return dp.getCitiesCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, null);
        final TextView name = (TextView) v.findViewById(R.id.city_name);
        name.setText(dp.getCityInfoByPosition(i + 1).name);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dp.cityListFragment.selectItem(i);
            }
        });
        Button delete = (Button) v.findViewById(R.id.button_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dp.deleteCity(name.getText().toString());
                dp.cityListFragment.adapter.notifyDataSetChanged();
            }
        });

        return v;
    }
}
