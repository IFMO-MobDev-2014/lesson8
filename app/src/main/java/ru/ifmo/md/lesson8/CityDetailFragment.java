package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import ru.ifmo.md.lesson8.database.WeatherProvider;

public class CityDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_CITY_ID = "city_id";
    private static final int LOADER_CITY_INFO = 0;

    private Cursor mCursor;
    private BroadcastReceiver mUpdateReceiver;

    public CityDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        if (getArguments().containsKey(ARG_CITY_ID)) {
            getLoaderManager().initLoader(LOADER_CITY_INFO, getArguments(), this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle argCity = new Bundle();
                argCity.putString(ARG_CITY_ID, getArguments().getString(ARG_CITY_ID));
                getLoaderManager().restartLoader(LOADER_CITY_INFO, argCity, CityDetailFragment.this);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, new IntentFilter("update"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_city_detail, container, false);
        updateUserInterface();
        return rootView;
    }

    private void updateUserInterface() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_refresh:
//                if (mCityId != null && mCityWeatherId != null) {
//                    WeatherLoaderService.startActionUpdateCity(getActivity(), mCityId, mCityWeatherId);
//                }
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOADER_CITY_INFO:
                return new CursorLoader(
                        getActivity(),
                        WeatherProvider.buildCityUri(bundle.getString(ARG_CITY_ID)),
                        null, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(LOADER_CITY_INFO, null, this).forceLoad();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mCursor.moveToFirst();
        updateUserInterface();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
