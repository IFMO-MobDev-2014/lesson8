package ru.ifmo.md.lesson8;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity
        implements ItemListFragment.Callbacks, LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    ArrayList<String> cities = new ArrayList<String>();
    final String request = "http://api.openweathermap.org/data/2.5/weather?q=";

    private boolean mTwoPane;

    SimpleCursorAdapter cursorAdapter;
    LocationListener locListener;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] Columns = new String[]{MyContentProvider.COLUMN_ID, MyContentProvider.COLUMN_CITY_NAME, MyContentProvider.COLUMN_WEATHER, MyContentProvider.COLUMN_WEATHER_ICON, MyContentProvider.COLUMN_WIND, MyContentProvider.COLUMN_WIND_SPEED, MyContentProvider.COLUMN_TEMP};
        return new CursorLoader(this, MyContentProvider.TABLE_CITIES_URI, Columns, null, null, null);
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onDestroy() {
        getLoaderManager().destroyLoader(0);
        super.onDestroy();
    }

    void updateAllCities() {
        for (int i = 0; i<cities.size(); i++) {
            fetchCurrCityWeather(cities.get(i), i+1);
        }
    }

    class weatherUpdater {
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public void updateEveryHour () {
            Runnable upd = new Runnable() {
                @Override
                public void run() {
                    updateAllCities();
                }
            };
            final ScheduledFuture updHandle = scheduler.scheduleAtFixedRate(upd, 0, 60*5, TimeUnit.SECONDS);
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    updHandle.cancel(true);
                }
            }, 60*60*24, TimeUnit.SECONDS);
        }
    }

    void createInitialCityList() {
        Cursor c = getContentResolver().query(MyContentProvider.TABLE_CITIES_URI, null, null, null, null);
        Log.d("COUNTER", Integer.toString(c.getCount()));
        if (c.getCount()==0) {
            cities.addAll(Arrays.asList(new String[]{"London", "Paris", "Moscow", "Berlin", "Rome"}));
        } else {
            while (c.moveToNext()) {
                cities.add(c.getString(c.getColumnIndex(MyContentProvider.COLUMN_CITY_NAME)));
            }
        }
        c.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);


        createInitialCityList();
        new weatherUpdater().updateEveryHour();

        String[] Columns = new String[]{MyContentProvider.COLUMN_CITY_NAME, MyContentProvider.COLUMN_WEATHER, MyContentProvider.COLUMN_WEATHER_ICON, MyContentProvider.COLUMN_WIND, MyContentProvider.COLUMN_WIND_SPEED, MyContentProvider.COLUMN_TEMP};
        int[] elements = new int[]{R.id.city_name_textview, R.id.city_weather_textview, R.id.curr_weather_imgview, R.id.wind_textview, R.id.wind_speed_textview, R.id.temperature_textview};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.list_element, null, Columns, elements, 0);
        ((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setListAdapter(cursorAdapter);

        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                    if (view.getId() == R.id.curr_weather_imgview) {
                        ImageView imgView = (ImageView)view;
                        String imgName = "i"+cursor.getString(3);
                        int imgResource = getResources().getIdentifier(imgName, "drawable", getPackageName());
                        imgView.setImageBitmap(BitmapFactory.decodeResource(getResources(), imgResource));
                        return true;
                }
                return false;
            }
        });

        ImageButton updateButton = (ImageButton)findViewById(R.id.update_button);
        updateButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAllCities();
                Toast.makeText(getApplicationContext(), "Force update started", Toast.LENGTH_LONG).show();
            }
        });
        updateButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.refresh));

        ImageButton addButton = (ImageButton)findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = ((TextView)findViewById(R.id.add_text_view)).getText().toString();
                if (!cities.contains(city)) {
                    cities.add(city);
                    updateAllCities();
                    cursorAdapter.notifyDataSetChanged();
                }
            }
        });
        addButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.search));

        locListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        ImageButton getByLocButton = (ImageButton)findViewById(R.id.add_by_loc_button);
        getByLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1000, locListener);
                Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                double Lat = location.getLatitude();
                double Long = location.getLongitude();
                Log.d("GPS", "LAT "+Lat + ", LON " + Long);
                Intent loadFeed = new Intent(ItemListActivity.this, MyLoaderIntentService.class);
                String link = "http://api.openweathermap.org/data/2.5/weather?lat=" + Lat + "&lon=" + Long + "&mode=xml";
                MyResultReceiver rs = new MyResultReceiver(new Handler());
                loadFeed.putExtra("link", link);
                loadFeed.putExtra("city_name", "default");
                loadFeed.putExtra("city_id", 0);
                loadFeed.putExtra("mode", 3);
                loadFeed.putExtra("receiver", rs);
                startService(loadFeed);
                Log.d("GPS", "Started service: " + link);

            }
        });
        getByLocButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.geoloc));

        getLoaderManager().initLoader(0, null, this);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            Log.d("Activated state", "true");
            ((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    class MyResultReceiver extends ResultReceiver{

        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int code, Bundle bundle) {
            cities.add(bundle.getString("city"));
            Log.d("CYTU ADDED", bundle.getString("city"));
            updateAllCities();
        }
    }
    void fetchCurrCityWeather(String city, int city_id) {
        String link = request + city + "&mode=xml";
        Intent loadFeed = new Intent(ItemListActivity.this, MyLoaderIntentService.class);
        loadFeed.putExtra("link", link);
        loadFeed.putExtra("city_name", city);
        loadFeed.putExtra("city_id", city_id);
        loadFeed.putExtra("mode", 2);
        startService(loadFeed);
    }

    @Override
    public void onItemLongClick(String name) {
        Log.d("DEL",name + " " + getContentResolver().delete(MyContentProvider.TABLE_CITIES_URI, MyContentProvider.COLUMN_CITY_NAME + " = '" + name + "'", null));
        cities.remove(name);
        //cursorAdapter.notifyDataSetChanged();
        updateAllCities();

    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */



    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
            arguments.putString("city_name", cities.get(Integer.parseInt(id)-1));
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, Integer.toString(Integer.parseInt(id)-1));
            Log.d("onItemSelected",cities.get(Integer.parseInt(id) - 1)+ " "+ (Integer.parseInt(id)-1));
            detailIntent.putExtra("city_name", cities.get(Integer.parseInt(id) - 1));
            startActivity(detailIntent);
        }
    }


}
