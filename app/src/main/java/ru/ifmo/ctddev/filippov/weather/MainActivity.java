package ru.ifmo.ctddev.filippov.weather;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, WeatherFragment.OnFragmentInteractionListener, CitySearchFragment.InteractionListener {
    private NavigationDrawerFragment navigationDrawerFragment;
    private static long timeOfLastUpdate = 0;
    private static final long timeToUpdate = 500000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((System.currentTimeMillis() - timeOfLastUpdate) > timeToUpdate) {
            WeatherLoader.getAll(getApplicationContext());
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String name, int id) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, WeatherFragment.newInstance(id, name)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            menu.clear();
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            navigationDrawerFragment.closeDrawer();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, CitySearchFragment.newInstance()).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCityAdded(String cityName, int cityId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherDatabase.COLUMN_NAME, cityName);
        contentValues.put(WeatherDatabase.COLUMN_URL, cityId);

        getContentResolver().insert(WeatherContentProvider.URI_CITY_DIRECTORY, contentValues);
        getContentResolver().notifyChange(WeatherContentProvider.URI_CITY_DIRECTORY, null);
        WeatherLoader.getSingle(getApplicationContext(), cityId);
        onNavigationDrawerItemSelected(cityName, cityId);
    }

    public void setCityName(String cityName) {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(cityName);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    public void onClickRefresh(View view) {
        Log.i("MainActivity", "Updating weather");
        WeatherLoader.getAll(getApplicationContext());
    }

    public static void updateTime() {
        timeOfLastUpdate = System.currentTimeMillis();
    }
}
