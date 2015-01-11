package ru.ifmo.md.lesson8;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;


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
        implements ItemListFragment.Callbacks, AppResultReceiver.Receiver {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    double lat;
    double lon;

    private AppResultReceiver mReceiver;
    LocationManager locationManager;

    Button add;
    AlertDialog.Builder dialog;
    EditText editText;
    ImageButton location;
    TextView help;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        editText = (EditText) findViewById(R.id.editText);
        add = (Button) findViewById(R.id.add_city);
        location = (ImageButton) findViewById(R.id.imageButton);
        help = (TextView) findViewById((R.id.textView2));
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocation();
                help.setVisibility(View.INVISIBLE);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCity();
                editText.setText("");
            }
        });


        mReceiver = new AppResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(this, AlarmService.class);
        intent.putExtra("receiver", mReceiver);
        startService(intent);
        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            updateLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                Log.wtf("Status GPS: ", String.valueOf(status));
            }
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                Log.wtf("Status Network: ", String.valueOf(status));
            }
        }
    };



    private void updateLocation(Location location) {
        if (location == null)
            return;
        lon = location.getLongitude();
        lat = location.getLatitude();
    }

    private void checkEnabled() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int position) {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.item_open);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Bundle arguments = new Bundle();
            arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, position);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_open, fragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, position);
            startActivity(detailIntent);
        }
       /* if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, position);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, position);
            startActivity(detailIntent);
        } */
    }

    public void addCity() {
        String name = editText.getText().toString();
        if (name.equals("")) {
            Toast.makeText(this, "It's Neverland?", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, UpdaterService.class);
        intent.putExtra("name", name);
        intent.putExtra("receiver", mReceiver);
        startService(intent);

    }

    public void addLocation() {
        Intent intent = new Intent(this, UpdaterService.class);
        intent.putExtra("lon", lon);
        intent.putExtra("lat", lat);
        intent.putExtra("receiver", mReceiver);
        startService(intent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode) {
            case AppResultReceiver.ERROR:
                Toast.makeText(this, data.getString("error"), Toast.LENGTH_SHORT).show();
                break;
            case AppResultReceiver.OK:
                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
                break;
        }

            ItemListFragment fragment = (ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list);
            fragment.update();
    }
}
