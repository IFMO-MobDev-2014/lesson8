package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_CITY_ID = "city_id";
    private SimpleCursorAdapter adapter;

    public WeatherFragment() {
    }

    public static WeatherFragment newInstance(long cityId) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CITY_ID, cityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) view.findViewById(R.id.weathers_list);
        String[] from = new String[]{"weekday", "weather_code", "weather_type", "temp"};
        int[] to = new int[]{R.id.weather_weekday, R.id.weather_type_image, R.id.weather_type_text, R.id.weather_temperature};
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.day_weather, null, from, to, 0);
        listView.setAdapter(adapter);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                int id = view.getId();
                if (id == R.id.weather_type_image) {
                    int code = cursor.getInt(columnIndex);
                    int resId = view.getResources().getIdentifier("@drawable/" + "p" + code, "drawable", view.getContext().getPackageName());
                    ((ImageView) view).setImageResource(resId);
                    return true;
                } else if (id == R.id.weather_temperature) {
                    int temp = cursor.getInt(columnIndex);
                    ((TextView) view).setText(temp + view.getResources().getString(R.string.degree_celsius));
                    return true;
                }
                return false;
            }
        });

        getActivity().getLoaderManager().initLoader(MainActivity.WEATHERS_LOADER_ID, getArguments(), this);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {"_id", "weekday", "weather_code", "weather_type", "temp"};
        Long cityId = bundle.getLong(ARG_CITY_ID);
        final Uri uri = Uri.withAppendedPath(WeatherContentProvider.CONTENT_URI_WEATHERS, "" + cityId);
        return new CursorLoader(getActivity(), uri, projection, null, null, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.getLoaderManager().restartLoader(MainActivity.WEATHERS_LOADER_ID, getArguments(), this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        cursor.moveToFirst();
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }
}
