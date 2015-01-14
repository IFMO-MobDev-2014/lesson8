package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ifmo.md.lesson8.database.WeatherProvider;
import ru.ifmo.md.lesson8.database.WeatherTable;

public class CityDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_CITY_ID = "city_id";
    private static final int LOADER_CITY_WEATHER = 0;

    private View mRootView;
    private Cursor mCursor;
    private int mCityId;
    private int mCityWoeid;
    private BroadcastReceiver mUpdateReceiver;

    public CityDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_CITY_ID)) {
            mCityId = Integer.parseInt(getArguments().getString(ARG_CITY_ID));
            getLoaderManager().initLoader(LOADER_CITY_WEATHER, getArguments(), this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                mCityWoeid = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_WOEID));
                WeatherLoaderService.startActionUpdateCity(getActivity(), mCityId, mCityWoeid);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TAG", "updated came");
                Bundle argCity = new Bundle();
                mCityId = Integer.parseInt(getArguments().getString(ARG_CITY_ID));
                argCity.putString(ARG_CITY_ID, getArguments().getString(ARG_CITY_ID));
                getLoaderManager().restartLoader(LOADER_CITY_WEATHER, argCity, CityDetailFragment.this);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, new IntentFilter("update"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_city_detail, container, false);
        updateUserInterface();
        return mRootView;
    }

    /*
        public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WOEID = "woeid";
    public static final String COLUMN_LASTUPD = "last_upd";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_CITY = "city";

    public static final String COLUMN_CONDITION_DESCRIPTION = "condition_description";
    public static final String COLUMN_CONDITION_TEMP = "condition_temp";
    public static final String COLUMN_CONDITION_CODE = "condition_code";
    public static final String COLUMN_CONDITION_DATE = "condition_date";

    public static final String COLUMN_ATMOSPHERE_PRESSURE = "atmosphere_pressure";
    public static final String COLUMN_ATMOSPHERE_HUMIDITY = "atmosphere_humidity";

    public static final String COLUMN_WIND_DIRECTION = "wind_direction";
    public static final String COLUMN_WIND_SPEED = "wind_speed";

    public static final String COLUMN_FORECAST = "forecast";

     */

    private void updateUserInterface() {
        if (mCursor == null) {
            return;
        }
        final String cityName = mCursor.getString(mCursor.getColumnIndex(WeatherTable.COLUMN_CITY));
        final String weatherDesc = mCursor.getString(mCursor.getColumnIndex(WeatherTable.COLUMN_CONDITION_DESCRIPTION));
        final int temp = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_CONDITION_TEMP));
        final int currentWeatherIconId = getImageByCode(mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_CONDITION_CODE)));

        final String temperature = String.format("%d°", temp);
//        final String lastUpdate = "Last update: " + mCursor.getString(mCursor.getColumnIndex(WeatherContract.City.CITY_LAST_UPDATE)) .replace("T", " ");
        final String humidityInfo = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_ATMOSPHERE_HUMIDITY)) + "%";
        final String windInfo = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_WIND_SPEED)) + " kph " +
                mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_WIND_DIRECTION)) + "°";

        final double pressure = mCursor.getDouble(mCursor.getColumnIndex(WeatherTable.COLUMN_ATMOSPHERE_PRESSURE));
        final String pressureInfo = String.format("%.1f mb", pressure);

        ((ImageView) mRootView.findViewById(R.id.weather_icon))
                .setImageBitmap(BitmapFactory.decodeResource(getResources(), currentWeatherIconId));
        ((TextView) mRootView.findViewById(R.id.city_name)).setText(cityName);
        ((TextView) mRootView.findViewById(R.id.weather_desc)).setText(weatherDesc);
        ((TextView) mRootView.findViewById(R.id.detail_temperature)).setText(temperature);

        ((TextView) mRootView.findViewById(R.id.wind_info)).setText(windInfo);
        ((TextView) mRootView.findViewById(R.id.humidity_info)).setText(humidityInfo);
        ((TextView) mRootView.findViewById(R.id.pressure_info)).setText(pressureInfo);

        String forecast = mCursor.getString(mCursor.getColumnIndex(WeatherTable.COLUMN_FORECAST));
        if (forecast == null || !forecast.contains("|")) {
            WeatherLoaderService.startActionUpdateCity(getActivity(), mCityId, mCityWoeid);
            return;
        }

        Log.d("Tag", "FORECAST");
        Log.d("TAG", forecast);

        String[] parts = forecast.split("\\|");
