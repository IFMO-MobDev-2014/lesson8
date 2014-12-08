package com.example.lesson8;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class WeatherActivity extends Activity {
    public static String currentQueryCity;
    private MyBroadcast broadcast;
    private IntentFilter filter;
    private ImageButton imageButton;
    private boolean finish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentQueryCity = intent.getStringExtra("city");
        WeatherDB weatherDb = new WeatherDB(this);
        weatherDb.open();
        Intent serviceIntent = new Intent(this, WeatherDownload.class);
        serviceIntent.putExtra("city", currentQueryCity);
        startService(serviceIntent);
        /*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)

        else
            setContentView(R.l);*/
        setContentView(R.layout.weather_activity);
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(currentQueryCity);
        broadcast = new MyBroadcast();
        filter = new IntentFilter(WeatherDownload.DEFAULT_KEY);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcast, filter);
        imageButton = (ImageButton) findViewById(R.id.refreshButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextIntent = new Intent(v.getContext(), WeatherDownload.class);
                nextIntent.putExtra("city", currentQueryCity);
                nextIntent.addCategory(Intent.CATEGORY_DEFAULT);
                startService(nextIntent);
                imageButton.setEnabled(false);
                finish = true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


                /*Intent nextIntent = new Intent(this, WeatherDownload.class);
                nextIntent.putExtra("city", currentQueryCity);
                nextIntent.addCategory(Intent.CATEGORY_DEFAULT);
                startService(nextIntent);
                imageButton.setEnabled(false);
                finish = true;
                return true;*/
        finish();
        return true;



    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Forecast forecast = (Forecast) intent.getSerializableExtra("forecast");
            if (forecast != null) {
                WeatherDB weatherDB = new WeatherDB(context);
                weatherDB.open();
                weatherDB.updateCity(forecast.getCity(), forecast);
                update(forecast);
            }
        }
    }

    private void update(Forecast forecast) {
        if (finish) {
            finish = false;
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.success_update), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            imageButton.setEnabled(true);
            toast.show();
        }
        TextView temperature = (TextView) findViewById(R.id.temperature);
        TextView weather = (TextView) findViewById(R.id.weather);
        ImageView imageView = (ImageView) findViewById(R.id.imageWeather);
        imageView.setImageResource(getImageId(forecast.getWeather()));
        temperature.setText(forecast.getTemp() + "°С");
        weather.setText(forecast.getWeather());

        TextView textView = (TextView) findViewById(R.id.date1);

        ArrayList<DayForecast> nextWeather = forecast.getCities();

        DayForecast current = nextWeather.get(0);
        TextView dateView0 = (TextView) findViewById(R.id.date1);
        TextView temperatureView0 = (TextView) findViewById(R.id.temperature1);
        //ImageView imageView0 = (ImageView) findViewById(R.id.imageWeather1);

        dateView0.setText(current.getDate());
        temperatureView0.setText(current.getTemp() + "°С");
       // imageView0.setImageResource(getImageId(current.getWeather()));

        current = nextWeather.get(1);
        TextView dateView1 = (TextView) findViewById(R.id.date2);
        TextView temperatureView1 = (TextView) findViewById(R.id.temperature2);
       // ImageView imageView1 = (ImageView) findViewById(R.id.imageWeather2);

        dateView1.setText(current.getDate());
        temperatureView1.setText(current.getTemp() + "°С");
        //imageView1.setImageResource(getImageId(current.getWeather()));


        current = nextWeather.get(2);
        TextView dateView2 = (TextView) findViewById(R.id.date3);
        TextView temperatureView2 = (TextView) findViewById(R.id.temperature3);
        //ImageView imageView2 = (ImageView) findViewById(R.id.imageWeather3);

        dateView2.setText(current.getDate());
        temperatureView2.setText(current.getTemp() + "°С");
        //imageView2.setImageResource(getImageId(current.getWeather()));


    }

    public int getImageId(String weather) {
        weather = weather.toLowerCase();
        if (weather.contains("thunder"))
            return R.drawable.thunder;
        if (weather.contains("sunny"))
            return R.drawable.sunny;
        if (weather.contains("clear"))
            return R.drawable.sunny;
        if (weather.contains("cloudy"))
            return R.drawable.cloudy;
        if (weather.contains("overcast"))
            return R.drawable.overcast;
        if (weather.contains("mist") || weather.contains("fog"))
            return R.drawable.mist;
        if (weather.contains("snow"))
            return R.drawable.snow_cloud;
        if (weather.contains("drizzle"))
            return R.drawable.cloud_rain;
        if (weather.contains("rain"))
            return R.drawable.rain;
        return R.drawable.overcast;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onPause() {
        unregisterReceiver(broadcast);
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(broadcast, filter);
        super.onResume();
    }


}
