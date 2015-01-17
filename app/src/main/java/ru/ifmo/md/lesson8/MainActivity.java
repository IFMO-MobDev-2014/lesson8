package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ru.ifmo.md.lesson8.db.WeatherContentProvider;
import ru.ifmo.md.lesson8.db.WeatherDBHelper;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, AppResultsReceiver.Receiver{
    Toolbar toolbar;
    Spinner spinner;
    CityCursorAdapter cityCursorAdapter;
    DayCursorAdapter dayCursorAdapter;
    public AppResultsReceiver mReceiver;
    SharedPreferences prefs = null;
    String currentCityWoeid;
    String currentCityName;
    AutoCompleteTextView edt;

    private static final int ORIENTATION_PORTRAIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("ru.ifmo.md.lesson8", MODE_PRIVATE);

        mReceiver = new AppResultsReceiver(new Handler());
        mReceiver.setReceiver(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        cityCursorAdapter = new CityCursorAdapter(this, null);
        dayCursorAdapter = new DayCursorAdapter(this, null);

        final Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
        spinner.setAdapter(cityCursorAdapter);

        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(dayCursorAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Called when a new item is selected (in the Spinner)
             */
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                City city = cityCursorAdapter.get(pos);
                currentCityWoeid = city.getWoeid();
                currentCityName = city.getCityName();
                getSupportLoaderManager().restartLoader(1, null, MainActivity.this);
                getSupportLoaderManager().restartLoader(2, null, MainActivity.this);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        edt = (AutoCompleteTextView) findViewById(R.id.edtCity);
        final CityAdapter cityAdapter = new CityAdapter(this, null);
        edt.setAdapter(cityAdapter);
        edt.setVisibility(View.GONE);
        edt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                edt.setVisibility(View.GONE);
                City city = cityAdapter.getItem(pos);
                city.cutName();
                refresh(city, "add");
            }
        });

        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            getLocation(true);
        }

        Intent alarm = new Intent(this, AlarmService.class);
        alarm.putExtra("receiver", mReceiver);
        startService(alarm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new AppResultsReceiver(new Handler());
        mReceiver.setReceiver(this);

        if (prefs.getBoolean("firstrun", true)) {
            refresh(new City("Saint Petersburg", "2123260"), "add");
            refresh(new City("Moscow", "2122265"), "add");
            refresh(new City("London", "44418"), "add");
            refresh(new City("Barcelona", "753692"), "add");
            refresh(new City("Rome", "721943"), "add");
            refresh(new City("Paris", "615702"), "add");

            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    public void getLocation(boolean internet) {
        final LocationManager mLocationManager;

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        final boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        final boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (internet) {
            if (isNetworkEnabled) {
                if (isNetworkConnected()) {
                    MyLocationListener mLocationListener = new MyLocationListener();
                    mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.internet_gps_problem), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.internet_not_allowed), Toast.LENGTH_LONG).show();
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Determining your location").setMessage(getResources().getString(R.string.which_way)).setPositiveButton("GPS",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (isGPSEnabled) {
                                MyLocationListener mLocationListener = new MyLocationListener();
                                mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
                            } else {
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.gps_enabled), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            builder.setNegativeButton("Internet",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (isNetworkEnabled) {
                                if (isNetworkConnected()) {
                                    MyLocationListener mLocationListener = new MyLocationListener();
                                    mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
                                } else {
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.internet_gps_problem), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.internet_not_allowed), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            gps(Double.toString(loc.getLatitude()), Double.toString(loc.getLongitude()), "delete and add");
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                edt.setText("");
                edt.setVisibility(View.VISIBLE);
                edt.setSelection(0);
                break;
            case R.id.action_refresh:
                if (cityCursorAdapter.getCount() > 0) {
                    refresh(new City(currentCityName, currentCityWoeid), "update");
                } else {
                    Toast.makeText(this, getResources().getString(R.string.there_is_no_any_city), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_locate:
                getLocation(false);
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        CursorLoader loader;
        if (id == 0) {
            loader = new CursorLoader(this,
                    WeatherContentProvider.CITY_CONTENT_URL, null, null, null, null);
        } else if (id == 1) {
            loader = new CursorLoader(this,
                    WeatherContentProvider.NOW_CONTENT_URL, null, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {currentCityWoeid}, null);
        } else {
            loader = new CursorLoader(this,
                    WeatherContentProvider.FORECAST_CONTENT_URL, null, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[] {currentCityWoeid}, null);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        if (arg0.getId() == 0) {
            cityCursorAdapter.swapCursor(cursor);
        } else if (arg0.getId() == 1) {
            if (cursor.moveToFirst()) {
                Now now = new Now();

                now.setTemp(cursor.getString(cursor.getColumnIndex("temp")) + "\u00b0");
                now.setCondition(cursor.getString(cursor.getColumnIndex("condition")));
                now.setConditionCode(cursor.getString(cursor.getColumnIndex("condition_code")));
                now.setWindSpeed(cursor.getString(cursor.getColumnIndex("wind_speed")) + " km/h");
                now.setHumidity(cursor.getString(cursor.getColumnIndex("humidity")) + "%");

                displayNowWeather(now);

                cursor.close();
            } else {
                Now now = new Now();

                now.setTemp("");
                now.setCondition("");
                now.setConditionCode("");
                now.setWindSpeed("");
                now.setHumidity("");

                displayNowWeather(now);
            }
        } else if (arg0.getId() == 2) {
            dayCursorAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        if (arg0.getId() == 0) {
            cityCursorAdapter.swapCursor(null);
        } else if (arg0.getId() == 2) {
            dayCursorAdapter.swapCursor(null);
        }
    }

    public void gps(String lat, String lon, String update) {
        Intent intent = new Intent(this, CityFromGpsIntentService.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lon", lon);
        intent.putExtra("update", update);
        intent.putExtra("receiver", mReceiver);
        this.startService(intent);
    }

    public void refresh(City city, String update) {
        Intent intent = new Intent(this, WeatherIntentService.class);
        intent.putExtra("cityName", city.getCityName());
        intent.putExtra("woeid", city.getWoeid());
        intent.putExtra("update", update);
        intent.putExtra("receiver", mReceiver);
        this.startService(intent);
    }

    public void displayNowWeather(Now now) {
        TextView temp = (TextView)findViewById(R.id.temp);
        temp.setText(now.getTemp());

        TextView condition = (TextView)findViewById(R.id.condition);
        condition.setText(now.getCondition());

        ImageView conditionImage = (ImageView)findViewById(R.id.condition_image);
        conditionImage.setImageResource(getResources().getIdentifier("w" + now.getConditionCode() , "drawable", getPackageName()));

        TextView windSpeed = (TextView)findViewById(R.id.wind_speed);
        windSpeed.setText(now.getWindSpeed());

        TextView humidity = (TextView)findViewById(R.id.humidity);
        humidity.setText(now.getHumidity());
    }

    /*public void addCity(City city) {
        refresh(city, Integer.toString(currentCityId), false);
    }*/

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode) {
            case AppResultsReceiver.STATUS_REFRESHED :
                //mProgress.setVisibility(View.INVISIBLE);
                getSupportLoaderManager().restartLoader(0, null, this);
                getSupportLoaderManager().restartLoader(1, null, this);
                getSupportLoaderManager().restartLoader(2, null, this);
                Toast.makeText(this, getResources().getString(R.string.current_city_weather_refreshed), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_ADDED:
                getSupportLoaderManager().restartLoader(0, null, this);
                break;
            case AppResultsReceiver.STATUS_INTERNET_ERROR:
                Toast.makeText(this, getResources().getString(R.string.internet_problem), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_PARSE_ERROR:
                Toast.makeText(this, getResources().getString(R.string.parse_problem), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_GPS_FINISHED_ADD:
                refresh(new City(data.getString("cityName"), data.getString("woeid")), "add_gps");
                break;
            case AppResultsReceiver.STATUS_GPS_FINISHED_DELETE_AND_ADD:
                refresh(new City(data.getString("cityName"), data.getString("woeid")), "delete and add");
                break;
            case AppResultsReceiver.STATUS_ALREADY_ADDED:
                Toast.makeText(this, getResources().getString(R.string.already_added), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.STATUS_GPS_INTERNET_ERROR:
                Toast.makeText(this, getResources().getString(R.string.internet_gps_problem), Toast.LENGTH_LONG).show();
            case AppResultsReceiver.STATUS_DELETE_AND_ADD_REFRESHED:
                getSupportLoaderManager().restartLoader(0, null, this);
                Toast.makeText(this, getResources().getString(R.string.current_location_weather_refreshed), Toast.LENGTH_SHORT).show();
                break;
            case AppResultsReceiver.ALARM_STARTS:

                break;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        } else
            return true;
    }

    class CityCursorAdapter extends CursorAdapter {
        public CityCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        public class ViewHolder {
            public ImageButton button;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.toolbar_spinner_item_actionbar, parent, false);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView cityName = (TextView) view.findViewById(R.id.text1);
            cityName.setText(cursor.getString(cursor.getColumnIndex("city_name")));

            final String woeid = cursor.getString(cursor.getColumnIndex("woeid"));

            ViewHolder holder = (ViewHolder) view.getTag();

            if (holder != null) {
                holder.button.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {
                        MainActivity.this.getContentResolver().delete(WeatherContentProvider.CITY_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{woeid});
                        MainActivity.this.getContentResolver().delete(WeatherContentProvider.NOW_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{woeid});
                        MainActivity.this.getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{woeid});
                        MainActivity.this.getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{woeid});
                        MainActivity.this.getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{woeid});
                        MainActivity.this.getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{woeid});
                        MainActivity.this.getContentResolver().delete(WeatherContentProvider.FORECAST_CONTENT_URL, WeatherDBHelper.COLUMN_NAME_WOEID + "=?", new String[]{woeid});
                        notifyDataSetChanged();
                        MainActivity.this.getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
                        MainActivity.this.getSupportLoaderManager().restartLoader(1, null, MainActivity.this);
                        MainActivity.this.getSupportLoaderManager().restartLoader(2, null, MainActivity.this);
                    }
                });
            }
        }

        @Override
        public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = LayoutInflater.from(context).inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
            holder.button = (ImageButton) view.findViewById(R.id.button_delete);
            view.setTag(holder);

            return view;
        }

        public City get(int position) {
            Cursor cursor = getCursor();
            City city = new City();

            if(cursor.moveToPosition(position)) {
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setWoeid(cursor.getString(cursor.getColumnIndex("woeid")));
            }

            return city;
        }
    }
}
