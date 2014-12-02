package ru.ifmo.md.lesson8;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import static ru.ifmo.md.lesson8.WeatherColumns.CITY_NAME;

public class WeatherActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    SpinnerAdapter adapter;
    MenuItem refreshButton;
    boolean progress;

    public void setProgressShown(boolean shown) {
        progress = shown;
        if (refreshButton != null) {
            refreshButton.setVisible(!shown);
            invalidateOptionsMenu();
        }
        setProgressBarIndeterminateVisibility(shown);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Uri.parse("content://net.dimatomp.weather.provider/cities"), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new SpinnerAdapter(this);
        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext())
            adapter.addCity(data.getString(data.getColumnIndex(CITY_NAME)));
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setTitle("");
        actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                ((CityWeather) getFragmentManager().findFragmentById(R.id.city_weather)).setCity(adapter.getItem(itemPosition).toString());
                return true;
            }
        });
        actionBar.setSelectedNavigationItem(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_weather);

        setProgressShown(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        refreshButton = menu.findItem(R.id.action_refresh);
        refreshButton.setVisible(!progress);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                ((CityWeather) getFragmentManager().findFragmentById(R.id.city_weather)).refreshWeather();
                break;
            case R.id.action_add:
                new AddCityDialog().show(getFragmentManager(), "addCityDialog");
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
