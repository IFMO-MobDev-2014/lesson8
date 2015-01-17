package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

    Context context;

    ResponseReceiver responseReceiver;
    IntentFilter mStatusIntentFilter;

    private static final String ARG_SECTION_CITY = "section_city";

    public static PlaceholderFragment newInstance(String city) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_CITY, city);
        fragment.setArguments(args);
        return fragment;
    }

    public PlaceholderFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");

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

        Cursor cursor = context.getContentResolver().query(
                Weather.JustWeather.CONTENT_URI,
                null,
                Weather.JustWeather.CITY_NAME + "=\""
                        + getArguments().getString(ARG_SECTION_CITY) + "\"",
                null, null);
        cursor.moveToLast();

        try {
            JSONObject json = new JSONObject(cursor.getString(Weather.JustWeather.TODAY_COLUMN));
            renderToday(json);
            json = new JSONObject(cursor.getString(Weather.JustWeather.FUTURE_COLUMN));
            renderForecast(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();

        mStatusIntentFilter = new IntentFilter(
                WeatherIntentService.BROADCAST_ACTION);
        responseReceiver = new ResponseReceiver();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(responseReceiver, mStatusIntentFilter);
        updateWeatherData(getArguments().getString(ARG_SECTION_CITY));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(responseReceiver);
        super.onPause();
    }

    private void updateWeatherData(final String city) {
        Intent intent = new Intent(context, WeatherIntentService.class);
        intent.putExtra(WeatherIntentService.EXTRA_CITY, city);
        context.startService(intent);
    }

    private void renderToday(JSONObject json) {
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
            showToast(context.getString(R.string.error_message));
            e.printStackTrace();
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

                LinearLayout item = (LinearLayout) LayoutInflater.from(context)
                        .inflate(R.layout.future_weather_item, null);
                ((TextView) item.findViewById(R.id.item_date)).setText(dayDate);
                ((TextView) item.findViewById(R.id.item_sign)).setTypeface(weatherFont);
                ((TextView) item.findViewById(R.id.item_sign)).setText(daySign);
                ((TextView) item.findViewById(R.id.item_temp)).setText(dayTemp);
                futureList.addView(item);
            }
        } catch (Exception e) {
            showToast(context.getString(R.string.error_message));
            e.printStackTrace();
        }
    }

    private String getWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = context.getString(R.string.weather_sunny);
            } else {
                icon = context.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = context.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = context.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = context.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = context.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = context.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = context.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {}

        public void onReceive(Context context, Intent intent) {
            try {
                String today = intent.getStringExtra(WeatherIntentService.EXTRA_TODAY);
                if (today != null && !today.isEmpty()) {
                    renderToday(new JSONObject(today));
                    showToast(context.getString(R.string.today_success));
                }
                String forecast = intent.getStringExtra(WeatherIntentService.EXTRA_FORECAST);
                if (forecast != null && !forecast.isEmpty()) {
                    renderForecast(new JSONObject(forecast));
                    showToast(context.getString(R.string.forecast_success));
                }
            } catch (JSONException e) {
                showToast(context.getString(R.string.error_message));
                e.printStackTrace();
            }
        }
    }

    private void showToast(final String text) {
        Toast.makeText(context,
                text,
                Toast.LENGTH_SHORT)
                .show();
    }
}
