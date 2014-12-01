package odeen.weatherpredictor.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import odeen.weatherpredictor.Location;
import odeen.weatherpredictor.PictureManager;
import odeen.weatherpredictor.R;
import odeen.weatherpredictor.SQLiteCursorLoader;
import odeen.weatherpredictor.WeatherManager;
import odeen.weatherpredictor.WeatherProvider;

/**
 * Created by Женя on 27.11.2014.
 */
public class LocationListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_LOCATION = 0;
    private static final String DIALOG_LOCATION = "DIALOG!!!";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(0, null, this);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channel_list_options, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);
        ListView listView = (ListView) v.findViewById(android.R.id.list);
        registerForContextMenu(listView);
        return v;
    }

    private void showEditDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        LocationPickerDialog dialog = LocationPickerDialog.newInstance();
        dialog.setTargetFragment(LocationListFragment.this, REQUEST_LOCATION);
        dialog.show(fm, DIALOG_LOCATION);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.channel_list_item_context, menu);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Location location = ((LocationCursorAdapter)getListAdapter()).get(position);
        Intent i = new Intent(getActivity(), WeatherPagerActivity.class);
        i.putExtra(CurrentWeatherActivity.EXTRA_CITY, location.getCity());
        i.putExtra(CurrentWeatherActivity.EXTRA_CITY_ID, location.getId());
        i.putExtra(CurrentWeatherActivity.EXTRA_CITY_COLOR, location.getColor());
        startActivity(i);
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        Location loc = ((LocationCursorAdapter)getListAdapter()).get(position);
        switch (item.getItemId()) {
            case R.id.menu_item_delete_channel:
                WeatherManager.getInstance(getActivity()).removeLocation(loc);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_city:
                showEditDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_LOCATION) {
            String name = data.getStringExtra(LocationPickerDialog.EXTRA_CITY_NAME);
            WeatherManager.getInstance(getActivity()).insertOrUpdateLocation(-1, name);
        }
    }



    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        LocationListCursorLoader loader = new LocationListCursorLoader(getActivity());
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        setListAdapter(new LocationCursorAdapter(getActivity(), (WeatherProvider.LocationCursor) cursor));
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        setListAdapter(null);
    }


    private static class LocationListCursorLoader extends SQLiteCursorLoader {
        public LocationListCursorLoader(Context context) {
            super(context);
        }
        @Override
        protected Cursor loadCursor() {
            return WeatherManager.getInstance(getContext()).getLocations();
        }
    }

    private static class LocationCursorAdapter extends CursorAdapter {

        private WeatherProvider.LocationCursor mCursor;

        public LocationCursorAdapter(Context context, WeatherProvider.LocationCursor cursor) {
            super(context, cursor, true);
            mCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.fragment_channel_list_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Location loc = mCursor.getLocation();
            TextView name = (TextView) view.findViewById(R.id.channel_list_item_channelName);
            int nc = Color.argb(150, Color.red(loc.getColor()), Color.green(loc.getColor()), Color.blue(loc.getColor()));
            view.setBackgroundColor(loc.getColor());
            name.setText(loc.getCity());
        }

        public Location get(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLocation();
        }
    }



}
