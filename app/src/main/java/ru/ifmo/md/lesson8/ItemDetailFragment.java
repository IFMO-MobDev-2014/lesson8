package ru.ifmo.md.lesson8;

import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.nio.channels.NonWritableChannelException;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private long mWeatherId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mWeatherId = getArguments().getLong(ARG_ITEM_ID, -1);
            WeatherFetchingService.startActionUpdateWeather(getActivity(), mWeatherId);
        }

    }

    ScrollView rootView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.item_detail_layout, container, false);
        rootView = ((ScrollView) result.findViewById(R.id.svContent));
        fillData();
        return result;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_WEATHER)
            return new CursorLoader(getActivity(),
                    Uri.parse(WeatherContentProvider.WEATHER_URI.toString()), null, null, null, null);
        return new CursorLoader(getActivity(),
                Uri.parse(WeatherContentProvider.FORECAST_URI.toString() + "/" + mWeatherId), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mWeatherId != -1 && data != null) {
            int[] pictures = getResources().getIntArray(R.array.weatherPicturesByCode);
            if (loader.getId() == LOADER_WEATHER) {
                View currentWeather = rootView.findViewById(R.id.llCurrentWeather);
                currentWeather.setVisibility(View.GONE);
                TextView tvTemperature = (TextView) (currentWeather.findViewById(R.id.tvTemperature));
                ImageView ivConditions = (ImageView) (currentWeather.findViewById(R.id.ivConditions));
                TextView tvDetails = (TextView) (currentWeather.findViewById(R.id.tvDetails));
                data.moveToFirst();
                do {
                    if (data.getLong(data.getColumnIndex(DBAdapter.KEY_ID)) == mWeatherId) {
                        if (data.getDouble(data.getColumnIndex(DBAdapter.KEY_WEATHER_ATMOSPHERE_PRESSURE)) != 0.) {
                            currentWeather.setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.pbProgress).setVisibility(View.GONE);
                        }
                        tvTemperature.setText
                                ("" +
                                        data.getInt(data.getColumnIndex(DBAdapter.KEY_WEATHER_TEMPERATURE)) +
                                        getString(R.string.celcius));
                        String conditions = getString(R.string.humidity) +
                                data.getInt(data.getColumnIndex(DBAdapter.KEY_WEATHER_ATMOSPHERE_HUMIDITY)) +
                                getString(R.string.percents_sign)
                                + "\n" + getString(R.string.pressure)
                                + Math.round(data.getDouble(data.getColumnIndex(DBAdapter.KEY_WEATHER_ATMOSPHERE_PRESSURE)) * 0.750061561303) + getString(R.string.pressure_unit)
                                + "\n" + getString(R.string.wind) + windDirectionToString(
                                data.getInt(data.getColumnIndex(DBAdapter.KEY_WEATHER_WIND_DIRECTION)))
                                + ", " + (int) (data.getDouble(data.getColumnIndex(DBAdapter.KEY_WEATHER_WIND_SPEED)) * 1000.0 / 3600) + " " + getString(R.string.metres_per_second)
                                + "\n" + data.getString(data.getColumnIndex(DBAdapter.KEY_WEATHER_TEXT));
                        tvDetails.setText(conditions);
                        int picture = pictures[data.getInt(data.getColumnIndex(DBAdapter.KEY_WEATHER_CODE))];
                        ivConditions.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("i96_" +
                                        picture,"drawable",
                                        getActivity().getPackageName())));
                        ActionBar supportActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
                        if (supportActionBar != null)
                            supportActionBar.setTitle(data.getString(data.getColumnIndex(DBAdapter.KEY_WEATHER_CITY)));
                        break;
                    }
                } while (data.moveToNext());
            } else if (loader.getId() == LOADER_FORECAST) {
                LinearLayout layout = ((LinearLayout) rootView.findViewById(R.id.llItemDetail));
                layout.removeAllViews();
                data.moveToFirst();
                if (data.getCount() > 0)
                    do {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View forecastView = inflater.inflate(R.layout.weather_forecast, layout, false);
                        ImageView ivConditions = (ImageView) forecastView.findViewById(R.id.ivConditions);
                        TextView tvDay = (TextView) (forecastView.findViewById(R.id.tvDay));
                        TextView tvDetails = (TextView) (forecastView.findViewById(R.id.tvDetails));
                        TextView tvTemperature = (TextView) (forecastView.findViewById(R.id.tvTemperature));
                        tvDay.setText(
                                data.getPosition() == 0 ? getString(R.string.dayToday) :
                                data.getPosition() == 1 ? getString(R.string.dayTommorow) :
                                       data.getString(data.getColumnIndex(DBAdapter.KEY_FORECASTS_DAY)));
                        String description = data.getInt(data.getColumnIndex(DBAdapter.KEY_FORECASTS_LOW)) + getString(R.string.celcius)
                                + "\n" + data.getInt(data.getColumnIndex(DBAdapter.KEY_FORECASTS_HIGH)) + getString(R.string.celcius);
                        tvTemperature.setText(description);
                        tvDetails.setText(data.getString(data.getColumnIndex(DBAdapter.KEY_FORECASTS_TEXT)));
                        layout.addView(forecastView);
                        ivConditions.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("i96_" +
                                        pictures[data.getInt(data.getColumnIndex(DBAdapter.KEY_WEATHER_CODE))],"drawable",
                                getActivity().getPackageName())));
                    } while (data.moveToNext());
            }
        } else
            Log.e("ru.ifmo.md.lesson8", "Cursor is null");
    }

    private String windDirectionToString(int direction) {
        double sectorBorder = 360. / 16;
        if (direction <= sectorBorder || direction > sectorBorder * 15)
            return "N";
        if (direction <= sectorBorder * 3)
            return "NE";
        if (direction <= sectorBorder * 5)
            return "E";
        if (direction <= sectorBorder * 7)
            return "SE";
        if (direction <= sectorBorder * 9)
            return "S";
        if (direction <= sectorBorder * 11)
            return "SW";
        if (direction <= sectorBorder * 13)
            return "W";
        if (direction <= sectorBorder * 15)
            return "NW";
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_WEATHER)
            rootView.findViewById(R.id.llCurrentWeather).setVisibility(View.GONE);
        else if (loader.getId() == LOADER_FORECAST)
            ((ViewGroup) rootView.findViewById(R.id.llItemDetail)).removeAllViews();
    }

    public static final int LOADER_WEATHER = 0;
    public static final int LOADER_FORECAST = 1;

    private void fillData() {
        getLoaderManager().initLoader(LOADER_WEATHER, null, this);
        getLoaderManager().initLoader(LOADER_FORECAST, null, this);
    }
}
