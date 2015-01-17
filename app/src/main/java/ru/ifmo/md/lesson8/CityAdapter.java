package ru.ifmo.md.lesson8;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mikhail on 16.01.15.
 */
public class CityAdapter extends ArrayAdapter<City> {

    private Context ctx;
    private List<City> cityList = new ArrayList<City>();

    public CityAdapter(Context ctx, List<City> cityList) {
        super(ctx, R.layout.cityresult_layout, cityList);
        this.cityList = cityList;
        this.ctx = ctx;
    }


    @Override
    public City getItem(int position) {
        if (cityList != null)
            return cityList.get(position);

        return null;
    }

    @Override
    public int getCount() {
        if (cityList != null)
            return cityList.size();

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;

        if (result == null) {
            LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            result = inf.inflate(R.layout.cityresult_layout, parent, false);

        }

        TextView tv = (TextView) result.findViewById(R.id.txtCityName);
        tv.setText(cityList.get(position).getCityName());

        return result;
    }

    @Override
    public long getItemId(int position) {
        if (cityList != null)
            return cityList.get(position).hashCode();

        return 0;
    }

    @Override
    public Filter getFilter() {
        Filter cityFilter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() < 2)
                    return results;

                List<City> cityResultList = YahooClient.getCityList(constraint.toString());
                results.values = cityResultList;
                results.count = cityResultList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                cityList = (List) results.values;
                notifyDataSetChanged();
            }
        };

        return cityFilter;
    }
}
