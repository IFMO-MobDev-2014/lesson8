package ru.ifmo.md.weather;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ru.ifmo.md.weather.db.WeatherContentProvider;
import ru.ifmo.md.weather.db.model.CityTable;

/**
 * Created by Kirill on 08.12.2014.
 */
public class AddNewCityActivity extends Activity implements AdapterView.OnItemClickListener{

    private static final String LOG_TAG = "ExampleApp";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyASyEEziroVwOJiJJLMK4PzJoVNDTftD2E";


    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.onepane_add_new_city);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete_country);
        PlacesAutoCompleteAdapter adapter = new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item);
        autoCompView.setAdapter(adapter);
        autoCompView.setOnItemClickListener(this);
    }

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?sensor=false&key=" + API_KEY);
            sb.append("&types=(cities)&anguage=en&");
            sb.append("&input=").append(URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());

            for (int i = 0; i < predsJsonArray.length(); i++) {
                JSONArray terms = (predsJsonArray.getJSONObject(i)).getJSONArray("terms");
                String result = "";
                String city = (terms.getJSONObject(0)).getString("value");
                String country = "";
                String region = "";
                if (terms.length() == 2) {
                    country = (terms.getJSONObject(1)).getString("value");
                    result += country + ", " + city;
                }
                else if (terms.length() == 3) {
                    region = (terms.getJSONObject(1)).getString("value");
                    country = (terms.getJSONObject(2)).getString("value");
                    result += country + ", " + region + ", " + city;
                }

                resultList.add(result);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }



    public void buttonClick(View view) {



        AutoCompleteTextView input = (AutoCompleteTextView)findViewById(R.id.autocomplete_country);
        String text = input.getText().toString();
        if (text != null && !text.equals("")) {
            String[] parts = text.split(",");
            String country = parts[0];
            String city = parts[parts.length-1];
            Toast.makeText(this, "country: " + country + ", city: " + city, Toast.LENGTH_SHORT).show();
            ContentValues values = new ContentValues();
            values.put(CityTable.NAME_COLUMN, city.trim());
            values.put(CityTable.COUNTRY_COLUMN, country.trim());
            Uri id = getContentResolver().insert(WeatherContentProvider.CONTENT_URI_CITIES, values);
            finish();

        } else {
            Toast.makeText(this, "Enter city name", Toast.LENGTH_SHORT).show();
        }


    }


}
