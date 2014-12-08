package md.ifmo.ru.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {
    //public static final String[] CITIES = {"Москва", "Санкт-Петербург", "Оттава"};
    public static final String CODE = "city";
    private DBAdapter myDBAdapter;
    private Cursor cursor;
    private LocationManager locationManager;
    String localCity;
    Button locationButton;
    Boolean locationNeeded;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationButton = (Button) findViewById(R.id.button);
        locationButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                locationNeeded = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000 * 10, 10, locationListener);
                if (localCity!=null) {
                    Intent intent = new Intent(view.getContext(), AddCityActivity.class);
                    intent.putExtra(CODE, localCity);
                    startActivity(intent);
                }
            }
        });
        myDBAdapter = new DBAdapter(this);
        myDBAdapter.open();
        cursor = myDBAdapter.fetchCities();
        ListView lvChooser = (ListView) findViewById(R.id.lvChooser);
        registerForContextMenu(lvChooser);
        lvChooser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                int cityID = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID));
                myDBAdapter.setLast(cityID);
                String city = cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_CITY));
                Intent intent = new Intent(view.getContext(), WeatherActivity.class);
                intent.putExtra(CODE, city);
                startActivity(intent);
            }
        });
        showCities();
        Cursor cursorLast = myDBAdapter.getLast();
        cursorLast.moveToFirst();
        int last = cursorLast.getInt(0);
        if (last != -1) {
            cursorLast = myDBAdapter.fetchCity(last);
            if (cursorLast.moveToFirst()) {
                String city = cursorLast.getString(cursorLast.getColumnIndexOrThrow(DBAdapter.KEY_CITY));
                Intent intent = new Intent(this, WeatherActivity.class);
                intent.putExtra(CODE, city);
                startActivity(intent);
            }
        }
    }

    private void showCities() {
        cursor = myDBAdapter.fetchCities();
        String[] from = new String[]{DBAdapter.KEY_CITY};
        int[] to = new int[]{R.id.city_row_name};
        ListView lvChooser = (ListView) findViewById(R.id.lvChooser);
        SimpleCursorAdapter cities = new SimpleCursorAdapter(this, R.layout.city_row, cursor, from, to);
        lvChooser.setAdapter(cities);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCities();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == R.id.lvChooser) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            MenuInflater inflater = getMenuInflater();
            cursor.moveToPosition(info.position);
            menu.setHeaderTitle(cursor.getString(cursor.getColumnIndexOrThrow(DBAdapter.KEY_CITY)));
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        cursor.moveToPosition(info.position);
        int cityID = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID));
        switch (item.getItemId()) {
            case R.id.remove:
                myDBAdapter.deleteCity(cityID);
                showCities();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void clickPlus(View v) {
        Intent intent = new Intent(this, AddCityActivity.class);
        intent.putExtra(CODE," ");
        startActivity(intent);
    }

    public void locationResolve(Location location) throws IOException {
            Geocoder geocoder = new Geocoder(this.getBaseContext(), Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Log.i("123", "" + addressList);
            if (addressList.size()>0) {
                localCity = addressList.get(0).getLocality();
            }

    }

    private LocationListener locationListener = new LocationListener() {


        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onLocationChanged(Location location) {
            try {
                if(location != null) {
                    if(locationNeeded) {
                        locationResolve(location);
                        locationNeeded = false;
                    }
                }

            } catch (IOException e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Включите службы геолокации.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    };
}
