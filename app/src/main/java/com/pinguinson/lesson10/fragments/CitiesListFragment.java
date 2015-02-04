package com.pinguinson.lesson10.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.pinguinson.lesson10.activities.MainActivity;
import com.pinguinson.lesson10.R;
import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.db.WeatherContentProvider;
import com.pinguinson.lesson10.services.ForecastService;

public class CitiesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, ListView.OnItemLongClickListener {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id, Bundle data) {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;
    private CitiesListFragment self = this;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isAdded()) {
                return;
            }
            int resCode = intent.getIntExtra(ForecastService.STATUS, -1);
            if (resCode == ForecastService.STATUS_OK) {
                getLoaderManager().restartLoader(0, null, self);
            }
        }
    };

    private int mActivatedPosition = ListView.INVALID_POSITION;
    private SimpleCursorAdapter mAdapter;

    public CitiesListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                null,
                new String[]{CitiesTable.COLUMN_NAME_CITY_NAME},
                new int[]{android.R.id.text1}, 0);
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

        Button currentLocBtn = (Button) view.findViewById(R.id.current_location_button);
        ImageButton newCityBtn = (ImageButton) view.findViewById(R.id.add_new_city_button);

        currentLocBtn.setOnClickListener((MainActivity) getActivity());
        newCityBtn.setOnClickListener((MainActivity) getActivity());

        getListView().setOnItemLongClickListener(this);
        setEmptyText(getResources().getString(R.string.cities_list_empty_text));
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
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
        final String name = c.getString(c.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_CITY_NAME));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_delete_city)
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
                                new String[]{cityId});
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
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
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
                new String[]{"*"},
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

    public interface Callbacks {
        public void onItemSelected(String id, Bundle data);
    }
}
