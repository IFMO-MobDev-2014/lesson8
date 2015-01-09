package com.alex700.AWeather;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        WeatherFragment.OnFragmentInteractionListener
{

    public static final String SP_NAME = "WEATHER";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static final String API_KEY = "d690342f9a15003127f67bc6aff8418c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        Location lastKnownLocation = ((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastKnownLocation != null) {
            Log.d("LOCATION", "start");
            Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
            startService(new Intent(getApplicationContext(), CityGetNameService.class)
                    .putExtra(CityGetNameService.LATITUDE, lastKnownLocation.getLatitude())
                    .putExtra(CityGetNameService.LONGITUDE, lastKnownLocation.getLongitude()));
            CityGetNameService.setHandler(new Handler(new Handler.Callback() {
                        private AlertDialog alertDialog;
                        @Override
                        public boolean handleMessage(Message msg) {
                            if (msg.what == CityGetNameService.OK) {
                                final String name = (String) msg.obj;
                                Cursor c = getContentResolver().query(WeatherContentProvider.CITY_CONTENT_URI, null,
                                        WeatherDatabaseHelper.CITY_NAME + " = '" + name +"'", null, null);
                                c.moveToNext();
                                if (c.isAfterLast()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Geolocation");
                                    builder.setMessage("You are in " + name + "\n" +
                                            "Do you want to add " + name + " in your list of cities?");
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ContentValues cv = new ContentValues();
                                            cv.put(WeatherDatabaseHelper.CITY_NAME, name);
                                            getContentResolver().insert(WeatherContentProvider.CITY_CONTENT_URI, cv);
                                            mNavigationDrawerFragment.getLoaderManager().restartLoader(2221, null, mNavigationDrawerFragment);
                                            mNavigationDrawerFragment.selectItemAfterLoading(name);
                                        }
                                    });
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.dismiss();
                                        }
                                    });
                                    alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            }
                            return true;
                        }
                    })
            );
            //Log.d("LOCATION", currentName);
        } else {
            Toast.makeText(this, "Your location is not available", Toast.LENGTH_SHORT).show();
            Log.d("LOCATION", "no location");
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public void onAddClick() {
        Intent intent = new Intent(this, AddCityActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String name = data.getStringExtra(AddCityActivity.CITY_NAME);
            Log.d("NAME", name);
            Cursor c = getContentResolver().query(WeatherContentProvider.CITY_CONTENT_URI, null,
                    WeatherDatabaseHelper.CITY_NAME + "='" + name + "'", null, null);
            c.moveToNext();
            if (c.isAfterLast()) {
                ContentValues cv = new ContentValues();
                cv.put(WeatherDatabaseHelper.CITY_NAME, name);
                getContentResolver().insert(WeatherContentProvider.CITY_CONTENT_URI, cv);
            } else {
                Toast.makeText(this, "This city in this list already", Toast.LENGTH_SHORT).show();
            }
            mNavigationDrawerFragment.selectItemAfterLoading(name);
            mNavigationDrawerFragment.getLoaderManager().restartLoader(2225, null, mNavigationDrawerFragment);
        }
    }

    private boolean checkInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, WeatherFragment.newInstance(position + 1,
                        mNavigationDrawerFragment.getElements().get(position),
                        mNavigationDrawerFragment.getCities().get(position).getId()))
                .commit();
    }

    public void onSectionAttached(int number) {
        if (mNavigationDrawerFragment != null && mNavigationDrawerFragment.getElements() != null) {
            mTitle = mNavigationDrawerFragment.getElements().get(number - 1);
        } else {
            mTitle = "Rotate";
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //   return true;
        //}

        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String name) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("name", name);
            fragment.setArguments(args);

            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ((TextView) rootView.findViewById(R.id.section_label)).setText(getArguments().getString("name"));

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}