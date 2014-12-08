package ru.ifmo.md.lesson8;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class AddCityActivity extends ActionBarActivity implements LocationListener {

    private LocationManager locationManager;
    private EditText editText;
    private String location_city;
    private ServiceResultReceiver receiver;
    private AsyncTask getCityNameTask;
    private String provider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_city, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_location) {
            if (location_city == null) {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            } else {
                editText.setText(location_city);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buttonClicked(View view) {
        Intent intent = new Intent(this, AddCityService.class);
        intent.putExtra("city_name", editText.getText().toString());
        intent.putExtra("receiver", receiver);
        startService(intent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        editText = (EditText) findViewById(R.id.city_name_edit);
        final Button button = (Button) findViewById(R.id.add_city_button);
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                button.setEnabled(s != null && s.length() > 0);
            }
        });

        receiver = new ServiceResultReceiver(new Handler(), this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            onLocationChanged(location);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        if (getCityNameTask != null) {
            getCityNameTask.cancel(true);
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        getCityNameTask = new GetCityNameTask(this, location).execute();
    }

    public void onLocationFound(String city_name) {
        location_city = city_name;
        locationManager.removeUpdates(this);
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

    class GetCityNameTask extends AsyncTask<Void, Integer, List<Address>> {
        AddCityActivity activity;
        Location location;

        public GetCityNameTask(AddCityActivity activity, Location location) {
            this.activity = activity;
            this.location = location;
        }

        @Override
        protected List<Address> doInBackground(Void... arg0) {
            Geocoder geoCoder = new Geocoder(activity, Locale.getDefault());
            List<Address> results = null;
            try {
                results = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException ignored) {
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<Address> address) {
            if (address != null && address.size() > 0) {
                String city_name = address.get(0).getLocality();
                activity.onLocationFound(city_name);
            }
        }
    }
}
