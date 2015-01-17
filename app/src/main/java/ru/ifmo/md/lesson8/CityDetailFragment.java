package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    public boolean isNetworkAvailable() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                mCityWoeid = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_WOEID));
                if (!isNetworkAvailable()) {
                    Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_LONG).show();
                    return true;
                }
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
                if (!isAdded())
                    return;
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

    private static final int SMALL_FONT_SIZE = 20;
    private void updateUserInterface() {
        if (mCursor == null) {
            return;
        }
        final String cityName = mCursor.getString(mCursor.getColumnIndex(WeatherTable.COLUMN_CITY));
        final String lastUpdateDate = mCursor.getString(mCursor.getColumnIndex(WeatherTable.COLUMN_LASTUPD));
        final String weatherDesc = mCursor.getString(mCursor.getColumnIndex(WeatherTable.COLUMN_CONDITION_DESCRIPTION));
        final int temp = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_CONDITION_TEMP));
        final int currentWeatherIconId = getImageByCode(mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_CONDITION_CODE)));

        final String temperature = String.format("%d°", temp);
//        final String lastUpdate = "Last update: " + mCursor.getString(mCursor.getColumnIndex(WeatherContract.City.CITY_LAST_UPDATE)) .replace("T", " ");
        final String humidityInfo = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_ATMOSPHERE_HUMIDITY)) + "%";

        long windSpeed = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_WIND_SPEED));
        windSpeed = Math.round(windSpeed / 3.6);
        int windAngle = mCursor.getInt(mCursor.getColumnIndex(WeatherTable.COLUMN_WIND_DIRECTION));
        final String windInfo = windSpeed + " m/s " + getWindDirection(windAngle);

        final double pressure = 0.7500637 * mCursor.getDouble(mCursor.getColumnIndex(WeatherTable.COLUMN_ATMOSPHERE_PRESSURE));
        final String pressureInfo = String.format("%.1f mm Hg", pressure);

        String lastUpdate = "Last update: " + lastUpdateDate;
        if (TextUtils.isEmpty(lastUpdate))
            lastUpdate = "";
        ((TextView) mRootView.findViewById(R.id.tv_last_update)).setText(lastUpdate);

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

        String[] parts = forecast.split("\\|");
        try {
            for (int i = 0; i < 5 * 6; i += 6) {
                String day = parts[i];
                String date = parts[i + 1];
                String description = parts[i + 2];
                String minTemp = parts[i + 3] + "°";
                String maxTemp = parts[i + 4] + "°";
                String tempBounds = minTemp + "... " + maxTemp;
                int code = Integer.parseInt(parts[i + 5]);
                int index = i / 6;

                if (description.contains("AM"))
                    description = description.replace("AM", "");
                else if (description.contains("PM"))
                    description = description.replace("PM", "");

                TextView tvTextView;
                switch (index) {
                    case 0:
                        ((TextView) mRootView.findViewById(R.id.forecast_dayofweek1)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_date1)).setText(date);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc1)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_1))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        tvTextView = (TextView) mRootView.findViewById(R.id.forecast_temp1);
                        tvTextView.setText(tempBounds);
                        if (tvTextView.getLineCount() > 1)
                            tvTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL_FONT_SIZE);
                        break;
                    case 1:
                        ((TextView) mRootView.findViewById(R.id.forecast_dayofweek2)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_date2)).setText(date);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc2)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_2))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        tvTextView = (TextView) mRootView.findViewById(R.id.forecast_temp2);
                        tvTextView.setText(tempBounds);
                        if (tvTextView.getLineCount() > 1)
                            tvTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL_FONT_SIZE);
                        break;
                    case 2:
                        ((TextView) mRootView.findViewById(R.id.forecast_dayofweek3)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_date3)).setText(date);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc3)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_3))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        tvTextView = (TextView) mRootView.findViewById(R.id.forecast_temp3);
                        tvTextView.setText(tempBounds);
                        if (tvTextView.getLineCount() > 1)
                            tvTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL_FONT_SIZE);
                        break;
                    case 3:
                        ((TextView) mRootView.findViewById(R.id.forecast_dayofweek4)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_date4)).setText(date);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc4)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_4))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        tvTextView = (TextView) mRootView.findViewById(R.id.forecast_temp4);
                        tvTextView.setText(tempBounds);
                        if (tvTextView.getLineCount() > 1)
                            tvTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL_FONT_SIZE);
                        break;
                    case 4:
                        ((TextView) mRootView.findViewById(R.id.forecast_dayofweek5)).setText(day);
                        ((TextView) mRootView.findViewById(R.id.forecast_date5)).setText(date);
                        ((TextView) mRootView.findViewById(R.id.forecast_desc5)).setText(description);
                        ((ImageView) mRootView.findViewById(R.id.forecast_icon_5))
                                .setImageBitmap(BitmapFactory.decodeResource(getResources(), getImageByCode(code)));
                        tvTextView = (TextView) mRootView.findViewById(R.id.forecast_temp5);
                        tvTextView.setText(tempBounds);
                        if (tvTextView.getLineCount() > 1)
                            tvTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SMALL_FONT_SIZE);
                        break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        }
    }

    private String getWindDirection(int angle) {
        double dAngle = angle * 1.0;
        if (348.75 <= dAngle && dAngle <= 360.0 || 0 <= dAngle && dAngle <= 11.25)
            return "N";
        if (11.25 <= dAngle && dAngle <= 33.75)
            return "NNE";
        if (33.75 <= dAngle && dAngle <= 56.25)
            return "NE";
        if (56.25 <= dAngle && dAngle <= 78.75)
            return "ENE";
        if (78.75 <= dAngle && dAngle <= 101.25)
            return "E";
        if (101.25 <= dAngle && dAngle <= 123.75)
            return "ESE";
        if (123.75 <= dAngle && dAngle <= 146.25)
            return "SE";
        if (146.25 <= dAngle && dAngle <= 168.75)
            return "SSE";
        if (168.75 <= dAngle && dAngle <= 191.25)
            return "S";
        if (191.25 <= dAngle && dAngle <= 213.75)
            return "SSW";
        if (213.75 <= dAngle && dAngle <= 236.25)
            return "SWW";
        if (236.25 <= dAngle && dAngle <= 258.75)
            return "WSW";
        if (258.75 <= dAngle && dAngle <= 281.25)
            return "W";
        if (281.25 <= dAngle && dAngle <= 303.75)
            return "WNW";
        if (303.75 <= dAngle && dAngle <= 326.25)
            return "NW";
        if (326.25 <= dAngle && dAngle <= 348.75)
            return "NNW";
        return "?";
    }

    private int getImageByCode(int code) {
        if (code == 48)
            return R.drawable.wna;
        String name = "w" + (code < 10 ? "0" + code : String.valueOf(code));
        return getResources().getIdentifier(name, "drawable", getActivity().getPackageName());
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
