package com.alex700.lesson9;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeatherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String CITY_NUMBER = "city_number";
    private static final String CITY_NAME = "city_name";
    private static final String CITY_ID = "city_id";

    private int cityNumber;
    private String cityName;
    private int cityId;
    private Handler handler;
    private WeatherLoaderService service;
    private WeatherAdapter adapter;

    private OnFragmentInteractionListener mListener;

    public static WeatherFragment newInstance(int cityNumber, String cityName, int cityId) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putInt(CITY_NUMBER, cityNumber);
        args.putString(CITY_NAME, cityName);
        args.putInt(CITY_ID, cityId);
        fragment.setArguments(args);
        return fragment;
    }

    public WeatherFragment() {
        // Required empty public constructor
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
        Log.d("on create", "start");
        service = new WeatherLoaderService();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WeatherLoaderService.UPDATED:
                        Log.d("update", "start");
                        Toast.makeText(getActivity(), "updated", Toast.LENGTH_SHORT).show();
                        getLoaderManager().restartLoader(2222, null, WeatherFragment.this);
                        break;
                    case WeatherLoaderService.ALREADY_UPDATED:
                        Toast.makeText(getActivity(), "Already updated", Toast.LENGTH_SHORT).show();
                        getLoaderManager().restartLoader(2222, null, WeatherFragment.this);
                        break;
                    case WeatherLoaderService.UPDATING:
                        break;
                }
            }
        };
        service.setHandler(handler);
        Log.d("on create", "load");
        WeatherLoaderService.loadCity(getActivity(), cityName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        Log.d("FRAGMENTS", "createView " + "hash = " + view.hashCode());
        return view;
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
            Log.d("finish", "OK");
            wd = WeatherDatabaseHelper.WeatherDataCursor.getWeatherData(cursor);
            setCurrentWeather(wd);
            cursor.moveToNext();
        }
        adapter.clear();
        while (!cursor.isAfterLast()) {
            Log.d("finish", "1");
            wd = WeatherDatabaseHelper.WeatherDataCursor.getWeatherData(cursor);
            adapter.add(wd);

            cursor.moveToNext();
        }
        adapter.notifyDataSetChanged();
    }

    public void setCurrentWeather(WeatherData wd) {

        ((TextView) getActivity().findViewById(R.id.temperature_main)).setText(wd.getT() + " °C");
        ((TextView) getActivity().findViewById(R.id.temperature_min_max)).setText(wd.gettMin() + " / " + wd.gettMax() + " °C");
        ((TextView) getActivity().findViewById(R.id.wind_speed)).setText(wd.getWindSpeed() + " m/s");
        ((TextView) getActivity().findViewById(R.id.humidity)).setText(wd.getHumidity() + " %");
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
