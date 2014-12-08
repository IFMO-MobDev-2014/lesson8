package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.os.Bundle;
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
import ru.ifmo.md.lesson8.db.ForecastsTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.service.ForecastService;
import ru.ifmo.md.lesson8.service.Receiver;
import ru.ifmo.md.lesson8.service.SupportReceiver;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is contained in a {@link MainActivity}
 */
public class ForecastListFragment extends SwipeRefreshListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        Receiver,
        SwipeRefreshLayout.OnRefreshListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final int LOADER_ID = 1;

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
    public ForecastListFragment() {
    }

    @Override
    public void onRefresh() {
        Toast.makeText(getActivity(), getResources().getString(R.string.feed_refresh_toast), Toast.LENGTH_LONG).show();
        ForecastService.fetchForecasts(getActivity(), cityId, woeid, new SupportReceiver(new Handler(), this));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mAdapter = new ForecastAdapter(getActivity(), null);
        setListAdapter(mAdapter);

        if (getArguments().containsKey(MainActivity.CITY_ID)) {
            Bundle args = new Bundle();
            cityId = getArguments().getString(MainActivity.CITY_ID);
            woeid = getArguments().getLong(MainActivity.WOEID);
            args.putString(MainActivity.CITY_ID, cityId);
            getLoaderManager().initLoader(LOADER_ID, args, this);
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

        if(getArguments().containsKey(MainActivity.CITY_ID)) {
            setEmptyText(getResources().getString(R.string.forecasts_loading_text));
        } else {
            setEmptyText(getResources().getString(R.string.forecasts_invalid_text));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                WeatherContentProvider.FORECASTS_CONTENT_URL,
                new String[] {"*"},
                ForecastsTable.COLUMN_NAME_CITY_ID + "=?",
                new String[] {cityId},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        setEmptyText(getResources().getText(R.string.forecasts_empty_text));
        setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
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
                    args.putString(MainActivity.CITY_ID, cityId);
                    getLoaderManager().restartLoader(LOADER_ID, args, this);
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(isAdded() && !((MainActivity) getActivity()).isTwoPane()) {
            inflater.inflate(R.menu.forecast_list_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_back:
                if(isAdded()) {
                    Bundle data = new Bundle();
                    data.putString(MainActivity.CITY_ID, cityId);
                    data.putLong(MainActivity.WOEID, woeid);
                    ((MainActivity) getActivity()).applyTodayFragment(data);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
