package odeen.weatherpredictor.view;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import odeen.weatherpredictor.Location;
import odeen.weatherpredictor.PictureManager;
import odeen.weatherpredictor.R;
import odeen.weatherpredictor.Weather;
import odeen.weatherpredictor.WeatherManager;
import odeen.weatherpredictor.WeatherService;

/**
 * Created by Женя on 28.11.2014.
 */
public class ForecastWeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<Weather>> {
    private static final String TAG = "ForecastWeatherFragment";

    private BroadcastReceiver mOnWeatherFetch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getLoaderManager().restartLoader(0, null, ForecastWeatherFragment.this);
        }
    };
    private ArrayList<Weather> mList;

    public static ForecastWeatherFragment getInstance(String name, int id, int color) {
        ForecastWeatherFragment fragment = new ForecastWeatherFragment();
        Bundle args = new Bundle();
        args.putString(WeatherPagerActivity.EXTRA_CITY, name);
        args.putInt(WeatherPagerActivity.EXTRA_CITY_ID, id);
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
        IntentFilter filter = new IntentFilter(WeatherManager.ForWeatherFetched);
        getActivity().registerReceiver(mOnWeatherFetch, filter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getArguments().getString(WeatherPagerActivity.EXTRA_CITY);
        int id = getArguments().getInt(WeatherPagerActivity.EXTRA_CITY_ID, -1);
        int color = getArguments().getInt(CurrentWeatherActivity.EXTRA_CITY_COLOR, -1);
        mLoc = new Location(id, name, color);
        getLoaderManager().initLoader(0, null, this);
        setRetainInstance(true);
    }

    private SimpleDateFormat format1 = new SimpleDateFormat("dd LLL, EEE");


    private View fillView(Weather a, Weather b) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout v = (LinearLayout) inflater.inflate(R.layout.pair_forecast, null);
        {
            LinearLayout x = (LinearLayout) v.getChildAt(0);
            TextView date = (TextView) x.findViewById(R.id.day_textView);
            ImageView i = (ImageView) x.findViewById(R.id.iconView);
            TextView temp = (TextView) x.findViewById(R.id.temp_textView);
            TextView hum = (TextView) x.findViewById(R.id.hum_textView);
            TextView wind = (TextView) x.findViewById(R.id.wind_textView);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(a.getTime() * 1000);
            date.setText(format1.format(c.getTime()));

            i.setImageBitmap(PictureManager.get(getActivity()).getIcon(a.getIconId()));
            temp.setText(Math.round(a.getTemperature() - 273.15) + "\u2103");
            hum.setText(a.getHumidity() + "%");
            wind.setText(a.getWindSpeed() + " m/s");


        }
        if (b == null) {
            v.removeViewAt(1);
            return v;
        }
        {
            LinearLayout x = (LinearLayout) v.getChildAt(1);
            TextView date = (TextView) x.findViewById(R.id.day_textView);
            ImageView i = (ImageView) x.findViewById(R.id.iconView);
            TextView temp = (TextView) x.findViewById(R.id.temp_textView);
            TextView hum = (TextView) x.findViewById(R.id.hum_textView);
            TextView wind = (TextView) x.findViewById(R.id.wind_textView);
            date.setText(format1.format(new Date(b.getTime() * 1000)));
            i.setImageBitmap(PictureManager.get(getActivity()).getIcon(b.getIconId()));
            temp.setText(Math.round(b.getTemperature() - 273.15) + "\u2103");
            hum.setText(b.getHumidity() + "%");
            wind.setText(b.getWindSpeed() + " m/s");
        }
        return v;
    }

    private View fillViewLand(Weather a, Weather b, Weather c1) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout v = (LinearLayout) inflater.inflate(R.layout.pair_forecast, null);
        {
            LinearLayout x = (LinearLayout) v.getChildAt(0);
            TextView date = (TextView) x.findViewById(R.id.day_textView);
            ImageView i = (ImageView) x.findViewById(R.id.iconView);
            TextView temp = (TextView) x.findViewById(R.id.temp_textView);
            TextView hum = (TextView) x.findViewById(R.id.hum_textView);
            TextView wind = (TextView) x.findViewById(R.id.wind_textView);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(a.getTime() * 1000);
            date.setText(format1.format(c.getTime()));

            i.setImageBitmap(PictureManager.get(getActivity()).getIcon(a.getIconId()));
            temp.setText(Math.round(a.getTemperature() - 273.15) + "\u2103");
            hum.setText(a.getHumidity() + "%");
            wind.setText(a.getWindSpeed() + " m/s");
        }
        if (b == null) {
            Log.d("HUINYA", v.getChildCount()+"");
            v.removeViewAt(2);
            v.removeViewAt(1);
            return v;
        }
        {
            LinearLayout x = (LinearLayout) v.getChildAt(1);
            TextView date = (TextView) x.findViewById(R.id.day_textView);
            ImageView i = (ImageView) x.findViewById(R.id.iconView);
            TextView temp = (TextView) x.findViewById(R.id.temp_textView);
            TextView hum = (TextView) x.findViewById(R.id.hum_textView);
            TextView wind = (TextView) x.findViewById(R.id.wind_textView);
            date.setText(format1.format(new Date(b.getTime() * 1000)));
            i.setImageBitmap(PictureManager.get(getActivity()).getIcon(b.getIconId()));
            temp.setText(Math.round(b.getTemperature() - 273.15) + "\u2103");
            hum.setText(b.getHumidity() + "%");
            wind.setText(b.getWindSpeed() + " m/s");
        }
        if (c1 == null) {
            v.removeViewAt(2);
            return v;
        }
        {
            LinearLayout x = (LinearLayout) v.getChildAt(2);
            TextView date = (TextView) x.findViewById(R.id.day_textView);
            ImageView i = (ImageView) x.findViewById(R.id.iconView);
            TextView temp = (TextView) x.findViewById(R.id.temp_textView);
            TextView hum = (TextView) x.findViewById(R.id.hum_textView);
            TextView wind = (TextView) x.findViewById(R.id.wind_textView);
            date.setText(format1.format(new Date(c1.getTime() * 1000)));
            i.setImageBitmap(PictureManager.get(getActivity()).getIcon(c1.getIconId()));
            temp.setText(Math.round(c1.getTemperature() - 273.15) + "\u2103");
            hum.setText(c1.getHumidity() + "%");
            wind.setText(c1.getWindSpeed() + " m/s");
        }
        return v;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.forecast_weather_fragment, container, false);
        if (mList != null) {
            OnReceiveAsyncTask task = new OnReceiveAsyncTask() {
                @Override
                protected void onPostExecute(LinearLayout linearLayout) {
                    ((ScrollView)mView).removeAllViews();
                    ((ScrollView) mView).addView(linearLayout);
                }
            };
            task.execute(mList);
        }
        mView.setBackgroundColor(mLoc.getColor());
        return mView;
    }


    @Override
    public Loader<ArrayList<Weather>> onCreateLoader(int i, Bundle bundle) {
        return new ForecastWeatherLoader(getActivity(), mLoc);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Weather>> arrayListLoader, ArrayList<Weather> forecasts) {
        OnReceiveAsyncTask task = new OnReceiveAsyncTask() {
            @Override
            protected void onPostExecute(LinearLayout linearLayout) {
                ((ScrollView)mView).removeAllViews();
                ((ScrollView) mView).addView(linearLayout);
            }
        };
        task.execute(forecasts);
        mList = forecasts;
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Weather>> arrayListLoader) {

    }


    private static class ForecastWeatherLoader extends AsyncTaskLoader<ArrayList<Weather>> {
        private Location mLoc;
        public ForecastWeatherLoader(Context context, Location loc) {
            super(context);
            mLoc = loc;
        }
        @Override
        public ArrayList<Weather> loadInBackground() {
            return WeatherManager.getInstance(getContext()).getForecast(mLoc);
        }
        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    private class OnReceiveAsyncTask extends AsyncTask<ArrayList<Weather>, Void, LinearLayout> {

        @Override
        protected LinearLayout doInBackground(ArrayList<Weather>... forecast) {
            ArrayList<Weather> forecasts = forecast[0];
            Collections.reverse(forecasts);
            LinearLayout a = new LinearLayout(getActivity());
            a.setOrientation(LinearLayout.VERTICAL);
            int orientation = getActivity().getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                for (int i = 0; i < forecasts.size(); i += 2) {
                    if (i + 1 == forecasts.size()) {
                        a.addView(fillView(forecasts.get(i), null));
                    } else {
                        a.addView(fillView(forecasts.get(i), forecasts.get(i + 1)));
                    }
                }
            } else {
                for (int i = 0; i < forecasts.size(); i += 3) {
                    if (i + 1 == forecasts.size()) {
                        a.addView(fillViewLand(forecasts.get(i), null, null));
                    } else if (i + 2 == forecasts.size()) {
                        a.addView(fillViewLand(forecasts.get(i), forecasts.get(i + 1), null));
                    } else {
                        a.addView(fillViewLand(forecasts.get(i), forecasts.get(i + 1), forecasts.get(i + 2)));
                    }
                }
            }
            return a;
        }
    }

}
