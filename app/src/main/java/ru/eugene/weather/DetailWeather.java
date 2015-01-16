package ru.eugene.weather;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ru.eugene.weather.ContentProviders.WeatherProvider;
import ru.eugene.weather.database.CityDataSource;
import ru.eugene.weather.database.WeatherInfoDataSource;
import ru.eugene.weather.database.WeatherItem;
import ru.eugene.weather.downloadWeather.ServiceDownload;

public class DetailWeather extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private String link;
    private int idCity;
    private TextView tempView;
    private Context context;
    private ImageView mainWeather;
    private ProgressDialog progress;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object result = intent.getSerializableExtra(ServiceDownload.RESULT);
            if (result != null) {
                ArrayList<WeatherItem> weatherItems = (ArrayList<WeatherItem>) result;
                try {
                    updInterface(weatherItems);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ContentValues values[] = new ContentValues[weatherItems.size()];
                int i = 0;
                for (WeatherItem item : weatherItems) {
                    item.setIdCity(idCity);
                    values[i++] = WeatherProvider.generateContentValuesFromWeatherItem(item);
                }
                getContentResolver().delete(WeatherProvider.CONTENT_URI_WEATHER_INFO,
                        WeatherInfoDataSource.COLUMN_ID_CITY + "=?", new String[] {"" + idCity});
                getContentResolver().bulkInsert(WeatherProvider.CONTENT_URI_WEATHER_INFO, values);
            } else {
                Toast.makeText(context, "something go wrong", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_weather);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar));
        link = getIntent().getStringExtra(CityDataSource.COLUMN_LINK);
        idCity = getIntent().getIntExtra(CityDataSource.COLUMN_ID, -1);
        tempView = (TextView) findViewById(R.id.temp);
        mainWeather = (ImageView) findViewById(R.id.mainWeather);
        context = this;
        progress = new ProgressDialog(this);
        progress.setMessage("Please wait");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail_weather, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = WeatherInfoDataSource.getProjection();
        String selection = WeatherInfoDataSource.COLUMN_ID_CITY + "=" + idCity;
        return new CursorLoader(this, WeatherProvider.CONTENT_URI_WEATHER_INFO, projection,
                selection, null, WeatherInfoDataSource.COLUMN_ID);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("LOG", "loadFinish Detail");
        if (data == null || data.getCount() == 0) {
            Log.i("LOG", "data null");
            updData();
        } else {
            try {
                updInterface(cursorToItems(data));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    ArrayList<WeatherItem> cursorToItems(Cursor data) {
        ArrayList<WeatherItem> weatherItems = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                int id = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_ID));
                int id_city = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_ID_CITY));
                int temp_cur = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_TEMP_CUR));
                int temp_min = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_TEMP_MIN));
                int temp_max = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_TEMP_MAX));
                double speed = data.getDouble(data.getColumnIndex(WeatherInfoDataSource.COLUMN_SPEED));
                int humidity = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_HUMIDITY));
                double visibility = data.getDouble(data.getColumnIndex(WeatherInfoDataSource.COLUMN_VISIBILITY));
                int code = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_CODE));
                String pub_date = data.getString(data.getColumnIndex(WeatherInfoDataSource.COLUMN_PUB_DATE));
                String text = data.getString(data.getColumnIndex(WeatherInfoDataSource.COLUMN_TEXT));
                int chill = data.getInt(data.getColumnIndex(WeatherInfoDataSource.COLUMN_CHILL));
                WeatherItem item = new WeatherItem(id, id_city, temp_cur, temp_min, temp_max, speed, humidity, visibility, code, pub_date, text, chill);
                weatherItems.add(item);
            } while (data.moveToNext());
        }
        return weatherItems;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("LOG", "onResume Detail");
        registerReceiver(receiver, new IntentFilter(ServiceDownload.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        Log.i("LOG", "onPause Detail");
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void updData() {
        progress.show();
        Log.i("LOG", "updData Detail");
        Intent intent = new Intent(context, ServiceDownload.class);
        intent.putExtra(ServiceDownload.URL_ADDRESS, link);
        startService(intent);
    }

    private String getValue(int a) {
        if (a >= 0) {
            if (a < 10) {
                return "  " + a;
            } else {
                return " " + a;
            }
        } else if (a > -10) {
            return " " + a;
        }
        return "" + a;
    }

    private void updInterface(ArrayList<WeatherItem> items) throws ParseException {
        progress.dismiss();
        Log.i("LOG", "updInterface");
        WeatherItem mainItem = items.get(0);

        int id = mainItem.getCode();
        tempView.setText(getValue(mainItem.getTemp()) + "°C");
        mainWeather.setImageResource(getCloudyId(id));

        try {
            String city = getIntent().getStringExtra(CityDataSource.COLUMN_CITY);
            String[] rest = city.split(",");
            ((TextView) findViewById(R.id.city)).setText(rest[0]);
            ((TextView) findViewById(R.id.city2)).setText(rest[1] + ", " + rest[2]);
        } catch (Exception e) {}

        ((TextView) findViewById(R.id.text)).setText("  " + mainItem.getText());

        if (good(mainItem.getHumidity())) {
            ((TextView) findViewById(R.id.humidity)).setText("Humidity " + Double.toString(mainItem.getHumidity()) + "%");
        }

        if (good(mainItem.getVisibility())) {
            ((TextView) findViewById(R.id.visibility)).setText("Visibility " + Double.toString(mainItem.getVisibility()) + "km");
        }

        ((TextView) findViewById(R.id.min)).setText(mainItem.getTempMin() + "° .. " + mainItem.getTempMax() + "° ");

        SimpleDateFormat parserSDF1 = new SimpleDateFormat("EEE", Locale.US);
        SimpleDateFormat parserSDF2 = new SimpleDateFormat("EEEE", Locale.US);
        WeatherItem day1 = items.get(1);
        String date1 = parserSDF2.format(parserSDF1.parse(day1.getPubDate()));
        ((TextView) findViewById(R.id.day1)).setText(" " + date1);
        ((TextView) findViewById(R.id.tempMin1)).setText(getValue(day1.getTempMin()) + "° .. "+ getValue(day1.getTempMax()) + "° ");

        WeatherItem day2 = items.get(2);
        String date2 = parserSDF2.format(parserSDF1.parse(day2.getPubDate()));
        ((TextView) findViewById(R.id.day2)).setText(" " + date2);
        ((TextView) findViewById(R.id.tempMin2)).setText(getValue(day2.getTempMin()) + "° .. " + getValue(day2.getTempMin()) + "° ");

        WeatherItem day3 = items.get(3);
        String date3 = parserSDF2.format(parserSDF1.parse(day3.getPubDate()));
        ((TextView) findViewById(R.id.day3)).setText(" " + date3);
        ((TextView) findViewById(R.id.tempMin3)).setText(getValue(day3.getTempMin()) + "° .. " + getValue(day3.getTempMin()) + "° ");

        WeatherItem day4 = items.get(4);
        String date4 = parserSDF2.format(parserSDF1.parse(day4.getPubDate()));
        ((TextView) findViewById(R.id.day4)).setText(" " + date4);
        ((TextView) findViewById(R.id.tempMin4)).setText(getValue(day4.getTempMin()) + "° .. " + getValue(day4.getTempMin()) + "° ");



//        for (WeatherItem item : items) {
//            Log.i("LOG", item.getText());
//        }
    }

    boolean good(double a) {
       return Math.abs(a) < 1e8;
    }

    public static int getCloudyId(int code) {
//        13, 14, 15, 16 -- snow
//        41, 42, 43, 46 -- snow_showers
//        35, 6 -- rain_and_hail
//        11, 12, 39, 40 -- showers
//        5 -- rain_and_snow
//        10 -- freezing rain
//        31, 33 -- fair_night
//        32, 34 -- fair_day
//        26 -- cloudy
//        27 -- mostly_cloudy_night
//        28 -- mostly_cloudy_day
//        29 -- partly_cloudy_night
//        30 -- partly_cloudy_day
//        20, 22 -- foggy (smoky)
//        3, 4, 37, 38, 39 -- thunderstorms
//        36 -- hot
//        24 -- windy

        // 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        // 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
        // 40, 41, 42, 43, 44, 45, 46, 47

        switch (code) {
            case 13:
            case 14:
            case 15:
            case 16:
                return R.drawable.snow;
            case 41:
            case 42:
            case 43:
            case 46:
                return R.drawable.snow_showers;
            case 35:
            case 6:
                return R.drawable.rain_and_hail;
            case 11:
            case 12:
            case 39:
            case 40:
                return R.drawable.showers;
            case 5:
                return R.drawable.rain_and_snow;
            case 10:
                return R.drawable.freezing_rain;
            case 31:
            case 33:
                return R.drawable.fair_night;
            case 32:
            case 34:
                return R.drawable.fair_day;
            case 24:
                return R.drawable.windy;
            case 26:
                return R.drawable.cloudy;
            case 27:
                return R.drawable.mostly_cloudy_night;
            case 28:
                return R.drawable.mostly_cloudy_day;
            case 29:
                return R.drawable.partly_cloudy_night;
            case 30:
                return R.drawable.partly_cloudy_day;
            case 20:
            case 22:
                return R.drawable.foggy;
            case 3:
            case 4:
            case 37:
            case 38:
                return R.drawable.thunderstorms;
            case 36:
                return R.drawable.hot;
            default:
                Log.i("LOG", "Unknown weather code: " + code);
                return R.drawable.weather_icon;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
