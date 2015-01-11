package ru.ifmo.md.lesson8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PlaceholderFragment extends Fragment {
    Typeface weatherFont;

    Button upd;
    TextView date;
    TextView sign;
    TextView temperature;
    TextView humidity;
    TextView pressure;
    TextView wind;
    TextView mood;
    LinearLayout futureList;

    Handler handler = new Handler();

    private static final String ARG_SECTION_CITY = "section_city";

    public static PlaceholderFragment newInstance(String city) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_CITY, city);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

//        Log.i("", "PLACEHOLDER ON_CREATE");
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");

        date = (TextView) rootView.findViewById(R.id.weather_date);
        sign = (TextView) rootView.findViewById(R.id.weather_sign);
        sign.setTypeface(weatherFont);
        temperature = (TextView) rootView.findViewById(R.id.weather_temp);
        humidity = (TextView) rootView.findViewById(R.id.weather_humidity);
        pressure = (TextView) rootView.findViewById(R.id.weather_pressure);
        wind = (TextView) rootView.findViewById(R.id.weather_wind);
        mood = (TextView) rootView.findViewById(R.id.weather_mood);

        futureList = (LinearLayout) rootView.findViewById(R.id.weather_future);

        upd = (Button) rootView.findViewById(R.id.update_button);
        upd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWeatherData(getArguments().getString(ARG_SECTION_CITY));

                showToast(getString(R.string.please_wait));
            }
        });

        Cursor cursor = getActivity().getContentResolver().query(
                Weather.JustWeather.CONTENT_URI,
                null,
                Weather.JustWeather.CITY_NAME + "=\""
                        + getArguments().getString(ARG_SECTION_CITY) + "\"",
                null, null);
        cursor.moveToLast();
        try {
            JSONObject json = new JSONObject(cursor.getString(2));
            renderToday(json);
            json = new JSONObject(cursor.getString(3));
            renderForecast(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter mStatusIntentFilter = new IntentFilter(
                WeatherIntentService.BROADCAST_ACTION);
        ResponseReceiver responseReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(responseReceiver, mStatusIntentFilter);

        updateWeatherData(getArguments().getString(ARG_SECTION_CITY));

        return rootView;
    }

    private void updateWeatherData(final String city) {
//        Log.i("", "PLACEHOLDER UPDATE_WEATHER_DATA");
        Intent intent = new Intent(getActivity(), WeatherIntentService.class);
        intent.putExtra(WeatherIntentService.EXTRA_CITY, city);
        getActivity().startService(intent);
    }

    private void renderToday(JSONObject json) {
//        Log.i("", "PLACEHOLDER RENDER_WEATHER");
        try {
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            JSONObject jwind = json.getJSONObject("wind");

            DateFormat df = new SimpleDateFormat("E, d MMM HH:mm");
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));

            date.setText(updatedOn);
            temperature.setText(String.format("%.0f", main.getDouble("temp")) + " ℃");
            humidity.setText(main.getString("humidity") + "%");
            pressure.setText(main.getString("pressure") + " hPa");
            wind.setText(String.format("%.1f m/s  %.0f˚",
                    jwind.getDouble("speed"), jwind.getDouble("deg")));
            mood.setText(details.getString("description").toUpperCase(Locale.US));

            sign.setText(getWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000));
        } catch (Exception e) {
            //showToast(getString(R.string.error_message));
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void renderForecast(JSONObject json) {
        try {
            JSONArray list = json.getJSONArray("list");
            int l = list.length();

            futureList.removeAllViews();
            for (int i = 0; i < l; i++) {
                JSONObject day = list.getJSONObject(i);

                DateFormat df = new SimpleDateFormat("E, d MMM");
                String dayDate = df.format(new Date(day.getLong("dt") * 1000));

                JSONObject details = day.getJSONArray("weather").getJSONObject(0);
                String daySign = getWeatherIcon(details.getInt("id"),
                        new Date().getTime(),
                        new Date().getTime() + 100000);

                String dayTemp = String.format("%.0f", day.getJSONObject("temp")
                        .getDouble("day")) + " ℃";

                LinearLayout item = (LinearLayout) LayoutInflater.from(getActivity())
                        .inflate(R.layout.future_weather_item, null);
                ((TextView) item.findViewById(R.id.item_date)).setText(dayDate);
                ((TextView) item.findViewById(R.id.item_sign)).setTypeface(weatherFont);
                ((TextView) item.findViewById(R.id.item_sign)).setText(daySign);
                ((TextView) item.findViewById(R.id.item_temp)).setText(dayTemp);
                futureList.addView(item);
            }
        } catch (Exception e) {
            //showToast(getActivity().getString(R.string.error_message));
            e.printStackTrace();
        }
    }

    private String getWeatherIcon(int actualId, long sunrise, long sunset) {
//        Log.i("", "PLACEHOLDER SET_ICON");
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }



    private class ResponseReceiver extends BroadcastReceiver
    {
        private ResponseReceiver() {
        }

        public boolean todayCalled = false;
        public boolean forecastCalled = false;
        public void onReceive(Context context, Intent intent) {
            if (!todayCalled || !forecastCalled) {
//                Log.i("RESPONSE", " - OK");
                try {
                    String today = intent.getStringExtra(WeatherIntentService.EXTRA_TODAY);
                    if (today != null && !today.isEmpty() && !todayCalled) {
                        renderToday(new JSONObject(today));
//                        todayCalled = true;
                        //showToast(getString(R.string.today_success));
                    }
                    String forecast = intent.getStringExtra(WeatherIntentService.EXTRA_FORECAST);
                    if (forecast != null && !forecast.isEmpty() && !forecastCalled) {
                        renderForecast(new JSONObject(forecast));
//                        forecastCalled = true;
                        //showToast(getString(R.string.forecast_success));
                    }
                } catch (JSONException e) {
                    //showToast(getString(R.string.error_message));
                    e.printStackTrace();
                }
            }
        }
    }

    private void showToast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        text,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