/*        try {
            for (int i = 0; i < 5 * 6; i += 6) {
                String day = parts[i];
                String date = parts[i + 1];
                String description = parts[i + 2];
                String minTemp = "MIN: " + parts[i + 3] + "°";
                String maxTemp = "MAX: " + parts[i + 4] + "°";
                int code = Integer.parseInt(parts[i + 5]);
                int index = i / 6;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
                SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
                try {
                    Date dt = dateFormat.parse(date);
                    date = toTitleCase(dayOfWeekFormat.format(dt));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                switch (index) {
                    case 0:
                        ((TextView) mRootView.findViewById(R.id.min_temp1)).setText(minTemp);
                        ((TextView) mRootView.findViewById(R.id.max_temp1)).setText(maxTemp);
                        ((TextView) mRootView.findViewById(R.id.forecast_date1)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc1)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_1))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        break;
                    case 1:
                        ((TextView) mRootView.findViewById(R.id.min_temp2)).setText(minTemp);
                        ((TextView) mRootView.findViewById(R.id.max_temp2)).setText(maxTemp);
                        ((TextView) mRootView.findViewById(R.id.forecast_date2)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc2)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_2))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        break;
                    case 2:
                        ((TextView) mRootView.findViewById(R.id.min_temp3)).setText(minTemp);
                        ((TextView) mRootView.findViewById(R.id.max_temp3)).setText(maxTemp);
                        ((TextView) mRootView.findViewById(R.id.forecast_date3)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc3)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_3))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        break;
                    case 3:
                        ((TextView) mRootView.findViewById(R.id.min_temp4)).setText(minTemp);
                        ((TextView) mRootView.findViewById(R.id.max_temp4)).setText(maxTemp);
                        ((TextView) mRootView.findViewById(R.id.forecast_date4)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc4)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_4))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        break;
                    case 4:
                        ((TextView) mRootView.findViewById(R.id.min_temp5)).setText(minTemp);
                        ((TextView) mRootView.findViewById(R.id.max_temp5)).setText(maxTemp);
                        ((TextView) mRootView.findViewById(R.id.forecast_date5)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc5)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_5))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        }*/
    }

    private int getImageByCode(int code) {
        return R.drawable.snowflake;
                /*
            0	tornado
            1	tropical storm
            2	hurricane
            3	severe thunderstorms
            4	thunderstorms
            5	mixed rain and snow
            6	mixed rain and sleet
            7	mixed snow and sleet
            8	freezing drizzle
            9	drizzle
            10	freezing rain
            11	showers
            12	showers
            13	snow flurries
            14	light snow showers
            15	blowing snow
            16	snow
            17	hail
            18	sleet
            19	dust
            20	foggy
            21	haze
            22	smoky
            23	blustery
            24	windy
            25	cold
            26	cloudy
            27	mostly cloudy (night)
            28	mostly cloudy (day)
            29	partly cloudy (night)
            30	partly cloudy (day)
            31	clear (night)
            32	sunny
            33	fair (night)
            34	fair (day)
            35	mixed rain and hail
            36	hot
            37	isolated thunderstorms
            38	scattered thunderstorms
            39	scattered thunderstorms
            40	scattered showers
            41	heavy snow
            42	scattered snow showers
            43	heavy snow
            44	partly cloudy
            45	thundershowers
            46	snow showers
            47	isolated thundershowers
            3200	not available


         */

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOADER_CITY_WEATHER:
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
        getLoaderManager().initLoader(LOADER_CITY_WEATHER, null, this).forceLoad();
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
        mCityWoeid = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_WOEID));
        updateUserInterface();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
