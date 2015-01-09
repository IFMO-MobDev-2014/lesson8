package com.alex700.AWeather;

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class WeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String MESSAGE_ALREADY_UPDATED = "Already updated";
    private static final String MESSAGE_UPDATED = "Updated";
    private static final String MESSAGE_ERROR = "Error in updating";
    private static final String LAST_SELECTED = "last_selected";
    private static final String CITY_NUMBER = "city_number";
    private static final String CITY_NAME = "city_name";
    private static final String CITY_ID = "city_id";

    private int cityNumber;
    private String cityName;
    private int cityId;
    private Handler handler;
    private WeatherLoaderService service;
    private WeatherAdapter adapter;
    private AlertDialog alarmDialog;

    private OnFragmentInteractionListener mListener;

    public WeatherFragment() {
        // Required empty public constructor
    }

    public static WeatherFragment newInstance(int cityNumber, String cityName, int cityId) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putInt(CITY_NUMBER, cityNumber);
        args.putString(CITY_NAME, cityName);
        args.putInt(CITY_ID, cityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cityNumber = getArguments().getInt(CITY_NUMBER);
            cityName = getArguments().getString(CITY_NAME);
            cityId = getArguments().getInt(CITY_ID);
        }
        ((MainActivity) getActivity()).onSectionAttached(cityNumber);
        ((MainActivity) getActivity()).restoreActionBar();
        setHasOptionsMenu(true);
        service = new WeatherLoaderService();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d("UPDATING", "start");
                switch (msg.what) {
                    case WeatherLoaderService.UPDATED:
                        stopLoading();
                        Toast.makeText(getActivity(), MESSAGE_UPDATED, Toast.LENGTH_SHORT).show();
                        getLoaderManager().restartLoader(2222, null, WeatherFragment.this);
                        break;
                    case WeatherLoaderService.ALREADY_UPDATED:
                        Toast.makeText(getActivity(), MESSAGE_ALREADY_UPDATED, Toast.LENGTH_SHORT).show();
                        getLoaderManager().restartLoader(2222, null, WeatherFragment.this);
                        break;
                    case WeatherLoaderService.UPDATING:
                        startLoading();
                        break;
                    case WeatherLoaderService.ERROR:
                        stopLoading();
                        Toast.makeText(getActivity(), MESSAGE_ERROR, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        service.setHandler(handler);
        getLoaderManager().restartLoader(2221, null, this);
        if (checkInternet()) {
            WeatherLoaderService.loadCity(getActivity(), cityName);
        } else {
            Toast.makeText(getActivity(), "no internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLoading() {
        if (getActivity() != null) {
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(null);
            }
        }
    }

    private void startLoading() {
        if (getActivity() != null) {
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle("Updating...");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Log.d("FRAGMENT", "refresh");
            if (checkInternet()) {
                WeatherLoaderService.loadCity(getActivity(), cityName);
            } else {
                Toast.makeText(getActivity(), "no internet", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.action_alarm) {
            Log.d("ALARM_SETTINGS", "start");

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose update interval");
            final CharSequence[] items = new CharSequence[]{"Never", "1 hour", "2 hour", "6 hour", "24 hour"};
            final int[] intervals = new int[] {-1, 1, 2, 6, 24};
            int lastChoice = getActivity().getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE).getInt(LAST_SELECTED, 0);
            builder.setSingleChoiceItems(items, lastChoice, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlarmManagerHelper.disableServiceAlarm(getActivity());
                    if (which != 0) {
                        AlarmManagerHelper.enableServiceAlarm(getActivity(), intervals[which] * 3600 * 1000);
                    }
                    getActivity().getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE).edit().putInt(LAST_SELECTED, which).apply();
                    alarmDialog.dismiss();
                }
            });
            alarmDialog = builder.create();
            alarmDialog.show();
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d("MENU", "create");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.weather_fragment_menu, menu);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    //********CHECK-INTERNET*********

    private boolean checkInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    //*******************************

    //************LOADER*************
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), WeatherContentProvider.WEATHER_CONTENT_URI, null,
                WeatherDatabaseHelper.WEATHER_CITY_ID + "=" + cityId, null, WeatherDatabaseHelper.WEATHER_DATE + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (adapter == null) {
            adapter = new WeatherAdapter(getActivity().getAssets());
            ((ListView) getActivity().findViewById(R.id.small_weather_list)).setAdapter(adapter);
        }
        WeatherData wd;
        cursor.moveToNext();
        if (!cursor.isAfterLast()) {
            wd = WeatherDatabaseHelper.WeatherDataCursor.getWeatherData(cursor);
            setCurrentWeather(wd);
            cursor.moveToNext();
        }
        adapter.clear();
        while (!cursor.isAfterLast()) {
            wd = WeatherDatabaseHelper.WeatherDataCursor.getWeatherData(cursor);
            adapter.add(wd);

            cursor.moveToNext();
        }
        adapter.notifyDataSetChanged();
    }

    String dateToString(Calendar c) {
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String month = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
        String dayOfWeek = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
        return day + " of " + month + ", " + dayOfWeek;
    }

    public void setCurrentWeather(WeatherData wd) {
        Calendar c = new GregorianCalendar();
        c.setTimeZone(TimeZone.getDefault());
        c.setTimeInMillis(wd.getDate());
        ((TextView) getActivity().findViewById(R.id.date_main)).setText(dateToString(c));
        ((TextView) getActivity().findViewById(R.id.temperature_main)).setText(wd.getTString());
        ((TextView) getActivity().findViewById(R.id.temperature_min_max)).setText(wd.gettMin() + " / " + wd.gettMax() + " °C");
        ((TextView) getActivity().findViewById(R.id.wind_speed)).setText(wd.getWindSpeed() + " m/s");
        ((TextView) getActivity().findViewById(R.id.humidity)).setText(wd.getHumidity() == 0 ? "-" : wd.getHumidity() + " %");
        ((TextView) getActivity().findViewById(R.id.pressure)).setText(wd.getPressure() + " mb");
        AssetManager manager = getActivity().getAssets();
        Bitmap icon = null;
        try {
            icon = BitmapFactory.decodeStream(manager.open(wd.getWeatherInfo().getIconName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((ImageView) getActivity().findViewById(R.id.imageView)).setImageBitmap(icon);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
    //************LOADER*************


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
