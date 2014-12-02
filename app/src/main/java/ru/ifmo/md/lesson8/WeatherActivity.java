package ru.ifmo.md.lesson8;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import static ru.ifmo.md.lesson8.WeatherColumns.CITY_NAME;

public class WeatherActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    CitiesAdapter adapter;
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
        if (data.getCount() != 0)
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext())
                adapter.addCity(data.getString(data.getColumnIndex(CITY_NAME)));
        else
            adapter.addCity("Saint Petersburg");
        ((ViewPager) findViewById(R.id.city_pager)).setAdapter(adapter);
        setProgressShown(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_weather);

        setProgressShown(true);
        adapter = new CitiesAdapter(getSupportFragmentManager());
        getSupportLoaderManager().initLoader(0, null, this);
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
                ((CityWeather) adapter.getItem(((ViewPager) findViewById(R.id.city_pager)).getCurrentItem())).refreshWeather();
                break;
            case R.id.action_add:
                new AddCityDialog().show(getFragmentManager(), "addCityDialog");
                break;
            case R.id.action_remove:
                int current = ((ViewPager) findViewById(R.id.city_pager)).getCurrentItem();
                String cityName = ((CityWeather) adapter.getItem(current)).getCity();
                adapter.remove(current);
                Intent intent = new Intent(this, WeatherUpdater.class);
                intent.setData(Uri.parse("content://net.dimatomp.weather.provider/city?name=" + Uri.encode(cityName)));
                startService(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
