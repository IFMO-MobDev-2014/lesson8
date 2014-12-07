package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ru.ifmo.md.lesson8.provider.WeatherDatabaseHelper;
import ru.ifmo.md.lesson8.provider.WeatherProvider;

/**
 * Created by pva701 on 05.12.14.
 */
public class CitiesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private CityAdapter adapter;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), WeatherProvider.CITY_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter = new CityAdapter(getActivity(), R.layout.city_list_item);
        WeatherDatabaseHelper.CityCursor wc = new WeatherDatabaseHelper.CityCursor(cursor);
        while (cursor.moveToNext()) {
            City c = wc.getCity();
            adapter.add(c);
        }
        cursor.close();
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                City newSelCity = (City)adapterView.getItemAtPosition(i);
                DataManager.get(getActivity()).setSelect(newSelCity);
                getLoaderManager().restartLoader(0, null, CitiesFragment.this);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        setListAdapter(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_cities, menu);
    }

    private final int REQUEST_ADD_CITY = 0;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_add_city) {
            startActivityForResult(new Intent(getActivity(), AddCityActivity.class), REQUEST_ADD_CITY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_CITY) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra(AddCityActivity.ADDED_NAME);
                DataManager.get(getActivity()).insertCity(new City(0, name, 0, 0));
                getLoaderManager().restartLoader(0, null, this);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(v.findViewById(android.R.id.list));
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.fragment_city_list_context, menu);
    }

    private void selFirst() {
        Cursor cursor = getActivity().getContentResolver().query(WeatherProvider.CITY_CONTENT_URI, null, WeatherDatabaseHelper.CITY_IS_SELECTED + " = 1", null, null);
        if (cursor.isAfterLast()) {
            Cursor cur = getActivity().getContentResolver().query(WeatherProvider.CITY_CONTENT_URI, null, null, null, WeatherDatabaseHelper.CITY_ID + " asc LIMIT 1");
            if (cur.isAfterLast())
                return;
            cur.moveToNext();
            City d = WeatherDatabaseHelper.CityCursor.getCity(cur);
            DataManager.get(getActivity()).setSelect(d);
        }
        cursor.close();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int pos = info.position;
        if (item.getItemId() == R.id.menu_item_delete_city) {
            boolean d = DataManager.get(getActivity()).deleteCity(adapter.getItem(pos));
            if (!d) {
                Toast.makeText(getActivity(), "Specify at least one city", Toast.LENGTH_SHORT).show();
                return true;
            }
            selFirst();
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public class CityAdapter extends ArrayAdapter <City> {
        private int resId;
        public CityAdapter(Context context, int id) {
            super(context, id);
            resId = id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getActivity()).
                        inflate(resId, parent, false);
            City item = getItem(position);
            TextView cityName = (TextView)convertView.findViewById(R.id.city_name);
            ImageView selectedIcon = (ImageView)convertView.findViewById(R.id.selectedIcon);
            cityName.setText(item.getName());
            if (item.isSelected())
                selectedIcon.setBackgroundResource(R.drawable.selected);
            else
                selectedIcon.setBackgroundResource(R.drawable.notselected);
            return convertView;
        }
    }

}
