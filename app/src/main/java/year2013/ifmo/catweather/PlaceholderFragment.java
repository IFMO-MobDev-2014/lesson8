package year2013.ifmo.catweather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PlaceholderFragment extends Fragment {
    private Typeface weatherFont;

    private TextView todayDate;
    private TextView todayWeatherPic;
    private TextView todayTemp;
    private TextView todayHumidity;
    private TextView todayPressure;
    private TextView todayWind;
    private TextView todayMood;
    private LinearLayout futureList;

    private Context context;

    private ResponseReceiver responseReceiver;
    private IntentFilter mStatusIntentFilter;

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

        weatherFont = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.fonts));

        todayDate = (TextView) rootView.findViewById(R.id.weather_date);
        todayWeatherPic = (TextView) rootView.findViewById(R.id.weather_sign);
        todayWeatherPic.setTypeface(weatherFont);
        todayTemp = (TextView) rootView.findViewById(R.id.weather_temp);
        todayHumidity = (TextView) rootView.findViewById(R.id.weather_humidity);
        todayPressure = (TextView) rootView.findViewById(R.id.weather_pressure);
        todayWind = (TextView) rootView.findViewById(R.id.weather_wind);
        todayMood = (TextView) rootView.findViewById(R.id.weather_mood);

        futureList = (LinearLayout) rootView.findViewById(R.id.weather_future);

        Button upd = (Button) rootView.findViewById(R.id.update_button);
        upd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWeatherData(getArguments().getString(ARG_SECTION_CITY));

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
        long lastUpd = 0;
        try {
            DateFormat df = new SimpleDateFormat(context.getString(R.string.date_format));
            df.setTimeZone(TimeZone.getDefault());
            Date d = df.parse(todayDate.getText().toString());
            d.setYear(new Date().getYear());
            lastUpd = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (new Date().getTime() - lastUpd > 3L * 60 * 60 * 1000) { // if 3 hrs since last upd
            updateWeatherData(getArguments().getString(ARG_SECTION_CITY));
        }
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(responseReceiver);
        super.onPause();
    }

    private boolean isOnline() {
        String cs = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(cs);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void updateWeatherData(final String city) {
        if (isOnline()) {
            showToast(getString(R.string.please_wait));
            Intent intent = new Intent(context, WeatherIntentService.class);
            intent.setAction(WeatherIntentService.ACTION_WEATHER);
            intent.putExtra(WeatherIntentService.EXTRA_CITY, city);
            context.startService(intent);
        } else {
            showToast(getString(R.string.check_internet));
        }
    }

    private void renderToday(JSONObject json) {
        try {
            JSONObject details = json.getJSONArray(context.getString(R.string.weather)).getJSONObject(0);
            JSONObject main = json.getJSONObject(context.getString(R.string.main));
            JSONObject jwind = json.getJSONObject(context.getString(R.string.wind));

            DateFormat df = new SimpleDateFormat(context.getString(R.string.date_format));
            df.setTimeZone(TimeZone.getDefault());
            String updatedOn = df.format(new Date(json.getLong(context.getString(R.string.dt)) * 1000));

            todayDate.setText(updatedOn);
            todayTemp.setText(String.format(context.getString(R.string.temp_format), main.getDouble(context.getString(R.string.temp))));
            todayHumidity.setText(main.getString(context.getString(R.string.humidity)) + "%");
            todayPressure.setText((main.getInt(context.getString(R.string.pressure)) * 0.75) + context.getString(R.string.pressure_format));
            todayMood.setText(details.getString(context.getString(R.string.description)).toUpperCase(Locale.US));

            todayWeatherPic.setText(getWeatherIcon(details.getInt(context.getString(R.string.id)),
                    json.getJSONObject(context.getString(R.string.sys)).getLong(context.getString(R.string.sunrise)) * 1000,
                    json.getJSONObject(context.getString(R.string.sys)).getLong(context.getString(R.string.sunset)) * 1000));

            todayWind.setText(String.format(context.getString(R.string.wind_format),
                    jwind.getDouble(context.getString(R.string.speed))));
        } catch (Exception e) {
            showToast(context.getString(R.string.error_message));
            e.printStackTrace();
        }
    }

    private void renderForecast(JSONObject json) {
        try {
            JSONArray list = json.getJSONArray(context.getString(R.string.list));

            futureList.removeAllViews();
            for (int i = 0; i < 5; i++) {
                JSONObject day = list.getJSONObject(i);

                DateFormat df = new SimpleDateFormat(context.getString(R.string.date_format2));
                String dayDate = df.format(new Date(day.getLong(context.getString(R.string.dt)) * 1000));

                JSONObject details = day.getJSONArray(context.getString(R.string.weather)).getJSONObject(0);
                String daySign = getWeatherIcon(details.getInt(context.getString(R.string.id)),
                        new Date().getTime(),
                        new Date().getTime() + 100000);

                String dayTemp = String.format(context.getString(R.string.temp_format), day.getJSONObject(context.getString(R.string.temp)).getDouble(context.getString(R.string.day)));

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
                case 5:
                    icon = context.getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = context.getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = context.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = context.getString(R.string.weather_cloudy);
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
                }
                String forecast = intent.getStringExtra(WeatherIntentService.EXTRA_FORECAST);
                if (forecast != null && !forecast.isEmpty()) {
                    renderForecast(new JSONObject(forecast));
                }
                showToast(String.format(context.getString(R.string.weather_success),
                        intent.getStringExtra(WeatherIntentService.EXTRA_CITY)));
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
