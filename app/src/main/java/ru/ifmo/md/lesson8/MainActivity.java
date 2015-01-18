package ru.ifmo.md.lesson8;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener
        , LoaderManager.LoaderCallbacks<Cursor> {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    SimpleCursorAdapter adapter;

    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        String from[] = { Weather.JustWeather.CITY_NAME };
        int to[] = { R.id.action_bar_item_text };
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

    static final String[] SUMMARY_PROJECTION = new String[] {
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
}
