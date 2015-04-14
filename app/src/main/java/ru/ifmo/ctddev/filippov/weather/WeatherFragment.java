package ru.ifmo.ctddev.filippov.weather;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Dima_2 on 01.04.2015.
 */
public class WeatherFragment extends Fragment {
    private static final String CITY_ID = "cityId";
    private static final String CITY_NAME = "cityName";

    private int cityId;
    private ListView forecastList;
    private View forecastView;
    private OnFragmentInteractionListener listener;

    public WeatherFragment() {

    }

    public interface OnFragmentInteractionListener {
        public void setCityName(String cityName);
    }

    private class WeatherCursorAdapter extends CursorAdapter {

        public WeatherCursorAdapter(Context context, Cursor cursor, boolean autoUpdate) {
            super(context, cursor, autoUpdate);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.layout_weather_day, parent, false);
            bindView(view, context, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.weather_image);
            imageView.setImageResource(WeatherUtils.getIconForCode(
                            cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_DESCRIPTION)),
                            cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_CLOUDS)),
                            false)
            );

            TextView textView = (TextView) view.findViewById(R.id.temperature_description);
            textView.setText(
                    (int) (cursor.getDouble(cursor.getColumnIndex(WeatherDatabase.COLUMN_TEMPERATURE_MIN))) +
                            getResources().getString(R.string.slash_sign) +
                            (int) (cursor.getDouble(cursor.getColumnIndex(WeatherDatabase.COLUMN_TEMPERATURE_MAX))) +
                            getResources().getString(R.string.degrees)
            );

            textView = (TextView) view.findViewById(R.id.wind_description);
            textView.setText(
                    (int) (cursor.getDouble(cursor.getColumnIndex(WeatherDatabase.COLUMN_WIND))) +
                            getResources().getString(R.string.metres_per_second) +
                            WeatherUtils.getWindDirection(cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_WIND_DIRECTION)))
            );

            textView = (TextView) view.findViewById(R.id.wet_description);
            textView.setText(
                    ((int) (cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_PRESSURE)) * 0.75f)) +
                            getResources().getString(R.string.millimetres) +
                            cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_WET)) +
                            getResources().getString(R.string.percent_sign)
            );

            textView = (TextView) view.findViewById(R.id.weather_description);
            textView.setText(WeatherUtils.getDescriptionForCode(cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_DESCRIPTION))));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(WeatherDatabase.COLUMN_TIME)) * 1000L);
            textView = (TextView) view.findViewById(R.id.weather_this_moment);
            textView.setText(DateFormat.getDateInstance().format(calendar.getTime()));
        }
    }

    public static WeatherFragment newInstance(int cityId, String cityName) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle fragments = new Bundle();
        fragments.putInt(CITY_ID, cityId);
        fragments.putString(CITY_NAME, cityName);
        fragment.setArguments(fragments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String cityName;
        cityId = getArguments().getInt(CITY_ID);
        cityName = getArguments().getString(CITY_NAME);
        listener.setCityName(cityName);

        getLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity().getApplicationContext(),
                        WeatherContentProvider.URI_CITY_DIRECTORY.buildUpon().appendPath(cityId + "").build(),
                        WeatherDatabase.FULL_WEATHER_DESCRIPTION,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                ((CursorAdapter) forecastList.getAdapter()).swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });

        LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity().getApplicationContext(),
                        WeatherContentProvider.URI_CITY_DIRECTORY,
                        WeatherDatabase.FULL_CITY_DESCRIPTION,
                        WeatherDatabase.COLUMN_URL + " = ?",
                        new String[]{"" + cityId},
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                onFreshCityData(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        };
        getLoaderManager().initLoader(2, null, cursorLoaderCallbacks);
    }

    public void onFreshCityData(Cursor cursor) {
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            return;
        }
        ImageView imageView = (ImageView) forecastView.findViewById(R.id.weather_image);
        imageView.setImageResource(WeatherUtils.getIconForCode(
                cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_CLOUDS)),
                false
        ));

        TextView textView = (TextView) forecastView.findViewById(R.id.temperature_description);
        textView.setText(String.valueOf(
                (int) (cursor.getDouble(cursor.getColumnIndex(WeatherDatabase.COLUMN_TEMPERATURE))) +
                        getResources().getString(R.string.degrees)
        ));

        textView = (TextView) forecastView.findViewById(R.id.wind_description);
        textView.setText(
                (int) (cursor.getDouble(cursor.getColumnIndex(WeatherDatabase.COLUMN_WIND))) +
                        getResources().getString(R.string.metres_per_second) +
                        WeatherUtils.getWindDirection(cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_WIND_DIRECTION)))
        );

        textView = (TextView) forecastView.findViewById(R.id.wet_description);
        textView.setText(
                ((int) (cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_PRESSURE)) * 0.75f)) +
                        getResources().getString(R.string.millimetres) +
                        cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_WET)) +
                        getResources().getString(R.string.percent_sign)
        );

        textView = (TextView) forecastView.findViewById(R.id.weather_description);
        textView.setText(WeatherUtils.getDescriptionForCode(cursor.getInt(cursor.getColumnIndex(WeatherDatabase.COLUMN_DESCRIPTION))));

        textView = (TextView) forecastView.findViewById(R.id.weather_this_moment);
        textView.setText(getActivity().getString(R.string.now_weather));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_fragment, container, false);
        forecastView = view;
        forecastList = (ListView) view.findViewById(R.id.forecast_list);
        forecastList.setAdapter(new WeatherCursorAdapter(getActivity().getApplicationContext(), null, false));
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnFragmentInteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
