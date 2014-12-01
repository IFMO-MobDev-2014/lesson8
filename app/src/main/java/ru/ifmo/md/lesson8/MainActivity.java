package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Weather>, ActionBar.OnNavigationListener {
    private static final int WEATHER_LOADER_ID = 0;
    private static final String[] cities = new String[]{"Санкт-Петербург", "Москва", "Сидней", "Нью-Йорк", "Лондон"};
    private static final String[] urls = new String[]{
            "http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=saint_petersburg&num_of_days=5&tp=24&format=xml",
            "http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=moscow&num_of_days=5&tp=24&format=xml",
            "http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=sydney&num_of_days=5&tp=24&format=xml",
            "http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=new_york&num_of_days=5&tp=24&format=xml",
            "http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=london&num_of_days=5&tp=24&format=xml"
    };
    private ActionBar bar;
    private int currentCity = 0;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(WeatherDownloadService.ACTION_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bar.setListNavigationCallbacks(adapter, this);

        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public Loader<Weather> onCreateLoader(int i, Bundle bundle) {
        return new WeatherLoader(this, cities[currentCity]);
    }

    @Override
    public void onLoadFinished(Loader<Weather> weatherLoader, Weather weather) {
        ((TextView)findViewById(R.id.date)).setText(weather.date);
        ((ImageView)findViewById(R.id.image)).setImageResource(CodeToWeather.getWeatherImage(weather.type));
        ((TextView)findViewById(R.id.tempr)).setText(weather.tempr + "°C");
        ((TextView)findViewById(R.id.type)).setText(CodeToWeather.getWeatherType(weather.type));
        ((TextView)findViewById(R.id.humidity)).setText(weather.humidity + "%");
        ((TextView)findViewById(R.id.wind)).setText(weather.wind + "км/ч");

        if (weather.weather5Days.size() >= 5) {
            ((TextView) findViewById(R.id.date1)).setText(weather.weather5Days.get(0).date);
            ((ImageView) findViewById(R.id.image1)).setImageResource(CodeToWeather.getWeatherImage(weather.weather5Days.get(0).type));
            ((TextView) findViewById(R.id.tempr1)).setText(weather.weather5Days.get(0).mi + ".." + weather.weather5Days.get(0).ma);

            ((TextView) findViewById(R.id.date2)).setText(weather.weather5Days.get(1).date);
            ((ImageView) findViewById(R.id.image2)).setImageResource(CodeToWeather.getWeatherImage(weather.weather5Days.get(1).type));
            ((TextView) findViewById(R.id.tempr2)).setText(weather.weather5Days.get(1).mi + ".." + weather.weather5Days.get(1).ma);

            ((TextView) findViewById(R.id.date3)).setText(weather.weather5Days.get(2).date);
            ((ImageView) findViewById(R.id.image3)).setImageResource(CodeToWeather.getWeatherImage(weather.weather5Days.get(2).type));
            ((TextView) findViewById(R.id.tempr3)).setText(weather.weather5Days.get(2).mi + ".." + weather.weather5Days.get(2).ma);

            ((TextView) findViewById(R.id.date4)).setText(weather.weather5Days.get(3).date);
            ((ImageView) findViewById(R.id.image4)).setImageResource(CodeToWeather.getWeatherImage(weather.weather5Days.get(3).type));
            ((TextView) findViewById(R.id.tempr4)).setText(weather.weather5Days.get(3).mi + ".." + weather.weather5Days.get(3).ma);

            ((TextView) findViewById(R.id.date5)).setText(weather.weather5Days.get(4).date);
            ((ImageView) findViewById(R.id.image5)).setImageResource(CodeToWeather.getWeatherImage(weather.weather5Days.get(4).type));
            ((TextView) findViewById(R.id.tempr5)).setText(weather.weather5Days.get(4).mi + ".." + weather.weather5Days.get(4).ma);
        }
    }

    @Override
    public void onLoaderReset(Loader<Weather> weatherLoader) {
        new WeatherLoader(this, cities[currentCity]);
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        Log.d("debug1", "NavigationItemSelected, i = " + Integer.toString(i));
        currentCity = i;
        display();
        startService(new Intent(this, WeatherDownloadService.class).
                putExtra(WeatherDownloadService.URL_TAG, urls[i]).
                putExtra(WeatherDownloadService.CITY_TAG, cities[i])
        );
        return true;
    }

    void display() {
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("debug1", "onRecive!");
            display();
        }
    }
}
