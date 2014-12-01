package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, WeatherFragment.OnFragmentInteractionListener, CitySearchFragment.OnFragmentInteractionListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;

    static long lastUpdate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    public static void updateSuccessful() {
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if((System.currentTimeMillis() - lastUpdate) / 1000L > 1800) { // magic: 30 minuntes
            WeatherLoaderService.startActionGetAll(getApplicationContext());
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String name, int id) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, WeatherFragment.newInstance(id, name))
                .commit();
    }

    public void setCityName(String cityName) {
        getActionBar().setTitle(cityName);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        // actionBar.setTitle(mTitle); // TODO: improve
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
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
            mNavigationDrawerFragment.closeDrawer();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, CitySearchFragment.newInstance())
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCityAdded(String name, int cityId) {
        ContentValues cv = new ContentValues();
        cv.put(WeatherDatabase.Structure.COLUMN_NAME, name);
        cv.put(WeatherDatabase.Structure.COLUMN_URL, cityId);

        getContentResolver().insert(WeatherContentProvider.URI_CITY_DIR, cv);
        getContentResolver().notifyChange(WeatherContentProvider.URI_CITY_DIR, null);

        WeatherLoaderService.startActionGetSingle(getApplicationContext(), cityId);

        onNavigationDrawerItemSelected(name, cityId);
    }
}
