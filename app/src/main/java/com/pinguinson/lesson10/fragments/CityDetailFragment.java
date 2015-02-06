package com.pinguinson.lesson10.fragments;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinguinson.lesson10.R;
import com.pinguinson.lesson10.activities.ForecastActivity;
import com.pinguinson.lesson10.adapters.ForecastAdapter;
import com.pinguinson.lesson10.db.WeatherContentProvider;
import com.pinguinson.lesson10.db.tables.CitiesTable;
import com.pinguinson.lesson10.services.ForecastService;

/**
 * Created by pinguinson.
 */
public class CityDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_ID = 2;

    private CityDetailFragment self = this;
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
    private String cityId;
    private TextView cityName;
    private TextView weatherDesc;
    private TextView temperature;
    private ImageView weatherImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ForecastActivity.CITY_ID)) {
            Bundle args = new Bundle();
            cityId = getArguments().getString(ForecastActivity.CITY_ID);
            args.putString(ForecastActivity.CITY_ID, cityId);
            getLoaderManager().initLoader(LOADER_ID, args, this);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                    new IntentFilter(ForecastService.ACTION_FORECASTS_FETCH));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_weather, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cityName = (TextView) view.findViewById(R.id.city_name);
        weatherDesc = (TextView) view.findViewById(R.id.weather_desc);
        temperature = (TextView) view.findViewById(R.id.temperature);
        weatherImg = (ImageView) view.findViewById(R.id.weather_icon);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                WeatherContentProvider.CITIES_CONTENT_URL,
                new String[]{"*"},
                CitiesTable._ID + "=?",
                new String[]{cityId},
                null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        int nameColumn = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_CITY_NAME);
        int descriptionColumn = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_СURRENT_DESCRIPTION);
        int temperatureColumn = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_CURRENT_TEMPERATURE);
        int conditionColumn = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_CURRENT_CONDITIONS);

        cityName.setText(data.getString(nameColumn));
        weatherDesc.setText(data.getString(descriptionColumn));

        int temp = data.getInt(temperatureColumn);
        int condition = data.getInt(conditionColumn);

        temperature.setText(temp + ForecastAdapter.DEGREE);

        int imgId = ForecastAdapter.getIconID(condition, getActivity());
        if (imgId != -1) {
            weatherImg.setImageResource(imgId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
