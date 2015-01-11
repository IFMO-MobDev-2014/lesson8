package ru.ifmo.md.lesson8;

import android.content.Context;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.md.lesson8.logic.CityFindResult;
import ru.ifmo.md.lesson8.logic.YahooClient;

/**
 * Created by sergey on 11.01.15.
 */
public class CityFinderActivity extends ActionBarActivity {
/*
  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_finder_layout);
        AutoCompleteTextView edt = (AutoCompleteTextView) this.findViewById(R.id.edtCity);
        CityAdapter adpt = new CityAdapter(this, null);
        edt.setAdapter(adpt);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        edt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CityFindResult result = (CityFindResult) parent.getItemAtPosition(position);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(CityFinderActivity.this);
                //Log.d("SwA", "WOEID [" + result.getWoeid() + "]");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("woeid", result.getWoeid());
                editor.putString("cityName", result.getCityName());
                editor.putString("country", result.getCountry());
                editor.commit();
                NavUtils.navigateUpFromSameTask(CityFinderActivity.this);
            }
        });

    }
    private class CityAdapter extends ArrayAdapter<CityFindResult> implements Filterable {

        private Context ctx;
        private List<CityFindResult> cityList = new ArrayList<CityFindResult>();

        public CityAdapter(Context ctx, List<CityFindResult> cityList) {
            super(ctx, R.layout.city_finder_layout, cityList);
            this.cityList = cityList;
            this.ctx = ctx;
        }


        @Override
        public CityFindResult getItem(int position) {
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
                result = inf.inflate(R.layout.CityFindResult_layout, parent, false);

            }

            TextView tv = (TextView) result.findViewById(R.id.txtCityName);
            tv.setText(cityList.get(position).getCityName() + "," + cityList.get(position).getCountry());

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
                protected Filter.FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() < 2)
                        return results;

                    List<CityFindResult> CityFindResultList = YahooClient.getCityList(constraint.toString());
                    results.values = CityFindResultList;
                    results.count = CityFindResultList.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    cityList = (List) results.values;
                    notifyDataSetChanged();
                }
            };

            return cityFilter;
        }
    }

*/
}
