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
import android.widget.Toast;

import ru.ifmo.md.weather.db.WeatherContentProvider;
import ru.ifmo.md.weather.db.model.WeatherTable;

/**
 * Created by Kirill on 08.12.2014.
 */
public class ForecastFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String CITY_ID = "cityItemId";

    private ForecastCursorAdapter forecastCursorAdapter;
    private Activity activity;

    private long id = -1;

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
                    forecastCursorAdapter.changeCursor(context.getContentResolver().query(WeatherContentProvider.CONTENT_URI_WEATHER, null, null, null, null));
                    forecastCursorAdapter.notifyDataSetChanged();
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
        activity = getActivity();
        if (id >= 0L) {
            display(id);
        } else {
            setListAdapter(forecastCursorAdapter);
            forecastCursorAdapter.notifyDataSetChanged();
        }
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
        forecastCursorAdapter = new ForecastCursorAdapter(this.getActivity(), null, 0);
        id = -1;
        if (icicle != null)
            icicle.getLong(CITY_ID, -1L);

        /*Cursor c = weatherCursorAdapter.getCursor();
        while (c.moveToNext()) {

        }*/
        forecastCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
        forecastCursorAdapter.notifyDataSetChanged();
        getActivity().registerReceiver(receiver, new IntentFilter(LoadWeatherService.NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        forecastCursorAdapter.notifyDataSetChanged();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    public void display(long cityId) {
        Log.i("ForecastFragment", "display()");
        if (activity != null)
            Log.i("", "activity != null");
        else
            Log.i("", "activity is still null :(");
        ContentResolver cr = activity.getContentResolver();

        forecastCursorAdapter.changeCursor(cr.query(WeatherContentProvider.CONTENT_URI_WEATHER, null,
                WeatherTable.CITY_ID_COLUMN + " = ", new String[]{cityId + ""}, null));
        forecastCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(this.getActivity(),
                WeatherContentProvider.CONTENT_URI_WEATHER, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastCursorAdapter.swapCursor(data);
        forecastCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastCursorAdapter.swapCursor(null);
        forecastCursorAdapter.notifyDataSetChanged();
    }
}
