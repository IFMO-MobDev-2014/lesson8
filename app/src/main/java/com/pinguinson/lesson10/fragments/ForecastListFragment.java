package com.pinguinson.lesson10.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pinguinson.lesson10.R;
import com.pinguinson.lesson10.activities.ForecastActivity;
import com.pinguinson.lesson10.adapters.ForecastAdapter;
import com.pinguinson.lesson10.db.WeatherContentProvider;
import com.pinguinson.lesson10.db.tables.ForecastsTable;
import com.pinguinson.lesson10.services.ForecastService;


public class ForecastListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    public static final int LOADER_ID = 1;
    private ForecastListFragment self = this;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isAdded()) {
                return;
            }
            int resCode = intent.getIntExtra(ForecastService.STATUS, -1);
            switch (intent.getAction()) {
                case ForecastService.ACTION_FORECASTS_FETCH:
                    if (resCode == ForecastService.STATUS_OK) {
                        getLoaderManager().restartLoader(LOADER_ID, null, self);
                    }
                    break;
            }
        }
    };
    private ForecastAdapter forecastAdapter;
    private String cityId;
    private long woeid;

    public ForecastListFragment() {
    }

    @Override
    public void onRefresh() {
        Toast.makeText(getActivity(), getResources().getString(R.string.feed_refresh_toast), Toast.LENGTH_LONG).show();
        ForecastService.fetchForecasts(getActivity(), cityId, woeid);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forecastAdapter = new ForecastAdapter(getActivity(), null);
        setListAdapter(forecastAdapter);

        if (getArguments().containsKey(ForecastActivity.CITY_ID)) {
            Bundle args = new Bundle();
            cityId = getArguments().getString(ForecastActivity.CITY_ID);
            woeid = getArguments().getLong(ForecastActivity.WOEID);
            args.putString(ForecastActivity.CITY_ID, cityId);
            getLoaderManager().initLoader(LOADER_ID, args, this);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                    new IntentFilter(ForecastService.ACTION_FORECASTS_FETCH));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments().containsKey(ForecastActivity.CITY_ID)) {
            setEmptyText(getResources().getString(R.string.forecasts_loading_text));
        } else {
            setEmptyText(getResources().getString(R.string.forecasts_invalid_text));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                WeatherContentProvider.FORECASTS_CONTENT_URL,
                new String[]{"*"},
                ForecastsTable.COLUMN_NAME_CITY_ID + "=?",
                new String[]{cityId},
                null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
        setEmptyText(getResources().getText(R.string.forecasts_empty_text));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }
}
