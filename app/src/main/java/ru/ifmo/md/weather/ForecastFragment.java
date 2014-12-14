package ru.ifmo.md.weather;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import ru.ifmo.md.weather.db.WeatherContentProvider;
import ru.ifmo.md.weather.db.model.WeatherTable;

/**
 * Created by Kirill on 08.12.2014.
 */
public class ForecastFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private WeatherCursorAdapter weatherCursorAdapter;
    private Activity activity;

    public ForecastFragment() {
        super();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(LoadWeatherService.RESULT);
                String error = bundle.getString(LoadWeatherService.RESULT);
                if (resultCode == 1) {
                    Toast.makeText(context,
                            "Forecast download complete.", Toast.LENGTH_SHORT).show();
                    weatherCursorAdapter.changeCursor(context.getContentResolver().query(WeatherContentProvider.CONTENT_URI_WEATHER, null, null, null, null));
                    weatherCursorAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Error: " + error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public void onStart() {
        Log.i("ForecastFragment", "onStart()");
        super.onStart();
        setListAdapter(weatherCursorAdapter);
        weatherCursorAdapter.notifyDataSetChanged();
        activity = getActivity();
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i("ForecastFragment", "onAttach()");
        super.onAttach(activity);
        //this.activity = activity;
    }
    @Override
    public void onActivityCreated(Bundle bundle) {
        Log.i("ForecastFragment", "onActivityCreated()");
        super.onActivityCreated(bundle);
        //activity = getActivity();
        /*if (activity != null)
            Log.i("", "activity != null");
        else
            Log.i("", "activity is null :(");*/
    }

    @Override
    public void onCreate(Bundle icicle) {
        Log.i("ForecastFragment", "onCreate()");
        super.onCreate(icicle);
        getLoaderManager().initLoader(0, null, this);
        weatherCursorAdapter = new WeatherCursorAdapter(this.getActivity(), null, 0);
        /*Cursor c = weatherCursorAdapter.getCursor();
        while (c.moveToNext()) {

        }*/
        weatherCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
        weatherCursorAdapter.notifyDataSetChanged();
        getActivity().registerReceiver(receiver, new IntentFilter(LoadWeatherService.NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        weatherCursorAdapter.notifyDataSetChanged();
        getActivity().unregisterReceiver(receiver);
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.forecast_fragment_root, container, false);

        return view;
    }*/


    public void display(long cityId) {
        Log.i("ForecastFragment", "display()");
        if (activity != null)
            Log.i("", "activity != null");
        else
            Log.i("", "activity is still null :(");
        ContentResolver cr = activity.getContentResolver();

        weatherCursorAdapter.changeCursor(cr.query(WeatherContentProvider.CONTENT_URI_WEATHER, null,
                WeatherTable.CITY_ID_COLUMN + " = ", new String[]{cityId + ""}, null));
        weatherCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(this.getActivity(),
                WeatherContentProvider.CONTENT_URI_WEATHER, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        weatherCursorAdapter.swapCursor(data);
        weatherCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        weatherCursorAdapter.swapCursor(null);
        weatherCursorAdapter.notifyDataSetChanged();
    }
}
