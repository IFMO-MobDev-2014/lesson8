package year2013.ifmo.catweather;


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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class CityAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private final Context mContext;
    private List<String> mResults;

    private static final String OPEN_WEATHER_LIKE_SEARCH = "http://api.openweathermap.org/data/2.5/find?q=%s&type=like&mode=json&cnt=10";

    public CityAutoCompleteAdapter(Context context) {
        mContext = context;
        mResults = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mResults.size();
    }

    @Override
    public String getItem(int index) {
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
            convertView = inflater.inflate(R.layout.simple_dropdown_1line_item, parent, false);
        }
        String city = getItem(position);
        ((TextView) convertView.findViewById(R.id.text1)).setText(city);


        return convertView;
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<String> cities = findCities(constraint.toString());
                    filterResults.values = cities;
                    filterResults.count = cities.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResults = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private JSONObject download(String city) {
        JSONObject data = null;
        try {
            URL url = new URL(String.format(OPEN_WEATHER_LIKE_SEARCH, change(city)));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.addRequestProperty(mContext.getString(R.string.api_key), mContext.getString(R.string.open_weather_maps_app_id));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            data = new JSONObject(json.toString());

            // This value will be 404 if the request was not
            // successful
            if (data.getInt(mContext.getString(R.string.cod)) != 200) {
                return null;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private List<String> findCities(String city) {

        List<String> cities = new ArrayList<>();
        JSONArray list;

        try {
            JSONObject dowloaded = download(city);
            if (dowloaded != null) {
                list = dowloaded.getJSONArray(mContext.getString(R.string.list));
                int l = list.length();

                for (int i = 0; i < l; i++) {
                    String nameCity = list.getJSONObject(i).getString(mContext.getString(R.string.name));
                    String nameCountry = list.getJSONObject(i).getJSONObject(mContext.getString(R.string.sys)).getString(mContext.getString(R.string.country));
                    cities.add(nameCity + ", " + nameCountry);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cities;
    }

    private String change(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
                s = s.substring(0, i) + mContext.getString(R.string.space) + s.substring(i + 1);
                i += 2;
            }
        }
        return s;
    }

}
