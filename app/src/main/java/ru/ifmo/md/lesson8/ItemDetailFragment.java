package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.LoaderManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ru.ifmo.md.lesson8.adapters.ForecastAdapter;
import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.ForecastsTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.service.ForecastService;
import ru.ifmo.md.lesson8.service.Receiver;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends SwipeRefreshListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        Receiver,
        SwipeRefreshLayout.OnRefreshListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String CITY_ID = "city_id";
    public static final String WOEID = "woeid";
    public static final int LOADER_ID = 1;
    public static final int CITY_LOADER_ID = 2;

    /**
     * The adapter for displaying list of forecasts
     */
    private ForecastAdapter mAdapter;

    private String cityId;
    private long woeid;
    private String curCityName;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onRefresh() {
        Toast.makeText(getActivity(), getResources().getString(R.string.feed_refresh_toast), Toast.LENGTH_LONG).show();
        ForecastService.fetchForecasts(getActivity(), cityId, woeid, new RefreshReceiver(new Handler(), this));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mAdapter = new ForecastAdapter(getActivity(), null);
        setListAdapter(mAdapter);

        if (getArguments().containsKey(CITY_ID)) {
            Bundle args = new Bundle();
            cityId = getArguments().getString(CITY_ID);
            woeid = getArguments().getLong(WOEID);
            args.putString(CITY_ID, cityId);
            getLoaderManager().initLoader(LOADER_ID, args, this);
            getLoaderManager().initLoader(CITY_LOADER_ID, args, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setOnRefreshListener(this);

        if(getArguments().containsKey(CITY_ID)) {
            setEmptyText(getResources().getString(R.string.forecasts_loading_text));
        } else {
            setEmptyText(getResources().getString(R.string.forecasts_invalid_text));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.item_detail_menu, menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String cityId = args.getString(CITY_ID);
        switch(id) {
            case LOADER_ID:
                return new CursorLoader(getActivity(),
                        WeatherContentProvider.FORECASTS_CONTENT_URL,
                        new String[] {"*"},
                        ForecastsTable.COLUMN_NAME_CITY_ID + "=?",
                        new String[] {cityId},
                        null);
            case CITY_LOADER_ID:
                return new CursorLoader(getActivity(),
                        WeatherContentProvider.CITIES_CONTENT_URL,
                        new String[] {"*"},
                        CitiesTable._ID + "=?",
                        new String[] {cityId},
                        null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID:
                mAdapter.swapCursor(data);
                setEmptyText(getResources().getText(R.string.forecasts_empty_text));
                setRefreshing(false);
                break;
            case CITY_LOADER_ID:
                data.moveToFirst();
                curCityName = data.getString(data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_NAME));
                if(isAdded() && getActivity().getActionBar() != null) {
                    getActivity().getActionBar().setTitle(curCityName);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID:
                mAdapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void onReceiveResult(int resCode, Bundle resData) {
        switch (resCode) {
            case ForecastService.STATUS_ERROR:
                Toast.makeText(getActivity(), resData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_SHORT).show();
                break;
            case ForecastService.STATUS_OK:
                if(isAdded()) {
                    Bundle args = new Bundle();
                    args.putString(CITY_ID, cityId);
                    getLoaderManager().restartLoader(LOADER_ID, args, this);
                }
                break;
        }
    }

    /**
     * Wrapper class used for passing fragment callbacks to IntentService
     */
    public class RefreshReceiver extends ResultReceiver {
        Receiver mReceiver;

        public RefreshReceiver(Handler handler, Receiver mReceiver) {
            super(handler);
            this.mReceiver = mReceiver;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(mReceiver != null) {
                mReceiver.onReceiveResult(resultCode, resultData);
            }
        }
    }
}
