package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ru.ifmo.md.lesson8.adapters.ForecastAdapter;
import ru.ifmo.md.lesson8.db.CitiesTable;
import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.service.ForecastService;
import ru.ifmo.md.lesson8.service.Receiver;
import ru.ifmo.md.lesson8.service.SupportReceiver;

/**
 * Created by flyingleafe on 08.12.14.
 */
public class CityDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener,
        Receiver {

    public static final int LOADER_ID = 2;

    private String cityId;
    private long woeid;

    private TextView cityName;
    private TextView weatherDesc;
    private TextView temperature;
    private ImageView weatherImg;
    private Button forecastBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments().containsKey(MainActivity.CITY_ID)) {
            Bundle args = new Bundle();
            cityId = getArguments().getString(MainActivity.CITY_ID);
            woeid = getArguments().getLong(MainActivity.WOEID);
            args.putString(MainActivity.CITY_ID, cityId);
            getLoaderManager().initLoader(LOADER_ID, args, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.today_weather, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cityName = (TextView) view.findViewById(R.id.city_name);
        weatherDesc = (TextView) view.findViewById(R.id.weather_desc);
        temperature = (TextView) view.findViewById(R.id.temperature);
        weatherImg = (ImageView) view.findViewById(R.id.weather_icon);
        forecastBtn = (Button) view.findViewById(R.id.view_forecast_button);

        if(forecastBtn != null) {
            forecastBtn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.view_forecast_button:
                Bundle data = new Bundle();
                data.putString(MainActivity.CITY_ID, cityId);
                data.putLong(MainActivity.WOEID, woeid);
                ((MainActivity) getActivity()).applyForecastFragment(data);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                WeatherContentProvider.CITIES_CONTENT_URL,
                new String[] {"*"},
                CitiesTable._ID + "=?",
                new String[] {cityId},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        int nameCol = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_NAME);
        int descCol = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_СUR_DESC);
        int tempCol = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_CUR_TEMP);
        int condCol = data.getColumnIndexOrThrow(CitiesTable.COLUMN_NAME_CUR_COND);

        cityName.setText(data.getString(nameCol));
        weatherDesc.setText(data.getString(descCol));

        int temp = data.getInt(tempCol);
        int cond = data.getInt(condCol);

        temperature.setText(temp + ForecastAdapter.CELSIUM);
        temperature.setTextColor(ForecastAdapter.getTempColor(temp));

        int imgId = ForecastAdapter.getImageId(cond, getActivity());
        if(imgId != -1) {
            weatherImg.setImageResource(imgId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do fukken nothing
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.today_weather_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_refresh:
                Toast.makeText(getActivity(), getResources().getString(R.string.feed_refresh_toast), Toast.LENGTH_LONG).show();
                ForecastService.fetchForecasts(getActivity(), cityId, woeid, new SupportReceiver(new Handler(), this));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveResult(int resCode, Bundle resData) {
        switch (resCode) {
            case ForecastService.STATUS_ERROR:
                Toast.makeText(getActivity(), resData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_SHORT).show();
                break;
            case ForecastService.STATUS_OK:
                if(isAdded()) {
                    getLoaderManager().restartLoader(LOADER_ID, null, this);
                }
                break;
        }
    }
}
