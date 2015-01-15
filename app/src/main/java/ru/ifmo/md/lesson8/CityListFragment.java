package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ru.ifmo.md.lesson8.database.WeatherProvider;
import ru.ifmo.md.lesson8.database.WeatherTable;

public class CityListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_CITIES = 0;
    private CursorAdapter mAdapter;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOADER_CITIES:
                return new CursorLoader(getActivity(), WeatherProvider.CONTENT_URI, null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private Callbacks mCallbacks = sDummyCallbacks;

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {
        public void onItemSelected(String id);
    }

    public CityListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new CursorAdapter(getActivity(), null, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(
                        android.R.layout.simple_list_item_activated_1, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final String city = cursor.getString(cursor.getColumnIndex(WeatherTable.COLUMN_CITY));
                final TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(city);
                final long cityWoeid = cursor.getInt(cursor.getColumnIndex(WeatherTable.COLUMN_WOEID));
                final long currentCityWeatherId = WeatherLoaderService.getCurrentCity(getActivity());
                if (cityWoeid == currentCityWeatherId) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_mylocation, 0, 0, 0);
                }
            }
        };
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
        getLoaderManager().initLoader(LOADER_CITIES, Bundle.EMPTY, this);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setBackgroundColor(Color.WHITE);
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(Long.toString(id));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_context_cities, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private void showFirstCityWeather() {
        getListView().performItemClick(mAdapter.getView(0, null, null), 0, mAdapter.getItemId(0));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                Cursor cursor = (Cursor) getListAdapter().getItem(acmi.position);
                final int cityId = cursor.getInt(cursor.getColumnIndex(WeatherTable.COLUMN_ID));
                final int cityWoeid = cursor.getInt(cursor.getColumnIndex(WeatherTable.COLUMN_WOEID));
                final int currentCityWoeid = WeatherLoaderService.getCurrentCity(getActivity());
                if (cityWoeid == currentCityWoeid) {
                    Toast.makeText(getActivity(), "You cannot delete current city", Toast.LENGTH_SHORT).show();
                    return true;
                }
                showFirstCityWeather();
                getActivity().getContentResolver().delete(WeatherProvider.buildCityUri(Integer.toString(cityId)), null, null);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }
        mActivatedPosition = position;
    }
}
