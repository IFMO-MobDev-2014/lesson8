package ru.ifmo.md.lesson8;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.md.lesson8.logic.CityFindResult;
import ru.ifmo.md.lesson8.logic.YahooClient;

public class CitiesActivity extends ActionBarActivity implements CityListFragment.Callbacks {

    private boolean mTwoPane;
    private DrawerLayout myDrawerLayout;
    private ActionBarDrawerToggle myDrawerToggle;

    private CharSequence myDrawerTitle;
    private CharSequence myTitle;
    private Menu searchMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTwoPane = myDrawerLayout == null;
        if (!mTwoPane) {
            myTitle = getTitle();
            myDrawerTitle = getResources().getString(R.string.city_list);

            // enabling action bar app icon and behaving it as toggle button
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            myDrawerToggle = new ActionBarDrawerToggle(this, myDrawerLayout,
                    R.string.toggle_open, //nav menu toggle icon
                    R.string.toggle_close
            ) {
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle(myTitle);
                    // calling onPrepareOptionsMenu() to show action bar icons
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle(myDrawerTitle);
                    // calling onPrepareOptionsMenu() to hide action bar icons
                    invalidateOptionsMenu();
                }
            };
            myDrawerLayout.setDrawerListener(myDrawerToggle);
        }

/*        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                double latitude = 59.57;
                double longitude = 30.18;
                int woeid = YahooClient.getWoeidByCoord(latitude, longitude);
                Log.d("woeid", "" + woeid);
                int woeid = 2121267;
                WeatherLoaderService.startActionAddNewCity(getApplicationContext(), woeid);
            }
        });
        t.start();
*/

//        if (savedInstanceState == null) {
//            onItemSelected(0);
//        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link CityListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        Bundle arguments = new Bundle();
        arguments.putString(CityDetailFragment.ARG_CITY_ID, id);
        Log.d("TAG", "Clicked id = " + id);
        CityDetailFragment fragment = new CityDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.city_detail_container, fragment)
                .commit();
        if (!mTwoPane)
            myDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.searchMenu = menu;
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.action_add).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                loadMatchingCities(query);
                return true;
            }
        });
        return true;
    }

    private void loadMatchingCities(String query) {
        String[] columns = new String[]{"_id", "city"};
        Object[] temp = new Object[]{0, "Moscow"};

        List<String> items = new ArrayList<>();
        items.add("Abakan");
        items.add("Tashkent");
        items.add("Barnaul");

        MatrixCursor cursor = new MatrixCursor(columns);
        for (int i = 0; i < items.size(); i++) {
            temp[0] = i;
            temp[1] = items.get(i);
            cursor.addRow(temp);
        }

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) searchMenu.findItem(R.id.action_add).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setSuggestionsAdapter(new SuggestionsAdapter(this, cursor, items));
    }

    private class SuggestionsAdapter extends CursorAdapter {

        private List<String> items;
        private TextView text;

        public SuggestionsAdapter(Context context, Cursor cursor, List items) {
            super(context, cursor, false);
            this.items = items;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            text.setText(items.get(cursor.getPosition()));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.suggestions_item, parent, false);
            text = (TextView) view.findViewById(R.id.tvSuggestion);
            return view;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (myDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_update_all:
                WeatherLoaderService.startActionUpdateAll(getApplicationContext());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     *
     * @Override public boolean onPrepareOptionsMenu(Menu menu) {
     * // if navigation drawer is opened, hide the action items
     * boolean drawerOpen = myDrawerLayout.isDrawerOpen(GravityCompat.START);
     * menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
     * menu.findItem(R.id.action_update_all).setVisible(!drawerOpen);
     * menu.findItem(R.id.action_add).setVisible(drawerOpen);
     * return super.onPrepareOptionsMenu(menu);
     * }
     * @Override public void setTitle(CharSequence title) {
     * myTitle = title;
     * getSupportActionBar().setTitle(myTitle);
     * }
     * <p/>
     * /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (!mTwoPane)
            myDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        if (!mTwoPane)
            myDrawerToggle.onConfigurationChanged(newConfig);
    }

}
