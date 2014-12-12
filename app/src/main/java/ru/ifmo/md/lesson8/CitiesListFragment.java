package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


import ru.ifmo.md.lesson8.adapters.CitiesAdapter;
import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.service.ForecastService;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ForecastListFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class CitiesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, ListView.OnItemLongClickListener {

    private CitiesListFragment self = this;
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

    /**
     * List adapter for fragment.
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id, Bundle data);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id, Bundle data) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CitiesListFragment() {
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!isAdded()) {
                return;
            }
            int resCode = intent.getIntExtra(ForecastService.STATUS, -1);
            if(resCode == ForecastService.STATUS_OK) {
                getLoaderManager().restartLoader(0, null, self);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                null,
                new String[] {CitiesTable.COLUMN_NAME_NAME},
                new int[] {android.R.id.text1}, 0);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        IntentFilter inf = new IntentFilter();
        inf.addAction(ForecastService.ACTION_GET_LOCATION_WOEID);
        inf.addAction(ForecastService.ACTION_UPDATE_CITIES_LIST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, inf);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.nav_drawer, container, false);
        parent.addView(v, 2);
        return parent;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundResource(android.R.color.background_light);

        Button currentLocBtn = (Button) view.findViewById(R.id.current_location_btn);
        ImageButton newCityBtn = (ImageButton) view.findViewById(R.id.add_new_city_btn);

        currentLocBtn.setOnClickListener((MainActivity) getActivity());
        newCityBtn.setOnClickListener((MainActivity) getActivity());

        getListView().setOnItemLongClickListener(this);
        setEmptyText(getResources().getString(R.string.cities_list_empty_text));
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
        Cursor c = ((SimpleCursorAdapter) listView.getAdapter()).getCursor();
        c.moveToPosition(position);
        String cityId = c.getString(c.getColumnIndexOrThrow(CitiesTable._ID));
        long woeid = c.getLong(c.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_WOEID));
        Bundle data = new Bundle();
        data.putLong(MainActivity.WOEID, woeid);
        mCallbacks.onItemSelected(cityId, data);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();
        c.moveToPosition(position);
        final String cityId = c.getString(c.getColumnIndexOrThrow(CitiesTable._ID));
        final String name = c.getString(c.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_NAME));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Delete the city")
                .setMessage("Delete city " + name + "?")
                .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        self.getActivity().getContentResolver().delete(WeatherContentProvider.CITIES_CONTENT_URL,
                                CitiesTable._ID + "=?",
                                new String[] {cityId});
                        self.getLoaderManager().restartLoader(0, null, self);
                        dialog.dismiss();
                    }
                });
        builder.create().show();
        return true;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                WeatherContentProvider.CITIES_CONTENT_URL,
                new String[] {"*"},
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
