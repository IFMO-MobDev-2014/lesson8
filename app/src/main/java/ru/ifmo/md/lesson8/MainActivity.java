package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, WeatherFragment.OnFragmentInteractionListener {

    public static final String CITY_DEFAULT_SELECTED = "city_default_selected";
    public static final String CITY_DEFAULT = "city_default";
    private static final float updateTimeInMills = 1200000L;//20min
    static long lastUpdate = 0;
    public SharedPreferences mSettings;
    public int currentCity = 0;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    public static void updateSuccessful() {
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), R.string.help, Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_main);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mSettings.contains(CITY_DEFAULT_SELECTED)) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(CITY_DEFAULT_SELECTED, false);
            editor.apply();
        }
        if (mSettings.getBoolean(CITY_DEFAULT_SELECTED, false) && mSettings.contains(CITY_DEFAULT)) {
            Intent intent = new Intent(getApplicationContext(), LoaderService.class);
            intent.putExtra(LoaderService.EXTRA_SINGLE_ID, mSettings.getInt(CITY_DEFAULT, 0));
            startService(intent);
        } else {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(CITY_DEFAULT_SELECTED, false);
            editor.putInt(CITY_DEFAULT, 0);
            editor.apply();
            onCityAdded(getString(R.string.current_location), 0);

        }
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ((System.currentTimeMillis() - lastUpdate) > updateTimeInMills) {
            Cursor cursor = getContentResolver().query(DatabaseContentProvider.URI_CITY_DIR, new String[]{CitiesTable.URL}, null, null, null);
            cursor.moveToNext();
            while (!cursor.isAfterLast()) {
                Intent intent = new Intent(getApplicationContext(), LoaderService.class);
                intent.putExtra(LoaderService.EXTRA_SINGLE_ID, cursor.getInt(0));
                startService(intent);
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(String name, int id) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, WeatherFragment.newInstance(id, name))
                .commit();
        currentCity = id;
    }

    public void setCityName(String cityName) {
        try {
            getActionBar().setTitle(cityName);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.setDisplayShowTitleEnabled(true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add && mNavigationDrawerFragment.isDrawerOpen()) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivityForResult(intent, 1);
            return true;
        } else if (id == R.id.action_update && mNavigationDrawerFragment.isDrawerOpen()) {
            updateAll();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String name = data.getStringExtra(CitiesTable.NAME);
            int id = data.getIntExtra(CitiesTable.URL, -1);
            if (id != -1) {
                onCityAdded(name, id);
            }
        }
    }

    public void onCityAdded(String name, int cityId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CitiesTable.NAME, name);
        contentValues.put(CitiesTable.URL, cityId);
        getContentResolver().insert(DatabaseContentProvider.URI_CITY_DIR, contentValues);
        getContentResolver().notifyChange(DatabaseContentProvider.URI_CITY_DIR, null);
        Intent intent = new Intent(getApplicationContext(), LoaderService.class);
        intent.putExtra(LoaderService.EXTRA_SINGLE_ID, cityId);
        startService(intent);
        onNavigationDrawerItemSelected(name, cityId);
    }

    public void updateAll() {
        if ((System.currentTimeMillis() - lastUpdate) > updateTimeInMills) {
            Cursor cursor = getContentResolver().query(DatabaseContentProvider.URI_CITY_DIR, new String[]{CitiesTable.URL}, null, null, null);
            cursor.moveToNext();
            while (!cursor.isAfterLast()) {
                Intent intent = new Intent(getApplicationContext(), LoaderService.class);
                intent.putExtra(LoaderService.EXTRA_SINGLE_ID, cursor.getInt(0));
                startService(intent);
                cursor.moveToNext();
            }
            cursor.close();
        } else {
            Toast.makeText(this, R.string.update_notifier, Toast.LENGTH_SHORT).show();
        }
    }
}
