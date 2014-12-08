package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class WeatherFragment extends Fragment {
    private static final String ARG_CITYID = "cityId";
    private static final String ARG_CITYNAME = "cityName";

    private int cityId;
    private String cityName;

    private ListView forecastList;
    private View forecastView;

    private CursorAdapter adapterNow, adapterForecast;
    private MergeAdapter mergeAdapter;

    private OnFragmentInteractionListener mListener;

    public static WeatherFragment newInstance(int cityId, String cityName) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CITYID, cityId);
        args.putString(ARG_CITYNAME, cityName);
        fragment.setArguments(args);
        return fragment;
    }

    public WeatherFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityId = getArguments().getInt(ARG_CITYID);
            cityName = getArguments().getString(ARG_CITYNAME);
        } else {
            throw new NullPointerException("No argument passed to WeatherFragment. Bug?");
        }
        mListener.setCityName(cityName);

        getLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity().getApplicationContext(),
                        WeatherContentProvider.URI_CITY_DIR.buildUpon().appendPath(cityId + "").build(),
                        WeatherDatabase.Structure.FULL_WEATHER_PROJECTION,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                //((CursorAdapter) forecastList.getAdapter()).swapCursor(data);
                adapterForecast.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });

        LoaderManager.LoaderCallbacks<Cursor> myCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity().getApplicationContext(),
                        WeatherContentProvider.URI_CITY_DIR,
                        WeatherDatabase.Structure.FULL_CITY_PROJECTION,
                        WeatherDatabase.Structure.COLUMN_URL + " = ?",
                        new String[] {"" + cityId},
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                //onFreshCityData(data);
                adapterNow.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // nothing
            }
        } ;

        getLoaderManager().initLoader(2, null, myCallbacks);
    }

    public void onFreshCityData(Cursor cr) {
        cr.moveToFirst();
        if(cr.isAfterLast())
            return;

        ImageView iv = (ImageView) forecastView.findViewById(R.id.weatherImage);
        iv.setImageResource(WeatherDataUtils.getIconForCode(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_DESCRIPTION)), cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_CLOUDS)), false));

        TextView tv = (TextView) forecastView.findViewById(R.id.tempText);
        tv.setText(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_TEMPERATURE)) / 10.0f + " °C");

        tv = (TextView) forecastView.findViewById(R.id.windText);
        tv.setText(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_WIND)) / 10.0f + " m/s " + WeatherDataUtils.getWindDirByDegs(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_WIND_DIR))));

        tv = (TextView) forecastView.findViewById(R.id.pressHumText);
        tv.setText(((int) (cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_PRESSURE)) * 0.75d)) + " mmHg " + cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_HUMIDITY)) + "%");

        tv = (TextView) forecastView.findViewById(R.id.weatherSubText);
        tv.setText(WeatherDataUtils.getDescriptionForCode(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_DESCRIPTION))));

        tv = (TextView) forecastView.findViewById(R.id.weatherDayText);
        tv.setText(getActivity().getString(R.string.now));

        //mListener.setCityName(cr.getString(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_NAME)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View w = inflater.inflate(R.layout.fragment_weather, container, false);
        forecastView = w;

        forecastList = (ListView) w.findViewById(R.id.forecastList);

        adapterNow = new WeatherCursorAdapter(getActivity(), null, 0, true);
        adapterForecast = new WeatherCursorAdapter(getActivity(), null, 0, false);
        mergeAdapter = new MergeAdapter(adapterNow, adapterForecast);

        forecastList.setAdapter(mergeAdapter);

        return w;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void setCityName(String cityName);
    }

    private class WeatherCursorAdapter extends CursorAdapter {

        private boolean isNow;

        public WeatherCursorAdapter(Context context, Cursor c, int flags, boolean isNow) {
            super(context, c, flags);
            this.isNow = isNow;
        }

        @Override
        public View newView(Context context, Cursor cr, ViewGroup parent) {
            View rv = getActivity().getLayoutInflater().inflate(R.layout.layout_weather_day, parent, false);

            bindView(rv, context, cr);

            return rv;
        }

        @Override
        public void bindView(View view, Context context, Cursor cr) {
            ImageView iv = (ImageView) view.findViewById(R.id.weatherImage);
            iv.setImageResource(WeatherDataUtils.getIconForCode(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_DESCRIPTION)), cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_CLOUDS)), false));

            TextView tv = (TextView) view.findViewById(R.id.windText);
            tv.setText(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_WIND)) / 10.0f + " m/s " + WeatherDataUtils.getWindDirByDegs(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_WIND_DIR))));


            tv = (TextView) view.findViewById(R.id.tempText);
            if(!isNow) {
                tv.setText(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_TEMPERATURE_MIN)) / 10.0f + "/" + cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_TEMPERATURE_MAX)) / 10.0f + " °C");
            } else {
                tv.setText(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_TEMPERATURE)) / 10.0f + " °C");
            }


            tv = (TextView) view.findViewById(R.id.pressHumText);
            tv.setText(((int) (cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_PRESSURE)) * 0.75d)) + " mmHg " + cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_HUMIDITY)) + "%");

            tv = (TextView) view.findViewById(R.id.weatherSubText);
            tv.setText(WeatherDataUtils.getDescriptionForCode(cr.getInt(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_DESCRIPTION))));

            if(!isNow) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(cr.getLong(cr.getColumnIndex(WeatherDatabase.Structure.COLUMN_TIME)) * 1000L);
                tv = (TextView) view.findViewById(R.id.weatherDayText);
                tv.setText(DateFormat.getDateInstance().format(cal.getTime()));
            } else {
                tv = (TextView) view.findViewById(R.id.weatherDayText);
                tv.setText(R.string.now);
            }
        }
    }

}
