package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import ru.ifmo.md.lesson8.provider.WeatherDatabaseHelper;
import ru.ifmo.md.lesson8.provider.WeatherProvider;


/**
 * Created by pva701 on 22.11.14.
 */
public class CommonWeatherFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    //private static String CUR_CITY = "St.Petersburg";
    public static String CITY_ID_EXTRA = "city_id";
    public static String CITY_NAME_EXTRA = "city_name";
    private int cityId;
    private String cityName;

    private ForecastListAdapter adapter;
    private RecyclerView forecastList;
    private LinearLayout describeWeatherLayout;
    private int selectedDay = -1;
    private Handler handler;
    private boolean userRequestUpdate = false;

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), WeatherProvider.FORECAST_CONTENT_URI, null, WeatherDatabaseHelper.FORECAST_CITY_ID + " = " + cityId, null,
                WeatherDatabaseHelper.FORECAST_DATE + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (adapter == null) {
            adapter = new ForecastListAdapter(getActivity());
            adapter.setOnItemClickListener(new ForecastListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    adapter.setCurrentItem(pos);
                    setDescriptionWeather(pos);
                }
            });
            forecastList.setAdapter(adapter);
        }
        if (cursor.isAfterLast())
            return;
        adapter.clear();
        WeatherDatabaseHelper.WeatherCursor wc = new WeatherDatabaseHelper.WeatherCursor(cursor);
        while (cursor.moveToNext())
            adapter.add(wc.getWeather());
        adapter.notifyDataSetChanged();
        setDescriptionWeather(0);
        adapter.setCurrentItem(0);
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor>cursorLoader) {
        adapter = null;
        forecastList.setAdapter(null);
    }

    public int getBackgroundRes(ShortWeatherData weatherData) {
        int code = weatherData.getConditionCode();
        if (code < 600)
            return R.drawable.rain;
        if (code < 700)
            return R.drawable.snow;
        if (code < 800)
            return R.drawable.mist;
        if (code == 800)
            return R.drawable.clear;
        if (code < 900)
            return R.drawable.clouds;
        return R.drawable.clear;
    }

    public String temp(int x) {
        String ret;
        if (x > 0)
            ret = "+" + x + "°";
        else
            ret = x + "°";
        return ret;
    }

    public void setDescriptionWeather(int id) {
        ShortWeatherData weather = adapter.getItem(id);
        selectedDay = id;
        if (describeWeatherLayout.getBackground() == null) {
            int backgroundRes = getBackgroundRes(weather);
            describeWeatherLayout.setBackgroundResource(backgroundRes);
        } else {
            int backgroundRes = getBackgroundRes(weather);
            Drawable from = describeWeatherLayout.getBackground();
            if (getActivity() != null && getActivity().getResources() != null) {
                Drawable to = getActivity().getResources().getDrawable(backgroundRes);
                TransitionDrawable td = new TransitionDrawable(new Drawable[]{from, to});
                describeWeatherLayout.setBackground(td);
                td.startTransition(800);
            }
        }
        ((TextView)describeWeatherLayout.findViewById(R.id.temp)).setText(temp(weather.getTemp()));
        ((TextView)describeWeatherLayout.findViewById(R.id.temp_max)).setText(temp(weather.getTempMax()));
        ((TextView)describeWeatherLayout.findViewById(R.id.temp_min)).setText(temp(weather.getTempMin()));
        if (weather.getWeatherMain().toLowerCase().equals(weather.getWeatherDescription()))
            ((TextView)describeWeatherLayout.findViewById(R.id.describe)).setText(weather.getWeatherMain());
        else {
            char c = weather.getWeatherDescription().charAt(0);
            if ('a' <= c && c <= 'z')
                c -= 32;
            String res = c + weather.getWeatherDescription().substring(1);
            ((TextView) describeWeatherLayout.findViewById(R.id.describe)).setText(res);
        }
        ((TextView)describeWeatherLayout.findViewById(R.id.wind)).setText("Wind: " + weather.getWindSpeed() + " m/s");
        if (weather.getHumidity() != 0)
            ((TextView)describeWeatherLayout.findViewById(R.id.humidity)).setText("Humidity: " + weather.getHumidity() + "%");
        else
            ((TextView)describeWeatherLayout.findViewById(R.id.humidity)).setText("");
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cityId = getArguments().getInt(CITY_ID_EXTRA);
        cityName = getArguments().getString(CITY_NAME_EXTRA);
        setRetainInstance(true);
        if (isOnline())
            startLoading(false);
        else
            Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_SHORT).show();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == NetworkLoaderService.DATABASE_UPDATED && msg.arg1 == cityId) {
                    getActivity().getLoaderManager().restartLoader(0, null, CommonWeatherFragment.this);
                    stopLoading();
                } else if (msg.what == NetworkLoaderService.UPDATING_STARTED && msg.arg1 == cityId)
                    getActivity().getActionBar().setSubtitle(R.string.updating);
                else if (msg.what == NetworkLoaderService.ALREADY_UPDATED && msg.arg1 == cityId) {
                    stopLoading();
                    if (userRequestUpdate)
                        Toast.makeText(getActivity(), "Weather has already been updated", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_common_weather, menu);
    }

    public void startLoading(boolean userRequestUpdate) {
        this.userRequestUpdate = userRequestUpdate;
        NetworkLoaderService.loadCity(getActivity(), cityName);
        //getActivity().getActionBar().setSubtitle(R.string.updating);
    }

    public void stopLoading() {
        getActivity().getActionBar().setSubtitle(null);
    }

    private AlertDialog intervalDialog;
    private static String LAST_SELECTED_ID = CommonWeatherActivity.APP + ".last_selected_item";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_refresh) {
            if (!isOnline())
                Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_SHORT).show();
            else
                startLoading(true);
        } else if (item.getItemId() == R.id.menu_item_cities)
            startActivity(new Intent(getActivity(), CitiesActivity.class));
        else if (item.getItemId() == R.id.menu_item_auto_refresh) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Interval");
            final CharSequence[] intervals = {"Never", "1 hour", "2 hour", "6 hour", "1 day"};
            int last = getActivity().getSharedPreferences(CommonWeatherActivity.APP, Context.MODE_PRIVATE).getInt(LAST_SELECTED_ID, 0);
            builder.setSingleChoiceItems(intervals, last, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0)
                        NetworkLoaderService.setServiceAlarm(getActivity(), false, null);
                    else {
                        int j = 0, num = 0;
                        String s = intervals[i].toString();
                        while (Character.isDigit(s.charAt(j))) {
                            num = num * 10 + s.charAt(j) - '0';
                            ++j;
                        }
                        ++j;
                        int mills = 0;
                        if (s.charAt(j) == 'h') mills = num * 3600;
                        else if (s.charAt(j) == 'd') mills = num * 3600 * 24;
                        NetworkLoaderService .setServiceAlarm(getActivity(), false, null);
                        mills *= 1000;
                        Bundle bundle = new Bundle();
                        bundle.putString(NetworkLoaderService.CITY_NAME, NetworkLoaderService.ALL_CITIES);
                        bundle.putInt(NetworkLoaderService.LOAD_INTERVAL, mills);
                        NetworkLoaderService.setServiceAlarm(getActivity(), true, bundle);
                    }
                    getActivity().getSharedPreferences(CommonWeatherActivity.APP, Context.MODE_PRIVATE).edit().putInt(LAST_SELECTED_ID, i).apply();
                    intervalDialog.dismiss();
                }
            });
            intervalDialog = builder.create();
            intervalDialog.show();
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_weather, container, false);
        setHasOptionsMenu(true);
        describeWeatherLayout = (LinearLayout) view.findViewById(R.id.main);
        forecastList = (RecyclerView)view.findViewById(R.id.forecast_list);
        if (forecastList.getTag().equals("horizontal"))
            forecastList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        else
            forecastList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        forecastList.setItemAnimator(new DefaultItemAnimator());
        forecastList.setHasFixedSize(true);
        if (selectedDay != -1) {
            setDescriptionWeather(selectedDay);
            adapter.setCurrentItem(selectedDay);
        }
        if (adapter != null)
            forecastList.setAdapter(adapter);
        else
            getActivity().getLoaderManager().restartLoader(0, null, this);
        return view;
    }

    @Override
    public void onStart() {
        super.onResume();
        NetworkLoaderService.addHandler(handler);
        if (NetworkLoaderService.isLoading(cityName))
            getActivity().getActionBar().setSubtitle(R.string.updating);
    }

    @Override
    public void onStop() {
        super.onPause();
        NetworkLoaderService.removeHandler(handler);
        getActivity().getActionBar().setSubtitle(null);
    }
}
