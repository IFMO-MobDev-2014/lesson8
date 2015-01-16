package ru.eugene.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ru.eugene.weather.database.CityItem;
import ru.eugene.weather.downloadWeather.ServiceDownload;

/**
 * Created by eugene on 1/13/15.
 */
public class CityAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;

    private final Context mContext;
    private ArrayList<CityItem> mResults;

    public CityAutoCompleteAdapter(Context context) {
        mContext = context;
        mResults = new ArrayList<CityItem>();
    }

    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public CityItem getItem(int index) {
        return mResults.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }
        CityItem city = getItem(position);
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(city.getCity());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    ArrayList<CityItem> cities = findCities(constraint.toString());
                    // Assign the data to the FilterResults
                    filterResults.values = cities;
                    filterResults.count = cities.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResults = (ArrayList<CityItem>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }

    private ArrayList<CityItem> readMessageArray(String text) {
        ArrayList<CityItem> cityItems = new ArrayList<>();
        try {
            JSONObject reader = new JSONObject(text);
            JSONArray place = reader.getJSONObject("query").getJSONObject("results").getJSONArray("place");
            for (int i = 0; i < place.length(); i++) {
                JSONObject curObject = place.getJSONObject(i);

                String name = curObject.getString("name");
                String country = curObject.getJSONObject("country").getString("code");
                String region = curObject.getString("admin1");
                int woeid = Integer.parseInt(curObject.getString("woeid"));

                CityItem cityItem = new CityItem();
                cityItem.setCity(name + ", " + region + ", " + country);
                cityItem.setLink(WeatherInfo.getLink(woeid));

                cityItems.add(cityItem);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return cityItems;
    }

    static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private ArrayList<CityItem> findCities(String nameCity) {
        String result = "";
        try {
            InputStream inputStream = ServiceDownload.downloadUrl(WeatherInfo.getQuery(nameCity));
            result = convertStreamToString(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readMessageArray(result);
    }
}
