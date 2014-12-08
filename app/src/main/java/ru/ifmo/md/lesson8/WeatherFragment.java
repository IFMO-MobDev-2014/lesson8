package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String CITY_ID_EXTRA = "city_id";
    public static final String CITY_NAME_EXTRA = "city_name";
    public static final String NO_INTERNET_CONNECTION = "No internet connection";
    public static final String WEATHER_ALREADY_UPDATED = "No update is required";
    public static final String UPDATING_MESSAGE = "Updating";
    public static final SimpleDateFormat MINIMAL_DATE_FORMAT = new SimpleDateFormat("EEEE, MMM d");
    public static final String ERROR_LOADING_CITY = "Couldn't load city";
    private static final String LAST_SELECTED_ID = "last_selected_id";
    private int cityId;
    private String cityName;

    private WeatherDataAdapter adapter;
    private RecyclerView weatherRecycleView;
    private View mainView;
    private Handler handler;
    private boolean needToDisplayToast = false;
    private AlertDialog intervalDialog;

    public WeatherFragment() {
    }

    public static WeatherFragment newInstance(City city) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putInt(CITY_ID_EXTRA, city.getId());
        args.putString(CITY_NAME_EXTRA, city.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("WeatherFragment", "onAttach: " + getArguments().getString(CITY_NAME_EXTRA));
        ((WeatherActivity) activity).onSectionAttached(getArguments().getString(CITY_NAME_EXTRA));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), WeatherContentProvider.WEATHER_CONTENT_URI, null,
                WeatherDatabaseHelper.WEATHER_CITY_ID + " = " + cityId, null,
                WeatherDatabaseHelper.WEATHER_DATE + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (adapter == null) {
            adapter = new WeatherDataAdapter(getActivity());
            adapter.setOnItemClickListener(new WeatherDataAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    adapter.setCurrentItem(pos);
                    setMinimalDescription(pos);
                }
            });
            weatherRecycleView.setAdapter(adapter);
        }
        if (cursor.isAfterLast()) {
            return;
        }
        adapter.clear();
        WeatherDatabaseHelper.WeatherDataCursor wc = new WeatherDatabaseHelper.WeatherDataCursor(cursor);
        while (cursor.moveToNext()) {
            adapter.add(wc.getWeatherData());
        }
        setMinimalDescription(0);
        setMainWeather(0);
        adapter.setCurrentItem(0);
        adapter.notifyDataSetChanged();
    }

    public void setMainWeather(int id) {
        WeatherData weatherData = adapter.getItem(id);
        ((TextView) mainView.findViewById(R.id.temperatureTextView)).setText(
                weatherData.getTemperatureMin() + "°C/" + weatherData.getTemperatureMax() + "°C");
        ((TextView) mainView.findViewById(R.id.pressureTextView)).setText(
                weatherData.getPressure() + " mb");
        ((TextView) mainView.findViewById(R.id.windTextView)).setText(
                weatherData.getWindSpeed() + " m/s");
        if (weatherData.getHumidity() != 0) {
            ((TextView) mainView.findViewById(R.id.humidityTextView)).setText(
                    weatherData.getHumidity() + "%");
        } else {
            ((TextView) mainView.findViewById(R.id.humidityTextView)).setText("-");
        }
        try {
            AssetManager manager = getActivity().getAssets();
            Bitmap bitmap = BitmapFactory.decodeStream(manager.open(weatherData.getWeatherInfo().getIconName()));
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Configuration config = getActivity().getResources().getConfiguration();
            mainView.findViewById(R.id.iconWeatherBig).setBackground(new BitmapDrawable(new Resources(manager, metrics, config), bitmap));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setMinimalDescription(int id) {
        WeatherData weatherData = adapter.getItem(id);
        ((TextView) mainView.findViewById(R.id.dateMinTextView)).setText(MINIMAL_DATE_FORMAT.format(new Date(weatherData.getDate())));
        ((TextView) mainView.findViewById(R.id.infoMinTextView)).setText(
                "Temperature from " + weatherData.getTemperatureMin() +
                        "°C to " + weatherData.getTemperatureMax() + "°C. " + "Wind speed is " +
                        weatherData.getWindSpeed() + " m/s. " + "Pressure is " +
                        weatherData.getPressure() + " mb." + (weatherData.getHumidity() == 0 ? "" :
                        " Humidity is " + weatherData.getHumidity() + "%."));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter = null;
        weatherRecycleView.setAdapter(null);
    }

    private boolean checkForInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cityId = getArguments().getInt(CITY_ID_EXTRA);
        cityName = getArguments().getString(CITY_NAME_EXTRA);
        Log.d("WeatherFragment", "I've got " + cityId + " " + cityName);
        setRetainInstance(true);
        if (checkForInternet()) {
            needToDisplayToast = false;
            beginLoading();
        } else {
            Toast.makeText(getActivity(), NO_INTERNET_CONNECTION, Toast.LENGTH_SHORT).show();
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WeatherLoaderService.UPDATED:
                        if (getActivity() != null && getActivity().getLoaderManager() != null) {
                            getActivity().getLoaderManager().restartLoader(75436789, null, WeatherFragment.this);
                            stopLoading();
                        }
                        break;
                    case WeatherLoaderService.ALREADY_UPDATED:
                        if (needToDisplayToast) {
                            Toast.makeText(getActivity(), WEATHER_ALREADY_UPDATED, Toast.LENGTH_SHORT).show();
                        }
                        stopLoading();
                        break;
                    case WeatherLoaderService.UPDATING:
                        ActionBar actionBar = getActivity().getActionBar();
                        if (actionBar != null) {
                            actionBar.setSubtitle(UPDATING_MESSAGE);
                        }
                        break;
                    case WeatherLoaderService.ERROR:
                        stopLoading();
                        Toast.makeText(getActivity(), ERROR_LOADING_CITY, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    public void beginLoading() {
        WeatherLoaderService.loadCity(getActivity(), cityName);
    }

    public void stopLoading() {
        if (getActivity() != null) {
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(null);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_refresh_weather) {
            if (!checkForInternet()) {
                Toast.makeText(getActivity(), NO_INTERNET_CONNECTION, Toast.LENGTH_SHORT).show();
            } else {
                Log.d("WeatherFragment", "Refreshing!");
                needToDisplayToast = true;
                beginLoading();
            }
        } else if (item.getItemId() == R.id.menu_item_alarm_management) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Interval");
            final CharSequence[] intervals = {"Never", "1 hour", "2 hour", "6 hour", "24 hours"};
            final int[] intervalValues = {-1, 3600 * 1000, 3600 * 2 * 1000, 3600 * 6 * 1000, 3600 * 24 * 1000};
            int last = getActivity().getSharedPreferences(WeatherActivity.APP, Context.MODE_PRIVATE).getInt(LAST_SELECTED_ID, 0);
            builder.setSingleChoiceItems(intervals, last, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0)
                        AlarmManagerHelper.disableServiceAlarm(getActivity().getApplicationContext());
                    else {
                        AlarmManagerHelper.disableServiceAlarm(getActivity().getApplicationContext());
                        AlarmManagerHelper.enableServiceAlarm(getActivity().getApplicationContext(), intervalValues[i]);
                    }
                    getActivity().getSharedPreferences(WeatherActivity.APP, Context.MODE_PRIVATE).edit().putInt(LAST_SELECTED_ID, i).apply();
                    intervalDialog.dismiss();
                }
            });
            intervalDialog = builder.create();
            intervalDialog.show();
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_weather, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        mainView = view;
        setHasOptionsMenu(true);
        weatherRecycleView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        if (weatherRecycleView.getTag().equals("horizontal")) {
            weatherRecycleView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        } else {
            weatherRecycleView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        }
        weatherRecycleView.setHasFixedSize(true);
        if (adapter != null) {
            weatherRecycleView.setAdapter(adapter);
        } else {
            getLoaderManager().restartLoader(24562, null, this);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        WeatherLoaderService.setHandler(handler);
    }

    @Override
    public void onPause() {
        super.onPause();

        WeatherLoaderService.setHandler(null);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(null);
        }
    }
}
