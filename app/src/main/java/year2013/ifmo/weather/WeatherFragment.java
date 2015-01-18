package year2013.ifmo.weather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class WeatherFragment extends Fragment {

    public static final String EXTRA_CITY_NAME = "city_name";

    Context context;

    ResponseReceiver responseReceiver;
    IntentFilter mStatusIntentFilter;

    TextView cityName;
    TextView timeDayWeek;
    TextView today;
    TextView temperature;
    ImageView pictureWeather;
    TextView humidity;
    TextView pressure;
    TextView wind;
    TextView description;
    TextView firstDate;
    ImageView firstPic;
    TextView firstTemp;
    TextView secondDate;
    ImageView secondPic;
    TextView secondTemp;
    TextView thirdDate;
    ImageView thirdPic;
    TextView thirdTemp;
    TextView forthDate;
    ImageView forthPic;
    TextView forthTemp;


    public WeatherFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //String CITY_NAME = getActivity().getIntent().getStringExtra(EXTRA_CITY_NAME);
        //if (CITY_NAME!= null && !CITY_NAME.isEmpty()) new CityPreference(getActivity()).setCity(CITY_NAME);

        //String city = new CityPreference(getActivity()).getCity();
        //if (isOnline()) loadForecast(city);

        String CITY_NAME = getArguments().getString(EXTRA_CITY_NAME);

        mStatusIntentFilter = new IntentFilter(
                DownloadingForecast.BROADCAST_ACTION);
        responseReceiver = new ResponseReceiver();

        LocalBroadcastManager.getInstance(context)
                .registerReceiver(responseReceiver, mStatusIntentFilter);

        //Log.d("WeatherFragment", "I'm in onCreate method! ");
        if (isOnline()) loadForecast(CITY_NAME);
        else {
            Toast.makeText(context.getApplicationContext(),
                    "Check Internet connection",
                    Toast.LENGTH_SHORT)
                    .show();
            updateWeatherForecast(CITY_NAME);
        }

        //updateWeatherForecast(city);

        // getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(responseReceiver, mStatusIntentFilter);
        updateWeatherForecast(getArguments().getString(EXTRA_CITY_NAME));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(responseReceiver);
        super.onPause();
    }

    public void changeCity(String city) {
        //Log.d("WeatherFragment", "I'm in changeCity method!");
        if (isOnline()) loadForecast(city);
        else {
            Toast.makeText(context.getApplicationContext(),
                    "Check Internet connection",
                    Toast.LENGTH_SHORT)
                    .show();
            updateWeatherForecast(city);
        }
        //loadForecast(city);

        // updateWeatherForecast(city);
    }

    private void loadForecast(String CITY_NAME) {
        //Log.d("WeatherFragment", "I'm in loadForecast method! " + CITY_NAME);
        /*getActivity().getContentResolver().delete(Forecast.CONTENT_URI,
               "(" + Forecast.CITY_NAME + "=\"" + CITY_NAME + "\")", null);*/
        Intent intent = new Intent(context, DownloadingForecast.class);
        intent.putExtra(DownloadingForecast.EXTRA_CITY_NAME, CITY_NAME);
        context.startService(intent);
    }

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String city = intent.getStringExtra(DownloadingForecast.EXTRA_CITY_NAME);
            updateWeatherForecast(city);
        }
    }

    private boolean isOnline() {
        String cs = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(cs);
        if (cm.getActiveNetworkInfo() == null) {
            return false;
        }
        return cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void updateWeatherForecast(String CITY_NAME) {
        //Log.d("WeatherFragment", "I'm in updateWeatherForecast method! " + CITY_NAME);
        Cursor cursor = context.getContentResolver().query(Forecast.CONTENT_URI,
                null,
                Forecast.CITY_NAME + "=\"" + CITY_NAME + "\"",
                null, null);
        //Log.d("WeatherFragment", "I'm in updateWeatherForecast method! " + CITY_NAME);
        cursor.moveToFirst();
        //Log.d("WeatherFragment", "I'm in updateWeatherForecast method! " +
        //((Integer)cursor.getString(Forecast.CURRENT_FORECAST_COLUMN).length()));
        try {
            JSONObject json;
            //Log.d("WeatherFragment", "Trying to convert in JSONObject " + CITY_NAME);
            String current = cursor.getString(Forecast.CURRENT_FORECAST_COLUMN);
            if (current != null && !current.isEmpty()) {
                json = new JSONObject(current);
                currentWeatherUpdate(json);
            }
            //Log.d("WeatherFragment", "Trying to convert in JSONObject " + CITY_NAME);
            String daily = cursor.getString(Forecast.DAYS_FORECAST_COLUMN);
            if (daily != null && !daily.isEmpty()) {
                json = new JSONObject(daily);
                daysWeatherUpdate(json);
            }
        } catch (JSONException e) {
            //Log.d("WeatherFragment", " Trouble " + CITY_NAME);
            Toast.makeText(context.getApplicationContext(),
                    "Something went wrong",
                    Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    public void daysWeatherUpdate(JSONObject json) {
        try {
            JSONArray list = null;
            list = json.getJSONArray("list");

            JSONObject day = list.getJSONObject(1);
            JSONObject details = day.getJSONArray("weather").getJSONObject(0);
            DateFormat df = new SimpleDateFormat("E, d MMM");
            firstDate.setText(df.format(new Date(day.getLong("dt") * 1000)));
            firstPic.setImageResource(setItem(details.getInt("id"), new Date().getTime(),
                    new Date().getTime() + 100000));
            firstTemp.setText(String.format("%.0f .. %.0f ℃", day.getJSONObject("temp")
                    .getDouble("min"), day.getJSONObject("temp")
                    .getDouble("max")));

            day = list.getJSONObject(2);
            details = day.getJSONArray("weather").getJSONObject(0);
            df = new SimpleDateFormat("E, d MMM");
            secondDate.setText(df.format(new Date(day.getLong("dt") * 1000)));
            secondPic.setImageResource(setItem(details.getInt("id"), new Date().getTime(),
                    new Date().getTime() + 100000));
            secondTemp.setText(String.format("%.0f .. %.0f ℃", day.getJSONObject("temp")
                    .getDouble("min"), day.getJSONObject("temp")
                    .getDouble("max")));

            day = list.getJSONObject(3);
            details = day.getJSONArray("weather").getJSONObject(0);
            df = new SimpleDateFormat("E, d MMM");
            thirdDate.setText(df.format(new Date(day.getLong("dt") * 1000)));
            thirdPic.setImageResource(setItem(details.getInt("id"), new Date().getTime(),
                    new Date().getTime() + 100000));
            thirdTemp.setText(String.format("%.0f .. %.0f ℃", day.getJSONObject("temp")
                    .getDouble("min"), day.getJSONObject("temp")
                    .getDouble("max")));

            day = list.getJSONObject(4);
            details = day.getJSONArray("weather").getJSONObject(0);
            df = new SimpleDateFormat("E, d MMM");
            forthDate.setText(df.format(new Date(day.getLong("dt") * 1000)));
            forthPic.setImageResource(setItem(details.getInt("id"), new Date().getTime(),
                    new Date().getTime() + 100000));
            forthTemp.setText(String.format("%.0f .. %.0f ℃", day.getJSONObject("temp")
                    .getDouble("min"), day.getJSONObject("temp")
                    .getDouble("max")));

        } catch (JSONException e) {
            Toast.makeText(context.getApplicationContext(),
                    "Something went wrong",
                    Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    private void currentWeatherUpdate(JSONObject json) {
        //Log.d("WeatherFragment", "I'm in currentWeatherUpdate method! ");
        try {
            cityName.setText(json.getString("name"));
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            JSONObject windSpeed = json.getJSONObject("wind");

            description.setText(details.getString("description").toUpperCase(Locale.US));
            DateFormat df = new SimpleDateFormat("E, d MMM HH:mm");
            timeDayWeek.setText("Last update:  " + df.format(new Date(json.getLong("dt") * 1000)));
            df = new SimpleDateFormat("E, d MMM");
            today.setText(df.format(new Date(json.getLong("dt") * 1000)));
            temperature.setText(String.format("%.0f", main.getDouble("temp")) + " ℃");
            humidity.setText(main.getString("humidity") + "%");
            pressure.setText(String.format("%.0f mm Hg", main.getDouble("pressure") * 0.75));
            wind.setText(String.format("%.1f m/s", windSpeed.getDouble("speed")));

            pictureWeather.setImageResource(setItem(details.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000));


        } catch (JSONException e) {
            Toast.makeText(context.getApplicationContext(),
                    "Something went wrong",
                    Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    public int setItem(int id, long sunrise, long sunset) {
        long currentTime = new Date().getTime();
        boolean night = true;
        if (currentTime >= sunrise && currentTime < sunset) night = false;
        if (id < 300 || (id >= 520 && id <= 531)) return (R.drawable.dn11);
        else if (id < 500) return (R.drawable.dn09);
        else if (id < 511) {
            if (night) return (R.drawable.n10);
            else return (R.drawable.d10);
        } else if (id == 511 || id < 700) return (R.drawable.dn13);
        else if (id < 800) return (R.drawable.dn50);
        else if (id == 803 || id == 804) return (R.drawable.dn04);
        else if (id == 802) return (R.drawable.dn03);
        else if (id == 801) {
            if (night) return (R.drawable.n02);
            else return (R.drawable.d02);
        } else {
            if (night) return (R.drawable.n01);
            else return (R.drawable.d01);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d("WeatherFragment", "I'm in onCreateView method! ");
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityName = (TextView) rootView.findViewById(R.id.city_name);
        timeDayWeek = (TextView) rootView.findViewById(R.id.time_day_week);
        today = (TextView) rootView.findViewById(R.id.today);
        temperature = (TextView) rootView.findViewById(R.id.temperature);
        humidity = (TextView) rootView.findViewById(R.id.humidity);
        pressure = (TextView) rootView.findViewById(R.id.pressure);
        wind = (TextView) rootView.findViewById(R.id.wind);
        description = (TextView) rootView.findViewById(R.id.description);
        firstDate = (TextView) rootView.findViewById(R.id.first_date);
        firstTemp = (TextView) rootView.findViewById(R.id.first_temp);
        secondDate = (TextView) rootView.findViewById(R.id.second_date);
        secondTemp = (TextView) rootView.findViewById(R.id.second_temp);
        thirdDate = (TextView) rootView.findViewById(R.id.third_date);
        thirdTemp = (TextView) rootView.findViewById(R.id.third_temp);
        forthDate = (TextView) rootView.findViewById(R.id.forth_date);
        forthTemp = (TextView) rootView.findViewById(R.id.forth_temp);
        pictureWeather = (ImageView) rootView.findViewById(R.id.weather_picture);
        firstPic = (ImageView) rootView.findViewById(R.id.first_pic);
        secondPic = (ImageView) rootView.findViewById(R.id.second_pic);
        thirdPic = (ImageView) rootView.findViewById(R.id.third_pic);
        forthPic = (ImageView) rootView.findViewById(R.id.forth_pic);
        return rootView;
    }
}
