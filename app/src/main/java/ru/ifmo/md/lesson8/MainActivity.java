package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Weather>, ActionBar.OnNavigationListener {
    public static final String APP_PREFERENCES = "mySettings";
    public static final String APP_PREFERENCES_CURRENT_CITY = "currentCity";
    SharedPreferences settings;
    private static final int WEATHER_LOADER_ID = 0;
    public ArrayList<String> cities = new ArrayList<String>();
    public ArrayList<String> urls = new ArrayList<String>();
    private ActionBar bar;
    private int currentCity;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherTypes = getResources().getStringArray(R.array.weather_types);

        Cursor c = getContentResolver().query(WeatherContentProvider.CITIES_URI, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast() && !c.isBeforeFirst()) {
            Log.d("debug1", "onCreate added city " + c.getString(c.getColumnIndex(DBWeather.CITY_NAME)));
            cities.add(c.getString(c.getColumnIndex(DBWeather.CITY_NAME)));
            urls.add(c.getString(c.getColumnIndex(DBWeather.CITY_URL)));
            c.moveToNext();
        }
        c.close();

        if (cities.size() <= 0) {
            Log.d("debug1", "add default city");
            ContentValues cv = new ContentValues();
            cv.put(DBWeather.CITY_NAME, "Лондон");
            cv.put(DBWeather.CITY_URL, "http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=london&num_of_days=5&tp=24&showlocaltime=yes&format=xml");
            getContentResolver().insert(WeatherContentProvider.CITIES_URI, cv);
            cities.add("Лондон");
            urls.add("http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=london&num_of_days=5&tp=24&showlocaltime=yes&format=xml");
        }

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(WeatherDownloadService.ACTION_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
        displayCities();

        if (!isOnline()) {
            showMessage(getString(R.string.no_internet));
        }

        AlarmManagerHelper.stopAlarm(getApplicationContext());
        AlarmManagerHelper.startAlarm(getApplicationContext(), 3 * 60 * 60 * 1000); //every 3 hours
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(APP_PREFERENCES_CURRENT_CITY, currentCity);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (settings.contains(APP_PREFERENCES_CURRENT_CITY)) {
            currentCity = settings.getInt(APP_PREFERENCES_CURRENT_CITY, 0);
        } else currentCity = 0;
        bar.setSelectedNavigationItem(currentCity);
        display();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public Loader<Weather> onCreateLoader(int i, Bundle bundle) {
        return new WeatherLoader(this, cities.get(currentCity));
    }

    @Override
    public void onLoadFinished(Loader<Weather> weatherLoader, Weather weather) {
        ((TextView)findViewById(R.id.date)).setText(weather.date);
        ((ImageView)findViewById(R.id.image)).setImageResource(getWeatherImage(weather.type));
        ((TextView)findViewById(R.id.tempr)).setText(weather.tempr + "°C");
        ((TextView)findViewById(R.id.type)).setText(getWeatherType(weather.type));
        ((TextView)findViewById(R.id.humidity)).setText(weather.humidity + "%");
        ((TextView)findViewById(R.id.wind)).setText(weather.wind + "км/ч");

        if (weather.weather5Days.size() >= 5) {
            ((TextView) findViewById(R.id.date1)).setText(weather.weather5Days.get(0).date);
            ((ImageView) findViewById(R.id.image1)).setImageResource(getWeatherImage(weather.weather5Days.get(0).type));
            ((TextView) findViewById(R.id.tempr1)).setText(weather.weather5Days.get(0).mi + ".." + weather.weather5Days.get(0).ma);

            ((TextView) findViewById(R.id.date2)).setText(weather.weather5Days.get(1).date);
            ((ImageView) findViewById(R.id.image2)).setImageResource(getWeatherImage(weather.weather5Days.get(1).type));
            ((TextView) findViewById(R.id.tempr2)).setText(weather.weather5Days.get(1).mi + ".." + weather.weather5Days.get(1).ma);

            ((TextView) findViewById(R.id.date3)).setText(weather.weather5Days.get(2).date);
            ((ImageView) findViewById(R.id.image3)).setImageResource(getWeatherImage(weather.weather5Days.get(2).type));
            ((TextView) findViewById(R.id.tempr3)).setText(weather.weather5Days.get(2).mi + ".." + weather.weather5Days.get(2).ma);

            ((TextView) findViewById(R.id.date4)).setText(weather.weather5Days.get(3).date);
            ((ImageView) findViewById(R.id.image4)).setImageResource(getWeatherImage(weather.weather5Days.get(3).type));
            ((TextView) findViewById(R.id.tempr4)).setText(weather.weather5Days.get(3).mi + ".." + weather.weather5Days.get(3).ma);

            ((TextView) findViewById(R.id.date5)).setText(weather.weather5Days.get(4).date);
            ((ImageView) findViewById(R.id.image5)).setImageResource(getWeatherImage(weather.weather5Days.get(4).type));
            ((TextView) findViewById(R.id.tempr5)).setText(weather.weather5Days.get(4).mi + ".." + weather.weather5Days.get(4).ma);
        }
    }

    @Override
    public void onLoaderReset(Loader<Weather> weatherLoader) {
        new WeatherLoader(this, cities.get(currentCity));
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        Log.d("debug1", "NavigationItemSelected, i = " + Integer.toString(i));
        currentCity = i;
        display();

        //check internet connection
        if (isOnline()) {
            startService(new Intent(this, WeatherDownloadService.class).
                            putExtra(WeatherDownloadService.URL_TAG, urls.get(i)).
                            putExtra(WeatherDownloadService.CITY_TAG, cities.get(i))
            );
        }
        return true;
    }

    private void display() {
        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }

    private void displayCities() {
        bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bar.setListNavigationCallbacks(adapter, this);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("debug1", "onRecive!");
            display();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!isOnline()) Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show(); else {
                    startService(new Intent(this, WeatherDownloadService.class).
                                    putExtra(WeatherDownloadService.URL_TAG, urls.get(currentCity)).
                                    putExtra(WeatherDownloadService.CITY_TAG, cities.get(currentCity))
                    );
                }
                return true;
            case R.id.action_add:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final EditText editText = new EditText(this);

                builder.setTitle(R.string.input_city_name)
                        .setView(editText)
                        .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new AddCityTask().execute(editText.getText().toString());
                            }
                        });
                builder.show();
                return true;
            case R.id.action_delete:
                if (cities.size() >= 2) {
                    getContentResolver().delete(WeatherContentProvider.CITIES_URI, DBWeather.CITY_NAME + " = \"" + cities.get(currentCity) + "\"", null);
                    getContentResolver().delete(WeatherContentProvider.WEATHER1_URI, DBWeather.CITY1 + " = \"" + cities.get(currentCity) + "\"", null);
                    getContentResolver().delete(WeatherContentProvider.WEATHER2_URI, DBWeather.CITY2 + " = \"" + cities.get(currentCity) + "\"", null);

                    cities.remove(currentCity);
                    urls.remove(currentCity);
                    if (currentCity > 0) {
                        currentCity--;
                    }
                    displayCities();
                    display();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isAvailable() && netInfo.isConnected();
    }

    private void showMessage(String str) {
        Toast tst = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        tst.setGravity(Gravity.TOP, 0, 0);
        tst.show();
    }

    public class AddCityTask extends AsyncTask<String, Void, Boolean> {
        public AddCityTask() {}

        String name;

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                showMessage("Город " + name + " добавлен");
            } else {
                showMessage("Город " + name + " не найден");
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            name = params[0];
            Log.d("debug1", "addCityTask started for " + name);

            try {
                String translationRU = Translator.translate("ru", name);
                String translationEN = Translator.translate("en", name);
                if (translationRU.length() >= 1) {
                    translationRU = Character.toUpperCase(translationRU.charAt(0)) + translationRU.substring(1);
                }
                if (translationEN.length() >= 1) {
                    translationEN = Character.toUpperCase(translationEN.charAt(0)) + translationEN.substring(1);
                }
                translationEN.replaceAll(" ", "_");
                Log.d("debug1", "translation = " + translationRU + " " + translationEN);
                String url = "http://api.worldweatheronline.com/free/v2/weather.ashx?key=05489d954c3b344b296d7ef09e4b7&q=" + translationEN + "&num_of_days=5&tp=24&showlocaltime=yes&format=xml";

                XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
                InputStream stream = new URL(url).openStream();
                xpp.setInput(stream, null);

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                    if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("error")) {
                        Log.d("debug1", "wrong city name:" + name);
                        return false;
                    }
                    xpp.next();
                }
                getContentResolver().delete(WeatherContentProvider.CITIES_URI, DBWeather.CITY_NAME + " = \"" + translationRU + "\"", null);
                getContentResolver().delete(WeatherContentProvider.WEATHER1_URI, DBWeather.CITY1 + " = \"" + translationRU + "\"", null);
                getContentResolver().delete(WeatherContentProvider.WEATHER2_URI, DBWeather.CITY2 + " = \"" + translationRU + "\"", null);
                ContentValues cv = new ContentValues();
                cv.put(DBWeather.CITY_NAME, translationRU);
                cv.put(DBWeather.CITY_URL, url);
                getContentResolver().insert(WeatherContentProvider.CITIES_URI, cv);
                cities.add(translationRU);
                urls.add(url);
                Log.d("debug1", "addCityTask finished for " + name);
                displayCities();
            } catch (Exception e)  {
                Log.d("debug1", "addCityTask failed for " + name);
                e.printStackTrace();
            }

            return true;
        }
    }

    public void onCurLocationClick(View view) {
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            showMessage("Не удалось получить местоположение");
            return;
        }
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = new ArrayList<Address>();
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            Log.d("debug1", "cur place = " + addresses.get(0).getLocality());
            showMessage("Вы находитесь в " + addresses.get(0).getLocality());
        }
    }

    private String[] weatherTypes;
    private final String[] codes = new String[]{"395", "392", "389", "386", "377", "374", "371", "368", "365", "362", "359", "356",
            "353", "350", "338", "335", "332", "329", "326", "323", "320", "317", "314", "311", "308", "305", "302", "299",
            "296", "293", "284", "281", "266", "263", "260", "248", "230", "227", "200", "185", "182", "179", "176", "143", "122",
            "119", "116", "113"};
    private final int[] images = new int[]{R.drawable.snow_thunder_sun, R.drawable.snow_thunder_sun, R.drawable.rain_thunder,
            R.drawable.rain_thunder, R.drawable.ice, R.drawable.ice_snow,
            R.drawable.snow, R.drawable.snow, R.drawable.rain_snow, R.drawable.rain_snow, R.drawable.heavy_rain, R.drawable.rain,
            R.drawable.rain_sun, R.drawable.ice, R.drawable.heavysnow,
            R.drawable.heavysnow, R.drawable.snow, R.drawable.snow, R.drawable.snow_sun, R.drawable.snow_sun, R.drawable.rain_snow,
            R.drawable.rain_snow, R.drawable.rain_snow, R.drawable.ice_snow, R.drawable.heavy_rain,
            R.drawable.heavy_rain, R.drawable.rain,
            R.drawable.rain, R.drawable.rain, R.drawable.rain, R.drawable.heavysnow,
            R.drawable.heavysnow, R.drawable.rain_sun, R.drawable.rain_sun, R.drawable.foggy, R.drawable.foggy, R.drawable.heavysnow,
            R.drawable.snow, R.drawable.rain_thunder_sun, R.drawable.cold, R.drawable.rain_snow, R.drawable.snow,
            R.drawable.rain, R.drawable.foggy, R.drawable.sunny, R.drawable.overcast, R.drawable.cloudy, R.drawable.sunny};

    public String getWeatherType(String code) {
        for (int i = 0; i < codes.length; i++)
            if (codes[i].equals(code)) {
                return weatherTypes[i];
            }
        return "O_O";
    }

    public int getWeatherImage(String code) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(code)) {
                return images[i];
            }
        }
        return R.drawable.sunny;
    }
}

