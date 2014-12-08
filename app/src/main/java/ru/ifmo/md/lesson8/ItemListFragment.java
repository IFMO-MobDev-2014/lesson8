package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ItemDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, AdapterView.OnItemLongClickListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add) {
            new AddCityDialog().show(getActivity().getFragmentManager(), "DLG_ADD_CITY");
        } else if (v.getId() == R.id.location) {
            if (currentCityId != -1)
                mCallbacks.onItemSelected(currentCityId);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (id != currentCityId)
            getActivity().getContentResolver().delete(WeatherContentProvider.WEATHER_URI, Long.toString(id), null);
        return true;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         *
         * @param id
         */
        public void onItemSelected(long id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id) {
        }
    };

    ListView lvCities = null;
    View add = null;
    TextView tvCurrentCityName = null;
    View location = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.cities_fragment, container, false);
        lvCities = ((ListView) result.findViewById(android.R.id.list));
        add = result.findViewById(R.id.add);
        add.setOnClickListener(this);
        tvCurrentCityName = (TextView) result.findViewById(R.id.tvCurrentCity);
        location = result.findViewById(R.id.location);
        location.setOnClickListener(this);

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        android.location.Location l = locationManager.getLastKnownLocation(locationProvider);
        double lat = l.getLatitude();
        double lon = l.getLongitude();

        new CurrentCityResolver() {
            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    long id = DBAdapter.getOpenedInstance(getActivity()).getIdByCityName(s);
                    if (id != -1) {
                        currentCityId = id;
                        tvCurrentCityName.setText(s);
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put(DBAdapter.KEY_WEATHER_ATMOSPHERE_HUMIDITY, 0);
                        cv.put(DBAdapter.KEY_WEATHER_ATMOSPHERE_PRESSURE, 0);
                        cv.put(DBAdapter.KEY_WEATHER_CODE, 0);
                        cv.put(DBAdapter.KEY_WEATHER_TEMPERATURE, 0);
                        cv.put(DBAdapter.KEY_WEATHER_DATE, "");
                        cv.put(DBAdapter.KEY_WEATHER_WIND_DIRECTION, 0);
                        cv.put(DBAdapter.KEY_WEATHER_WIND_SPEED, 0);
                        cv.put(DBAdapter.KEY_WEATHER_CITY, s);
                        currentCityId = DBAdapter.getOpenedInstance(getActivity()).createWeather(cv);
                    }
                    if (getActivity() != null && PreferenceManager.getDefaultSharedPreferences(getActivity()) != null)
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .edit()
                                .putLong(WeatherContentProvider.CURRENT_CITY_ID, currentCityId)
                                .commit();
                    if (getActivity() != null)
                        WeatherFetchingService.startActionUpdateWeather(getActivity(), currentCityId);
                }
                super.onPostExecute(s);
            }
        }.execute(lat, lon);

        return result;
    }

    long currentCityId = -1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fillData();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemLongClickListener(this);
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
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

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void fillData() {
        getLoaderManager().initLoader(0, null, this);
        setListAdapter(new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
                new String[]{DBAdapter.KEY_WEATHER_CITY},
                new int[]{android.R.id.text1}, 0));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                WeatherContentProvider.WEATHER_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((CursorAdapter) getListAdapter()).swapCursor(data);
        currentCityId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(WeatherContentProvider.CURRENT_CITY_ID, -1);
        if (data.moveToFirst()) {
            do {
                if (data.getLong(data.getColumnIndex(DBAdapter.KEY_ID)) == currentCityId) {
                    tvCurrentCityName.setText(data.getString(data.getColumnIndex(DBAdapter.KEY_WEATHER_CITY)));
                    break;
                }
            } while (data.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }
}
