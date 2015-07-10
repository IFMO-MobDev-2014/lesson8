package year2013.ifmo.catweather;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements AddByNameDialog.NoticeDialogListener, ActionBar.OnNavigationListener
        , LoaderManager.LoaderCallbacks<Cursor>, DeleteCityDialog.NoticeDialogListener {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private SimpleCursorAdapter adapter;

    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        String from[] = {Weather.JustWeather.CITY_NAME};
        int to[] = {R.id.action_bar_item_text};
        adapter = new SimpleCursorAdapter(this,
                R.layout.action_bar_item, null, from, to);

        actionBar.setListNavigationCallbacks(
                adapter,
                this);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(
                        adapter.getCursor().getString(Weather.JustWeather.CITY_COLUMN)))
                .commit();
        return true;
    }

    static final String[] SUMMARY_PROJECTION = new String[]{
            Weather.JustWeather._ID,
            Weather.JustWeather.CITY_NAME,
            Weather.JustWeather.TODAY_NAME,
            Weather.JustWeather.FUTURE_NAME
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = Weather.JustWeather.CONTENT_URI;

        return new CursorLoader(getBaseContext(), baseUri,
                SUMMARY_PROJECTION, null, null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onDialogPositiveClick(String city) {

        try {
            Cursor cursor = getContentResolver().query(Weather.JustWeather.CONTENT_URI, null,
                    Weather.JustWeather.CITY_NAME + "=\"" + city + "\"", null, null);
            cursor.moveToLast();
            cursor.isNull(Weather.JustWeather.CITY_COLUMN);
            cursor.close();
            Toast.makeText(getApplicationContext(), getString(R.string.exist), Toast.LENGTH_SHORT).show();
        } catch (android.database.CursorIndexOutOfBoundsException e) {

            ContentValues cv = new ContentValues();
            cv.put(Weather.JustWeather.CITY_NAME, city);
            getContentResolver().insert(Weather.JustWeather.CONTENT_URI, cv);

            //PlaceholderFragment.newInstance(city);

            Intent intent = new Intent(this, WeatherIntentService.class);
            intent.setAction(WeatherIntentService.ACTION_WEATHER);
            intent.putExtra(WeatherIntentService.EXTRA_CITY, city);
            startService(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_by_name) {
            if (isOnline()) {
                DialogFragment dialogFragment = new AddByNameDialog();
                dialogFragment.show(getFragmentManager(), "");
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.action_delete) {
            DialogFragment dialogFragment = new DeleteCityDialog();
            dialogFragment.show(getFragmentManager(), "");

        } else if (id == R.id.action_add_by_location) {
            Toast.makeText(getApplicationContext(), getString(R.string.start_looking_for), Toast.LENGTH_SHORT).show();
            locationListener = new MyLocationListener();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 35000, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 35000, 10, locationListener);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this,
                        "Please, enable mobile network",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void getLocation(Location location) {
        Intent intent = new Intent(this, WeatherIntentService.class);
        intent.setAction(WeatherIntentService.ACTION_LATLON);
        intent.putExtra(WeatherIntentService.EXTRA_LATITUDE, location.getLatitude() + "");
        intent.putExtra(WeatherIntentService.EXTRA_LONGITUDE, location.getLongitude() + "");
        startService(intent);
    }

    @Override
    public void onDialogPositiveClick(long city, String cityName) {
        Uri uri = ContentUris.withAppendedId(Weather.JustWeather.CONTENT_URI, city);
        getContentResolver().delete(uri, null, null);
        Toast.makeText(getApplicationContext(), String.format(getString(R.string.delete_toast), cityName), Toast.LENGTH_SHORT).show();
    }

    private boolean isOnline() {
        String cs = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager)
                getApplicationContext().getSystemService(cs);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private final class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (isOnline()) getLocation(location);
            else {
                Toast.makeText(getApplicationContext(), getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }
            locationManager.removeUpdates(locationListener);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), getString(R.string.gps_or_internet_connection), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

}
