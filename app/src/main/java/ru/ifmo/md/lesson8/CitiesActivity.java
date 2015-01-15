package ru.ifmo.md.lesson8;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.md.lesson8.database.WeatherProvider;
import ru.ifmo.md.lesson8.database.WeatherTable;
import ru.ifmo.md.lesson8.logic.CityFindResult;
import ru.ifmo.md.lesson8.logic.YahooClient;

public class CitiesActivity extends ActionBarActivity implements CityListFragment.Callbacks {

    private boolean mTwoPane;
    private DrawerLayout myDrawerLayout;
    private ActionBarDrawerToggle myDrawerToggle;
    private LocationManager locationManager;
    private String provider;

    private CharSequence myDrawerTitle;
    private CharSequence myTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
//            Log.d("Latitude", "" + latitude);
//            Log.d("Longitude", "" + longitude);
            WeatherLoaderService.startActionAddNewCity(getApplicationContext(), latitude, longitude);
        } else {
            Toast.makeText(this, "Your location is not available now", Toast.LENGTH_LONG).show();
        }
        showFirstCityWeather();
       // TODO: If exposing deep links into your app, handle intents here.
    }

    private void showFirstCityWeather() {
        final int currentCityWoeid = WeatherLoaderService.getCurrentCity(getApplicationContext());
        Cursor cursor = getContentResolver().query(
                WeatherProvider.CONTENT_URI,
                new String[] {WeatherTable.COLUMN_ID, WeatherTable.COLUMN_WOEID},
                WeatherTable.COLUMN_WOEID + " = ?",
                new String[] {String.valueOf(currentCityWoeid)},
                null);
        if (cursor == null)
            return;
        cursor.moveToFirst();
        final int currentCityRowId = cursor.getInt(cursor.getColumnIndex(WeatherTable.COLUMN_ID));
        cursor.close();
        onItemSelected(String.valueOf(currentCityRowId));
    }

    // Callback indicating that the item with the given ID was selected.
    @Override
    public void onItemSelected(String cityId) {
        Bundle arguments = new Bundle();
        arguments.putString(CityDetailFragment.ARG_CITY_ID, cityId);
        CityDetailFragment fragment = new CityDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.city_detail_container, fragment)
                .commit();
        if (!mTwoPane)
            myDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private class CityAdapter extends ArrayAdapter<CityFindResult> implements Filterable {

        private Context ctx;
        private List<CityFindResult> cityList = new ArrayList<CityFindResult>();

        public CityAdapter(Context ctx, List<CityFindResult> cityList) {
            super(ctx, android.R.layout.simple_dropdown_item_1line, cityList);
            this.cityList = cityList;
            this.ctx = ctx;
        }


        @Override
        public CityFindResult getItem(int position) {
            return cityList != null ? cityList.get(position) : null;
        }

        @Override
        public int getCount() {
            return cityList != null ? cityList.size() : 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = convertView;
            if (result == null) {
                LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                result = inf.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            TextView tv = (TextView) result.findViewById(android.R.id.text1);
            tv.setText(cityList.get(position).getCityName() + ", " + cityList.get(position).getDistrict() + ", " + cityList.get(position).getCountry());
            return result;
        }

        @Override
        public long getItemId(int position) {
            return cityList != null ? cityList.get(position).hashCode() : 0;
        }

        @Override
        public Filter getFilter() {
            Filter cityFilter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() < 2)
                        return results;

                    List<CityFindResult> cityResultList = YahooClient.getCityList(constraint.toString());
                    results.values = cityResultList;
                    results.count = cityResultList.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    cityList = (List) results.values;
                    notifyDataSetChanged();
                }
            };

            return cityFilter;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void toggleCityFinder() {
        //Setting up custom search widget
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO);
        if (!mTwoPane) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_search, null);
        CleanableAutoCompleteTextView searchBox = (CleanableAutoCompleteTextView) v.findViewById(R.id.search_box);

        searchBox.setOnClearListener(new CleanableAutoCompleteTextView.OnClearListener() {
            @Override
            public void onClear() {
                toggleSearch(true);
            }
        });

        searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // handle clicks on search results here
                CityFindResult cityFindResult = (CityFindResult) parent.getItemAtPosition(position);
                WeatherLoaderService.startActionAddNewCity(getApplicationContext(), Integer.valueOf(cityFindResult.getWoeid()));
                toggleSearch(true);
            }
        });

        CityAdapter searchAdapter = new CityAdapter(this, null);
        searchBox.setAdapter(searchAdapter);
        actionBar.setCustomView(v);
    }

    // this toggles between the visibility of the search icon and the search box
    protected void toggleSearch(boolean reset) {
        CleanableAutoCompleteTextView searchBox = (CleanableAutoCompleteTextView) findViewById(R.id.search_box);
        if (reset) {
            // hide search box and show search icon
            searchBox.setText("");
            searchBox.setVisibility(View.GONE);
            // hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            if (!mTwoPane) {
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            invalidateOptionsMenu();
        } else {
            // hide search icon and show search box
            searchBox.setFocusable(true);
            searchBox.setFocusableInTouchMode(true);
            searchBox.setVisibility(View.VISIBLE);
            // show the keyboard
            if(searchBox.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
            searchBox.requestFocus();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (!mTwoPane && myDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_add:
                toggleCityFinder();

                item.setVisible(false);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if navigation drawer is opened, hide the action items
        if (!mTwoPane) {
            boolean drawerOpen = myDrawerLayout.isDrawerOpen(GravityCompat.START);
            menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
            menu.findItem(R.id.action_add).setVisible(drawerOpen);
            if (menu.findItem(R.id.action_update) != null) {
                menu.findItem(R.id.action_update).setVisible(!drawerOpen);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        myTitle = title;
        getSupportActionBar().setTitle(myTitle);
    }

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
