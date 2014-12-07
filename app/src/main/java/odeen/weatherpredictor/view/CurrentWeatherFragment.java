package odeen.weatherpredictor.view;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import odeen.weatherpredictor.Location;
import odeen.weatherpredictor.PictureManager;
import odeen.weatherpredictor.R;
import odeen.weatherpredictor.Weather;
import odeen.weatherpredictor.WeatherManager;
import odeen.weatherpredictor.WeatherService;

/**
 * Created by Женя on 28.11.2014.
 */
public class CurrentWeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Weather> {
    private static final String TAG = "CurrentWeatherFragment";

    private BroadcastReceiver mOnWeatherFetch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getLoaderManager().restartLoader(0, null, CurrentWeatherFragment.this);
        }
    };
    private Weather mWeather;

    public static CurrentWeatherFragment getInstance(String name, int id, int color) {
        CurrentWeatherFragment fragment = new CurrentWeatherFragment();
        Bundle args = new Bundle();
        args.putString(CurrentWeatherActivity.EXTRA_CITY, name);
        args.putInt(CurrentWeatherActivity.EXTRA_CITY_ID, id);
        args.putInt(CurrentWeatherActivity.EXTRA_CITY_COLOR, color);
        fragment.setArguments(args);
        return fragment;
    }

    private Location mLoc;
    private View mView;


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mOnWeatherFetch);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(WeatherManager.CurWeatherFetched);
        getActivity().registerReceiver(mOnWeatherFetch, filter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getArguments().getString(CurrentWeatherActivity.EXTRA_CITY);
        int id = getArguments().getInt(CurrentWeatherActivity.EXTRA_CITY_ID, -1);
        int color = getArguments().getInt(CurrentWeatherActivity.EXTRA_CITY_COLOR, -1);
        mLoc = new Location(id, name, color);
        getLoaderManager().initLoader(0, null, this);
        setRetainInstance(true);
    }

    private void fillView(Weather weather) {
        if (weather == null)
            return;
        ImageView v = (ImageView) mView.findViewById(R.id.imageView);
        v.setImageBitmap(PictureManager.get(getActivity()).getIcon(weather.getIconId()));
        TextView t = (TextView) mView.findViewById(R.id.temp_textView);
        t.setText(Math.round(weather.getTemperature() - 273.15) + "\u2103");
        TextView twh = (TextView) mView.findViewById(R.id.hum_textView);
        twh.setText("Humidity " + weather.getHumidity() + "%");
        TextView twp = (TextView) mView.findViewById(R.id.pres_textView);
        twp.setText("Pressure " + weather.getPressure() + " hPa");
        TextView tww = (TextView) mView.findViewById(R.id.wind_textView);
        tww.setText("Wind " + weather.getWindSpeed() + " m/s");
        TextView twd = (TextView) mView.findViewById(R.id.descr_textView);
        String description = Character.toUpperCase(weather.getDescription().charAt(0)) + weather.getDescription().substring(1);
        twd.setText(description);
        //TextView twlu = (TextView) mView.findViewById(R.id.last_updated_textView);
        //twlu.setText(format.format(new Date(weather.getTime() * 1000)));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.current_weather_fragment, container, false);
        TextView tv = (TextView)mView.findViewById(R.id.city_textView);
        tv.setText(mLoc.getCity());
        fillView(mWeather);
        mView.setBackgroundColor(mLoc.getColor());
        return mView;
    }

    @Override
    public Loader<Weather> onCreateLoader(int i, Bundle bundle) {
        return new CurrentWeatherLoader(getActivity().getApplicationContext(), mLoc);
    }

    private SimpleDateFormat format = new SimpleDateFormat("dd LLL HH:mm");
    @Override
    public void onLoadFinished(Loader<Weather> weatherLoader, Weather weather) {
        mWeather = weather;
        fillView(weather);
    }

    @Override
    public void onLoaderReset(Loader<Weather> weatherLoader) {

    }





    private static class CurrentWeatherLoader extends AsyncTaskLoader<Weather> {
        private Location mLoc;
        public CurrentWeatherLoader(Context context, Location loc) {
            super(context);
            mLoc = loc;
        }
        @Override
        public Weather loadInBackground() {
            return WeatherManager.getInstance(getContext()).getCurrentWeather(mLoc);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }



}
